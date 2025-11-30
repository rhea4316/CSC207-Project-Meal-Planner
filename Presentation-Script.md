# Meal Planner Application: Final Technical Defense

**Group 14, TUT0201**

---

## Slide 1: Introduction

**Presenter:** Mona

**Team Description:** A team of six developers building a comprehensive meal planning application using Clean Architecture principles, JavaFX, and modern software engineering practices.

### Script

"Good morning/afternoon. We are Group 14. We present the Meal Planner Application, PlanEat. This is not just a calendar; it is a **comprehensive state-management system** designed to handle complex nutritional data aggregation, recipe management, and persistence using strict software engineering principles."

### Director's Notes

- **Tone:** Confident and professional.
- **Visuals:** Title Slide with Team Members.

---

## Slide 2: Program Functionality

**Presenter:** Grace

### Script

“Functionally, the application solves the problem of nutritional planning. It manages a **State Object** representing a weekly schedule with distinct slots for Breakfast, Lunch, and Dinner. The system aggregates macronutrient data—calculating dynamic totals for Protein, Carbs, and Fat against a daily limit. Beyond simple scheduling, we implemented logic for **dynamic recipe instantiation** and serving size scaling, which requires real-time recalculation of entity attributes.”

### Director’s Notes

- **Key concept:** Emphasize “State Object” and “Dynamic Recalculation.”
- **Visuals:** A screenshot of the full weekly view populated with data.

---

## Slide 3: API Integration Strategy

**Presenter:** Regina

### Script

"To populate our system, we integrated two distinct external services: Spoonacular and Edamam. A major technical challenge was the **Data Mapping** process. Spoonacular provided raw recipe discovery through `SpoonacularApiClient`, while Edamam offered the specific micro-nutrient breakdown through `EdamamApiClient`. We implemented a centralized **`ApiResponseParser`** class that standardizes these differing JSON responses into our internal `Ingredient` and `Recipe` entities. This parser handles the conversion of JSON objects to our domain models, ensuring our core business logic in the Interactors remains decoupled from the specific format of the external API providers."

### Director's Notes

- **Key concept:** "ApiResponseParser," "Decoupling," and "Centralized Mapping."
- **Visuals:** A diagram showing JSON data from `SpoonacularApiClient` and `EdamamApiClient` entering `ApiResponseParser` and exiting as clean Java Entities (`Recipe`, `Ingredient`, `NutritionInfo`).

---

## Slide 4: Data Persistence & Serialization

**Presenter:** Aaryan

### Script

"For persistence, we chose a lightweight, file-based approach using **JSON Serialization with Gson**. We rejected a heavy SQL database in favor of local storage to satisfy our non-functional requirement for portability and offline access. We implemented **Data Access Objects (DAOs)**—specifically `FileScheduleDataAccessObject`, `FileRecipeDataAccessObject`, and `FileUserDataAccessObject`—that handle the serialization of the `Schedule` object and `Recipe` entities. Additionally, we use a **Repository Pattern** with `RecipeRepository` interface and `FileRecipeRepository` implementation, ensuring that when the application restarts, the `DataAccessInterface` cleanly reconstructs the user's state without the UI layer knowing the source of the data."

### Director's Notes

- **Key concept:** "DAO," "Repository Pattern," "Serialization," and "Interface segregation."
- **Visuals:** A snippet of the code showing the `save()` method from `FileScheduleDataAccessObject` or the JSON file structure.

---

## Slide 5: Clean Architecture Implementation

**Presenter:** Jerry

### Script

"Our project strictly adheres to **Clean Architecture**. As you can see in the diagram, we enforce the **Dependency Rule** where inner layers know nothing of outer layers.
* Our **Entities** (like `Recipe`, `Schedule`, `User`, `NutritionInfo`) are plain Java objects at the center.
* Our **Use Cases** (like `AddMealInteractor`, `StoreRecipeInteractor`, `LoginInteractor`, `AdjustServingSizeInteractor`) handle the business logic.
* Crucially, we utilize **Input and Output Boundaries**. The `Controller` (like `AddMealController`, `StoreRecipeController`) never talks to the `Entity` directly; it passes an `InputData` object (like `AddMealInputData`, `StoreRecipeInputData`) to the `Interactor` through an interface (`InputBoundary`). The `Interactor` communicates back through `OutputBoundary` to the `Presenter`, which updates the `ViewModel`. This strict separation allows us to swap out the UI or Database without breaking business rules."

