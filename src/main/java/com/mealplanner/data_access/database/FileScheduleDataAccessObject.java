package com.mealplanner.data_access.database;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;
import com.mealplanner.exception.UserNotFoundException;
import com.mealplanner.use_case.view_schedule.ViewScheduleDataAccessInterface;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// Data access object for schedule persistence - reads/writes schedule data to JSON files.
// Responsible: Grace (primary for meal plan), Mona (view schedule), Everyone (database)

public class FileScheduleDataAccessObject implements ViewScheduleDataAccessInterface {
    private static final String SCHEDULE_DIR = "data/schedules";

    private final Gson gson;

    public FileScheduleDataAccessObject() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        File dir = new File(SCHEDULE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Save the given schedule to a JSON file.
     */
    public void saveSchedule(Schedule schedule) {
        String fileName = schedule.getScheduleId() + ".json";
        File file = new File(SCHEDULE_DIR, fileName);

        ScheduleDTO dto = toDTO(schedule);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(dto, writer);
        } catch (IOException e) {
            e.printStackTrace(); // or wrap in your own custom exception
        }
    }

    /**
     * Load a schedule by id. Returns null if the file doesn't exist.
     */
    public Schedule loadSchedule(String scheduleId) {
        File file = new File(SCHEDULE_DIR, scheduleId + ".json");
        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            ScheduleDTO dto = gson.fromJson(reader, ScheduleDTO.class);
            if (dto == null) {
                return null;
            }
            return fromDTO(dto);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return false;
    }

    @Override
    public User getUserByUsername(String username) throws UserNotFoundException {
        return null;
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
