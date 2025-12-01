package com.mealplanner.util;

import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mealplanner.config.AppConfig;
import com.mealplanner.config.ConfigLoader;

/**
 * 레시피 이미지를 로컬 파일 시스템에 캐싱하는 매니저
 * 
 * 기능:
 * - URL 기반 이미지 다운로드 및 로컬 저장
 * - 캐시 만료 관리 (TTL)
 * - LRU 기반 캐시 크기 관리
 * - 비동기 다운로드 지원
 */
public class ImageCacheManager {
    private static final Logger logger = LoggerFactory.getLogger(ImageCacheManager.class);
    
    private static ImageCacheManager instance;
    
    private final Path cacheDirectory;
    private final int cacheTtlMinutes;
    private final int maxCacheSize;
    private final boolean cacheEnabled;
    
    private final ExecutorService downloadExecutor;
    private final Map<String, CompletableFuture<Image>> pendingDownloads;
    
    /**
     * 싱글톤 인스턴스 반환
     */
    public static synchronized ImageCacheManager getInstance() {
        if (instance == null) {
            instance = new ImageCacheManager();
        }
        return instance;
    }
    
    private ImageCacheManager() {
        // 설정 로드 (AppConfig 사용)
        this.cacheEnabled = AppConfig.isCacheEnabled();
        this.cacheTtlMinutes = AppConfig.getCacheTtlMinutes();
        this.maxCacheSize = AppConfig.getCacheMaxSize();
        
        // 캐시 디렉토리 초기화
        String cachePath = ConfigLoader.getProperty("cache.images.path", "data/cache/images");
        this.cacheDirectory = Paths.get(cachePath);
        initializeCacheDirectory();
        
        // 다운로드 스레드 풀 (최대 5개 동시 다운로드)
        this.downloadExecutor = Executors.newFixedThreadPool(5);
        this.pendingDownloads = new ConcurrentHashMap<>();
        
        // 시작 시 오래된 캐시 정리
        cleanupExpiredCache();
    }
    
    /**
     * 이미지 URL로부터 Image 객체를 가져옵니다.
     * 캐시에 있으면 즉시 반환, 없으면 다운로드 후 캐싱합니다.
     * 
     * @param imageUrl 이미지 URL
     * @return Image 객체 (비동기 로딩)
     */
    public Image getImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // Resources 폴더의 이미지는 직접 로드 (캐싱 불필요)
        if (imageUrl.startsWith("/recipe_images/") || imageUrl.startsWith("/images/")) {
            try {
                URL resourceUrl = ImageCacheManager.class.getResource(imageUrl);
                if (resourceUrl != null) {
                    logger.debug("Loading image from resources: {}", imageUrl);
                    return new Image(resourceUrl.toExternalForm(), true);
                } else {
                    logger.warn("Resource image not found: {}", imageUrl);
                    return null;
                }
            } catch (Exception e) {
                logger.warn("Failed to load resource image: {}", imageUrl, e);
                return null;
            }
        }
        
        if (!cacheEnabled) {
            // 캐시 비활성화 시 직접 로드
            return new Image(imageUrl, true);
        }
        
        String cacheKey = generateCacheKey(imageUrl);
        Path cachedFile = cacheDirectory.resolve(cacheKey);
        
        // 1. 캐시 파일이 있고 유효한지 확인
        if (Files.exists(cachedFile) && isCacheValid(cachedFile)) {
            try {
                String fileUrl = cachedFile.toUri().toURL().toString();
                logger.debug("Loading image from cache: {}", fileUrl);
                return new Image(fileUrl, true);
            } catch (Exception e) {
                logger.warn("Failed to load cached image: {}", e.getMessage());
                // 캐시 파일이 손상되었을 수 있으므로 삭제
                deleteCacheFile(cachedFile);
            }
        }
        
        // 2. 이미 다운로드 중인지 확인
        if (pendingDownloads.containsKey(imageUrl)) {
            // 이미 다운로드 중이면 기다림
            try {
                return pendingDownloads.get(imageUrl).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("Failed to get pending download: {}", e.getMessage());
            }
        }
        
