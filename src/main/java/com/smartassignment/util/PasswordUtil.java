package com.smartassignment.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing and verification.
 * Enforces SHA-256 encryption.
 */
public class PasswordUtil {

    /**
     * Hashes a plain-text password using the SHA-256 cryptographic hash function.
     * 
     * @param password the plain-text password.
     * @return a 64-character hexadecimal representation of the hash.
     * @throws RuntimeException if the SHA-256 algorithm is not supported by the environment.
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 hashing algorithm not found", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash.
     * 
     * @param password the plain-text password.
     * @param storedHash the stored hexadecimal SHA-256 hash.
     * @return true if the hashes match, false otherwise.
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        String hash = hashPassword(password);
        return hash.equalsIgnoreCase(storedHash);
    }

    /**
     * Generates a random alphanumeric temporary password.
     * Useful for password resets.
     * 
     * @return an 8-character random string.
     */
    public static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
