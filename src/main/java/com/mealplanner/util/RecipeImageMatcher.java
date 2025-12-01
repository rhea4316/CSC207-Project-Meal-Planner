package com.mealplanner.util;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 레시피 이름과 이미지 파일을 매칭하는 유틸리티 클래스
 */
public class RecipeImageMatcher {
    private static final Logger logger = LoggerFactory.getLogger(RecipeImageMatcher.class);
    
    private static final String RECIPE_IMAGES_PATH = "/recipe_images/";
    private static List<String> availableImageFiles = null;
    
    /**
     * 레시피 이름에 해당하는 이미지 URL을 찾습니다.
     * 
     * @param recipeName 레시피 이름
     * @return 이미지 URL (없으면 null)
     */
    public static String findImageUrl(String recipeName) {
        if (recipeName == null || recipeName.trim().isEmpty()) {
            return null;
        }
        
        // 이미지 파일 목록을 한 번만 로드 (캐싱)
        if (availableImageFiles == null) {
            loadAvailableImages();
        }
        
        // 정확한 매칭 시도
        String exactMatch = findExactMatch(recipeName);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // 대소문자 무시 매칭 시도
        String caseInsensitiveMatch = findCaseInsensitiveMatch(recipeName);
        if (caseInsensitiveMatch != null) {
            return caseInsensitiveMatch;
        }
        
        // 부분 매칭 시도 (레시피 이름의 주요 부분만 사용)
        String partialMatch = findPartialMatch(recipeName);
        if (partialMatch != null) {
            return partialMatch;
        }
        
        logger.debug("No image found for recipe: {}", recipeName);
        return null;
    }
    
    /**
     * 사용 가능한 이미지 파일 목록을 로드합니다.
     */
    private static synchronized void loadAvailableImages() {
        if (availableImageFiles != null) {
            return;
        }
        
        availableImageFiles = new ArrayList<>();
        
        try {
            // resources 폴더에서 이미지 파일 목록 가져오기
            URL resourceUrl = RecipeImageMatcher.class.getResource(RECIPE_IMAGES_PATH);
            if (resourceUrl != null) {
                if ("file".equals(resourceUrl.getProtocol())) {
                    // 개발 환경: 파일 시스템 경로
                    Path imagesPath = Paths.get(resourceUrl.toURI());
                    if (Files.exists(imagesPath) && Files.isDirectory(imagesPath)) {
                        try (Stream<Path> paths = Files.list(imagesPath)) {
                            paths.filter(Files::isRegularFile)
                                 .filter(path -> {
                                     String fileName = path.getFileName().toString().toLowerCase();
                                     return fileName.endsWith(".jpg") || 
                                            fileName.endsWith(".jpeg") || 
                                            fileName.endsWith(".png");
                                 })
                                 .forEach(path -> availableImageFiles.add(path.getFileName().toString()));
                        }
                    }
                } else if ("jar".equals(resourceUrl.getProtocol())) {
                    // JAR 파일 내부: 리소스 스트림으로 읽기
                    // JAR 내부에서는 디렉토리 리스팅이 어려우므로, 
                    // 알려진 이미지 파일 목록을 하드코딩하거나 다른 방법 사용
                    // 여기서는 개발 환경에서만 작동하도록 구현
                }
            }
            
            // resources 폴더 접근이 실패하면 src/main/resources 경로 직접 시도
            if (availableImageFiles.isEmpty()) {
                Path resourcesPath = Paths.get("src/main/resources/recipe_images");
                if (Files.exists(resourcesPath) && Files.isDirectory(resourcesPath)) {
                    try (Stream<Path> paths = Files.list(resourcesPath)) {
                        paths.filter(Files::isRegularFile)
                             .filter(path -> {
                                 String fileName = path.getFileName().toString().toLowerCase();
                                 return fileName.endsWith(".jpg") || 
                                        fileName.endsWith(".jpeg") || 
                                        fileName.endsWith(".png");
                             })
                             .forEach(path -> availableImageFiles.add(path.getFileName().toString()));
                    }
                }
            }
            
            logger.info("Loaded {} recipe images", availableImageFiles.size());
        } catch (Exception e) {
            logger.warn("Failed to load recipe images list: {}", e.getMessage());
            // 하드코딩된 이미지 목록 사용 (fallback)
            loadHardcodedImageList();
        }
    }
    
