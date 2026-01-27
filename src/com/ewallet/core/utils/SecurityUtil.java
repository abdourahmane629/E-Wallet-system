package com.ewallet.core.utils;

import java.security.MessageDigest;

public class SecurityUtil {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for(byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

   public static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
    
    // AJOUTER CES MÉTHODES POUR LE PIN
    public static String hashPin(String pin) {
        // Pour le PIN (4 chiffres), on peut utiliser SHA-256 aussi
        return hashPassword(pin);
    }
    
    public static boolean verifyPin(String pin, String pinHash) {
        if (pin == null || pinHash == null) return false;
        return hashPin(pin).equals(pinHash);
    }
    
    public static boolean isValidPin(String pin) {
        // PIN doit être 4 chiffres
        return pin != null && pin.matches("\\d{4}");
    }

    

}