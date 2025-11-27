# Meal Planner

A comprehensive meal planning application that helps users manage their daily and weekly meals with detailed nutrient tracking.

## Easy Setup & Run (Recommended)

You don't need to manually install Maven or libraries. Just run the script below!

### Windows
Open terminal and run: ```& $env:ComSpec /c "setup_and_run.bat"```

### Mac / Linux
Open terminal and run:
```bash
chmod +x setup_and_run.sh  # Run this only once to give permission
./setup_and_run.sh
```

This script will automatically:
1. Check your Java version.
2. Download all required libraries (dependencies).
3. Build the project.
4. Run the application.

---

## Manual Execution (If needed)

If you prefer running via your IDE (IntelliJ, etc.), run the main class located at:
**`src/main/java/com/mealplanner/app/Main.java`**

---

## Team Members (Group 14, TUT0201)

* Aaryan Patel
* Eden Chang
* Grace Lin
* Jerry Chai
* Mona El Yass
* Regina He

## Project Overview

This meal planning program helps users manage their daily and weekly meals. Users can enter ingredients they have to find matching recipes with nutrient information. Users can also browse recipes and see what ingredients are needed to make them. The program allows users to create and save their own recipes with cooking instructions. Users can plan their meals for breakfast, lunch, and dinner across a full week. The program also lets users adjust recipe serving sizes and view their scheduled meals. All recipes include detailed nutrient information to help users track their dietary intake.

## Work Timeline & Division of Work

https://maddening-year-2cf.notion.site/Meal-Planner-Project-Timeline-2a1e3eca9d8080409a2dcb7f40ee79e5

## User Stories

### User Story 1
As a user, I enter the ingredients and want to generate a recipe for my next meal, so that I know my nutrient intake for the meal.

### User Story 2
As a user, I am planning to get groceries for the next week and I want to know what ingredients are needed to make the recipes I am planning.

### User Story 3
As a user, I save my favorite meal in the program, inputting the ingredients needed and quantities and how to cook it, so that the app can provide the nutrient information for my stored meal.

### User Story 4
As a user, I want to be able to add, edit, and delete my meal plan schedule for the next three days in the program.

### User Story 5
As a user, I want to be able to adjust serving sizes for recipes to make appropriate portions for myself and others.

### User Story 6
As a user, I input my username and want to see what to eat from the schedule and recipes I saved from before.

## Use Cases

### Use Case 1: Enter Ingredients Receive Recipe

**Main Flow:**
1. User opens ingredient search page
2. User enters ingredients into the system
3. User clicks search button
4. System finds recipes that match the ingredients
5. System shows list of matching recipes
6. User selects a recipe from the list
7. System displays recipe details with nutrients

**Alternative Flow:**
* Not enough ingredients → Prompt to add more
* No ingredients entered → Prompt user to enter at least one ingredient
* No recipes found matching ingredients → Display message suggesting alternative ingredients
* Duplicate ingredients entered → Remove duplicates or show warning
* Invalid ingredient name (typo, special characters) → Suggest corrections or reject input
* API call fails → Display error message and retry option

### Use Case 2: Enter Recipe Receive Ingredients

**Main Flow:**
1. User opens recipe browse page
2. System shows list of available recipes
3. User selects a recipe
4. System displays the ingredient list with quantities

**Alternative Flow:**
* Recipe list is empty → Prompt user to search for recipes first
* Selected recipe has missing ingredient data → Display error message and suggest trying another recipe
* API timeout or connection error → Display error and retry option
* Recipe data corrupted → Handle gracefully with error message

### Use Case 3: Storing a Recipe

**Main Flow:**
1. User clicks "Create New Recipe" button
2. System shows input form
3. User enters recipe name
4. User adds ingredients with quantities
5. User writes cooking instructions
6. User clicks save button
7. System checks all fields are filled
8. System calculates nutrients from ingredients
9. System saves recipe to database
10. System shows confirmation message

**Alternative Flow:**
* No name entered → Prompt to add name
* No ingredients entered → Prompt to complete incomplete fields
* No cooking instructions entered → Prompt to add instructions
* Recipe name already exists → Prompt user to choose a different name or overwrite existing recipe
* Ingredient quantity is zero or negative → Prompt to enter valid quantity
* Special characters in recipe name → Validate or sanitize input
* Extremely long recipe name/instructions → Set character limits

### Use Case 4: Manage Meal Plan

**Main Flow:**
1. User opens weekly meal plan
2. System shows calendar with breakfast, lunch, dinner for each day
3. User selects a meal slot (specific day and meal type)
4. System asks user to choose: add, edit, or delete
5. User makes selection
6. System performs the chosen action
7. System updates and shows the new meal plan

**Alternative Flow:**
* User cancels adding a meal recipe → User returns to meal plan menu
* User tries to schedule past dates → Prevent or warn user
* Multiple users trying to edit same meal plan simultaneously → Handle concurrency

**Adding:**
* User submits invalid meal recipe → Prompt user to enter a valid meal recipe
* Meal slot already filled → Ask user to confirm overwrite

**Editing:**
* User tries to edit nonexistent meal recipe → Prompt user to enter a valid meal recipe
* No changes made → Display message and keep original meal

**Deleting:**
* User tries to delete invalid meal recipe → Prompt user to enter a valid meal recipe
* Meal slot already empty → Display message that no meal exists to delete

### Use Case 5: Adjust Recipe Serving Size

**Main Flow:**
1. User views a recipe
2. System shows recipe with default serving size
3. User clicks serving size adjustment option
4. System asks for desired number of servings
5. User enters new serving size
6. System checks if input is valid
7. System calculates new ingredient amounts
8. System calculates new nutrient values
9. System displays updated recipe

