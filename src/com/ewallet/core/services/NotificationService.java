
package com.ewallet.core.services;

import com.ewallet.core.dao.NotificationDAO;
import com.ewallet.core.models.Notification;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {
    private NotificationDAO notificationDAO;
    
    // Types de notifications
    public static final String TYPE_TRANSACTION = "transaction";
    public static final String TYPE_SECURITY = "securite";
    public static final String TYPE_SYSTEM = "systeme";
    public static final String TYPE_COMMISSION = "commission";
    public static final String TYPE_WALLET = "portefeuille";
    public static final String TYPE_ALERT = "alerte";
    
    public NotificationService(Connection connection) {
        this.notificationDAO = new NotificationDAO(connection);
    }
    
    /**
     * Crée une notification générique
     */
    public boolean createNotification(int userId, String titre, String message, String type) {
        Notification notification = new Notification();
        notification.setUtilisateurId(userId);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setType(type);
        notification.setEstLue(false);
        notification.setDateCreation(LocalDateTime.now());
        
        return notificationDAO.create(notification);
    }
    // ============ NOTIFICATIONS COMMUNES ============
    
    /**
     * Notification de connexion réussie
     */
    public void notifyLoginSuccess(int userId) {
        String titre = "Connexion réussie";
        String message = "Vous vous êtes connecté avec succès à votre compte.";
        createNotification(userId, titre, message, TYPE_SECURITY);
    }
    
    /**
     * Notification de connexion échouée
     */
    public void notifyLoginFailed(int userId, String ipAddress) {
        String titre = "Tentative de connexion échouée";
        String message = String.format("Une tentative de connexion a échoué depuis l'adresse IP: %s", ipAddress);
        createNotification(userId, titre, message, TYPE_SECURITY);
    }
    
    /**
     * Notification de modification de profil
     */
    public void notifyProfileUpdate(int userId) {
        String titre = "Profil mis à jour";
        String message = "Vos informations de profil ont été mises à jour avec succès.";
        createNotification(userId, titre, message, TYPE_SYSTEM);
    }
    
    /**
     * Notification de changement de mot de passe
     */
    public void notifyPasswordChange(int userId) {
        String titre = "Mot de passe modifié";
        String message = "Votre mot de passe a été changé avec succès.";
        createNotification(userId, titre, message, TYPE_SECURITY);
    }
    
    // ============ NOTIFICATIONS CLIENTS ============
    
    /**
     * Notification de transaction effectuée
     */
    public void notifyTransactionSent(int userId, double amount, String recipient, String transactionNumber) {
        String titre = "Transaction envoyée";
        String message = String.format("Vous avez envoyé %.2f à %s. Référence: %s", 
                                     amount, recipient, transactionNumber);
        createNotification(userId, titre, message, TYPE_TRANSACTION);
    }
    
    /**
     * Notification de transaction reçue
     */
    public void notifyTransactionReceived(int userId, double amount, String sender, String transactionNumber) {
        String titre = "Transaction reçue";
        String message = String.format("Vous avez reçu %.2f de %s. Référence: %s", 
                                     amount, sender, transactionNumber);
        createNotification(userId, titre, message, TYPE_TRANSACTION);
    }
    
    /**
     * Notification de retrait
     */
    public void notifyWithdrawal(int userId, double amount) {
        String titre = "Retrait effectué";
        String message = String.format("Retrait de %.2f effectué avec succès.", amount);
        createNotification(userId, titre, message, TYPE_WALLET);
    }
    
    /**
     * Notification de dépôt
     */
    public void notifyDeposit(int userId, double amount) {
        String titre = "Dépôt effectué";
        String message = String.format("Dépôt de %.2f effectué avec succès.", amount);
        createNotification(userId, titre, message, TYPE_WALLET);
    }
    
    /**
     * Notification d'approche de limite
     */
    public void notifyLimitWarning(int userId, String limitType, double current, double max) {
        String titre = "Alerte de limite";
        String message = String.format("Vous avez atteint %.2f sur %.2f de votre limite de %s.", 
                                     current, max, limitType);
        createNotification(userId, titre, message, TYPE_ALERT);
    }
    
    // ============ NOTIFICATIONS AGENTS ============
    
    /**
     * Notification de commission générée
     */
    public void notifyCommissionEarned(int agentId, double commissionAmount, int transactionId) {
        String titre = "Commission générée";
        String message = String.format("Vous avez gagné %.2f de commission sur la transaction #%d", 
                                     commissionAmount, transactionId);
        createNotification(agentId, titre, message, TYPE_COMMISSION);
    }
    
    /**
     * Notification de transaction à valider
     */
    public void notifyTransactionToValidate(int agentId, int transactionId, double amount) {
        String titre = "Transaction à valider";
        String message = String.format("Une transaction de %.2f (#%d) nécessite votre validation.", 
                                     amount, transactionId);
        createNotification(agentId, titre, message, TYPE_TRANSACTION);
    }
    
    /**
     * Notification de validation de transaction
     */
    public void notifyTransactionValidated(int agentId, int transactionId) {
        String titre = "Transaction validée";
        String message = String.format("La transaction #%d a été validée avec succès.", transactionId);
        createNotification(agentId, titre, message, TYPE_TRANSACTION);
    }
    
    // ============ NOTIFICATIONS ADMINS ============
    
    /**
     * Notification d'inscription d'un nouvel utilisateur
     */
    public void notifyNewUserRegistration(int adminId, String userEmail, String userType) {
        String titre = "Nouvel utilisateur inscrit";
        String message = String.format("Un nouveau %s s'est inscrit: %s", userType, userEmail);
        createNotification(adminId, titre, message, TYPE_SYSTEM);
    }
    
    /**
     * Notification d'action administrative
     */
    public void notifyAdminAction(int adminId, String action, String target) {
        String titre = "Action administrative";
        String message = String.format("%s effectué sur: %s", action, target);
        createNotification(adminId, titre, message, TYPE_SYSTEM);
    }
    
    /**
     * Notification de rapport généré
     */
    public void notifyReportGenerated(int adminId, String reportType) {
        String titre = "Rapport généré";
        String message = String.format("Le rapport %s a été généré avec succès.", reportType);
        createNotification(adminId, titre, message, TYPE_SYSTEM);
    }
    
    // ============ MÉTHODES UTILITAIRES ============
    
    /**
     * Récupère les notifications d'un utilisateur
     */
    public List<Notification> getUserNotifications(int userId, boolean unreadOnly) {
        return notificationDAO.getByUserId(userId, unreadOnly);
    }
    
    /**
     * Compte les notifications non lues
     */
    public int getUnreadCount(int userId) {
        return notificationDAO.getUnreadCount(userId);
    }
    
    /**
     * Marque une notification comme lue
     */
    public boolean markAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }
    
    /**
     * Marque toutes les notifications comme lues
     */
    public boolean markAllAsRead(int userId) {
        return notificationDAO.markAllAsRead(userId);
    }

    
    /**
     * Nettoie les anciennes notifications
     */
    public void cleanupOldNotifications() {
        notificationDAO.cleanupOldNotifications(30); // Garde 30 jours
    }
}