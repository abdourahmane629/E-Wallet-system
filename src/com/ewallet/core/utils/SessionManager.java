package com.ewallet.core.utils;

import com.ewallet.core.models.Utilisateur;

public class SessionManager {
    private static Utilisateur currentUser;
    
    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }
    
    public static Utilisateur getCurrentUser() {
        return currentUser;
    }
    
    public static void clearSession() {
        currentUser = null;
    }

    public static void updateCurrentUser(Utilisateur user) {
        currentUser = user;
    }
}