        // 3. 새로 다운로드 시작
        CompletableFuture<Image> downloadFuture = CompletableFuture.supplyAsync(() -> {
            return downloadAndCacheImage(imageUrl, cachedFile);
        }, downloadExecutor);
        
        pendingDownloads.put(imageUrl, downloadFuture);
        
        // 다운로드 완료 후 pendingDownloads에서 제거
        downloadFuture.whenComplete((image, throwable) -> {
            pendingDownloads.remove(imageUrl);
            if (throwable != null) {
                logger.error("Image download failed: {}", imageUrl, throwable);
            }
        });
        
        // 즉시 원본 URL로 로드 (백그라운드)
        // 다운로드가 완료되면 다음 요청부터는 캐시 사용
        return new Image(imageUrl, true);
    }
    
    /**
     * 이미지를 다운로드하고 캐시에 저장
     */
    private Image downloadAndCacheImage(String imageUrl, Path cacheFile) {
        try {
            logger.info("Downloading image: {}", imageUrl);
            
            // URL 유효성 검사
            URL url = new URL(imageUrl);
            if (!"http".equalsIgnoreCase(url.getProtocol()) && 
                !"https".equalsIgnoreCase(url.getProtocol())) {
                logger.warn("Unsupported protocol for image URL: {}", url.getProtocol());
                return new Image(imageUrl, true);
            }
            
            // 임시 파일로 다운로드 후 원자적 이동 (부분 다운로드 방지)
            Path tempFile = cacheFile.resolveSibling(cacheFile.getFileName().toString() + ".tmp");
            
            try (InputStream in = url.openStream();
                 FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                    if (totalBytes > MAX_FILE_SIZE) {
                        logger.warn("Image file too large, skipping: {}", imageUrl);
                        Files.deleteIfExists(tempFile);
                        return new Image(imageUrl, true);
                    }
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            // 다운로드 완료 후 원자적으로 이동
            Files.move(tempFile, cacheFile, StandardCopyOption.REPLACE_EXISTING);
            
            // 다운로드 완료 후 파일 URL로 Image 생성
            String fileUrl = cacheFile.toUri().toURL().toString();
            Image image = new Image(fileUrl, true);
            
            logger.info("Image cached successfully: {}", cacheFile.getFileName());
            
            // 캐시 크기 확인 및 정리
            enforceCacheSizeLimit();
            
            return image;
            
        } catch (java.net.MalformedURLException e) {
            logger.error("Invalid image URL: {}", imageUrl, e);
            return new Image(imageUrl, true);
        } catch (java.io.FileNotFoundException e) {
            logger.warn("Image not found: {}", imageUrl);
            return new Image(imageUrl, true);
        } catch (Exception e) {
            logger.error("Failed to download and cache image: {}", imageUrl, e);
            // 임시 파일 정리
            try {
                Path tempFile = cacheFile.resolveSibling(cacheFile.getFileName().toString() + ".tmp");
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // 무시
            }
            // 실패 시 원본 URL로 로드
            return new Image(imageUrl, true);
        }
    }
    
    /**
     * URL을 기반으로 캐시 키(파일명) 생성
     * URL을 SHA-256 해시하여 고유한 파일명 생성
     */
    private String generateCacheKey(String imageUrl) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(imageUrl.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // 파일 확장자 추출 (URL에서)
            String extension = getFileExtension(imageUrl);
            return hexString.toString() + extension;
            
        } catch (Exception e) {
            logger.error("Failed to generate cache key", e);
            // 폴백: URL의 해시코드 사용 (음수 방지)
            return String.valueOf(Math.abs(imageUrl.hashCode())) + getFileExtension(imageUrl);
        }
    }
    
    /**
     * URL에서 파일 확장자 추출
     */
    private String getFileExtension(String url) {
        try {
            // URL에서 쿼리 파라미터 제거
            String urlWithoutQuery = url;
            int queryIndex = url.indexOf('?');
            if (queryIndex > 0) {
                urlWithoutQuery = url.substring(0, queryIndex);
            }
            
            int lastDot = urlWithoutQuery.lastIndexOf('.');
            int lastSlash = urlWithoutQuery.lastIndexOf('/');
            
            if (lastDot > lastSlash && lastDot < urlWithoutQuery.length() - 1) {
                String ext = urlWithoutQuery.substring(lastDot);
                // 확장자 정규화 (소문자 변환 및 검증)
                ext = ext.toLowerCase();
                if (ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
                    return ext;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract file extension from URL: {}", url, e);
        }
        
        // 기본값: jpg
        return ".jpg";
    }
    
    /**
     * 캐시 파일이 유효한지 확인 (TTL 체크)
     */
    private boolean isCacheValid(Path cacheFile) {
        try {
            LocalDateTime fileTime = LocalDateTime.ofInstant(
                Files.getLastModifiedTime(cacheFile).toInstant(),
                java.time.ZoneId.systemDefault()
            );
            
            long minutesSinceModified = ChronoUnit.MINUTES.between(fileTime, LocalDateTime.now());
            return minutesSinceModified < (long) cacheTtlMinutes;
            
        } catch (Exception e) {
            logger.warn("Failed to check cache validity: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 캐시 디렉토리 초기화
     */
    private void initializeCacheDirectory() {
        try {
            if (!Files.exists(cacheDirectory)) {
                Files.createDirectories(cacheDirectory);
                logger.info("Created cache directory: {}", cacheDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create cache directory", e);
            throw new RuntimeException("Cannot initialize image cache", e);
        }
    }
    
    /**
     * 만료된 캐시 파일 정리
     */
    private void cleanupExpiredCache() {
        try {
            if (!Files.exists(cacheDirectory)) {
                return;
            }
            
            int deletedCount = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDirectory)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file) && !isCacheValid(file)) {
                        Files.delete(file);
                        deletedCount++;
                    }
                }
            }
            
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired cache files", deletedCount);
            }
            
        } catch (IOException e) {
            logger.warn("Failed to cleanup expired cache", e);
        }
    }
    
    /**
     * 캐시 크기 제한 적용 (LRU 기반)
     */
    private void enforceCacheSizeLimit() {
        try {
            if (!Files.exists(cacheDirectory)) {
                return;
            }
            
            // 파일 목록을 수정 시간 기준으로 정렬
            List<Path> files = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDirectory)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        files.add(file);
                    }
                }
            }
            
            // 수정 시간 기준 정렬 (오래된 것부터)
            files.sort(Comparator.comparing(path -> {
                try {
                    return Files.getLastModifiedTime(path);
                } catch (IOException e) {
                    return java.nio.file.attribute.FileTime.fromMillis(0);
                }
            }));
            
            // 최대 크기 초과 시 오래된 파일 삭제
            while (files.size() > maxCacheSize) {
                Path oldestFile = files.remove(0);
                try {
                    Files.delete(oldestFile);
                    logger.debug("Deleted old cache file: {}", oldestFile.getFileName());
                } catch (IOException e) {
                    logger.warn("Failed to delete old cache file: {}", oldestFile, e);
                }
            }
            
        } catch (IOException e) {
            logger.warn("Failed to enforce cache size limit", e);
        }
    }
    
    /**
     * 특정 캐시 파일 삭제
     */
    private void deleteCacheFile(Path cacheFile) {
        try {
            Files.deleteIfExists(cacheFile);
        } catch (IOException e) {
            logger.warn("Failed to delete cache file: {}", cacheFile, e);
        }
    }
    
    /**
     * 전체 캐시 삭제
     */
    public void clearCache() {
        try {
            if (Files.exists(cacheDirectory)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDirectory)) {
                    for (Path file : stream) {
                        if (Files.isRegularFile(file)) {
                            Files.delete(file);
                        }
                    }
                }
                logger.info("Cache cleared");
            }
        } catch (IOException e) {
            logger.error("Failed to clear cache", e);
        }
    }
    
    /**
     * 리소스 정리 (애플리케이션 종료 시 호출)
     */
    public void shutdown() {
        downloadExecutor.shutdown();
        try {
            if (!downloadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                downloadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            downloadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

