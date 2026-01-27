package com.ewallet.core.utils;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class NotificationUtil {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Retourne l'icône selon le type de notification
     */
    public static String getIconByType(String type) {
        if (type == null) return "🔔";
        
        switch (type.toLowerCase()) {
            case "transaction":
            case "transfert":
                return "💸";
            case "securite":
            case "security":
                return "🔒";
            case "systeme":
            case "system":
                return "⚙️";
            case "commission":
                return "💰";
            case "portefeuille":
            case "wallet":
                return "🏦";
            case "alerte":
            case "alert":
                return "⚠️";
            case "depot":
                return "📥";
            case "retrait":
                return "📤";
            case "paiement":
                return "💳";
            default:
                return "🔔";
        }
    }
    
    /**
     * Retourne la couleur CSS selon le type de notification
     */
    public static String getColorClassByType(String type) {
        if (type == null) return "#3498db";
        
        switch (type.toLowerCase()) {
            case "transaction":
            case "transfert":
                return "#3498db"; // Bleu
            case "securite":
            case "security":
                return "#e74c3c"; // Rouge
            case "systeme":
            case "system":
                return "#7f8c8d"; // Gris
            case "commission":
                return "#f39c12"; // Orange
            case "portefeuille":
            case "wallet":
                return "#9b59b6"; // Violet
            case "alerte":
            case "alert":
                return "#e74c3c"; // Rouge
            case "depot":
                return "#27ae60"; // Vert
            case "retrait":
                return "#e74c3c"; // Rouge
            case "paiement":
                return "#f39c12"; // Orange
            default:
                return "#3498db"; // Bleu par défaut
        }
    }
    
    /**
     * Formate le temps écoulé de façon humaine
     */
    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Récemment";
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return "À l'instant";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return "Il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return "Il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        } else if (seconds < 2592000) { // 30 jours
            long days = seconds / 86400;
            return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
        } else {
            return dateTime.format(DATE_FORMATTER);
        }
    }
    
    /**
     * Formate la date pour l'affichage
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        
        if (dateTime.toLocalDate().equals(now.toLocalDate())) {
            return "Aujourd'hui à " + dateTime.format(TIME_FORMATTER);
        } else if (dateTime.toLocalDate().equals(yesterday.toLocalDate())) {
            return "Hier à " + dateTime.format(TIME_FORMATTER);
        } else {
            return dateTime.format(FULL_FORMATTER);
        }
    }
    
    /**
     * Retourne la classe CSS pour le badge selon le type
     */
    public static String getBadgeClassByType(String type) {
        if (type == null) return "badge-primary";
        
        switch (type.toLowerCase()) {
            case "transaction":
            case "transfert":
                return "badge-primary";
            case "securite":
            case "security":
                return "badge-danger";
            case "systeme":
            case "system":
                return "badge-secondary";
            case "commission":
                return "badge-warning";
            case "portefeuille":
            case "wallet":
                return "badge-info";
            case "alerte":
            case "alert":
                return "badge-danger";
            case "depot":
                return "badge-success";
            case "retrait":
                return "badge-danger";
            case "paiement":
                return "badge-warning";
            default:
                return "badge-light";
        }
    }
    
    /**
     * Tronque un texte trop long
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Formate le montant pour l'affichage
     */
    public static String formatAmount(double amount) {
        return String.format("%,.2f", amount);
    }
    
    /**
     * Vérifie si la notification est récente (< 5 minutes)
     */
    public static boolean isRecent(LocalDateTime dateTime, int minutesThreshold) {
        if (dateTime == null) return false;
        
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        return duration.toMinutes() < minutesThreshold;
    }
    
    /**
     * Retourne le nom du type en français
     */
    public static String getTypeInFrench(String type) {
        if (type == null) return "Notification";
        
        switch (type.toLowerCase()) {
            case "transaction":
                return "Transaction";
            case "securite":
                return "Sécurité";
            case "systeme":
                return "Système";
            case "commission":
                return "Commission";
            case "portefeuille":
                return "Portefeuille";
            case "alerte":
                return "Alerte";
            case "depot":
                return "Dépôt";
            case "retrait":
                return "Retrait";
            case "transfert":
                return "Transfert";
            case "paiement":
                return "Paiement";
            default:
                return type;
        }
    }
}