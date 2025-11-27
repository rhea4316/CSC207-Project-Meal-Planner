package com.mealplanner.data_access.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mealplanner.entity.Recipe;
import com.mealplanner.entity.Schedule;
import com.mealplanner.entity.User;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Utility class for converting entities to/from JSON format.
// Responsible: Everyone (database shared responsibility)

public class JsonConverter {

    private static final Gson GSON;

    static {
        GSON = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    // Recipe serialization/deserialization

    /**
     * Converts a Recipe object to JSON string.
     * @param recipe the recipe to convert
     * @return JSON string representation
     */
    public static String recipeToJson(Recipe recipe) {
        return GSON.toJson(recipe);
    }

    /**
     * Converts a JSON string to a Recipe object.
     * @param json the JSON string
     * @return Recipe object
     * @throws JsonParseException if JSON is malformed
     */
    public static Recipe jsonToRecipe(String json) {
        return GSON.fromJson(json, Recipe.class);
    }

    // User serialization/deserialization

    /**
     * Converts a User object to JSON string.
     * @param user the user to convert
     * @return JSON string representation
     */
    public static String userToJson(User user) {
        return GSON.toJson(user);
    }

    /**
     * Converts a JSON string to a User object.
     * @param json the JSON string
     * @return User object
     * @throws JsonParseException if JSON is malformed
     */
    public static User jsonToUser(String json) {
        return GSON.fromJson(json, User.class);
    }

    // Schedule serialization/deserialization

    /**
     * Converts a Schedule object to JSON string.
     * @param schedule the schedule to convert
     * @return JSON string representation
     */
    public static String scheduleToJson(Schedule schedule) {
        return GSON.toJson(schedule);
    }

    /**
     * Converts a JSON string to a Schedule object.
     * @param json the JSON string
     * @return Schedule object
     * @throws JsonParseException if JSON is malformed
     */
    public static Schedule jsonToSchedule(String json) {
        return GSON.fromJson(json, Schedule.class);
    }

    // LocalDate TypeAdapter for Gson
    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(FORMATTER));
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsString(), FORMATTER);
        }
    }

    // Get the Gson instance for custom usage
    public static Gson getGson() {
        return GSON;
    }
}
