# Data Directory

This directory contains local data storage for the Meal Planner application.

## Structure

- `users/` - User account data (JSON files)
- `recipes/` - Saved recipe data (JSON files)
- `schedules/` - User meal schedules (JSON files)

## Note

This directory is ignored by git (except this README) to protect user privacy and avoid conflicts.
Each developer will have their own local data for testing.

## Sample Data

To create sample data for testing, you can manually create JSON files following these formats:

### User (users/user123.json)
```json
{
  "userID": "user123",
  "username": "test_user",
  "password": "password123",
  "savedRecipes": [],
  "nutritionGoals": {
    "calories": 2000,
    "protein": 150,
    "carbs": 200,
    "fat": 70
  }
}
```

### Recipe (recipes/recipe123.json)
```json
{
  "recipeID": "recipe123",
  "recipeName": "Sample Recipe",
  "ingredients": [],
  "steps": ["Step 1", "Step 2"],
  "servingSize": 2,
  "prepTime": 15,
  "cookTime": 30
}
```

### Schedule (schedules/schedule123.json)
```json
{
  "scheduleID": "schedule123",
  "userID": "user123",
  "weeklyMeals": {}
}
```
