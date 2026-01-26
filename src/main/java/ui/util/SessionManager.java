package ui.util;

import ui.model.User;

/**
 * Session Manager - Singleton pattern
 * Manages the current logged-in user session
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for Singleton
    }

    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Set current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("✅ User session created: " + user.getFullName());
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clear session (logout)
     */
    public void clearSession() {
        if (currentUser != null) {
            System.out.println("✅ User session cleared: " + currentUser.getFullName());
        }
        this.currentUser = null;
    }

    /**
     * Get current user's full name
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
}
