package com.mealplanner.repository.impl;

import com.mealplanner.config.AppConfig;
import com.mealplanner.entity.Schedule;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.ScheduleRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileScheduleRepository implements ScheduleRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileScheduleRepository.class);

    private final String dataDirectory;
    private final Gson gson;

    public FileScheduleRepository(String dataDirectory) {
        if (dataDirectory == null || dataDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("Data directory cannot be null or empty");
        }
        this.dataDirectory = dataDirectory.trim();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeDirectory();
        logger.info("FileScheduleRepository initialized with directory: {}", this.dataDirectory);
    }

    public FileScheduleRepository() {
        this(AppConfig.getScheduleDataPath());
    }

    private void initializeDirectory() {
        try {
            Path dirPath = Paths.get(dataDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                logger.info("Created schedule data directory: {}", dataDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create data directory: {}", dataDirectory, e);
            throw new RuntimeException("Failed to initialize schedule repository", e);
        }
    }

    private String getFilePath(String scheduleId) {
        return dataDirectory + File.separator + scheduleId + AppConfig.getDataFileExtension();
    }

    @Override
    public void save(Schedule schedule) throws DataAccessException {
        if (schedule == null) {
            throw new DataAccessException("Cannot save null schedule");
        }
        if (schedule.getScheduleId() == null || schedule.getScheduleId().isEmpty()) {
            throw new DataAccessException("Schedule must have a valid ID");
        }

        logger.debug("Saving schedule: {}", schedule.getScheduleId());
        String filePath = getFilePath(schedule.getScheduleId());

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(schedule, writer);
            logger.info("Successfully saved schedule: {}", schedule.getScheduleId());
        } catch (IOException e) {
            logger.error("Failed to save schedule: {}", schedule.getScheduleId(), e);
            throw new DataAccessException("Failed to save schedule: " + schedule.getScheduleId(), e);
        }
    }

    @Override
    public Optional<Schedule> findById(String scheduleId) throws DataAccessException {
        if (scheduleId == null || scheduleId.isEmpty()) {
            return Optional.empty();
        }

        logger.debug("Finding schedule by ID: {}", scheduleId);
        String filePath = getFilePath(scheduleId);
        File file = new File(filePath);

        if (!file.exists()) {
            logger.debug("Schedule not found: {}", scheduleId);
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(file)) {
            Schedule schedule = gson.fromJson(reader, Schedule.class);
            logger.debug("Successfully loaded schedule: {}", scheduleId);
            return Optional.ofNullable(schedule);
        } catch (IOException e) {
            logger.error("Failed to read schedule: {}", scheduleId, e);
            throw new DataAccessException("Failed to read schedule: " + scheduleId, e);
        }
    }

    @Override
    public Optional<Schedule> findByUserId(String userId) throws DataAccessException {
        if (userId == null || userId.isEmpty()) {
            return Optional.empty();
        }

        logger.debug("Finding schedule by user ID: {}", userId);

        return findAll().stream()
                .filter(schedule -> schedule != null && userId.equals(schedule.getUserId()))
                .findFirst();
    }

    @Override
    public List<Schedule> findAll() throws DataAccessException {
        logger.debug("Finding all schedules");
        File dir = new File(dataDirectory);

        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Schedule directory does not exist: {}", dataDirectory);
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dataDirectory), 1)) {
            List<Schedule> schedules = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(AppConfig.getDataFileExtension()))
                    .map(this::loadScheduleFromPath)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            logger.info("Loaded {} schedules", schedules.size());
            return schedules;
        } catch (IOException e) {
            logger.error("Failed to read schedules from directory", e);
            throw new DataAccessException("Failed to read schedules from directory", e);
        }
    }

    private Optional<Schedule> loadScheduleFromPath(Path path) {
        try (FileReader reader = new FileReader(path.toFile())) {
            Schedule schedule = gson.fromJson(reader, Schedule.class);
            return Optional.ofNullable(schedule);
        } catch (IOException e) {
            logger.error("Failed to load schedule from file: {}", path, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Schedule> findByDate(LocalDate date) throws DataAccessException {
        if (date == null) {
            return new ArrayList<>();
        }

        logger.debug("Finding schedules by date: {}", date);

        return findAll().stream()
                .filter(schedule -> schedule != null && schedule.getMealsForDate(date) != null
                        && !schedule.getMealsForDate(date).isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(String scheduleId) throws DataAccessException {
        if (scheduleId == null || scheduleId.isEmpty()) {
            return false;
        }

        logger.debug("Deleting schedule: {}", scheduleId);
        String filePath = getFilePath(scheduleId);
        File file = new File(filePath);

        if (!file.exists()) {
            logger.debug("Schedule file not found for deletion: {}", scheduleId);
            return false;
        }

        try {
            boolean deleted = file.delete();
            if (deleted) {
                logger.info("Successfully deleted schedule: {}", scheduleId);
            } else {
                logger.warn("Failed to delete schedule file: {}", scheduleId);
            }
            return deleted;
        } catch (SecurityException e) {
            logger.error("Security exception while deleting schedule: {}", scheduleId, e);
            throw new DataAccessException("Failed to delete schedule: " + scheduleId, e);
        }
    }

    @Override
    public boolean deleteByUserId(String userId) throws DataAccessException {
        if (userId == null || userId.isEmpty()) {
            return false;
        }

        logger.debug("Deleting schedule by user ID: {}", userId);
        Optional<Schedule> schedule = findByUserId(userId);

        if (schedule.isPresent()) {
            return delete(schedule.get().getScheduleId());
        }

        logger.debug("No schedule found for user: {}", userId);
        return false;
    }

    @Override
    public boolean exists(String scheduleId) throws DataAccessException {
        if (scheduleId == null || scheduleId.isEmpty()) {
            return false;
        }

        logger.debug("Checking if schedule exists: {}", scheduleId);
        String filePath = getFilePath(scheduleId);
        return new File(filePath).exists();
    }

    @Override
    public int count() throws DataAccessException {
        logger.debug("Counting schedules");
        File dir = new File(dataDirectory);

        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dataDirectory), 1)) {
            long count = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(AppConfig.getDataFileExtension()))
                    .count();

            logger.debug("Schedule count: {}", count);
            return (int) count;
        } catch (IOException e) {
            logger.error("Failed to count schedules", e);
            throw new DataAccessException("Failed to count schedules", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        logger.warn("Clearing all schedules");
        File dir = new File(dataDirectory);

        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Schedule directory does not exist, nothing to clear");
            return;
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dataDirectory), 1)) {
            List<Path> filesToDelete = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(AppConfig.getDataFileExtension()))
                    .collect(Collectors.toList());

            int deletedCount = 0;
            for (Path path : filesToDelete) {
                try {
                    Files.delete(path);
                    deletedCount++;
                } catch (IOException e) {
                    logger.error("Failed to delete file: {}", path, e);
                }
            }

            logger.info("Cleared {} schedules", deletedCount);
        } catch (IOException e) {
            logger.error("Failed to clear schedules", e);
            throw new DataAccessException("Failed to clear schedules", e);
        }
    }
}
