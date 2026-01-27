package com.ewallet.core.dao;

import com.ewallet.core.models.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private Connection connection;
    
    public NotificationDAO(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Crée une nouvelle notification
     */
    public boolean create(Notification notification) {
        String sql = "INSERT INTO notification (utilisateur_id, titre, message, type, est_lue) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, notification.getUtilisateurId());
            stmt.setString(2, notification.getTitre());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getType());
            stmt.setBoolean(5, notification.isEstLue());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notification.setNotificationId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur création notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère les notifications d'un utilisateur
     */
    public List<Notification> getByUserId(int userId, boolean unreadOnly) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE utilisateur_id = ?";
        
        if (unreadOnly) {
            sql += " AND est_lue = 0";
        }
        
        sql += " ORDER BY date_creation DESC LIMIT 50";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification notification = mapResultSetToNotification(rs);
                    notifications.add(notification);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération notifications: " + e.getMessage());
        }
        
        return notifications;
    }
    
    /**
     * Compte les notifications non lues d'un utilisateur
     */
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE utilisateur_id = ? AND est_lue = 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur comptage notifications: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Marque une notification comme lue
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notification SET est_lue = 1 WHERE notification_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur marquage notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notification SET est_lue = 1 WHERE utilisateur_id = ? AND est_lue = 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur marquage notifications: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime les notifications anciennes (plus de 30 jours)
     */
    public boolean cleanupOldNotifications(int daysToKeep) {
        String sql = "DELETE FROM notification WHERE date_creation < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, daysToKeep);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage notifications: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mappe un ResultSet vers un objet Notification
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUtilisateurId(rs.getInt("utilisateur_id"));
        notification.setTitre(rs.getString("titre"));
        notification.setMessage(rs.getString("message"));
        notification.setType(rs.getString("type"));
        notification.setEstLue(rs.getBoolean("est_lue"));
        notification.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return notification;
    }
}