package com.ewallet.core.utils;

import java.util.regex.Pattern;

public class ValidationUtil {

    /**
     * Valider un email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(regex, email);
    }

    /**
     * Valider un numéro de téléphone guinéen
     * Format: 62x xx xx xx, 65x xx xx xx, 61x xx xx xx (9 chiffres)
     * Exemples: 625676554, 611121212, 651122132
     */
    public static boolean isValidGuineanPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        
        // Enlever les espaces et tirets
        String cleaned = phone.replaceAll("[\\s\\-]", "");
        
        // Vérifier que c'est 9 chiffres
        if (cleaned.length() != 9) return false;
        
        // Vérifier que c'est tout des chiffres
        if (!cleaned.matches("\\d{9}")) return false;
        
        // Vérifier que ça commence par 62x, 65x ou 61x
        String prefix = cleaned.substring(0, 2);
        if (!prefix.equals("62") && !prefix.equals("65") && !prefix.equals("61")) {
            return false;
        }
        
        return true;
    }

    /**
     * Valider un mot de passe
     */
    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.length() >= 6;
    }

    /**
     * Valider que deux mots de passe correspondent
     */
    public static boolean passwordsMatch(String password1, String password2) {
        if (password1 == null || password2 == null) return false;
        return password1.equals(password2);
    }

    /**
     * Valider un nom/prénom
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return name.trim().length() >= 2;
    }

    /**
     * Obtenir message d'erreur pour téléphone
     */
    public static String getPhoneErrorMessage() {
        return "Numéro invalide.\nFormat: 9 chiffres commençant par 62x, 65x ou 61x\nExemples: 625676554, 611121212, 651122132";
    }
}