**Alternative Flow:**
* Invalid input (zero or negative number) → Prompt user to enter a valid positive number
* Extremely large serving size entered → Display confirmation message asking "Are you sure you want [X] servings?"
* No serving size entered → Keep original serving size and display message
* Non-numeric input (letters, symbols) → Reject and prompt for number
* Decimal input (2.5 servings) → Decide if allowed or round to integer
* Calculation results in fractional ingredients (0.3 eggs) → Round or display as fraction

### Use Case 6: View Meal Schedule

**Main Flow:**
1. User opens meal schedule viewer
2. System asks for username
3. User enters username
4. System checks username exists
5. System loads user's meal schedule
6. System displays weekly meal plan
7. User clicks on a meal
8. System shows full recipe details

**Alternative Flow:**
* Username not found → Prompt user to enter an existing username
* No recipe saved in schedule → Prompt to create a schedule first
* Selected recipe no longer exists → Display error message and offer to remove from schedule
* Empty username entered → Prompt to enter username
* Special characters in username → Validate input
* User account locked or inactive → Display appropriate message

## MVP Distribution

| Lead Developer | Use Case | User Story |
|---------------|----------|------------|
| Jerry | Enter Ingredients Receive Recipe | User Story #1 |
| Regina | Enter Recipe Receive Ingredients | User Story #2 |
| Aaryan | Storing a Recipe | User Story #3 |
| Grace | Manage Meal Plan | User Story #4 |
| Eden | Adjust Recipe Serving Size | User Story #5 |
| Mona | Login and browse user schedule | User Story #6 |
| Everyone | GUI, API Integration, Database and Data Access, Entity Classes, Testing, Documentation |

## Project Architecture

This project follows **Clean Architecture** principles with 4 distinct layers:

1. **Entities** (Yellow) - Core business objects (Recipe, Ingredient, User, etc.)
2. **Use Cases** (Red) - Business logic (Interactors)
3. **Interface Adapters** (Green) - Controllers, Presenters, ViewModels
4. **Frameworks & Drivers** (Blue) - GUI, Database, APIs

See [CODE_STRUCTURE.md](CODE_STRUCTURE.md) for detailed architecture documentation.

---

## Proposed Entities

### Class: Ingredient
* `name`: String
* `unit`: **Unit** (P0: Type-safe enum instead of String)
* `nutrition`: **NutritionInfo** (encapsulated nutrition data)

### Class: Recipe
* `recipeID`: String
* `recipeName`: String
* `ingredients`: hashmap(Ingredient,quantity: int)
* `steps`: List<String>
* `servingSize`: int
* `nutrition`: **NutritionInfo** (calculated from ingredients)
* `prepTime`: int (in minutes)
* `cookTime`: int (in minutes)

### Class: User
* `userID`: String
* `username`: String
* `passwordHash`: String (hashed, not plain text!)
* `savedRecipes`: List<Recipe>
* `mealSchedule`: Schedule
* `groceryList`: List<Ingredient>
* `nutritionGoals`: **NutritionGoals** (P0: Encapsulated nutrition goals)

### Class: Schedule
* `scheduleID`: String
* `userID`: String
* `weeklyMeals`: Map<LocalDate, Mealplan>
  * Key: LocalDate (proper date type)
  * Value: Map with **MealType** enum keys (P0) mapping to Recipe objects

### Class: MealPlan
* `breakfast`: Recipe
* `lunch`: Recipe
* `dinner`: Recipe
* `dailyNutrition`: **NutritionInfo** (calculated from all meals)

### Class: NutritionInfo 
* `calories`: int
* `protein`: double
* `carbs`: double
* `fat`: double

### Class: NutritionGoals
* `dailyCalories`: int
* `dailyProtein`: double
* `dailyCarbs`: double
* `dailyFat`: double

### Enums

#### MealType
* `BREAKFAST`, `LUNCH`, `DINNER`

#### Unit
* Weight: `GRAMS`, `KILOGRAMS`, `OUNCES`, `POUNDS`
* Volume: `MILLILITERS`, `LITERS`, `CUPS`, `TABLESPOONS`, `TEASPOONS`
* Count: `PIECES`, `ITEMS`, `WHOLE`
* Special: `PINCH`, `DASH`, `TO_TASTE`

#### DietaryRestriction
* `VEGETARIAN`, `VEGAN`, `GLUTEN_FREE`, `DAIRY_FREE`, `NUT_FREE`, `EGG_FREE`
* `KOSHER`, `HALAL`, `PALEO`, `KETO`, `LOW_SODIUM`, `LOW_SUGAR`

---

## APIs

This project uses the following APIs for recipe and meal planning data:

* [Spoonacular API](https://www.postman.com/spoonacular-api/spoonacular-api/documentation/rqqne3j/spoonacular-api?entity=request-7431899-80665f4f-4b24-4cb2-b654-a6a14b72e973)
* [Edamam Meal Planner API](https://developer.edamam.com/edamam-docs-meal-planner-api)

Configuration managed through `ApiConfig` (P0) - see [GETTING_STARTED.md](GETTING_STARTED.md) for setup.


---

## Manual Setup (Developers Only)

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- API keys from Spoonacular and Edamam

### Quick Start
```bash
# Clone repository
git clone <repository-url>
cd CSC207-Project-Meal-Planner

# Configure API keys
# Edit config/api-keys.properties with your keys

# Build project
mvn clean install

# Run tests
mvn test

# Run application
mvn exec:java -Dexec.mainClass="com.mealplanner.app.Main"
```

---

## License

This project is for educational purposes as part of CSC207 at University of Toronto.
