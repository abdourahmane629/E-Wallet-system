package com.ewallet.core.dao;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.models.Transaction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

@SuppressWarnings("unused")
public class TransactionDAO {

    /**
     * Créer une nouvelle transaction dans la table transaction
     */
    public int createTransaction(Transaction transaction) {
        String sql = "INSERT INTO transaction (numero_transaction, type_id, montant, " +
                     "portefeuille_source_id, portefeuille_destination_id, statut, description, agent_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Générer un numéro de transaction unique
            String numeroTransaction = "TXN" + System.currentTimeMillis() + 
                                     (int)(Math.random() * 1000);
            
            // Déterminer le type_id
            int typeId = getTypeIdByNom(transaction.getType());
            
            // Déterminer source et destination selon le type
            Integer sourceId = null;
            Integer destId = null;
            
            if ("DEBIT".equalsIgnoreCase(transaction.getType()) || 
                "RETRAIT".equalsIgnoreCase(transaction.getType())) {
                sourceId = transaction.getPortefeuilleId();
            } else if ("CREDIT".equalsIgnoreCase(transaction.getType()) || 
                      "DEPOT".equalsIgnoreCase(transaction.getType())) {
                destId = transaction.getPortefeuilleId();
            }
            
            ps.setString(1, numeroTransaction);
            ps.setInt(2, typeId);
            ps.setDouble(3, Math.abs(transaction.getMontant())); // Montant toujours positif
            ps.setObject(4, sourceId);
            ps.setObject(5, destId);
            ps.setString(6, "CONFIRME");
            ps.setString(7, transaction.getDescription());
            
            if (transaction.getAgentId() > 0) {
                ps.setInt(8, transaction.getAgentId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            
            if (rs.next()) {
                int transactionId = rs.getInt(1);
                System.out.println("[DAO] Transaction créée: ID=" + transactionId + 
                                 ", type=" + transaction.getType() + 
                                 ", montant=" + transaction.getMontant());
                return transactionId;
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créer une transaction");
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Créer une transaction simple (compatibilité)
     */
    public Transaction create(int portefeuilleId, double montant, String type, String description) {
        Transaction transaction = new Transaction();
        transaction.setPortefeuilleId(portefeuilleId);
        transaction.setMontant(montant);
        transaction.setType(type);
        transaction.setDescription(description);
        
        int transactionId = createTransaction(transaction);
        if (transactionId > 0) {
            transaction.setId(transactionId);
            return transaction;
        }
        return null;
    }

    /**
     * Récupérer toutes les transactions d'un portefeuille
     */
    public List<Transaction> findByPortefeuilleId(int portefeuilleId) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT t.transaction_id, t.numero_transaction, tt.nom_type as type, " +
                         "t.montant, t.portefeuille_source_id, t.portefeuille_destination_id, " +
                         "t.statut, t.description, t.date_transaction, t.agent_id " +
                         "FROM transaction t " +
                         "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                         "WHERE t.portefeuille_source_id = ? OR t.portefeuille_destination_id = ? " +
                         "ORDER BY t.date_transaction DESC LIMIT 50";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, portefeuilleId);
            ps.setInt(2, portefeuilleId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs, portefeuilleId));
            }
            
            System.out.println("[DAO] " + transactions.size() + " transactions trouvées pour portefeuille: " + portefeuilleId);
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer les transactions");
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Récupérer les X dernières transactions
     */
    public List<Transaction> findLatestByPortefeuilleId(int portefeuilleId, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT t.transaction_id, t.numero_transaction, tt.nom_type as type, " +
                         "t.montant, t.portefeuille_source_id, t.portefeuille_destination_id, " +
                         "t.statut, t.description, t.date_transaction, t.agent_id " +
                         "FROM transaction t " +
                         "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                         "WHERE t.portefeuille_source_id = ? OR t.portefeuille_destination_id = ? " +
                         "ORDER BY t.date_transaction DESC LIMIT ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, portefeuilleId);
            ps.setInt(2, portefeuilleId);
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs, portefeuilleId));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer les dernières transactions");
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Récupérer toutes les transactions (pour admin dashboard)
     */
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT t.transaction_id, t.numero_transaction, tt.nom_type as type, " +
                         "t.montant, t.portefeuille_source_id, t.portefeuille_destination_id, " +
                         "t.statut, t.description, t.date_transaction, t.agent_id " +
                         "FROM transaction t " +
                         "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                         "ORDER BY t.date_transaction DESC LIMIT 1000";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                // Pour toutes les transactions, on ne peut pas spécifier un portefeuille
                // On utilise 0 comme portefeuilleId par défaut
                Transaction t = new Transaction();
                t.setId(rs.getInt("transaction_id"));
                t.setNumeroTransaction(rs.getString("numero_transaction"));
                t.setPortefeuilleId(0); // Pas de portefeuille spécifique pour admin
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setStatut(rs.getString("statut"));
                t.setAgentId(rs.getInt("agent_id"));
                t.setMontant(rs.getDouble("montant")); // Montant absolu pour admin
                
