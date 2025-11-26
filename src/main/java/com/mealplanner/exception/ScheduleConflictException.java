package com.mealplanner.exception;

/**
 * Exception thrown when attempting to add a meal to an already occupied time slot.
 * Responsible: Grace (manage meal plan - add/edit operations)
 */
public class ScheduleConflictException extends MealPlannerException {

    private final String date;
    private final String mealType;

    public ScheduleConflictException(String date, String mealType) {
        super("Meal slot already occupied for " + (mealType != null ? mealType : "unknown") 
                + " on " + (date != null ? date : "unknown date"));
        this.date = date;
        this.mealType = mealType;
    }

    public ScheduleConflictException(String message, String date, String mealType) {
        super(message != null ? message : "Schedule conflict occurred");
        this.date = date;
        this.mealType = mealType;
    }

    public String getDate() {
        return date;
    }

    public String getMealType() {
        return mealType;
    }
}