### Director's Notes

- **Key concept:** "Dependency Rule" and "Boundaries."
- **Visuals:** [Critical] Use a diagram labeled with *your specific classes* (e.g., `RecipeController`, `RecipeInteractor`), not a generic Google image.

---

## Slide 6: GUI Architecture & Component System

**Presenter:** Eden

### Script

"For the user interface, we built a **component-based architecture** using JavaFX. Our GUI follows a strict separation: Views (like `DashboardView`, `ScheduleView`, `LoginView`) are pure presentation layers that implement `PropertyChangeListener` to react to ViewModel changes. We created a **`ViewManager`** that uses a `StackPane` to manage view switching, ensuring only one view is active at a time. 

Crucially, we developed a **reusable component library** with over 40 custom components in the `view.component` package—including `Input`, `Form`, `Dialog`, `Sonner` (toast notifications), `Progress`, and `SelectRecipeDialog`. These components follow a consistent design system defined in our **CSS file** (`style.css`), which implements a Tailwind-inspired color palette and utility classes. 

We integrated **ControlsFX** for enhanced components like `SearchableComboBox`, and **ValidatorFX** for real-time form validation. The CSS uses semantic color variables (like `-fx-theme-primary`, `-fx-theme-background`) that can be easily themed, and our components apply styles through CSS classes rather than inline styles, maintaining separation of concerns between structure and presentation."

### Director's Notes

- **Key concept:** "Component-based architecture," "ViewManager pattern," "CSS design system," and "Separation of concerns."
- **Visuals:** 
  - A diagram showing View → ViewModel → Presenter → Interactor flow
  - Screenshot of component library structure
  - CSS color palette and utility classes
  - Example of a styled component (e.g., `Input`, `Dialog`)

---

## Slide 7: Live Demonstration Plan (Technical Walkthrough)

**Presenter:** Jerry

### Script

“I will now lead a technical walkthrough of the execution flow. Rather than just showing features, we will trace the data flow from the **View** through the **Controller**, into the **Interactor**, and back through the **Presenter** for our core use cases: Authentication, Data Retrieval, State Mutation, and Persistence.”

### Director’s Notes

- **Goal:** Set the expectation that this is a code-focused demo.

---

## Slide 8: Demo 1 - Logic Flow: Initialization (US 6/1)

**Presenter:** Mona (Technical Commentary)

### Script

"We begin with Initialization. When I enter the username and password, the `LoginView` calls `LoginController.execute()`, which creates `LoginInputData` and passes it to the `LoginInteractor`. The Interactor validates the user via `LoginDataAccessInterface` (implemented by `FileUserDataAccessObject`), which checks the password hash using `PasswordUtil`. Upon success, it creates a `LoginOutputData` object containing the user's ID, username, and `User` entity. The `LoginPresenter` then updates the `LoginViewModel` and `ViewManagerModel`, setting the active view to `DASHBOARD_VIEW`. The `ViewScheduleController` is also triggered to load the user's schedule, which populates the dashboard with the loaded data."

### Director's Notes

- **Action:** Log in.
- **Emphasis:** The separation of data loading from view switching. Multiple ViewModels updated through Presenters.

---

## Slide 9: Demo 2 - API Request Cycle (US 2)

**Presenter:** Regina (Technical Commentary)

### Script

"Now, let's look at the API Request Cycle. When I search for 'Pasta' using ingredients, the `SearchByIngredientsController` triggers an external call. The `SearchByIngredientsInteractor` uses our **API clients**—`SpoonacularApiClient` for recipe discovery and `EdamamApiClient` for detailed nutrition data—to fetch raw JSON. Crucially, the **mapping logic** happens in `ApiResponseParser`: raw JSON data is converted into `Recipe` entities *before* reaching the View. The `SearchByIngredientsPresenter` then formats this into the `RecipeSearchViewModel`, ensuring the View layer never handles raw API data."

