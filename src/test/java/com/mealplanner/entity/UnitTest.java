package com.mealplanner.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Unit enum.
 * Tests unit types, conversions, and string parsing.
 *
 * Responsible: Everyone (P0 improvement)
 */
public class UnitTest {

    @Test
    public void testWeightUnits() {
        // Test GRAMS, KILOGRAMS, OUNCES, POUNDS
        assertNotNull(Unit.GRAMS);
        assertNotNull(Unit.KILOGRAMS);
        assertNotNull(Unit.OUNCES);
        assertNotNull(Unit.POUNDS);

        // Verify unit type is WEIGHT
        assertEquals(Unit.UnitType.WEIGHT, Unit.GRAMS.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.KILOGRAMS.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.OUNCES.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.POUNDS.getType());
    }

    @Test
    public void testVolumeUnits() {
        // Test MILLILITERS, LITERS, CUPS, TABLESPOONS, TEASPOONS
        assertNotNull(Unit.MILLILITERS);
        assertNotNull(Unit.LITERS);
        assertNotNull(Unit.CUPS);
        assertNotNull(Unit.TABLESPOONS);
        assertNotNull(Unit.TEASPOONS);
        assertNotNull(Unit.FLUID_OUNCES);

        // Verify unit type is VOLUME
        assertEquals(Unit.UnitType.VOLUME, Unit.MILLILITERS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.LITERS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.CUPS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.TABLESPOONS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.TEASPOONS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.FLUID_OUNCES.getType());
    }

    @Test
    public void testCountUnits() {
        // Test PIECES, ITEMS, WHOLE
        assertNotNull(Unit.PIECES);
        assertNotNull(Unit.ITEMS);
        assertNotNull(Unit.WHOLE);

        // Verify unit type is COUNT
        assertEquals(Unit.UnitType.COUNT, Unit.PIECES.getType());
        assertEquals(Unit.UnitType.COUNT, Unit.ITEMS.getType());
        assertEquals(Unit.UnitType.COUNT, Unit.WHOLE.getType());
    }

    @Test
    public void testSpecialUnits() {
        // Test PINCH, DASH, TO_TASTE
        assertNotNull(Unit.PINCH);
        assertNotNull(Unit.DASH);
        assertNotNull(Unit.TO_TASTE);

        // Verify unit type is SPECIAL
        assertEquals(Unit.UnitType.SPECIAL, Unit.PINCH.getType());
        assertEquals(Unit.UnitType.SPECIAL, Unit.DASH.getType());
        assertEquals(Unit.UnitType.SPECIAL, Unit.TO_TASTE.getType());
    }

