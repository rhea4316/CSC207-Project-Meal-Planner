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
        user = new User("user123", "john_doe", "password123");
    }

    @Test
    public void testUserCreation() {
        // Test user creation with valid data
        User validUser = new User("user456", "jane_smith", "securePass");
        assertEquals("user456", validUser.getUserId());
        assertEquals("jane_smith", validUser.getUsername());
        assertEquals("securePass", validUser.getPassword());
        assertNotNull(validUser.getSavedRecipeIds());
        assertNotNull(validUser.getGroceryList());

        // Test user creation with invalid data throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            new User("", "username", "password");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new User("userId", "", "password");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new User("userId", "username", "");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new User(null, "username", "password");
        });
    }

    @Test
    public void testAddSavedRecipe() {
        // Test adding recipe to saved recipes
        assertTrue(user.getSavedRecipeIds().isEmpty());

        user.addSavedRecipeId("recipe123");
        assertEquals(1, user.getSavedRecipeIds().size());
        assertTrue(user.getSavedRecipeIds().contains("recipe123"));

        // Test duplicate recipe handling
        user.addSavedRecipeId("recipe123");
        assertEquals(1, user.getSavedRecipeIds().size());

        user.addSavedRecipeId("recipe456");
        assertEquals(2, user.getSavedRecipeIds().size());
    }

    @Test
    public void testRemoveSavedRecipe() {
        user.addSavedRecipeId("recipe1");
        user.addSavedRecipeId("recipe2");
        user.addSavedRecipeId("recipe3");

        // Test removing recipe from saved recipes
        assertTrue(user.removeSavedRecipeId("recipe2"));
        assertEquals(2, user.getSavedRecipeIds().size());
        assertFalse(user.getSavedRecipeIds().contains("recipe2"));

        // Test removing non-existent recipe
        assertFalse(user.removeSavedRecipeId("recipe999"));
        assertEquals(2, user.getSavedRecipeIds().size());

        // Test removing null
        assertFalse(user.removeSavedRecipeId(null));
    }

    @Test
    public void testClearSavedRecipes() {
        user.addSavedRecipeId("recipe1");
        user.addSavedRecipeId("recipe2");
        assertEquals(2, user.getSavedRecipeIds().size());

        user.clearSavedRecipes();
        assertTrue(user.getSavedRecipeIds().isEmpty());
    }

    @Test
    public void testNutritionGoals() {
        // Test setting nutrition goals
        assertNull(user.getNutritionGoals());

        NutritionGoals goals = new NutritionGoals(2000, 150.0, 200.0, 70.0);
        user.setNutritionGoals(goals);

        assertEquals(goals, user.getNutritionGoals());
        assertEquals(2000, user.getNutritionGoals().getDailyCalories());
    }

    @Test
    public void testMealSchedule() {
        assertNull(user.getMealSchedule());

        Schedule schedule = new Schedule("schedule123", "user123");
        user.setMealSchedule(schedule);

        assertEquals(schedule, user.getMealSchedule());
        assertEquals("schedule123", user.getMealSchedule().getScheduleId());
    }

    @Test
    public void testGroceryList() {
        // Test adding ingredient to grocery list
        Ingredient ingredient1 = new Ingredient("Milk", 1.0, "L", 150, 8.0, 12.0, 8.0);
        user.addToGroceryList(ingredient1);

        assertEquals(1, user.getGroceryList().size());
        assertTrue(user.getGroceryList().contains(ingredient1));

        // Test removing ingredient from grocery list
        assertTrue(user.removeFromGroceryList(ingredient1));
        assertEquals(0, user.getGroceryList().size());

        // Test removing non-existent ingredient
        assertFalse(user.removeFromGroceryList(ingredient1));

        // Test clear grocery list
        user.addToGroceryList(ingredient1);
        user.addToGroceryList(new Ingredient("Eggs", 12.0, "pieces", 720, 72.0, 4.0, 48.0));
        assertEquals(2, user.getGroceryList().size());

        user.clearGroceryList();
        assertTrue(user.getGroceryList().isEmpty());

        // Test adding null
        user.addToGroceryList(null);
        assertEquals(0, user.getGroceryList().size());
    }

    @Test
    public void testUserWithNutritionGoalsAndSchedule() {
        NutritionGoals goals = new NutritionGoals(2500, 180.0, 250.0, 80.0);
        Schedule schedule = new Schedule("sched1", "user123");

        User userWithGoals = new User("user789", "alice", "pass", goals, schedule);

        assertEquals("user789", userWithGoals.getUserId());
        assertEquals("alice", userWithGoals.getUsername());
        assertEquals(goals, userWithGoals.getNutritionGoals());
        assertEquals(schedule, userWithGoals.getMealSchedule());
    }

    @Test
    public void testUsernameAndPasswordSetters() {
        user.setUsername("new_username");
        assertEquals("new_username", user.getUsername());

        user.setPassword("new_password");
        assertEquals("new_password", user.getPassword());

        // Test setting empty values throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            user.setUsername("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("");
        });
    }

    @Test
    public void testEquals() {
        User user1 = new User("id1", "john", "pass");
        User user2 = new User("id1", "john", "pass");
        User user3 = new User("id2", "jane", "pass");

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
    }

    @Test
    public void testHashCode() {
        User user1 = new User("id1", "john", "pass");
        User user2 = new User("id1", "john", "pass");

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testToString() {
        String result = user.toString();

        assertNotNull(result);
        assertTrue(result.contains("user123"));
        assertTrue(result.contains("john_doe"));
    }

    @Test
    public void testSavedRecipeIdsIsUnmodifiable() {
        user.addSavedRecipeId("recipe1");

        // Getting the list should return an unmodifiable view
        assertThrows(UnsupportedOperationException.class, () -> {
            user.getSavedRecipeIds().add("recipe2");
        });
    }

    @Test
    public void testGroceryListIsUnmodifiable() {
        Ingredient ingredient = new Ingredient("Sugar", 100.0, "g", 400, 0.0, 100.0, 0.0);
        user.addToGroceryList(ingredient);

        // Getting the list should return an unmodifiable view
        assertThrows(UnsupportedOperationException.class, () -> {
            user.getGroceryList().add(ingredient);
        });
    }
}
