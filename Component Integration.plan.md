# Component Integration Plan

## 1. View Refactoring Strategy
Refactor existing views to use the newly migrated JavaFX components (`com.mealplanner.view.component.*`) instead of raw JavaFX controls and inline CSS.

## 2. Detailed View Plans

### A. DashboardView
- **Header**:
  - Replace `TextField` search with `Input`.
  - Replace manual Circle avatar with `Avatar`.
- **Nutrition Section**:
  - Replace `ProgressBar` with `Progress` component.
  - Retain manual `Arc` for circular progress (as `Progress` is linear), but style consistently.
- **Meal Cards**:
  - Refactor `createMealCard` to use standard styling.
  - Use `Skeleton` for loading simulation if data is null.
- **Quick Actions**:
  - Ensure buttons use `primary-button`, `outline-button` classes correctly.

### B. ScheduleView (Weekly Plan)
- **Navigation**:
  - Use `Tabs` or `SegmentedControl` (if available via `ToggleGroup`) to switch views.
- **Calendar**:
  - Use `Calendar` component for date selection.
- **Meal Slots**:
  - Use `Card` styling.
  - Use `Dialog` or `Sheet` for adding meals.
  - Use `Tooltip` on meal slots for details.

### C. BrowseRecipeView (Recipe Catalog)
- **Search & Filter**:
  - Use `Command` or `Input` for search.
  - Use `Select` for filtering cuisine/category.
- **List**:
  - Use `Pagination` for results.
  - Use `HoverCard` for quick recipe preview.
- **Recipe Card**:
  - Use `Badge` for dietary tags.
  - Use `ContextMenu` for actions (Save, View).

### D. RecipeDetailView
- **Layout**:
  - Use `Breadcrumb` for navigation path.
  - Use `Separator` between header and content.
- **Content**:
  - Use `Tabs` to switch between Ingredients/Instructions.
  - Use `ScrollArea` for long text.
  - Use `Slider` to adjust serving sizes (scaling ingredients).

### E. StoreRecipeView (Add Recipe)
- **Form**:
  - Use `Form` container.
  - Use `Input`, `Textarea`, `Select`.
  - Use `Sonner` for "Recipe Saved" toast.
  - Use `Dialog` for "Discard Changes?" confirmation.

### F. ProfileSettingsView
- **Profile**:
  - Use `Avatar` (Large size).
  - Use `Input` for editing fields.
- **Settings**:
  - Use `Switch` for toggle settings (e.g., Notifications).
  - Use `InputOTP` for a mock "Two-Factor Auth" section.
  - Use `Alert` (Dialog) for critical actions (Delete Account).

### G. Login/Signup
- **Auth**:
  - Use `Card` wrapper.
  - Use `Input` (Password).
  - Use `AlertBanner` for error messages.

## 3. Execution Order
1.  **Common Views**: `ProfileSettingsView`, `LoginView`, `SignupView`.
2.  **Main Views**: `DashboardView`, `ScheduleView`.
3.  **Recipe Views**: `BrowseRecipeView`, `RecipeDetailView`, `StoreRecipeView`.

