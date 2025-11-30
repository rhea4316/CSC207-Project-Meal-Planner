package com.mealplanner.entity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
// Core entity representing a user with saved recipes, meal schedule, and nutrition goals.
// Responsible: Mona (primary for login/user management), Everyone (used across use cases)

public class User {
    private final String userId;
    private String username;
    private String password;
    private final LocalDateTime createdAt;

    private List<String> savedRecipeIds;              /// user saved recipes by recipe ID
    private List<Ingredient> groceryList;            /// user ingredient shopping list
    private Schedule mealSchedule;                          /// user meal schedule
    private NutritionGoals nutritionGoals;                  ///user nutrition goals


    public User(String userId, String username, String password) {
        this(userId, username, password, null, null, LocalDateTime.now());
    }

    public User(String userId, String username, String password, NutritionGoals nutritionGoals, Schedule mealSchedule) {
        this(userId, username, password, nutritionGoals, mealSchedule, LocalDateTime.now());
    }

    public User(String userId, String username, String password, NutritionGoals nutritionGoals, Schedule mealSchedule, LocalDateTime createdAt) {
        this.userId = requireNonBlank(userId, "userId");
        this.username = requireNonBlank(username, "username");
        this.password = requireNonBlank(password, "password");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.savedRecipeIds = new ArrayList<>();
        this.groceryList = new ArrayList<>();
        this.nutritionGoals = nutritionGoals;
        this.mealSchedule = mealSchedule;
    }




    ///  Getters and setters

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = requireNonBlank(username, "username");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = requireNonBlank(password, "password");
    }

    public NutritionGoals getNutritionGoals() {
        return nutritionGoals;
    }

    public void setNutritionGoals(NutritionGoals nutritionGoals) {
        this.nutritionGoals = nutritionGoals;
    }

    public Schedule getMealSchedule() {
        return mealSchedule;
    }

    public void setMealSchedule(Schedule mealSchedule) {
        this.mealSchedule = mealSchedule;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    ///  Managing the user's saved recipes


    ///  Return the User's saved recipe IDs
    public List<String> getSavedRecipeIds() {
        return Collections.unmodifiableList(savedRecipeIds);
    }

    /// Add a recipe ID to the user's saved recipes
    public void addSavedRecipeId(String recipeId) {
        String safeId = requireNonBlank(recipeId, "recipeId");
        if (!savedRecipeIds.contains(safeId)) {
            savedRecipeIds.add(safeId);
        }
    }

    /// Remove a recipe ID from the user's saved recipes. @return true if it was present and removed.
    public boolean removeSavedRecipeId(String recipeId) {
        if (recipeId == null) {
            return false;
        }
        return savedRecipeIds.remove(recipeId);
    }

    ///  clear the saved recipe list.
    public void clearSavedRecipes() {
        savedRecipeIds.clear();
    }


    // Managing User Grocery List

    /// return List view of all ingredients currently on grocery list

    public List<Ingredient> getGroceryList() {
        return Collections.unmodifiableList(groceryList);
    }
    ///  Add an ingredient to grocery list
    public void addToGroceryList(Ingredient ingredient) {
        if (ingredient != null) {
            groceryList.add(ingredient);
        }
    }
    ///  remove an ingredient from grocery list
    public boolean removeFromGroceryList(Ingredient ingredient) {
        if (ingredient == null) {
            return false;
        }
        return groceryList.remove(ingredient);
    }
    ///  clear the grocery list
    public void clearGroceryList() {
        groceryList.clear();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", savedRecipes=" + savedRecipeIds.size() +
                ", groceryListSize=" + groceryList.size() +
                '}';
    }


    // Helper: To ensure necessary fields are not left empty

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return trimmed;
    }
}