### Director's Notes

- **Action:** Search by ingredients and select a recipe.
- **Emphasis:** Data sanitization/mapping in `ApiResponseParser` before it hits the UI.

---

## Slide 10: Demo 3 - Entity Creation & Validation (US 3)

**Presenter:** Aaryan (Technical Commentary)

### Script

"For Recipe Creation, we utilize a **Repository Pattern**. When I input these ingredients and click Save, the `StoreRecipeController` passes `StoreRecipeInputData` to the `StoreRecipeInteractor`. The Interactor first performs **Input Validation** using `ValidationUtil` to ensure non-negative quantities and valid recipe names. It then instantiates a new `Recipe` entity with a unique UUID and calculates the aggregate macros (Protein, Carbs, Fat) from ingredients. This calculated entity is then passed to the `RecipeRepository` (implemented by `FileRecipeRepository`) for immediate persistence."

### Director's Notes

- **Action:** Create a custom recipe.
- **Emphasis:** Validation logic residing in the `StoreRecipeInteractor`, not the View. Repository pattern for data access.

---

## Slide 11: Demo 4 - State Mutation (US 4)

**Presenter:** Grace (Technical Commentary)

### Script

"Managing the Meal Plan demonstrates **State Mutation**. When I add this meal to 'Tuesday Lunch', the `AddMealController` passes `AddMealInputData` to the `AddMealInteractor`. The Interactor retrieves the current `Schedule` via `AddMealDataAccessInterface`, calls `schedule.addMeal()` to update the entity in memory, and saves it through `FileScheduleDataAccessObject`. We utilize the **Observer Pattern** with `PropertyChangeSupport` within the `ScheduleViewModel`; once the `MealPlanPresenter` updates the State, the `ScheduleView`—which implements `PropertyChangeListener`—automatically reflects this change without requiring a manual refresh."

### Director's Notes

- **Action:** Add a meal to the schedule.
- **Emphasis:** The Observer pattern (`PropertyChangeListener`) updating the UI automatically.

---

## Slide 12: Demo 5 - Dynamic Calculation Logic (US 5)

**Presenter:** Eden (Technical Commentary) - Eden's Use Case

### Script

"Adjusting serving size involves complex business logic. When I change servings from 2 to 4, the `AdjustServingSizeController` passes `AdjustServingSizeInputData` to the `AdjustServingSizeInteractor`. The Interactor retrieves the recipe via `AdjustServingSizeDataAccessInterface` and calls the `Recipe` entity's `adjustServingSize()` method. This method does not just multiply the numbers; it creates a new `Recipe` instance with scaled ingredient quantities and recalculates the total nutritional profile using `NutritionInfo`. The `AdjustServingSizePresenter` then updates the `RecipeDetailViewModel`, which triggers the View to refresh. This guarantees that data consistency is maintained in the Business Layer, ensuring accurate nutrient tracking."

### Director's Notes

- **Action:** Change serving size.
- **Emphasis:** Math happens in the Entity's `adjustServingSize()` method, called by the Use Case layer.

---

## Slide 13: Demo 6 - Persistence Cycle (US 6)

**Presenter:** Aaryan (Technical Commentary)

### Script

"Finally, we execute the Persistence Cycle. When a meal is added, the `AddMealInteractor` calls `FileScheduleDataAccessObject.saveSchedule()`, which uses **Gson** to serialize the current state of the `Schedule` object into a JSON file in `data/schedules/`. Similarly, recipes are persisted via `FileRecipeRepository.save()` to `data/recipes/`. To prove persistence, I will restart the application. The system initializes `FileScheduleDataAccessObject` and `FileUserDataAccessObject`, deserializes the JSON files, and the `ViewScheduleInteractor` loads the user's schedule. The `ViewSchedulePresenter` then updates the `ScheduleViewModel`, which triggers the `ScheduleView` to display the restored data, exactly as the user left it."

