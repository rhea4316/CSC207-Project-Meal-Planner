package com.mealplanner.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;

/**
 * Utility class for password hashing and verification using PBKDF2 algorithm.
 * Provides secure password storage and comparison functionality.
 *
 * Responsible: Everyone (shared utility)
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String DELIMITER = ":";

    /**
     * Hash a password using PBKDF2 with a random salt.
     *
     * @param password Plain text password to hash
     * @return Hashed password string in format "salt:hash" (base64 encoded)
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String password) {
        Objects.requireNonNull(password, "Password cannot be null");
        if (password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash the password
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Encode salt and hash to base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            // Return format: "salt:hash"
            return saltBase64 + DELIMITER + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a password against a stored hash.
     *
     * @param password Plain text password to verify
     * @param storedHash Stored hash in format "salt:hash" (base64 encoded)
     * @return true if password matches the hash, false otherwise
     * @throws IllegalArgumentException if password or storedHash is null or empty
     */
    public static boolean verifyPassword(String password, String storedHash) {
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(storedHash, "Stored hash cannot be null");

        if (password.trim().isEmpty() || storedHash.trim().isEmpty()) {
            return false;
        }

        try {
            // Split salt and hash
            String[] parts = storedHash.split(DELIMITER, 2);
            if (parts.length != 2) {
                return false;
            }

            String saltBase64 = parts[0];
            String hashBase64 = parts[1];

            // Decode salt and hash from base64
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] storedHashBytes = Base64.getDecoder().decode(hashBase64);

            // Hash the provided password with the same salt
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] computedHash = factory.generateSecret(spec).getEncoded();

            // Compare hashes using constant-time comparison to prevent timing attacks
            return constantTimeEquals(computedHash, storedHashBytes);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }

    /**
     * Constant-time comparison of two byte arrays to prevent timing attacks.
     *
     * @param a First byte array
     * @param b Second byte array
     * @return true if arrays are equal, false otherwise
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    private PasswordUtil() {
        // Utility class - prevent instantiation
    }
}

