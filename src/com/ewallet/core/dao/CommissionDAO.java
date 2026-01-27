package com.ewallet.core.dao;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.models.Commission;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class CommissionDAO {
    
    /**
     * Créer une nouvelle commission
     */
    public boolean create(Commission commission) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO commission (agent_id, transaction_id, montant_commission, pourcentage, statut) " +
                         "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, commission.getAgentId());
            ps.setInt(2, commission.getTransactionId());
            ps.setDouble(3, commission.getMontantCommission());
            ps.setDouble(4, commission.getPourcentage());
            ps.setString(5, commission.getStatut() != null ? commission.getStatut() : "PENDING");
            
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créer la commission");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Trouver les commissions d'un agent
     */
    public List<Commission> findByAgentId(int agentId) {
        List<Commission> commissions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT commission_id, agent_id, transaction_id, montant_commission, " +
                         "pourcentage, date_creation, statut " +
                         "FROM commission WHERE agent_id = ? ORDER BY date_creation DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                commissions.add(mapResultSetToCommission(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher les commissions");
            e.printStackTrace();
        }
        
        return commissions;
    }
    
    /**
     * Obtenir toutes les commissions (pour admin)
     */
    public List<Commission> findAll() {
        List<Commission> commissions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT c.commission_id, c.agent_id, c.transaction_id, c.montant_commission, " +
                         "c.pourcentage, c.date_creation, c.statut, " +
                         "u.nom as agent_nom, u.prenom as agent_prenom, u.email as agent_email " +
                         "FROM commission c " +
                         "LEFT JOIN utilisateur u ON c.agent_id = u.utilisateur_id " +
                         "ORDER BY c.date_creation DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Commission commission = mapResultSetToCommission(rs);
                // Ajouter les infos de l'agent
                commission.setAgentNom(rs.getString("agent_nom"));
                commission.setAgentPrenom(rs.getString("agent_prenom"));
                commission.setAgentEmail(rs.getString("agent_email"));
                commissions.add(commission);
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher toutes les commissions");
            e.printStackTrace();
        }
        
        return commissions;
    }
    
    /**
     * Trouver une commission par ID
     */
    public Commission findById(int commissionId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT c.commission_id, c.agent_id, c.transaction_id, c.montant_commission, " +
                         "c.pourcentage, c.date_creation, c.statut, " +
                         "u.nom as agent_nom, u.prenom as agent_prenom, u.email as agent_email " +
                         "FROM commission c " +
                         "LEFT JOIN utilisateur u ON c.agent_id = u.utilisateur_id " +
                         "WHERE c.commission_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, commissionId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Commission commission = mapResultSetToCommission(rs);
                commission.setAgentNom(rs.getString("agent_nom"));
                commission.setAgentPrenom(rs.getString("agent_prenom"));
                commission.setAgentEmail(rs.getString("agent_email"));
                return commission;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher la commission par ID");
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtenir le total des commissions d'un agent
     */
    public double getTotalCommissions(int agentId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT SUM(montant_commission) as total FROM commission WHERE agent_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de calculer le total des commissions");
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Obtenir les commissions d'aujourd'hui
     */
    public double getTodayCommissions(int agentId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT SUM(montant_commission) as total " +
                         "FROM commission " +
                         "WHERE agent_id = ? AND DATE(date_creation) = CURDATE()";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de calculer les commissions d'aujourd'hui");
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Obtenir les commissions non payées
     */
    public double getPendingCommissions(int agentId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT SUM(montant_commission) as total " +
                         "FROM commission WHERE agent_id = ? AND statut = 'PENDING'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de calculer les commissions en attente");
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Obtenir les commissions payées
     */
    public double getPaidCommissions(int agentId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT SUM(montant_commission) as total " +
                         "FROM commission WHERE agent_id = ? AND statut = 'PAID'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de calculer les commissions payées");
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Mettre à jour le statut d'une commission
     */
    public boolean updateStatus(int commissionId, String newStatus) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE commission SET statut = ? WHERE commission_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, commissionId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour le statut de la commission");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mettre à jour le statut avec justification
     */
    public boolean updateStatusWithJustification(int commissionId, String newStatus, String justification) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE commission SET statut = ? WHERE commission_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, commissionId);
            
            boolean success = ps.executeUpdate() > 0;
            
            if (success) {
                // Loguer dans une table d'audit si nécessaire
                System.out.println("[COMMISSION] Statut mis à jour: " + commissionId + " -> " + newStatus + " - " + justification);
            }
            
            return success;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour le statut de la commission");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtenir les statistiques globales des commissions
     */
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Total commissions
            String sql = "SELECT COUNT(*) as total, SUM(montant_commission) as montant_total FROM commission";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                stats.put("total_count", rs.getInt("total"));
                stats.put("total_amount", rs.getDouble("montant_total"));
            }
            
            // Par statut
            sql = "SELECT statut, COUNT(*) as count, SUM(montant_commission) as montant " +
                  "FROM commission GROUP BY statut";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map<String, Map<String, Object>> byStatus = new java.util.HashMap<>();
            while (rs.next()) {
                Map<String, Object> statusData = new java.util.HashMap<>();
                statusData.put("count", rs.getInt("count"));
                statusData.put("amount", rs.getDouble("montant"));
                byStatus.put(rs.getString("statut"), statusData);
            }
            stats.put("by_status", byStatus);
            
            // Par agent
            sql = "SELECT u.email, u.nom, u.prenom, " +
                  "COUNT(c.commission_id) as count, SUM(c.montant_commission) as total " +
                  "FROM commission c " +
                  "JOIN utilisateur u ON c.agent_id = u.utilisateur_id " +
                  "GROUP BY u.utilisateur_id, u.email, u.nom, u.prenom " +
                  "ORDER BY total DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            List<Map<String, Object>> byAgent = new java.util.ArrayList<>();
            while (rs.next()) {
                Map<String, Object> agentData = new java.util.HashMap<>();
                agentData.put("email", rs.getString("email"));
                agentData.put("nom", rs.getString("nom"));
                agentData.put("prenom", rs.getString("prenom"));
                agentData.put("count", rs.getInt("count"));
                agentData.put("total", rs.getDouble("total"));
                byAgent.add(agentData);
            }
            stats.put("by_agent", byAgent);
            
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de calculer les statistiques globales");
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Mapper ResultSet vers Commission
     */
    private Commission mapResultSetToCommission(ResultSet rs) throws SQLException {
        Commission c = new Commission();
        
        c.setId(rs.getInt("commission_id"));
        c.setAgentId(rs.getInt("agent_id"));
        c.setTransactionId(rs.getInt("transaction_id"));
        c.setMontantCommission(rs.getDouble("montant_commission"));
        c.setPourcentage(rs.getDouble("pourcentage"));
        c.setStatut(rs.getString("statut"));
        
        Timestamp date = rs.getTimestamp("date_creation");
        if (date != null) {
            c.setDateCommission(date.toLocalDateTime());
        }
        
        return c;
    }

    /**
     * Recherche de commissions avec filtres
     */
    public List<Commission> findWithFilters(Map<String, Object> filters) {
        List<Commission> commissions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.* FROM commission c WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        
        // Filtre par statut
        if (filters.containsKey("statut")) {
            sql.append(" AND c.statut = ?");
            params.add(filters.get("statut"));
        }
        
        // Filtre par agent
        if (filters.containsKey("agent_id")) {
            sql.append(" AND c.agent_id = ?");
            params.add(filters.get("agent_id"));
        }
        
        // Filtre par date de début
        if (filters.containsKey("start_date")) {
            sql.append(" AND c.date_creation >= ?");
            params.add(filters.get("start_date"));
        }
        
        // Filtre par date de fin
        if (filters.containsKey("end_date")) {
            sql.append(" AND c.date_creation <= ?");
            params.add(filters.get("end_date"));
        }
        
        sql.append(" ORDER BY c.date_creation DESC");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            // Définir les paramètres
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    commissions.add(mapResultSetToCommission(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Erreur recherche commissions avec filtres: " + e.getMessage());
            e.printStackTrace();
        }
        
        return commissions;
    }
}