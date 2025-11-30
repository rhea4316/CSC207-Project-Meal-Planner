package com.mealplanner.util;

import java.time.LocalDate;

public class ValidationUtil {

    public static boolean validateIngredientName(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            return false;
        }
        if (!StringUtil.isAlphanumeric(name)) {
            return false;
        }
        return StringUtil.isValidLength(name, 1, 100);
    }

    public static boolean validateServingSize(int servingSize) {
        return servingSize > 0 && servingSize <= 100;
    }

    public static boolean validateDate(LocalDate date) {
        return date != null;
    }

    public static boolean validateRecipeName(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            return false;
        }
        return StringUtil.isValidLength(name, 1, 100);
    }

    /**
     * Validate recipe description length.
     * Description is optional, but if provided, must not exceed 500 characters.
     *
     * @param description Recipe description to validate
     * @return true if valid (null/empty is valid, or length <= 500)
     */
    public static boolean validateRecipeDescription(String description) {
        if (StringUtil.isNullOrEmpty(description)) {
            return true; // Description is optional
        }
        return StringUtil.isValidLength(description, 0, 500);
    }

    public static boolean validateQuantity(double quantity) {
        return quantity > 0;
    }

    private ValidationUtil() {
    }
}
