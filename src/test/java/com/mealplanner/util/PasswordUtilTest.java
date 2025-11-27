package com.mealplanner.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PasswordUtil.
 * Tests password hashing and verification functionality.
 *
 * Responsible: Everyone (shared utility)
 */
public class PasswordUtilTest {

    @Test
    public void testHashPassword() {
        String password = "testPassword123";
        String hash = PasswordUtil.hashPassword(password);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertNotEquals(password, hash);
        // Hash should contain delimiter
        assertTrue(hash.contains(":"));
    }

    @Test
    public void testHashPasswordNull() {
        assertThrows(NullPointerException.class, () -> {
            PasswordUtil.hashPassword(null);
        });
    }

    @Test
    public void testHashPasswordEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("   ");
        });
    }

    @Test
    public void testVerifyPasswordCorrect() {
        String password = "testPassword123";
        String hash = PasswordUtil.hashPassword(password);
        
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    public void testVerifyPasswordIncorrect() {
        String password = "testPassword123";
        String hash = PasswordUtil.hashPassword(password);
        
        assertFalse(PasswordUtil.verifyPassword("wrongPassword", hash));
    }

    @Test
    public void testVerifyPasswordNull() {
        String hash = PasswordUtil.hashPassword("test");
        
        assertThrows(NullPointerException.class, () -> {
            PasswordUtil.verifyPassword(null, hash);
        });
        
        assertThrows(NullPointerException.class, () -> {
            PasswordUtil.verifyPassword("test", null);
        });
    }

    @Test
    public void testVerifyPasswordEmpty() {
        String hash = PasswordUtil.hashPassword("test");
        
        assertFalse(PasswordUtil.verifyPassword("", hash));
        assertFalse(PasswordUtil.verifyPassword("   ", hash));
        assertFalse(PasswordUtil.verifyPassword("test", ""));
        assertFalse(PasswordUtil.verifyPassword("test", "   "));
    }

    @Test
    public void testHashPasswordDifferentHashes() {
        String password = "testPassword123";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);
        
        // Same password should produce different hashes (due to random salt)
        assertNotEquals(hash1, hash2);
        
        // But both should verify correctly
        assertTrue(PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PasswordUtil.verifyPassword(password, hash2));
    }

    @Test
    public void testVerifyPasswordInvalidHashFormat() {
        String password = "testPassword123";
        
        // Invalid hash format (no delimiter)
        assertFalse(PasswordUtil.verifyPassword(password, "invalidhash"));
        
        // Invalid hash format (only one part)
        assertFalse(PasswordUtil.verifyPassword(password, "onlyonepart"));
    }

    @Test
    public void testPasswordSecurity() {
        String password1 = "password1";
        String password2 = "password2";
        
        String hash1 = PasswordUtil.hashPassword(password1);
        String hash2 = PasswordUtil.hashPassword(password2);
        
        // Different passwords should produce different hashes
        assertNotEquals(hash1, hash2);
        
        // Each password should only verify with its own hash
        assertTrue(PasswordUtil.verifyPassword(password1, hash1));
        assertFalse(PasswordUtil.verifyPassword(password1, hash2));
        assertTrue(PasswordUtil.verifyPassword(password2, hash2));
        assertFalse(PasswordUtil.verifyPassword(password2, hash1));
    }

    @Test
    public void testPasswordWithSpecialCharacters() {
        String password = "P@ssw0rd!@#$%^&*()";
        String hash = PasswordUtil.hashPassword(password);
        
        assertTrue(PasswordUtil.verifyPassword(password, hash));
        assertFalse(PasswordUtil.verifyPassword("P@ssw0rd!@#$%^&*() ", hash));
    }

    @Test
    public void testPasswordWithUnicode() {
        String password = "パスワード123";
        String hash = PasswordUtil.hashPassword(password);
        
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    public void testLongPassword() {
        String password = "a".repeat(1000);
        String hash = PasswordUtil.hashPassword(password);
        
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }
}

