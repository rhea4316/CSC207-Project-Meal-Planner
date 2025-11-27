package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for User entity.
 * Tests user creation, saved recipes management, and grocery list generation.
 *
 * Responsible: Mona (primary), Everyone (shared entity)
 */
public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("user-1", "testuser", "password123");
    }

    @Test
    public void testUserCreation() {
        User newUser = new User("user-1", "testuser", "password123");
        assertEquals("user-1", newUser.getUserId());
        assertEquals("testuser", newUser.getUsername());
        assertEquals("password123", newUser.getPassword());
        assertTrue(newUser.getSavedRecipeIds().isEmpty());
        assertTrue(newUser.getGroceryList().isEmpty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new User("", "testuser", "password123");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new User("user-1", "", "password123");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new User("user-1", "testuser", "");
        });
        
        assertThrows(NullPointerException.class, () -> {
            new User(null, "testuser", "password123");
        });
    }

    @Test
    public void testAddSavedRecipe() {
        user.addSavedRecipeId("recipe-1");
        assertEquals(1, user.getSavedRecipeIds().size());
        assertTrue(user.getSavedRecipeIds().contains("recipe-1"));
        
        user.addSavedRecipeId("recipe-2");
        assertEquals(2, user.getSavedRecipeIds().size());
        
        user.addSavedRecipeId("recipe-1");
        assertEquals(2, user.getSavedRecipeIds().size());
    }

    @Test
    public void testRemoveSavedRecipe() {
        user.addSavedRecipeId("recipe-1");
        user.addSavedRecipeId("recipe-2");
        
        assertTrue(user.removeSavedRecipeId("recipe-1"));
        assertEquals(1, user.getSavedRecipeIds().size());
        assertFalse(user.getSavedRecipeIds().contains("recipe-1"));
        
        assertFalse(user.removeSavedRecipeId("recipe-3"));
        assertFalse(user.removeSavedRecipeId(null));
    }

    @Test
    public void testGroceryListGeneration() {
        Ingredient ingredient1 = new Ingredient("Tomato", 100.0, "g", 18, 0.9, 3.9, 0.2);
        Ingredient ingredient2 = new Ingredient("Chicken", 200.0, "g", 231, 43.5, 0.0, 5.0);
        
        user.addToGroceryList(ingredient1);
        user.addToGroceryList(ingredient2);
        
        assertEquals(2, user.getGroceryList().size());
        assertTrue(user.getGroceryList().contains(ingredient1));
        assertTrue(user.getGroceryList().contains(ingredient2));
        
        user.removeFromGroceryList(ingredient1);
        assertEquals(1, user.getGroceryList().size());
        
        user.clearGroceryList();
        assertTrue(user.getGroceryList().isEmpty());
    }

    @Test
    public void testNutritionGoals() {
        NutritionGoals goals = new NutritionGoals(2000, 150.0, 200.0, 70.0);
        user.setNutritionGoals(goals);
        
        assertEquals(goals, user.getNutritionGoals());
        assertEquals(2000, user.getNutritionGoals().getDailyCalories());
        
        NutritionGoals defaultGoals = NutritionGoals.createDefault();
        user.setNutritionGoals(defaultGoals);
        assertEquals(defaultGoals, user.getNutritionGoals());
    }

    @Test
    public void testPasswordHandling() {
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());
        
        assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("");
        });
        
        assertThrows(NullPointerException.class, () -> {
            user.setPassword(null);
        });
    }

    @Test
    public void testMealSchedule() {
        Schedule schedule = new Schedule("schedule-1", "user-1");
        user.setMealSchedule(schedule);
        
        assertEquals(schedule, user.getMealSchedule());
        assertEquals("schedule-1", user.getMealSchedule().getScheduleId());
    }

    @Test
    public void testUsernameUpdate() {
        user.setUsername("newusername");
        assertEquals("newusername", user.getUsername());
        
        assertThrows(IllegalArgumentException.class, () -> {
            user.setUsername("");
        });
        
        assertThrows(NullPointerException.class, () -> {
            user.setUsername(null);
        });
    }

    @Test
    public void testClearSavedRecipes() {
        user.addSavedRecipeId("recipe-1");
        user.addSavedRecipeId("recipe-2");
        
        user.clearSavedRecipes();
        assertTrue(user.getSavedRecipeIds().isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        User user1 = new User("user-1", "testuser", "password123");
        User user2 = new User("user-1", "different", "different");
        User user3 = new User("user-2", "testuser", "password123");
        
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
