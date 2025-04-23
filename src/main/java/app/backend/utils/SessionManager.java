package app.backend.utils;

import app.backend.models.User;

public class SessionManager {

    private static User currentUser = null;

    // Set the currently logged-in user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Get the currently logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Clear the session (logout)
    public static void logout() {
        currentUser = null;
    }

    // Check if someone is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