    /**
     * 하드코딩된 이미지 목록 (fallback)
     */
    private static void loadHardcodedImageList() {
        availableImageFiles = new ArrayList<>();
        availableImageFiles.add("Authentic Greek Salad.jpg");
        availableImageFiles.add("Authentic Korean Bibimbap.jpg");
        availableImageFiles.add("Authentic Pad Thai (.jpg");
        availableImageFiles.add("Avocado Toast.jpg");
        availableImageFiles.add("Beef Stir Fry.jpg");
        availableImageFiles.add("Chicken Caesar Salad.jpg");
        availableImageFiles.add("Classic Chicken Fried Rice.jpg");
        availableImageFiles.add("Classic Chicken Noodle Soup.jpg");
        availableImageFiles.add("Classic Chocolate Chip Cookies.jpg");
        availableImageFiles.add("Classic Eggs Benedict.jpg");
        availableImageFiles.add("Classic Indian Chicken Curry.jpg");
        availableImageFiles.add("Classic Marinara Pasta.jpg");
        availableImageFiles.add("Classic Scrambled Eggs.jpg");
        availableImageFiles.add("Classic Turkey Club Sandwich.jpg");
        availableImageFiles.add("Creamy Homemade Mac and Cheese.jpg");
        availableImageFiles.add("Creamy Mushroom Risotto.jpg");
        availableImageFiles.add("French Toast.jpg");
        availableImageFiles.add("Garlic Shrimp Scampi.jpg");
        availableImageFiles.add("Grilled Flank Steak.jpg");
        availableImageFiles.add("Grilled Ribeye Steak with Roasted Vegetables.jpg");
        availableImageFiles.add("Grilled Salmon Bowl.jpg");
        availableImageFiles.add("High-Protein Power Bowl.jpg");
        availableImageFiles.add("Indian Chicken Curry.jpg");
        availableImageFiles.add("Keto Cauliflower Fried Rice.jpg");
        availableImageFiles.add("Mediterranean Quinoa Power Bowl.jpg");
        availableImageFiles.add("Mediterranean Quinoa Salad.jpg");
        availableImageFiles.add("Mexican-Style Stuffed Bell Peppers.jpg");
        availableImageFiles.add("Oatmeal with Berries.jpg");
        availableImageFiles.add("Penne Arrabbiata.jpg");
        availableImageFiles.add("Quick Black Bean Quesadilla.jpg");
        availableImageFiles.add("Spaghetti Carbonara.jpg");
        availableImageFiles.add("Spicy Chicken Tacos.jpg");
        availableImageFiles.add("Teriyaki Chicken Bowl.jpg");
        availableImageFiles.add("Vegan Buddha Bowl with Tahini Dressing.jpg");
        availableImageFiles.add("Vegan Pasta Primavera with Cashew Cream.jpg");
        availableImageFiles.add("Vegetable Stir Fry.jpg");
        availableImageFiles.add("Zucchini Noodles with Lean Turkey Marinara.jpg");
    }
    
    /**
     * 정확한 매칭을 시도합니다.
     */
    private static String findExactMatch(String recipeName) {
        String normalizedName = normalizeForMatching(recipeName);
        
        for (String imageFile : availableImageFiles) {
            String imageName = normalizeForMatching(removeExtension(imageFile));
            if (normalizedName.equals(imageName)) {
                return RECIPE_IMAGES_PATH + imageFile;
            }
        }
        
        return null;
    }
    
    /**
     * 대소문자 무시 매칭을 시도합니다.
     */
    private static String findCaseInsensitiveMatch(String recipeName) {
        String normalizedName = normalizeForMatching(recipeName).toLowerCase();
        
        for (String imageFile : availableImageFiles) {
            String imageName = normalizeForMatching(removeExtension(imageFile)).toLowerCase();
            if (normalizedName.equals(imageName)) {
                return RECIPE_IMAGES_PATH + imageFile;
            }
        }
        
        return null;
    }
    
    /**
     * 부분 매칭을 시도합니다 (레시피 이름의 주요 부분만 사용).
     */
    private static String findPartialMatch(String recipeName) {
        String normalizedName = normalizeForMatching(recipeName).toLowerCase();
        
        // "Classic", "Authentic" 같은 접두사 제거
        String[] prefixes = {"classic ", "authentic ", "vegan ", "keto ", "high-protein ", 
                            "mediterranean ", "mexican-style ", "quick "};
        String cleanedName = normalizedName;
        for (String prefix : prefixes) {
            if (cleanedName.startsWith(prefix)) {
                cleanedName = cleanedName.substring(prefix.length());
                break;
            }
        }
        
        for (String imageFile : availableImageFiles) {
            String imageName = normalizeForMatching(removeExtension(imageFile)).toLowerCase();
            
            // 이미지 이름에 레시피 이름이 포함되어 있는지 확인
            if (imageName.contains(cleanedName) || cleanedName.contains(imageName)) {
                return RECIPE_IMAGES_PATH + imageFile;
            }
        }
        
        return null;
    }
    
    /**
     * 매칭을 위해 문자열을 정규화합니다.
     * 공백, 특수 문자 등을 처리합니다.
     */
    private static String normalizeForMatching(String str) {
        if (str == null) {
            return "";
        }
        
        // 앞뒤 공백 제거
        String normalized = str.trim();
        
        // 여러 공백을 하나로
        normalized = normalized.replaceAll("\\s+", " ");
        
        // 특수 문자 제거 (하이픈은 유지)
        normalized = normalized.replaceAll("[^a-zA-Z0-9\\s-]", "");
        
        return normalized;
    }
    
    /**
     * 파일 확장자를 제거합니다.
     */
    private static String removeExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        
        return fileName;
    }
    
    /**
     * 캐시를 초기화합니다 (테스트용).
     */
    public static void clearCache() {
        availableImageFiles = null;
    }
}