### Director's Notes

- **Action:** Add meal, close, restart, verify data persists.
- **Emphasis:** Automatic persistence on state changes, not manual "Save" button.

---

## Slide 14: Code Quality & Tooling

**Presenter:** Mona

### Script

"To ensure maintainability, we adhered to the **Single Responsibility Principle**—each class has one job. For example, `StoreRecipeInteractor` only handles recipe creation logic, while `FileRecipeRepository` only handles persistence. For debugging the View layer, which is decoupled from logic, we utilized **Scenic View** to inspect the JavaFX Node Hierarchy and debug CSS issues. We also implemented a custom `LayoutDebugger` utility that can be toggled with F12 to visualize borders and padding. For logic, we relied on JUnit tests targeting our Interactors (like `LoginInteractorTest`, `AddMealInteractorTest`), mocking the DAOs and DataAccessInterfaces to test business rules in isolation."

### Director's Notes

- **Key concept:** Single Responsibility Principle (SRP) and Testing/Debugging strategy.
- **Visuals:** A screenshot of Scenic View, LayoutDebugger, or a Unit Test passing (e.g., `LoginInteractorTest`).

---

## Slide 15: GUI Implementation Details & User Experience

**Presenter:** Eden

### Script

"From a frontend perspective, we implemented a **modern, responsive UI** using JavaFX with a comprehensive CSS design system. Our `style.css` file defines a **semantic color palette** using CSS variables (like `-fx-theme-primary`, `-fx-theme-background`) that enables easy theming. We use **utility classes** (inspired by Tailwind CSS) such as `.text-gray-500`, `.card-panel`, and `.primary-button` for consistent styling across all views.

The UI architecture follows a **View-ViewModel-Presenter pattern**: each View (like `DashboardView`, `ScheduleView`) implements `PropertyChangeListener` and reacts to changes in its corresponding ViewModel. The `ViewManager` handles navigation using a `StackPane`, ensuring smooth transitions between screens.

For user feedback, we implemented **`Sonner`**—a custom toast notification system that provides non-intrusive success, error, and info messages. We also integrated **ControlsFX Notifications** for validation errors and **ValidatorFX** for real-time form validation with visual feedback. 

Our component library includes reusable UI elements like `Input`, `Form`, `Dialog`, `Progress`, and `SelectRecipeDialog`, all styled consistently through CSS classes. This component-based approach ensures visual consistency and reduces code duplication across the application."

### Director's Notes

- **Key concept:** "CSS Design System," "Component Library," "View-ViewModel-Presenter," "User Feedback Mechanisms."
- **Visuals:** 
  - Screenshot of the application showing the modern UI
  - CSS color palette and utility classes
  - Component library structure
  - Example of Sonner toast notification
  - Example of ValidatorFX validation feedback

---

## Slide 16: Conclusion & Challenges

**Presenter:** Aaryan

### Script

"In conclusion, PlanEat demonstrates a robust application of Clean Architecture with clear separation of concerns across four layers: Entities, Use Cases, Interface Adapters, and Frameworks. Our biggest challenge was the **impedance mismatch** between the external API data structures (Spoonacular and Edamam JSON formats) and our internal Entity model (`Recipe`, `Ingredient`, `NutritionInfo`). We solved this by implementing a centralized `ApiResponseParser` that handles all JSON-to-Entity conversions, and by using Repository and DAO patterns to abstract data access. Additionally, we integrated **ControlsFX** and **ValidatorFX** for enhanced UI components and form validation. The result is a system that is testable, maintainable, and fully meets all functional and non-functional requirements."

### Director's Notes

- **Emphasis:** How you solved the hardest technical problem (API data mapping) and additional enhancements (ControlsFX, ValidatorFX).

---

## Slide 17: Q&A

**Presenter:** All Team Members

### Script

“Thank you. We are now happy to answer questions regarding our architecture, specific class implementations, or our design process.”

### Director’s Notes

- Be ready to pull up code if the professor asks “Show me the Interactor.”