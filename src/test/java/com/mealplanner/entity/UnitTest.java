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
        assertEquals(Unit.UnitType.WEIGHT, Unit.GRAMS.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.KILOGRAMS.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.OUNCES.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.POUNDS.getType());
    }

    @Test
    public void testVolumeUnits() {
        assertEquals(Unit.UnitType.VOLUME, Unit.MILLILITERS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.LITERS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.CUPS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.TABLESPOONS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.TEASPOONS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.FLUID_OUNCES.getType());
    }

    @Test
    public void testCountUnits() {
        assertEquals(Unit.UnitType.COUNT, Unit.PIECES.getType());
        assertEquals(Unit.UnitType.COUNT, Unit.ITEMS.getType());
        assertEquals(Unit.UnitType.COUNT, Unit.WHOLE.getType());
    }

    @Test
    public void testSpecialUnits() {
        assertEquals(Unit.UnitType.SPECIAL, Unit.PINCH.getType());
        assertEquals(Unit.UnitType.SPECIAL, Unit.DASH.getType());
        assertEquals(Unit.UnitType.SPECIAL, Unit.TO_TASTE.getType());
    }

    @Test
    public void testFromString() {
        assertEquals(Unit.GRAMS, Unit.fromString("g"));
        assertEquals(Unit.GRAMS, Unit.fromString("G"));
        assertEquals(Unit.GRAMS, Unit.fromString("gram"));
        assertEquals(Unit.GRAMS, Unit.fromString("GRAMS"));
        assertEquals(Unit.KILOGRAMS, Unit.fromString("kg"));
        assertEquals(Unit.KILOGRAMS, Unit.fromString("kilogram"));
        assertEquals(Unit.CUPS, Unit.fromString("cup"));
        assertEquals(Unit.CUPS, Unit.fromString("CUPS"));
        assertEquals(Unit.TABLESPOONS, Unit.fromString("tbsp"));
        assertEquals(Unit.TEASPOONS, Unit.fromString("tsp"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            Unit.fromString("INVALID_UNIT");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Unit.fromString("");
        });
    }

    @Test
    public void testIsConvertibleTo() {
        assertTrue(Unit.GRAMS.isConvertibleTo(Unit.KILOGRAMS));
        assertTrue(Unit.KILOGRAMS.isConvertibleTo(Unit.GRAMS));
        assertTrue(Unit.CUPS.isConvertibleTo(Unit.MILLILITERS));
        assertTrue(Unit.MILLILITERS.isConvertibleTo(Unit.LITERS));
        
        assertFalse(Unit.GRAMS.isConvertibleTo(Unit.CUPS));
        assertFalse(Unit.CUPS.isConvertibleTo(Unit.GRAMS));
        assertFalse(Unit.PIECES.isConvertibleTo(Unit.PINCH));
        assertFalse(Unit.PINCH.isConvertibleTo(Unit.DASH));
    }

    @Test
    public void testGetAbbreviation() {
        assertEquals("g", Unit.GRAMS.getAbbreviation());
        assertEquals("kg", Unit.KILOGRAMS.getAbbreviation());
        assertEquals("oz", Unit.OUNCES.getAbbreviation());
        assertEquals("lb", Unit.POUNDS.getAbbreviation());
        assertEquals("ml", Unit.MILLILITERS.getAbbreviation());
        assertEquals("L", Unit.LITERS.getAbbreviation());
        assertEquals("cup", Unit.CUPS.getAbbreviation());
        assertEquals("tbsp", Unit.TABLESPOONS.getAbbreviation());
        assertEquals("tsp", Unit.TEASPOONS.getAbbreviation());
    }

    @Test
    public void testGetUnitType() {
        assertEquals(Unit.UnitType.WEIGHT, Unit.GRAMS.getType());
        assertEquals(Unit.UnitType.WEIGHT, Unit.KILOGRAMS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.CUPS.getType());
        assertEquals(Unit.UnitType.VOLUME, Unit.MILLILITERS.getType());
        assertEquals(Unit.UnitType.COUNT, Unit.PIECES.getType());
        assertEquals(Unit.UnitType.SPECIAL, Unit.PINCH.getType());
    }
}
