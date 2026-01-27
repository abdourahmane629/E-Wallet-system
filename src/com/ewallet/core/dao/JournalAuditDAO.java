package com.ewallet.core.dao;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.models.JournalAudit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JournalAuditDAO {
    
    /**
     * Enregistrer une action dans le journal d'audit
     */
    public int logAction(int utilisateurId, String action, String entite, 
                        Integer entiteId, String ancienneValeur, 
                        String nouvelleValeur, String adresseIp) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO journal_audit " +
                        "(utilisateur_id, action, entite, entite_id, " +
                        "ancienne_valeur, nouvelle_valeur, adresse_ip) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, utilisateurId);
            ps.setString(2, action);
            ps.setString(3, entite);
            
            if (entiteId != null) {
                ps.setInt(4, entiteId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            
            ps.setString(5, ancienneValeur);
            ps.setString(6, nouvelleValeur);
            ps.setString(7, adresseIp);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int journalId = rs.getInt(1);
                    System.out.println("[AUDIT] Action enregistrée: " + action + 
                                     " (ID: " + journalId + ")");
                    return journalId;
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("[ERREUR AUDIT] Impossible d'enregistrer l'action: " + action);
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Enregistrer une action simple
     */
    public boolean logSimpleAction(int utilisateurId, String action, String description) {
        return logAction(utilisateurId, action, null, null, null, description, "127.0.0.1") > 0;
    }
    
    /**
     * Récupérer l'historique d'audit pour un utilisateur
     */
    public List<JournalAudit> findByUtilisateurId(int utilisateurId) {
        List<JournalAudit> logs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT journal_id, utilisateur_id, action, entite, " +
                        "entite_id, ancienne_valeur, nouvelle_valeur, " +
                        "adresse_ip, date_action " +
                        "FROM journal_audit WHERE utilisateur_id = ? " +
                        "ORDER BY date_action DESC LIMIT 100";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToJournalAudit(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR AUDIT] Impossible de récupérer l'historique");
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Récupérer tous les logs récents
     */
    public List<JournalAudit> findAllRecent(int limit) {
        List<JournalAudit> logs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT journal_id, utilisateur_id, action, entite, " +
                        "entite_id, ancienne_valeur, nouvelle_valeur, " +
                        "adresse_ip, date_action " +
                        "FROM journal_audit ORDER BY date_action DESC LIMIT ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToJournalAudit(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR AUDIT] Impossible de récupérer les logs");
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Rechercher des logs par action
     */
    public List<JournalAudit> findByAction(String action) {
        List<JournalAudit> logs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT journal_id, utilisateur_id, action, entite, " +
                        "entite_id, ancienne_valeur, nouvelle_valeur, " +
                        "adresse_ip, date_action " +
                        "FROM journal_audit WHERE action LIKE ? " +
                        "ORDER BY date_action DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + action + "%");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToJournalAudit(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR AUDIT] Impossible de rechercher par action");
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Récupérer les logs par période
     */
    public List<JournalAudit> findByDateRange(Timestamp startDate, Timestamp endDate) {
        List<JournalAudit> logs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT journal_id, utilisateur_id, action, entite, " +
                        "entite_id, ancienne_valeur, nouvelle_valeur, " +
                        "adresse_ip, date_action " +
                        "FROM journal_audit WHERE date_action BETWEEN ? AND ? " +
                        "ORDER BY date_action DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, startDate);
            ps.setTimestamp(2, endDate);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToJournalAudit(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR AUDIT] Impossible de filtrer par date");
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Mapper ResultSet vers JournalAudit
     */
    private JournalAudit mapResultSetToJournalAudit(ResultSet rs) throws SQLException {
        JournalAudit log = new JournalAudit();
        
        log.setJournalId(rs.getInt("journal_id"));
        log.setUtilisateurId(rs.getInt("utilisateur_id"));
        log.setAction(rs.getString("action"));
        log.setEntite(rs.getString("entite"));
        
        int entiteId = rs.getInt("entite_id");
        if (!rs.wasNull()) {
            log.setEntiteId(entiteId);
        }
        
        log.setAncienneValeur(rs.getString("ancienne_valeur"));
        log.setNouvelleValeur(rs.getString("nouvelle_valeur"));
        log.setAdresseIp(rs.getString("adresse_ip"));
        log.setDateAction(rs.getTimestamp("date_action").toLocalDateTime());
        
        return log;
    }
    
    /**
     * Purger les logs anciens (maintenance)
     */
    public boolean purgeOldLogs(int daysToKeep) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "DELETE FROM journal_audit WHERE date_action < DATE_SUB(NOW(), INTERVAL ? DAY)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, daysToKeep);
            
            int rowsDeleted = ps.executeUpdate();
            System.out.println("[AUDIT] " + rowsDeleted + " logs anciens supprimés");
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR AUDIT] Impossible de purger les logs");
            e.printStackTrace();
            return false;
        }
    }
}