package com.smartassignment.util;

import java.util.regex.Pattern;

/**
 * Utility class for common input validations (email, phone, roll numbers, password strength, etc.).
 * Uses core Java methods to handle string null checks.
 */
public class ValidationUtil {

    // Standard email matching pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Simple phone matching pattern (+ prefix followed by 10 to 14 digits, or plain 10-digit number)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );

    /**
     * Checks if a string is null, empty, or whitespace only.
     * 
     * @param value the string to check.
     * @return true if empty/blank, false otherwise.
     */
    public static boolean isEmptyOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates email format.
     * 
     * @param email the email address string.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (isEmptyOrBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format.
     * Allowable formats include +12345678901 or 1234567890 (length between 10 and 15 digits).
     * 
     * @param phone the phone string.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidPhone(String phone) {
        if (isEmptyOrBlank(phone)) {
            return true; // Phone is typically optional/nullable in the schema
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates password strength.
     * Rules: Minimum 8 characters, must contain at least one letter and one number.
     * 
     * @param password the raw password.
     * @return true if it meets the strength criteria, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        if (isEmptyOrBlank(password) || password.length() < 8) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enforces sanitization of strings to prevent XSS.
     * Escapes standard HTML tags.
     * 
     * @param raw the raw input string.
     * @return escaped safe string.
     */
    public static String sanitizeHtml(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;")
                  .replace("/", "&#x2F;");
    }

    /**
     * Validates max length constraints.
     * 
     * @param value the string.
     * @param maxLength the maximum allowed length.
     * @return true if within limits, false if exceeded.
     */
    public static boolean isWithinLength(String value, int maxLength) {
        if (value == null) {
            return true;
        }
        return value.length() <= maxLength;
    }
}
