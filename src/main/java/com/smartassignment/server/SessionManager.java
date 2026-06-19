package com.smartassignment.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom thread-safe session manager to replace HttpSession.
 * Generates and validates tokens for authenticated API requests.
 */
public class SessionManager {

    private static final long SESSION_TIMEOUT_MS = 60 * 60 * 1000; // 1 hour session limit
    private static final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public static class UserSession {
        private final String token;
        private final int userId;
        private final String email;
        private final String role;
        private final String displayName;
        private final Integer classId; // Optional, only for students
        private long expiryTime;

        public UserSession(String token, int userId, String email, String role, String displayName, Integer classId) {
            this.token = token;
            this.userId = userId;
            this.email = email;
            this.role = role.toUpperCase();
            this.displayName = displayName;
            this.classId = classId;
            this.expiryTime = System.currentTimeMillis() + SESSION_TIMEOUT_MS;
        }

        public String getToken() { return token; }
        public int getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getDisplayName() { return displayName; }
        public Integer getClassId() { return classId; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        public void extendSession() {
            this.expiryTime = System.currentTimeMillis() + SESSION_TIMEOUT_MS;
        }
    }

    /**
     * Creates a new session and returns the generated session token.
     */
    public static UserSession createSession(int userId, String email, String role, String displayName, Integer classId) {
        // Clean up expired sessions periodically to save memory
        cleanExpiredSessions();

        String token = UUID.randomUUID().toString();
        UserSession session = new UserSession(token, userId, email, role, displayName, classId);
        sessions.put(token, session);
        return session;
    }

    /**
     * Gets a session by token, validating its existence and expiration status.
     * Extends session lifetime if valid.
     */
    public static UserSession getSession(String token) {
        if (token == null) return null;
        UserSession session = sessions.get(token);
        if (session != null) {
            if (session.isExpired()) {
                sessions.remove(token);
                return null;
            }
            session.extendSession(); // Keep session alive on active use
            return session;
        }
        return null;
    }

    /**
     * Destroys the session for the given token.
     */
    public static void destroySession(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private static void cleanExpiredSessions() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