    @Test
    public void testFromString() {
        // Test parsing valid unit strings
        assertEquals(Unit.GRAMS, Unit.fromString("g"));
        assertEquals(Unit.KILOGRAMS, Unit.fromString("kg"));
        assertEquals(Unit.CUPS, Unit.fromString("cup"));
        assertEquals(Unit.TABLESPOONS, Unit.fromString("tbsp"));
        assertEquals(Unit.TEASPOONS, Unit.fromString("tsp"));

        // Test parsing abbreviations ("g", "kg", "cup")
        assertEquals(Unit.GRAMS, Unit.fromString("g"));
        assertEquals(Unit.KILOGRAMS, Unit.fromString("kg"));
        assertEquals(Unit.OUNCES, Unit.fromString("oz"));
        assertEquals(Unit.POUNDS, Unit.fromString("lb"));
        assertEquals(Unit.MILLILITERS, Unit.fromString("ml"));
        assertEquals(Unit.LITERS, Unit.fromString("L"));

        // Test parsing full names ("grams", "kilograms")
        assertEquals(Unit.GRAMS, Unit.fromString("gram"));
        assertEquals(Unit.KILOGRAMS, Unit.fromString("kilogram"));
        assertEquals(Unit.OUNCES, Unit.fromString("ounce"));
        assertEquals(Unit.POUNDS, Unit.fromString("pound"));
        assertEquals(Unit.MILLILITERS, Unit.fromString("milliliter"));
        assertEquals(Unit.LITERS, Unit.fromString("liter"));

        // Test case insensitivity
        assertEquals(Unit.GRAMS, Unit.fromString("GRAMS"));
        assertEquals(Unit.CUPS, Unit.fromString("CUP"));
        assertEquals(Unit.TABLESPOONS, Unit.fromString("TABLESPOON"));

        // Test parsing invalid string throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            Unit.fromString("invalid_unit");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Unit.fromString("xyz");
        });
    }

    @Test
    public void testIsConvertibleTo() {
        // Test weight to weight conversion (should be true)
        assertTrue(Unit.GRAMS.isConvertibleTo(Unit.KILOGRAMS));
        assertTrue(Unit.KILOGRAMS.isConvertibleTo(Unit.GRAMS));
        assertTrue(Unit.OUNCES.isConvertibleTo(Unit.POUNDS));
        assertTrue(Unit.POUNDS.isConvertibleTo(Unit.OUNCES));

        // Test volume to volume conversion (should be true)
        assertTrue(Unit.MILLILITERS.isConvertibleTo(Unit.LITERS));
        assertTrue(Unit.LITERS.isConvertibleTo(Unit.MILLILITERS));
        assertTrue(Unit.CUPS.isConvertibleTo(Unit.TABLESPOONS));
        assertTrue(Unit.TEASPOONS.isConvertibleTo(Unit.TABLESPOONS));

        // Test weight to volume conversion (should be false)
        assertFalse(Unit.GRAMS.isConvertibleTo(Unit.MILLILITERS));
        assertFalse(Unit.KILOGRAMS.isConvertibleTo(Unit.LITERS));
        assertFalse(Unit.OUNCES.isConvertibleTo(Unit.CUPS));

        // Test count to special conversion (should be false)
        assertFalse(Unit.PIECES.isConvertibleTo(Unit.PINCH));
        assertFalse(Unit.ITEMS.isConvertibleTo(Unit.DASH));
        assertFalse(Unit.WHOLE.isConvertibleTo(Unit.TO_TASTE));

        // Test special units cannot convert to anything (including themselves conceptually for non-measurable)
        assertFalse(Unit.PINCH.isConvertibleTo(Unit.DASH));
        assertFalse(Unit.TO_TASTE.isConvertibleTo(Unit.PINCH));
    }

    @Test
    public void testGetAbbreviation() {
        // Test abbreviations are correct
        assertEquals("g", Unit.GRAMS.getAbbreviation());
        assertEquals("kg", Unit.KILOGRAMS.getAbbreviation());
        assertEquals("oz", Unit.OUNCES.getAbbreviation());
        assertEquals("lb", Unit.POUNDS.getAbbreviation());
        assertEquals("ml", Unit.MILLILITERS.getAbbreviation());
        assertEquals("L", Unit.LITERS.getAbbreviation());
        assertEquals("cup", Unit.CUPS.getAbbreviation());
        assertEquals("tbsp", Unit.TABLESPOONS.getAbbreviation());
        assertEquals("tsp", Unit.TEASPOONS.getAbbreviation());

        // Verify "g" for GRAMS, "kg" for KILOGRAMS, etc.
        assertNotEquals("gram", Unit.GRAMS.getAbbreviation());
        assertNotEquals("kilogram", Unit.KILOGRAMS.getAbbreviation());
    }

    @Test
    public void testGetUnitType() {
        // Test all units return correct type
        assertEquals(Unit.UnitType.WEIGHT, Unit.GRAMS.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.KILOGRAMS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.MILLILITERS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.LITERS.getType());
        assertEquals(Unit.UnitType.COUNT, Unit.PIECES.getType());
        assertEquals(Unit.UnitType.COUNT, Unit.ITEMS.getType());
        assertEquals(Unit.UnitType.SPECIAL, Unit.PINCH.getType());
        assertEquals(Unit.UnitType.SPECIAL, Unit.DASH.getType());
    }
}
