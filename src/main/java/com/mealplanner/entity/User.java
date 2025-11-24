package com.mealplanner.entity;
import java.util.*;
// Core entity representing a user with saved recipes, meal schedule, and nutrition goals.
// Responsible: Mona (primary for login/user management), Everyone (used across use cases)

// TODO: Implement user class with methods for managing saved recipes and generating grocery lists

public class User {
    private String username;                            /// Username for identification
    private String userId;                              /// User's ID (not sure if this is necessary)
    private List<String> savedRecipeIds;              /// user's recipes by recipe ID
    private List<Ingredient> groceryList;            /// user grocery list of ingredients
    private Schedule mealSchedule;                          /// user meal schedule
    private NutritionGoals nutritionGoals;                  ///user nutrition goals


    public User(String username) {
        this.username = requireNonBlank(username, "username");
        this.userId = requireNonBlank(userId, "userId");
        this.savedRecipeIds = new ArrayList<>();
        this.groceryList = new ArrayList<>();
        this.nutritionGoals = null;
        this.mealSchedule = null;

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


    ///  Managing the user's saved recipes


    ///  Return the User's saved recipes as a list of recipe IDs
    public List<String> getSavedRecipeIds() {
        return new  ArrayList<>(savedRecipeIds);
    }

    /// Add a recipe ID to the user's saved recipes
    public void addSavedRecipeId(String recipeId) {
        String saveId = requireNonBlank(recipeId, "recipeId");
        if (!savedRecipeIds.contains(saveId)) {
            savedRecipeIds.add(saveId);
        }
    }

    /// Remove a recipe ID from the user's saved recipes. return true if it was present and removed.
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

    /// return List of all ingredients currently on grocery list
    public List<Ingredient> getGroceryList() {
        return new ArrayList<>(groceryList);
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


    ///  Overriding: equals  ( Do we need to also do hashcode and toString?)

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


    /// Helper To make sure that important fields are not left empty

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return trimmed;


    }}
