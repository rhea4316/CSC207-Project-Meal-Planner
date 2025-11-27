package com.mealplanner.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for IngredientParser.
 * Tests parsing of ingredient strings and scaling functionality.
 *
 * Responsible: Everyone (shared utility)
 */
public class IngredientParserTest {

    @Test
    public void testParseSimpleIngredient() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("2 cups flour");
        
        assertEquals(2.0, parsed.getQuantity(), 0.001);
        assertEquals("cups", parsed.getUnit());
        assertEquals("flour", parsed.getName());
    }

    @Test
    public void testParseIngredientWithDecimal() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("1.5 cups milk");
        
        assertEquals(1.5, parsed.getQuantity(), 0.001);
        assertEquals("cups", parsed.getUnit());
        assertEquals("milk", parsed.getName());
    }

    @Test
    public void testParseIngredientWithFraction() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("1/2 cup sugar");
        
        assertEquals(0.5, parsed.getQuantity(), 0.001);
        assertEquals("cup", parsed.getUnit());
        assertEquals("sugar", parsed.getName());
    }

    @Test
    public void testParseIngredientWithMixedNumber() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("1 1/2 cups water");
        
        assertEquals(1.5, parsed.getQuantity(), 0.01);
        assertEquals("cups", parsed.getUnit());
        assertEquals("water", parsed.getName());
    }

    @Test
    public void testParseIngredientWithoutUnit() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("3 eggs");
        
        assertEquals(3.0, parsed.getQuantity(), 0.001);
        assertEquals("", parsed.getUnit());
        assertEquals("eggs", parsed.getName());
    }

    @Test
    public void testParseIngredientWithoutQuantity() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("salt");
        
        assertEquals(0.0, parsed.getQuantity(), 0.001);
        assertEquals("", parsed.getUnit());
        assertEquals("salt", parsed.getName());
    }

    @Test
    public void testParseIngredientWithSpecialUnit() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("pinch of salt");
        
        assertEquals(0.0, parsed.getQuantity(), 0.001);
        assertTrue(parsed.getUnit().isEmpty() || parsed.getUnit().equals("pinch"));
        assertTrue(parsed.getName().contains("salt"));
    }

    @Test
    public void testParseIngredientWithToTaste() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("pepper to taste");
        
        assertTrue(parsed.getName().contains("pepper"));
    }

    @Test
    public void testParseNull() {
        assertThrows(NullPointerException.class, () -> {
            IngredientParser.parse(null);
        });
    }

    @Test
    public void testParseEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            IngredientParser.parse("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            IngredientParser.parse("   ");
        });
    }

    @Test
    public void testFormatParsedIngredient() {
        IngredientParser.ParsedIngredient parsed = new IngredientParser.ParsedIngredient(2.0, "cups", "flour");
        String formatted = parsed.format();
        
        assertTrue(formatted.contains("2"));
        assertTrue(formatted.contains("cups"));
        assertTrue(formatted.contains("flour"));
    }

    @Test
    public void testFormatWithFraction() {
        IngredientParser.ParsedIngredient parsed = new IngredientParser.ParsedIngredient(0.5, "cup", "sugar");
        String formatted = parsed.format();
        
        assertTrue(formatted.contains("1/2") || formatted.contains("0.5"));
        assertTrue(formatted.contains("cup"));
        assertTrue(formatted.contains("sugar"));
    }

    @Test
    public void testFormatWithoutQuantity() {
        IngredientParser.ParsedIngredient parsed = new IngredientParser.ParsedIngredient(0, "", "salt");
        String formatted = parsed.format();
        
        assertEquals("salt", formatted);
    }

    @Test
    public void testScaleIngredient() {
        String scaled = IngredientParser.scaleIngredient("2 cups flour", 2.0);
        
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse(scaled);
        assertEquals(4.0, parsed.getQuantity(), 0.1);
        assertEquals("cups", parsed.getUnit());
        assertEquals("flour", parsed.getName());
    }

    @Test
    public void testScaleIngredientWithFraction() {
        String scaled = IngredientParser.scaleIngredient("1/2 cup milk", 2.0);
        
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse(scaled);
        assertEquals(1.0, parsed.getQuantity(), 0.1);
    }

    @Test
    public void testScaleIngredientHalve() {
        String scaled = IngredientParser.scaleIngredient("2 cups flour", 0.5);
        
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse(scaled);
        assertEquals(1.0, parsed.getQuantity(), 0.1);
    }

    @Test
    public void testScaleIngredientInvalidFactor() {
        assertThrows(IllegalArgumentException.class, () -> {
            IngredientParser.scaleIngredient("2 cups flour", 0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            IngredientParser.scaleIngredient("2 cups flour", -1);
        });
    }

    @Test
    public void testParseComplexIngredient() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("2 1/2 tablespoons olive oil");
        
        assertEquals(2.5, parsed.getQuantity(), 0.01);
        assertEquals("tablespoons", parsed.getUnit());
        assertEquals("olive oil", parsed.getName());
    }

    @Test
    public void testParseIngredientWithMultipleWords() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("1 cup brown sugar");
        
        assertEquals(1.0, parsed.getQuantity(), 0.001);
        assertEquals("cup", parsed.getUnit());
        assertEquals("brown sugar", parsed.getName());
    }

    @Test
    public void testParseIngredientWithWhitespace() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("  2  cups  flour  ");
        
        assertEquals(2.0, parsed.getQuantity(), 0.001);
        assertEquals("cups", parsed.getUnit());
        assertEquals("flour", parsed.getName());
    }

    @Test
    public void testScaleIngredientWithoutQuantity() {
        String scaled = IngredientParser.scaleIngredient("salt", 2.0);
        
        // Should remain unchanged if no quantity
        assertTrue(scaled.contains("salt"));
    }

    @Test
    public void testParseVariousUnits() {
        String[] units = {"cups", "tablespoons", "teaspoons", "grams", "ounces", "pounds"};
        
        for (String unit : units) {
            IngredientParser.ParsedIngredient parsed = IngredientParser.parse("1 " + unit + " ingredient");
            assertEquals(1.0, parsed.getQuantity(), 0.001);
            assertEquals(unit, parsed.getUnit());
            assertEquals("ingredient", parsed.getName());
        }
    }
}

