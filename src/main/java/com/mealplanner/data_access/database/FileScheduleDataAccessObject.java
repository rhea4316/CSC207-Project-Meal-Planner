package com.mealplanner.data_access.database;
import com.google.gson.Gson;
import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.exception.UserNotFoundException;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.use_case.manage_meal_plan.add.AddMealDataAccessInterface;
import com.mealplanner.use_case.manage_meal_plan.delete.DeleteMealDataAccessInterface;
import com.mealplanner.use_case.manage_meal_plan.edit.EditMealDataAccessInterface;
import com.mealplanner.use_case.view_schedule.ViewScheduleDataAccessInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Data access object for schedule persistence - reads/writes schedule data to JSON files.
// Responsible: Grace (primary for meal plan), Mona (view schedule), Everyone (database)

public class FileScheduleDataAccessObject implements ViewScheduleDataAccessInterface,
        AddMealDataAccessInterface, EditMealDataAccessInterface, DeleteMealDataAccessInterface {
    private static final String SCHEDULE_DIR = "data/schedules";
    private static final String FILE_EXTENSION = ".json";

    private final Gson gson;
    private final FileUserDataAccessObject userDataAccess;
    private final ViewManagerModel viewManagerModel;

    public FileScheduleDataAccessObject() {
        this(new FileUserDataAccessObject(), null);
    }

    public FileScheduleDataAccessObject(FileUserDataAccessObject userDataAccess) {
        this(userDataAccess, null);
    }

    public FileScheduleDataAccessObject(FileUserDataAccessObject userDataAccess, ViewManagerModel viewManagerModel) {
        this.userDataAccess = Objects.requireNonNull(userDataAccess, "FileUserDataAccessObject cannot be null");
        this.viewManagerModel = viewManagerModel;
        this.gson = JsonConverter.getGson();
        ensureDirectoryExists();
    }

    /**
     * Ensures the schedules directory exists, creating it if necessary.
     * @throws DataAccessException if directory creation fails
     */
    private void ensureDirectoryExists() {
        File directory = new File(SCHEDULE_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new DataAccessException("Failed to create schedules directory: " + SCHEDULE_DIR);
            }
        }
    }

    /**
     * Save the given schedule to a JSON file.
     * @param schedule the schedule to save
     * @throws DataAccessException if save operation fails
     */
    public void saveSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new DataAccessException("Schedule cannot be null");
        }

        String scheduleId = schedule.getScheduleId();
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new DataAccessException("Schedule ID cannot be null or empty");
        }

        String fileName = sanitizeFileName(scheduleId) + FILE_EXTENSION;
        File file = new File(SCHEDULE_DIR, fileName);

        ScheduleDTO dto = toDTO(schedule);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(dto, writer);
        } catch (IOException e) {
            throw new DataAccessException("Failed to save schedule: " + scheduleId, e);
        }
    }

    /**
     * Load a schedule by id. Returns null if the file doesn't exist.
     * @param scheduleId the schedule ID to load
     * @return Schedule object or null if not found
     * @throws DataAccessException if read operation fails
     */
    public Schedule loadSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            return null;
        }

        String fileName = sanitizeFileName(scheduleId) + FILE_EXTENSION;
        File file = new File(SCHEDULE_DIR, fileName);
        if (!file.exists()) {
            return null;
        }

        try {
            String json = new String(Files.readAllBytes(Paths.get(file.getPath())));
            ScheduleDTO dto = gson.fromJson(json, ScheduleDTO.class);
            if (dto == null) {
                return null;
            }
            return fromDTO(dto);
        } catch (IOException e) {
            throw new DataAccessException("Failed to load schedule: " + scheduleId, e);
        }
    }

    /**
     * Load a schedule by username. Finds the user first, then loads their schedule.
     * @param username the username to load schedule for
     * @return Schedule object or null if not found
     * @throws DataAccessException if read operation fails
     */
    public Schedule loadScheduleByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try {
            User user = userDataAccess.getUserByUsername(username);
            if (user == null || user.getMealSchedule() == null) {
                return null;
            }
            return user.getMealSchedule();
        } catch (UserNotFoundException e) {
            return null;
        }
    }

    /**
     * Find a schedule by userId by searching all schedule files.
     * @param userId the user ID to search for
     * @return Schedule object or null if not found
     * @throws DataAccessException if read operation fails
     */
    public Schedule findScheduleByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        File directory = new File(SCHEDULE_DIR);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
        if (files == null) {
            return null;
        }

        for (File file : files) {
            try {
                String json = new String(Files.readAllBytes(Paths.get(file.getPath())));
                ScheduleDTO dto = gson.fromJson(json, ScheduleDTO.class);
                if (dto != null && userId.equals(dto.userId)) {
                    return fromDTO(dto);
                }
            } catch (IOException e) {
                // Continue searching other files
                continue;
            }
        }

        return null;
    }

    /**
     * Sanitizes a filename to remove potentially dangerous characters.
     * @param fileName the original file name
     * @return sanitized file name
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userDataAccess.existsByUsername(username);
    }

    @Override
    public User getUserByUsername(String username) throws UserNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UserNotFoundException(username);
        }
        return userDataAccess.getUserByUsername(username);
    }

    // Implementation of AddMealDataAccessInterface, EditMealDataAccessInterface, DeleteMealDataAccessInterface
    @Override
    public Schedule getUserSchedule() {
        if (viewManagerModel == null) {
            throw new DataAccessException("ViewManagerModel is not set. Cannot determine current user.");
        }

        String currentUserId = viewManagerModel.getCurrentUserId();
        String currentUsername = viewManagerModel.getCurrentUsername();

        if (currentUserId != null && !currentUserId.trim().isEmpty()) {
            Schedule schedule = findScheduleByUserId(currentUserId);
            if (schedule != null) {
                return schedule;
            }
        }

        if (currentUsername != null && !currentUsername.trim().isEmpty()) {
            Schedule schedule = loadScheduleByUsername(currentUsername);
            if (schedule != null) {
                return schedule;
            }
        }

        // If no schedule found, create a new one for the current user
        if (currentUserId != null && !currentUserId.trim().isEmpty()) {
            return new Schedule(java.util.UUID.randomUUID().toString(), currentUserId, new HashMap<>());
        }

        throw new DataAccessException("Cannot determine current user. Please log in first.");
    }

    /**
     * JSON-friendly representation:
     * dates and meal types are stored as Strings.
     */
    private static class ScheduleDTO {
        String scheduleId;
        String userId;
        Map<String, Map<String, String>> mealsByDate;
    }

    private ScheduleDTO toDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.scheduleId = schedule.getScheduleId();
        dto.userId = schedule.getUserId();
        dto.mealsByDate = new HashMap<>();

        Map<LocalDate, Map<MealType, String>> meals = schedule.getAllMeals();
        for (Map.Entry<LocalDate, Map<MealType, String>> entry : meals.entrySet()) {
            String dateKey = entry.getKey().toString(); // "yyyy-MM-dd"
            Map<String, String> mealMap = new HashMap<>();
            for (Map.Entry<MealType, String> mealEntry : entry.getValue().entrySet()) {
                mealMap.put(mealEntry.getKey().name(), mealEntry.getValue());
            }
            dto.mealsByDate.put(dateKey, mealMap);
        }

        return dto;
    }

    private Schedule fromDTO(ScheduleDTO dto) {
        Map<LocalDate, Map<MealType, String>> meals = new HashMap<>();

        if (dto.mealsByDate != null) {
            for (Map.Entry<String, Map<String, String>> entry : dto.mealsByDate.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey());
                EnumMap<MealType, String> mealMap = new EnumMap<>(MealType.class);

                for (Map.Entry<String, String> mealEntry : entry.getValue().entrySet()) {
                    MealType type = MealType.valueOf(mealEntry.getKey());
                    mealMap.put(type, mealEntry.getValue());
                }

                meals.put(date, mealMap);
            }
        }

        // uses your constructor: Schedule(String scheduleId, String userId, Map<LocalDate, Map<MealType, String>> initialMeals)
        return new Schedule(dto.scheduleId, dto.userId, meals);
    }
}
