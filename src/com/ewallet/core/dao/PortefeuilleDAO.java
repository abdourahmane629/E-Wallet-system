package com.ewallet.core.dao;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.models.Portefeuille;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("unused")
public class PortefeuilleDAO {

    /**
     * Trouver un portefeuille par ID utilisateur
     */
    public Portefeuille findByUtilisateurId(int utilisateurId) {
        Portefeuille portefeuille = null;
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT portefeuille_id, utilisateur_id, numero_portefeuille, solde, " +
                         "devise, statut, limite_retrait_quotidien, limite_transfert, " +
                         "date_creation, date_modification " +
                         "FROM portefeuille WHERE utilisateur_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                portefeuille = mapResultSetToPortefeuille(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher le portefeuille par utilisateur ID: " + utilisateurId);
            e.printStackTrace();
        }
        return portefeuille;
    }

    /**
     * Trouver un portefeuille par ID
     */
    public static Portefeuille findById(int portefeuilleId) {
        Portefeuille portefeuille = null;
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT portefeuille_id, utilisateur_id, numero_portefeuille, solde, " +
                         "devise, statut, limite_retrait_quotidien, limite_transfert, " +
                         "date_creation, date_modification " +
                         "FROM portefeuille WHERE portefeuille_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, portefeuilleId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                portefeuille = mapResultSetToPortefeuille(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher le portefeuille par ID: " + portefeuilleId);
            e.printStackTrace();
        }
        return portefeuille;
    }

    /**
     * Créer un portefeuille pour un utilisateur
     */
    public boolean create(int utilisateurId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Vérifier d'abord si un portefeuille existe déjà
            String checkSql = "SELECT portefeuille_id FROM portefeuille WHERE utilisateur_id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, utilisateurId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                System.out.println("[DAO] Portefeuille existe déjà pour utilisateur: " + utilisateurId);
                return true; // Portefeuille existe déjà
            }
            
            // Générer un numéro de portefeuille unique
            String numeroPortefeuille = "WLT" + System.currentTimeMillis() + utilisateurId;
            if (numeroPortefeuille.length() > 50) {
                numeroPortefeuille = numeroPortefeuille.substring(0, 50);
            }
            
            // Créer le portefeuille
            String sql = "INSERT INTO portefeuille (utilisateur_id, numero_portefeuille, solde, devise, statut, " +
                         "limite_retrait_quotidien, limite_transfert) " +
                         "VALUES (?, ?, 0.00, 'GNF', 'ACTIF', 1000000.0, 500000.0)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, utilisateurId);
            ps.setString(2, numeroPortefeuille);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[DAO] Portefeuille créé avec succès pour utilisateur: " + utilisateurId);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créer le portefeuille");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Créditer un portefeuille
     */
    public boolean credit(int portefeuilleId, double montant) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE portefeuille SET solde = solde + ?, date_modification = CURRENT_TIMESTAMP " +
                         "WHERE portefeuille_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, montant);
            ps.setInt(2, portefeuilleId);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[DAO] Portefeuille crédité: ID=" + portefeuilleId + ", montant=" + montant);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créditer le portefeuille");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Débiter un portefeuille
     */
    public boolean debit(int portefeuilleId, double montant) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Vérifier d'abord le solde
            String checkSql = "SELECT solde FROM portefeuille WHERE portefeuille_id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, portefeuilleId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                double solde = rs.getDouble("solde");
                if (solde < montant) {
                    System.err.println("[DAO] Solde insuffisant: " + solde + " < " + montant);
                    return false;
                }
            } else {
                System.err.println("[DAO] Portefeuille non trouvé: " + portefeuilleId);
                return false;
            }
            
            // Effectuer le débit
            String sql = "UPDATE portefeuille SET solde = solde - ?, date_modification = CURRENT_TIMESTAMP " +
                         "WHERE portefeuille_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, montant);
            ps.setInt(2, portefeuilleId);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[DAO] Portefeuille débité: ID=" + portefeuilleId + ", montant=" + montant);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de débiter le portefeuille");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mettre à jour le solde
     */
    public boolean updateSolde(int portefeuilleId, double nouveauSolde) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE portefeuille SET solde = ?, date_modification = CURRENT_TIMESTAMP " +
                         "WHERE portefeuille_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, nouveauSolde);
            ps.setInt(2, portefeuilleId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour le solde");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mettre à jour les limites
     */
    public boolean updateLimites(int portefeuilleId, double limiteRetrait, double limiteTransfert) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE portefeuille SET limite_retrait_quotidien = ?, limite_transfert = ?, " +
                         "date_modification = CURRENT_TIMESTAMP WHERE portefeuille_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, limiteRetrait);
            ps.setDouble(2, limiteTransfert);
            ps.setInt(3, portefeuilleId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour les limites");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Transfert atomique entre deux portefeuilles
     */
    public boolean transfer(int fromPortefeuilleId, int toPortefeuilleId, double montant) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Vérifier solde expéditeur
            String checkSql = "SELECT solde FROM portefeuille WHERE portefeuille_id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, fromPortefeuilleId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                double solde = rs.getDouble("solde");
                if (solde < montant) {
                    conn.rollback();
                    System.err.println("[PORTEFEUILLE DAO] Transfert impossible: solde insuffisant");
                    return false;
                }
            } else {
                conn.rollback();
                return false;
            }
            
            // 2. Débiter expéditeur
            String debitSql = "UPDATE portefeuille SET solde = solde - ?, date_modification = CURRENT_TIMESTAMP " +
                             "WHERE portefeuille_id = ?";
            PreparedStatement debitPs = conn.prepareStatement(debitSql);
            debitPs.setDouble(1, montant);
            debitPs.setInt(2, fromPortefeuilleId);
            int debitRows = debitPs.executeUpdate();
            
            // 3. Créditer destinataire
            String creditSql = "UPDATE portefeuille SET solde = solde + ?, date_modification = CURRENT_TIMESTAMP " +
                              "WHERE portefeuille_id = ?";
            PreparedStatement creditPs = conn.prepareStatement(creditSql);
            creditPs.setDouble(1, montant);
            creditPs.setInt(2, toPortefeuilleId);
            int creditRows = creditPs.executeUpdate();
            
            if (debitRows > 0 && creditRows > 0) {
                conn.commit();
                System.out.println("[PORTEFEUILLE DAO] Transfert réussi: " + montant + " GNF de " + 
                                 fromPortefeuilleId + " à " + toPortefeuilleId);
                return true;
            } else {
                conn.rollback();
                return false;
            }
            
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.err.println("[PORTEFEUILLE DAO] Échec transfert atomique");
            e.printStackTrace();
            return false;
        } finally {
            try { 
                if (conn != null) conn.setAutoCommit(true); 
                if (conn != null) conn.close();
            } catch (SQLException e) {}
        }
    }

    /**
     * Vérifier si un débit est possible
     */
    public boolean canDebit(int portefeuilleId, double montant) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT solde, limite_retrait_quotidien FROM portefeuille WHERE portefeuille_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, portefeuilleId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                double solde = rs.getDouble("solde");
                return solde >= montant;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[PORTEFEUILLE DAO] Impossible de vérifier le débit");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer tous les portefeuilles (pour admin dashboard)
     */
    public List<Portefeuille> findAll() {
        List<Portefeuille> wallets = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT portefeuille_id, utilisateur_id, numero_portefeuille, solde, " +
                        "devise, statut, limite_retrait_quotidien, limite_transfert, " +
                        "date_creation, date_modification " +
                        "FROM portefeuille ORDER BY date_creation DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                wallets.add(mapResultSetToPortefeuille(rs));
            }
            
            System.out.println("[DAO ADMIN] " + wallets.size() + " portefeuilles trouvés");
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer tous les portefeuilles");
            e.printStackTrace();
        }
        return wallets;
    }

    /**
     * Obtenir le solde total de tous les portefeuilles
     */
    public double getTotalBalance() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT SUM(solde) as total FROM portefeuille WHERE statut = 'ACTIF'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer le solde total");
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Mettre à jour le statut d'un portefeuille
     */
    public boolean updateStatus(int portefeuilleId, String newStatus) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE portefeuille SET statut = ?, date_modification = CURRENT_TIMESTAMP " +
                        "WHERE portefeuille_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, portefeuilleId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour le statut du portefeuille");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Rechercher des portefeuilles avec critères
     */
    public List<Portefeuille> searchWallets(String searchTerm, String status) {
        List<Portefeuille> wallets = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT p.portefeuille_id, p.utilisateur_id, p.numero_portefeuille, p.solde, " +
                "p.devise, p.statut, p.limite_retrait_quotidien, p.limite_transfert, " +
                "p.date_creation, p.date_modification, u.email " +
                "FROM portefeuille p " +
                "JOIN utilisateur u ON p.utilisateur_id = u.utilisateur_id " +
                "WHERE 1=1"
            );
            
            List<Object> params = new ArrayList<>();
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                sql.append(" AND (p.numero_portefeuille LIKE ? OR u.email LIKE ? OR u.nom LIKE ? OR u.prenom LIKE ?)");
                String termPattern = "%" + searchTerm + "%";
                params.add(termPattern);
                params.add(termPattern);
                params.add(termPattern);
                params.add(termPattern);
            }
            
            if (status != null && !status.isEmpty() && !status.equals("TOUS")) {
                sql.append(" AND p.statut = ?");
                params.add(status);
            }
            
            sql.append(" ORDER BY p.date_creation DESC");
            
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Portefeuille wallet = mapResultSetToPortefeuille(rs);
                wallets.add(wallet);
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de rechercher des portefeuilles");
            e.printStackTrace();
        }
        return wallets;
    }

    /**
     * Obtenir les portefeuilles avec le plus gros solde (Top 10)
     */
    public List<Portefeuille> getTopWalletsByBalance(int limit) {
        List<Portefeuille> wallets = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT p.portefeuille_id, p.utilisateur_id, p.numero_portefeuille, p.solde, " +
                        "p.devise, p.statut, p.limite_retrait_quotidien, p.limite_transfert, " +
                        "p.date_creation, p.date_modification, u.email, u.nom, u.prenom " +
                        "FROM portefeuille p " +
                        "JOIN utilisateur u ON p.utilisateur_id = u.utilisateur_id " +
                        "WHERE p.statut = 'ACTIF' " +
                        "ORDER BY p.solde DESC LIMIT ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                wallets.add(mapResultSetToPortefeuille(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer les top portefeuilles");
            e.printStackTrace();
        }
        return wallets;
    }

    /**
     * Obtenir des statistiques sur les portefeuilles
     */
    public Map<String, Object> getWalletStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Statistiques générales
            String sql = "SELECT " +
                        "COUNT(*) as total, " +
                        "SUM(solde) as total_solde, " +
                        "AVG(solde) as solde_moyen, " +
                        "MAX(solde) as solde_max, " +
                        "MIN(solde) as solde_min " +
                        "FROM portefeuille WHERE statut = 'ACTIF'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("total_solde", rs.getDouble("total_solde"));
                stats.put("solde_moyen", rs.getDouble("solde_moyen"));
                stats.put("solde_max", rs.getDouble("solde_max"));
                stats.put("solde_min", rs.getDouble("solde_min"));
            }
            
            // Par statut
            sql = "SELECT statut, COUNT(*) as count, SUM(solde) as total_solde " +
                  "FROM portefeuille GROUP BY statut";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map<String, Map<String, Object>> statusStats = new HashMap<>();
            while (rs.next()) {
                Map<String, Object> statusData = new HashMap<>();
                statusData.put("count", rs.getInt("count"));
                statusData.put("total_solde", rs.getDouble("total_solde"));
                statusStats.put(rs.getString("statut"), statusData);
            }
            stats.put("by_status", statusStats);
            
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible d'obtenir les statistiques portefeuilles");
            e.printStackTrace();
        }
        return stats;
    }


     /**
     * Recherche de portefeuilles avec filtres
     */
    public List<Portefeuille> findWithFilters(Map<String, Object> filters) {
        List<Portefeuille> portefeuilles = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.* FROM portefeuille p WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        
        // Filtre par numéro de portefeuille
        if (filters.containsKey("search_wallet")) {
            sql.append(" AND p.numero_portefeuille LIKE ?");
            params.add("%" + filters.get("search_wallet") + "%");
        }
        
        // Filtre par client (recherche dans utilisateur)
        if (filters.containsKey("search_client")) {
            sql.append(" AND EXISTS (SELECT 1 FROM utilisateur u " +
                      "WHERE u.utilisateur_id = p.utilisateur_id " +
                      "AND (u.email LIKE ? OR u.nom LIKE ? OR u.prenom LIKE ?))");
            String searchTerm = "%" + filters.get("search_client") + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }
        
        // Filtre par statut
        if (filters.containsKey("statut")) {
            sql.append(" AND p.statut = ?");
            params.add(filters.get("statut"));
        }
        
        // Filtre par solde minimum
        if (filters.containsKey("min_balance")) {
            sql.append(" AND p.solde >= ?");
            params.add(filters.get("min_balance"));
        }
        
        // Filtre par solde maximum
        if (filters.containsKey("max_balance")) {
            sql.append(" AND p.solde <= ?");
            params.add(filters.get("max_balance"));
        }
        
        sql.append(" ORDER BY p.date_creation DESC");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            // Définir les paramètres
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    portefeuilles.add(mapResultSetToPortefeuille(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Erreur recherche portefeuilles avec filtres: " + e.getMessage());
            e.printStackTrace();
        }
        
        return portefeuilles;
    }
    
    /**
     * Mapper ResultSet vers Portefeuille
     */
    private static Portefeuille mapResultSetToPortefeuille(ResultSet rs) throws SQLException {
        Portefeuille p = new Portefeuille();
        
        p.setId(rs.getInt("portefeuille_id"));
        p.setUtilisateurId(rs.getInt("utilisateur_id"));
        p.setNumeroPortefeuille(rs.getString("numero_portefeuille"));
        p.setSolde(rs.getDouble("solde"));
        p.setDevise(rs.getString("devise"));
        p.setStatut(rs.getString("statut"));
        p.setLimiteRetraitQuotidien(rs.getDouble("limite_retrait_quotidien"));
        p.setLimiteTransfert(rs.getDouble("limite_transfert"));
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            p.setDateCreation(dateCreation.toLocalDateTime());
        }
        
        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            p.setDateModification(dateModification.toLocalDateTime());
        }
        
        return p;
    }
}