                Timestamp ts = rs.getTimestamp("date_transaction");
                if (ts != null) {
                    t.setDateTransaction(ts.toLocalDateTime());
                }
                
                transactions.add(t);
            }
            
            System.out.println("[DAO ADMIN] " + transactions.size() + " transactions trouvées");
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer toutes les transactions");
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Rechercher des transactions avec critères multiples
     */
    public List<Transaction> searchTransactions(java.util.Date startDate, java.util.Date endDate, String type, String searchTerm) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT t.transaction_id, t.numero_transaction, tt.nom_type as type, " +
                "t.montant, t.portefeuille_source_id, t.portefeuille_destination_id, " +
                "t.statut, t.description, t.date_transaction, t.agent_id " +
                "FROM transaction t " +
                "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                "WHERE 1=1"
            );
            
            List<Object> params = new ArrayList<>();
            
            if (startDate != null) {
                sql.append(" AND DATE(t.date_transaction) >= ?");
                params.add(new java.sql.Date(startDate.getTime()));
            }
            
            if (endDate != null) {
                sql.append(" AND DATE(t.date_transaction) <= ?");
                params.add(new java.sql.Date(endDate.getTime()));
            }
            
            if (type != null && !type.isEmpty() && !type.equals("TOUS")) {
                sql.append(" AND tt.nom_type = ?");
                params.add(type);
            }
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                sql.append(" AND (t.numero_transaction LIKE ? OR t.description LIKE ?)");
                String termPattern = "%" + searchTerm + "%";
                params.add(termPattern);
                params.add(termPattern);
            }
            
            sql.append(" ORDER BY t.date_transaction DESC LIMIT 500");
            
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("transaction_id"));
                t.setNumeroTransaction(rs.getString("numero_transaction"));
                t.setPortefeuilleId(0);
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setStatut(rs.getString("statut"));
                t.setAgentId(rs.getInt("agent_id"));
                t.setMontant(rs.getDouble("montant"));
                
                Timestamp ts = rs.getTimestamp("date_transaction");
                if (ts != null) {
                    t.setDateTransaction(ts.toLocalDateTime());
                }
                
                transactions.add(t);
            }
            
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de rechercher des transactions");
            e.printStackTrace();
        }
        return transactions;
    }


    /**
 * Récupérer les transactions effectuées par un agent
 */
    public List<Transaction> findByAgentId(int agentId, java.util.Date startDate, java.util.Date endDate) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT t.transaction_id, t.numero_transaction, tt.nom_type as type, " +
                "t.montant, t.portefeuille_source_id, t.portefeuille_destination_id, " +
                "t.statut, t.description, t.date_transaction, t.agent_id, " +
                "p.utilisateur_id, u.email as client_email, " +
                "CONCAT(u.prenom, ' ', u.nom) as client_nom " +
                "FROM transaction t " +
                "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                "LEFT JOIN portefeuille p ON (t.portefeuille_source_id = p.portefeuille_id OR " +
                "t.portefeuille_destination_id = p.portefeuille_id) " +
                "LEFT JOIN utilisateur u ON p.utilisateur_id = u.utilisateur_id " +
                "WHERE t.agent_id = ? AND t.statut = 'CONFIRME' "
            );
            
            List<Object> params = new ArrayList<>();
            params.add(agentId);
            
            if (startDate != null) {
                sql.append(" AND DATE(t.date_transaction) >= ?");
                params.add(new java.sql.Date(startDate.getTime()));
            }
            
            if (endDate != null) {
                sql.append(" AND DATE(t.date_transaction) <= ?");
                params.add(new java.sql.Date(endDate.getTime()));
            }
            
            sql.append(" ORDER BY t.date_transaction DESC");
            
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransactionForAgent(rs));
            }
            
            System.out.println("[DAO] " + transactions.size() + " transactions trouvées pour agent: " + agentId);
            
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer les transactions par agent");
            e.printStackTrace();
        }
        return transactions;
    }
    /**
     * Compter les transactions d'aujourd'hui
     */
    public int countTodayTransactions() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM transaction " +
                        "WHERE DATE(date_transaction) = CURRENT_DATE() " +
                        "AND statut = 'CONFIRME'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de compter les transactions d'aujourd'hui");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Obtenir le volume des transactions ce mois
     */
    public double getMonthlyTransactionVolume() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT SUM(ABS(montant)) as volume FROM transaction " +
                        "WHERE MONTH(date_transaction) = MONTH(CURRENT_DATE()) " +
                        "AND YEAR(date_transaction) = YEAR(CURRENT_DATE()) " +
                        "AND statut = 'CONFIRME'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("volume");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible d'obtenir le volume mensuel");
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Obtenir des statistiques sur les transactions
     */
    public Map<String, Object> getTransactionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Transactions aujourd'hui
            String sql = "SELECT COUNT(*) as today_count FROM transaction " +
                        "WHERE DATE(date_transaction) = CURRENT_DATE() " +
                        "AND statut = 'CONFIRME'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("today_count", rs.getInt("today_count"));
            }
            
            // Volume aujourd'hui
            sql = "SELECT SUM(ABS(montant)) as today_volume FROM transaction " +
                  "WHERE DATE(date_transaction) = CURRENT_DATE() " +
                  "AND statut = 'CONFIRME'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("today_volume", rs.getDouble("today_volume"));
            }
            
            // Transactions ce mois
            sql = "SELECT COUNT(*) as month_count FROM transaction " +
                  "WHERE MONTH(date_transaction) = MONTH(CURRENT_DATE()) " +
                  "AND YEAR(date_transaction) = YEAR(CURRENT_DATE()) " +
                  "AND statut = 'CONFIRME'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("month_count", rs.getInt("month_count"));
            }
            
            // Volume ce mois
            sql = "SELECT SUM(ABS(montant)) as month_volume FROM transaction " +
                  "WHERE MONTH(date_transaction) = MONTH(CURRENT_DATE()) " +
                  "AND YEAR(date_transaction) = YEAR(CURRENT_DATE()) " +
                  "AND statut = 'CONFIRME'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("month_volume", rs.getDouble("month_volume"));
            }
            
            // Par type de transaction (UNIQUEMENT les 4 types de votre BD)
            sql = "SELECT tt.nom_type, COUNT(t.transaction_id) as count, " +
                  "SUM(ABS(t.montant)) as volume " +
                  "FROM transaction t " +
                  "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                  "WHERE t.statut = 'CONFIRME' " +
                  "GROUP BY tt.nom_type";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map<String, Map<String, Object>> typeStats = new HashMap<>();
            while (rs.next()) {
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("count", rs.getInt("count"));
                typeData.put("volume", rs.getDouble("volume"));
                typeStats.put(rs.getString("nom_type"), typeData);
            }
            stats.put("by_type", typeStats);
            
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible d'obtenir les statistiques transactions");
            e.printStackTrace();
        }
        return stats;
    }

    /**
 * Obtenir des statistiques d'un agent spécifique
 */
    public Map<String, Object> getAgentStatistics(int agentId, String period) {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String dateFilter = "";
            
            switch (period.toUpperCase()) {
                case "TODAY":
                    dateFilter = "AND DATE(t.date_transaction) = CURRENT_DATE()";
                    break;
                case "WEEK":
                    dateFilter = "AND t.date_transaction >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY)";
                    break;
                case "MONTH":
                    dateFilter = "AND t.date_transaction >= DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY)";
                    break;
                default:
                    dateFilter = "";
            }
            
            // Total transactions
            String sql = "SELECT COUNT(*) as total_transactions, " +
                        "SUM(ABS(t.montant)) as total_amount, " +
                        "SUM(CASE WHEN tt.nom_type = 'DEPOT' THEN 1 ELSE 0 END) as deposit_count, " +
                        "SUM(CASE WHEN tt.nom_type = 'RETRAIT' THEN 1 ELSE 0 END) as withdrawal_count " +
                        "FROM transaction t " +
                        "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                        "WHERE t.agent_id = ? AND t.statut = 'CONFIRME' " + dateFilter;
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                stats.put("total_transactions", rs.getInt("total_transactions"));
                stats.put("total_amount", rs.getDouble("total_amount"));
                stats.put("deposit_count", rs.getInt("deposit_count"));
                stats.put("withdrawal_count", rs.getInt("withdrawal_count"));
            }
            
            // Commission générée
            sql = "SELECT COALESCE(SUM(montant_commission), 0) as total_commission " +
                "FROM commission WHERE agent_id = ? AND statut != 'CANCELLED'";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                stats.put("total_commission", rs.getDouble("total_commission"));
            }
            
            // Top clients (clients avec lesquels l'agent a le plus traité)
            sql = "SELECT u.email, COUNT(t.transaction_id) as transaction_count, " +
                "SUM(ABS(t.montant)) as total_amount " +
                "FROM transaction t " +
                "JOIN portefeuille p ON (t.portefeuille_source_id = p.portefeuille_id OR " +
                "t.portefeuille_destination_id = p.portefeuille_id) " +
                "JOIN utilisateur u ON p.utilisateur_id = u.utilisateur_id " +
                "WHERE t.agent_id = ? AND u.role = 'CLIENT' " +
                "GROUP BY u.email " +
                "ORDER BY total_amount DESC " +
                "LIMIT 5";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, agentId);
            rs = ps.executeQuery();
            
            List<Map<String, Object>> topClients = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> client = new HashMap<>();
                client.put("email", rs.getString("email"));
                client.put("transaction_count", rs.getInt("transaction_count"));
                client.put("total_amount", rs.getDouble("total_amount"));
                topClients.add(client);
            }
            stats.put("top_clients", topClients);
            
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer les statistiques de l'agent");
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Obtenir l'ID du type de transaction par son nom 
     */
    private int getTypeIdByNom(String type) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT type_id FROM type_transaction WHERE nom_type = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // Mapper les types - EXACTEMENT comme dans votre BD
            String nomType;
            switch (type.toUpperCase()) {
                case "DEPOT":
                case "CREDIT":
                    nomType = "DEPOT";
                    break;
                case "RETRAIT":
                case "DEBIT":
                    nomType = "RETRAIT";
                    break;
                case "TRANSFERT":
                    nomType = "TRANSFERT";
                    break;
                case "PAIEMENT_SERVICE":
                case "SERVICE":
                case "PAIEMENT":
                    nomType = "PAIEMENT_SERVICE";
                    break;
                default:
                    nomType = "DEPOT"; // Par défaut
            }
            
            ps.setString(1, nomType);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("type_id");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer type_id");
            e.printStackTrace();
        }
        return 1; // Dépot par défaut
    }

    /**
     * Mapper ResultSet vers Transaction
     */
    private Transaction mapResultSetToTransaction(ResultSet rs, int portefeuilleId) throws SQLException {
        Transaction t = new Transaction();
        
        t.setId(rs.getInt("transaction_id"));
        t.setNumeroTransaction(rs.getString("numero_transaction"));
        t.setPortefeuilleId(portefeuilleId);
        t.setType(rs.getString("type"));
        t.setDescription(rs.getString("description"));
        t.setStatut(rs.getString("statut"));
        t.setAgentId(rs.getInt("agent_id"));
        
        // Calculer le montant signé
        double montant = rs.getDouble("montant");
        Integer sourceId = rs.getObject("portefeuille_source_id") != null ? 
                          rs.getInt("portefeuille_source_id") : null;
        Integer destId = rs.getObject("portefeuille_destination_id") != null ? 
                        rs.getInt("portefeuille_destination_id") : null;
        
        if (sourceId != null && sourceId == portefeuilleId) {
            t.setMontant(-montant); // Débit (négatif)
        } else if (destId != null && destId == portefeuilleId) {
            t.setMontant(montant); // Crédit (positif)
        } else {
            t.setMontant(montant);
        }
        
        Timestamp ts = rs.getTimestamp("date_transaction");
        if (ts != null) {
            t.setDateTransaction(ts.toLocalDateTime());
        }
        
        return t;
    }

    /**
 * Mapper ResultSet vers Transaction pour la vue agent (avec info client)
 */
    private Transaction mapResultSetToTransactionForAgent(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        
        t.setId(rs.getInt("transaction_id"));
        t.setNumeroTransaction(rs.getString("numero_transaction"));
        t.setType(rs.getString("type"));
        t.setDescription(rs.getString("description"));
        t.setStatut(rs.getString("statut"));
        t.setAgentId(rs.getInt("agent_id"));
        t.setMontant(rs.getDouble("montant"));
        
        // Ajouter les infos client
        String clientEmail = rs.getString("client_email");
        String clientNom = rs.getString("client_nom");
        if (clientEmail != null) {
            t.setDescription(t.getDescription() + " (Client: " + clientEmail + ")");
        }
        
        Timestamp ts = rs.getTimestamp("date_transaction");
        if (ts != null) {
            t.setDateTransaction(ts.toLocalDateTime());
        }
        
        return t;
    }

    /**
     * Créer une transaction et retourner uniquement l'ID (pour compatibilité)
     */
    public int createAndReturnId(int portefeuilleId, double montant, String type, String description) {
        Transaction transaction = create(portefeuilleId, montant, type, description);
        return transaction != null ? transaction.getId() : -1;
    }

    /**
     * Supprimer une transaction (rarement utilisé)
     */
    public boolean delete(int transactionId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "DELETE FROM transaction WHERE transaction_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, transactionId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de supprimer la transaction");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Créer une transaction avec tous les champs
     */
    public int createFullTransaction(String numeroTransaction, int typeId, double montant, 
                                    Integer portefeuilleSourceId, Integer portefeuilleDestinationId,
                                    String statut, String description, Integer agentId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO transaction " +
                        "(numero_transaction, type_id, montant, portefeuille_source_id, " +
                        "portefeuille_destination_id, statut, description, agent_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, numeroTransaction);
            ps.setInt(2, typeId);
            ps.setDouble(3, montant);
            
            if (portefeuilleSourceId != null) {
                ps.setInt(4, portefeuilleSourceId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            
            if (portefeuilleDestinationId != null) {
                ps.setInt(5, portefeuilleDestinationId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            
            ps.setString(6, statut);
            ps.setString(7, description);
            
            if (agentId != null) {
                ps.setInt(8, agentId);
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            
            if (rs.next()) {
                int transactionId = rs.getInt(1);
                System.out.println("[TRANSACTION DAO] Transaction créée: ID=" + transactionId);
                return transactionId;
            }
        } catch (SQLException e) {
            System.err.println("[TRANSACTION DAO] Impossible de créer une transaction complète");
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Obtenir les transactions par jour pour graphique
     */
    public Map<String, Number> getDailyTransactionCount(int days) {
        Map<String, Number> dailyData = new LinkedHashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT DATE(date_transaction) as date, COUNT(*) as count " +
                        "FROM transaction " +
                        "WHERE date_transaction >= DATE_SUB(CURRENT_DATE(), INTERVAL ? DAY) " +
                        "AND statut = 'CONFIRME' " +
                        "GROUP BY DATE(date_transaction) " +
                        "ORDER BY date DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                java.sql.Date date = rs.getDate("date");
                int count = rs.getInt("count");
                if (date != null) {
                    dailyData.put(date.toString(), count);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[TRANSACTION DAO] Erreur récupération transactions quotidiennes");
            e.printStackTrace();
        }
        
        return dailyData;
    }


    /**
 * Créer un paiement de service
 */
    public int createServicePayment(int portefeuilleId, double montant, String serviceType, String reference) {
        String sql = "INSERT INTO transaction (numero_transaction, type_id, montant, " +
                    "portefeuille_source_id, portefeuille_destination_id, statut, description) " +
                    "VALUES (?, ?, ?, ?, NULL, 'CONFIRME', ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Générer un numéro de transaction
            String numeroTransaction = "TXN" + System.currentTimeMillis() + 
                                    (int)(Math.random() * 1000);
            
            // type_id = 4 pour PAIEMENT_SERVICE
            ps.setString(1, numeroTransaction);
            ps.setInt(2, 4); // PAIEMENT_SERVICE
            ps.setDouble(3, montant); // Montant positif
            ps.setInt(4, portefeuilleId); // Portefeuille source
            ps.setString(5, serviceType + " - Réf: " + reference + " - " + String.format("%.0f", montant) + " GNF");
            
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            
            if (rs.next()) {
                int transactionId = rs.getInt(1);
                System.out.println("[DAO] Paiement service créé: ID=" + transactionId + 
                                ", service=" + serviceType + ", montant=" + montant);
                return transactionId;
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créer le paiement de service");
            e.printStackTrace();
        }
        return -1;
    }



    /**
     * Recherche de transactions avec filtres
     */
    public List<Transaction> findWithFilters(Map<String, Object> filters) {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.* FROM transaction t WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        
        // Filtre par texte (recherche dans description, numero_transaction)
        if (filters.containsKey("search")) {
            sql.append(" AND (t.description LIKE ? OR t.numero_transaction LIKE ?)");
            String searchTerm = "%" + filters.get("search") + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }
        
        // Filtre par type
        if (filters.containsKey("type")) {
            sql.append(" AND t.type = ?");
            params.add(filters.get("type"));
        }
        
        // Filtre par statut
        if (filters.containsKey("statut")) {
            sql.append(" AND t.statut = ?");
            params.add(filters.get("statut"));
        }
        
        // Filtre par date de début
        if (filters.containsKey("start_date")) {
            sql.append(" AND t.date_transaction >= ?");
            params.add(filters.get("start_date"));
        }
        
        // Filtre par date de fin
        if (filters.containsKey("end_date")) {
            sql.append(" AND t.date_transaction <= ?");
            params.add(filters.get("end_date"));
        }
        
        sql.append(" ORDER BY t.date_transaction DESC");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            // Définir les paramètres
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransactionForAgent(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Erreur recherche transactions avec filtres: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
}