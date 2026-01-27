package com.ewallet.core.services;


import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportService {
    
   
   
    /**
     * Statistiques utilisateurs (CORRIGÉ pour vos rôles: ADMIN, AGENT, USER)
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Total utilisateurs
            String sql = "SELECT COUNT(*) as total FROM utilisateur";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
            }
            
            // Par rôle (selon votre BD: ADMIN, AGENT, USER)
            sql = "SELECT r.nom_role, COUNT(u.utilisateur_id) as count " +
                  "FROM utilisateur u " +
                  "JOIN role r ON u.role_id = r.role_id " +
                  "GROUP BY r.nom_role";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                String role = rs.getString("nom_role");
                int count = rs.getInt("count");
                
                // USER = CLIENT dans votre interface
                if ("USER".equals(role)) {
                    stats.put("clients", count);
                } else if ("AGENT".equals(role)) {
                    stats.put("agents", count);
                } else if ("ADMIN".equals(role)) {
                    stats.put("admins", count);
                }
            }
            
            // Par statut
            sql = "SELECT statut, COUNT(*) as count FROM utilisateur GROUP BY statut";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map<String, Integer> statusStats = new HashMap<>();
            while (rs.next()) {
                statusStats.put(rs.getString("statut"), rs.getInt("count"));
            }
            stats.put("par_statut", statusStats);
            
            // Nouveaux utilisateurs ce mois
            sql = "SELECT COUNT(*) as nouveaux_mois FROM utilisateur " +
                  "WHERE MONTH(date_inscription) = MONTH(CURRENT_DATE()) " +
                  "AND YEAR(date_inscription) = YEAR(CURRENT_DATE())";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("nouveaux_mois", rs.getInt("nouveaux_mois"));
            }
            
        } catch (SQLException e) {
            System.err.println("[REPORT] Erreur statistiques utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Statistiques financières
     */
    public Map<String, Object> getFinancialStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Total des soldes
            String sql = "SELECT SUM(solde) as total_balance FROM portefeuille WHERE statut = 'ACTIF'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("total_balance", rs.getDouble("total_balance"));
            }
            
            // Solde moyen
            sql = "SELECT AVG(solde) as avg_balance FROM portefeuille WHERE statut = 'ACTIF'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("avg_balance", rs.getDouble("avg_balance"));
            }
            
            // Volume des transactions du mois
            sql = "SELECT SUM(ABS(montant)) as month_volume FROM transaction " +
                  "WHERE MONTH(date_transaction) = MONTH(CURRENT_DATE()) " +
                  "AND YEAR(date_transaction) = YEAR(CURRENT_DATE()) " +
                  "AND statut = 'CONFIRME'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("month_volume", rs.getDouble("month_volume"));
            }
            
            // Top 10 portefeuilles
            sql = "SELECT p.numero_portefeuille, p.solde, u.email " +
                  "FROM portefeuille p " +
                  "JOIN utilisateur u ON p.utilisateur_id = u.utilisateur_id " +
                  "WHERE p.statut = 'ACTIF' " +
                  "ORDER BY p.solde DESC LIMIT 10";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            List<Map<String, Object>> topWallets = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> wallet = new HashMap<>();
                wallet.put("numero", rs.getString("numero_portefeuille"));
                wallet.put("solde", rs.getDouble("solde"));
                wallet.put("email", rs.getString("email"));
                topWallets.add(wallet);
            }
            stats.put("top_wallets", topWallets);
            
        } catch (SQLException e) {
            System.err.println("[REPORT] Erreur statistiques financières: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Statistiques transactions
     */
    public Map<String, Object> getTransactionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
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
            sql = "SELECT tt.nom_type, COUNT(t.transaction_id) as count " +
                  "FROM transaction t " +
                  "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                  "WHERE t.statut = 'CONFIRME' " +
                  "GROUP BY tt.nom_type";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map<String, Integer> typeStats = new HashMap<>();
            while (rs.next()) {
                typeStats.put(rs.getString("nom_type"), rs.getInt("count"));
            }
            stats.put("par_type", typeStats);
            
        } catch (SQLException e) {
            System.err.println("[REPORT] Erreur statistiques transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Statistiques commissions (CORRIGÉ pour inclure tous les statuts)
     */
    public Map<String, Object> getCommissionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Total commissions TOUS statuts
            String sql = "SELECT COUNT(*) as total, SUM(montant_commission) as total_montant " +
                        "FROM commission";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("total_commissions", rs.getInt("total"));
                stats.put("total_montant", rs.getDouble("total_montant"));
            }
            
            // Par statut
            sql = "SELECT statut, COUNT(*) as count, SUM(montant_commission) as montant " +
                  "FROM commission GROUP BY statut";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map<String, Map<String, Object>> statsParStatut = new HashMap<>();
            while (rs.next()) {
                Map<String, Object> statutData = new HashMap<>();
                statutData.put("count", rs.getInt("count"));
                statutData.put("montant", rs.getDouble("montant"));
                statsParStatut.put(rs.getString("statut"), statutData);
            }
            stats.put("par_statut", statsParStatut);
            
            // Commissions en attente
            sql = "SELECT COUNT(*) as en_attente, SUM(montant_commission) as montant_attente " +
                  "FROM commission WHERE statut = 'PENDING'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("commissions_en_attente", rs.getInt("en_attente"));
                stats.put("montant_en_attente", rs.getDouble("montant_attente"));
            }
            
            // Commissions payées
            sql = "SELECT COUNT(*) as payees, SUM(montant_commission) as montant_paye " +
                  "FROM commission WHERE statut = 'PAID'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("commissions_payees", rs.getInt("payees"));
                stats.put("montant_paye", rs.getDouble("montant_paye"));
            }
            
            // Top 5 agents par commissions (TOUS statuts)
            sql = "SELECT u.email, u.nom, u.prenom, " +
                  "COUNT(c.commission_id) as nb_commissions, " +
                  "SUM(c.montant_commission) as total_commissions " +
                  "FROM commission c " +
                  "JOIN utilisateur u ON c.agent_id = u.utilisateur_id " +
                  "GROUP BY u.utilisateur_id, u.email, u.nom, u.prenom " +
                  "ORDER BY total_commissions DESC LIMIT 5";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            List<Map<String, Object>> topAgents = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> agent = new HashMap<>();
                agent.put("email", rs.getString("email"));
                agent.put("nom", rs.getString("nom"));
                agent.put("prenom", rs.getString("prenom"));
                agent.put("nb_commissions", rs.getInt("nb_commissions"));
                agent.put("total_commissions", rs.getDouble("total_commissions"));
                topAgents.add(agent);
            }
            stats.put("top_agents", topAgents);
            
            // Commissions du jour
            sql = "SELECT SUM(montant_commission) as aujourdhui " +
                  "FROM commission WHERE DATE(date_creation) = CURRENT_DATE()";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("commissions_aujourdhui", rs.getDouble("aujourdhui"));
            }
            
            // Commissions ce mois
            sql = "SELECT SUM(montant_commission) as mois " +
                  "FROM commission WHERE MONTH(date_creation) = MONTH(CURRENT_DATE()) " +
                  "AND YEAR(date_creation) = YEAR(CURRENT_DATE())";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("commissions_mois", rs.getDouble("mois"));
            }
            
        } catch (SQLException e) {
            System.err.println("[REPORT] Erreur statistiques commissions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Distribution des utilisateurs pour graphique circulaire
     */
    public Map<String, Integer> getUserDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            String sql = "SELECT r.nom_role, COUNT(*) as count " +
                        "FROM utilisateur u " +
                        "JOIN role r ON u.role_id = r.role_id " +
                        "WHERE u.statut = 'ACTIF' " +
                        "GROUP BY r.nom_role";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String role = rs.getString("nom_role");
                // Convertir USER en CLIENT pour l'affichage
                if ("USER".equals(role)) {
                    distribution.put("CLIENT", rs.getInt("count"));
                } else {
                    distribution.put(role, rs.getInt("count"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[REPORT] Erreur distribution utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return distribution;
    }
    
    /**
     * Nombre de transactions par jour (derniers X jours)
     */
    public Map<String, Number> getDailyTransactionCount(int days) {
        Map<String, Number> dailyData = new LinkedHashMap<>();
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            String sql = "SELECT DATE(date_transaction) as date, COUNT(*) as count " +
                        "FROM transaction " +
                        "WHERE date_transaction >= DATE_SUB(CURRENT_DATE(), INTERVAL ? DAY) " +
                        "AND statut = 'CONFIRME' " +
                        "GROUP BY DATE(date_transaction) " +
                        "ORDER BY date";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            
            // Préparer les dates
            LocalDate today = LocalDate.now();
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM"));
                dailyData.put(dateStr, 0);
            }
            
            // Remplir avec les données réelles
            while (rs.next()) {
                java.sql.Date date = rs.getDate("date");
                int count = rs.getInt("count");
                
                if (date != null) {
                    LocalDate localDate = date.toLocalDate();
                    String dateStr = localDate.format(DateTimeFormatter.ofPattern("dd/MM"));
                    dailyData.put(dateStr, count);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[REPORT] Erreur transactions quotidiennes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dailyData;
    }
    
    /**
     * Volume de transactions par mois (derniers X mois)
     */
    public Map<String, Number> getMonthlyTransactionVolume(int months) {
        Map<String, Number> monthlyData = new LinkedHashMap<>();
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            String sql = "SELECT DATE_FORMAT(date_transaction, '%Y-%m') as month, " +
                        "SUM(ABS(montant)) as volume " +
                        "FROM transaction " +
                        "WHERE date_transaction >= DATE_SUB(CURRENT_DATE(), INTERVAL ? MONTH) " +
                        "AND statut = 'CONFIRME' " +
                        "GROUP BY DATE_FORMAT(date_transaction, '%Y-%m') " +
                        "ORDER BY month";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, months);
            ResultSet rs = ps.executeQuery();
            
            // Préparer les mois
            LocalDate today = LocalDate.now();
            for (int i = months - 1; i >= 0; i--) {
                LocalDate date = today.minusMonths(i);
                String monthStr = date.format(DateTimeFormatter.ofPattern("MMM yyyy"));
                monthlyData.put(monthStr, 0.0);
            }
            
            // Remplir avec les données réelles
            while (rs.next()) {
                String month = rs.getString("month");
                double volume = rs.getDouble("volume");
                
                if (month != null) {
                    LocalDate date = LocalDate.parse(month + "-01");
                    String monthStr = date.format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    monthlyData.put(monthStr, volume);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[REPORT] Erreur volume mensuel: " + e.getMessage());
            e.printStackTrace();
        }
        
        return monthlyData;
    }
    
    /**
     * Générer un rapport détaillé
     */
    public String generateReport(String reportType, LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        
        try {
            report.append("=".repeat(60)).append("\n");
            report.append("RAPPORT E-WALLET\n");
            report.append("Type: ").append(reportType).append("\n");
            report.append("Période: ").append(startDate).append(" à ").append(endDate).append("\n");
            report.append("Généré le: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
            report.append("=".repeat(60)).append("\n\n");
            
            switch (reportType) {
                case "Transactions par période":
                    generateTransactionReport(report, startDate, endDate);
                    break;
                    
                case "Utilisateurs par statut":
                    generateUserStatusReport(report, startDate, endDate);
                    break;
                    
                case "Portefeuilles par solde":
                    generateWalletBalanceReport(report, startDate, endDate);
                    break;
                    
                case "Commissions agents":
                    generateCommissionReport(report, startDate, endDate);
                    break;
                    
                case "Activité quotidienne":
                    generateDailyActivityReport(report, startDate, endDate);
                    break;
                    
                default:
                    report.append("Type de rapport non pris en charge.\n");
            }
            
            report.append("\n").append("=".repeat(60)).append("\n");
            report.append("FIN DU RAPPORT\n");
            report.append("=".repeat(60));
            
        } catch (Exception e) {
            report.append("\nERREUR lors de la génération du rapport: ")
                  .append(e.getMessage())
                  .append("\n");
            e.printStackTrace();
        }
        
        return report.toString();
    }
    
    private void generateTransactionReport(StringBuilder report, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Récapitulatif
            String sql = "SELECT " +
                        "COUNT(*) as total, " +
                        "SUM(CASE WHEN montant > 0 THEN montant ELSE 0 END) as total_depots, " +
                        "SUM(CASE WHEN montant < 0 THEN ABS(montant) ELSE 0 END) as total_retraits, " +
                        "SUM(ABS(montant)) as total_volume " +
                        "FROM transaction " +
                        "WHERE DATE(date_transaction) BETWEEN ? AND ? " +
                        "AND statut = 'CONFIRME'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                report.append("# RÉCAPITULATIF DES TRANSACTIONS\n");
                report.append(String.format("Nombre total: %,d\n", rs.getInt("total")));
                report.append(String.format("Total dépôts: %,.0f GNF\n", rs.getDouble("total_depots")));
                report.append(String.format("Total retraits: %,.0f GNF\n", rs.getDouble("total_retraits")));
                report.append(String.format("Volume total: %,.0f GNF\n\n", rs.getDouble("total_volume")));
            }
            
            // Par type (UNIQUEMENT les 4 types de votre BD)
            report.append("# DÉTAIL PAR TYPE DE TRANSACTION\n");
            sql = "SELECT tt.nom_type, COUNT(*) as count, SUM(ABS(t.montant)) as volume " +
                  "FROM transaction t " +
                  "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                  "WHERE DATE(t.date_transaction) BETWEEN ? AND ? " +
                  "AND t.statut = 'CONFIRME' " +
                  "GROUP BY tt.nom_type " +
                  "ORDER BY volume DESC";
            
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();
            
            while (rs.next()) {
                report.append(String.format("- %s: %,d transactions, %,.0f GNF\n",
                    rs.getString("nom_type"),
                    rs.getInt("count"),
                    rs.getDouble("volume")));
            }
            
            // Top 10 transactions
            report.append("\n# TOP 10 TRANSACTIONS (PAR MONTANT)\n");
            sql = "SELECT t.numero_transaction, tt.nom_type, t.montant, " +
                  "t.description, t.date_transaction " +
                  "FROM transaction t " +
                  "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                  "WHERE DATE(t.date_transaction) BETWEEN ? AND ? " +
                  "AND t.statut = 'CONFIRME' " +
                  "ORDER BY ABS(t.montant) DESC LIMIT 10";
            
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();
            
            int rank = 1;
            while (rs.next()) {
                report.append(String.format("%d. [%s] %+,.0f GNF - %s (%s)\n",
                    rank++,
                    rs.getString("numero_transaction"),
                    rs.getDouble("montant"),
                    rs.getString("description"),
                    rs.getTimestamp("date_transaction").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            }
        }
    }
    
    private void generateUserStatusReport(StringBuilder report, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Nouveaux utilisateurs
            String sql = "SELECT COUNT(*) as nouveaux FROM utilisateur " +
                        "WHERE DATE(date_inscription) BETWEEN ? AND ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                report.append("# NOUVEAUX UTILISATEURS\n");
                report.append(String.format("Nombre: %,d\n\n", rs.getInt("nouveaux")));
            }
            
            // Détail par rôle (USER = CLIENT)
            report.append("# RÉPARTITION PAR RÔLE\n");
            sql = "SELECT r.nom_role, COUNT(u.utilisateur_id) as count " +
                  "FROM utilisateur u " +
                  "JOIN role r ON u.role_id = r.role_id " +
                  "WHERE DATE(u.date_inscription) BETWEEN ? AND ? " +
                  "GROUP BY r.nom_role";
            
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();
            
            while (rs.next()) {
                String role = rs.getString("nom_role");
                String displayRole = "USER".equals(role) ? "CLIENT" : role;
                report.append(String.format("- %s: %,d utilisateurs\n",
                    displayRole,
                    rs.getInt("count")));
            }
            
            // Par statut
            report.append("\n# RÉPARTITION PAR STATUT\n");
            sql = "SELECT statut, COUNT(*) as count FROM utilisateur " +
                  "WHERE DATE(date_inscription) BETWEEN ? AND ? " +
                  "GROUP BY statut";
            
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();
            
            while (rs.next()) {
                report.append(String.format("- %s: %,d utilisateurs\n",
                    rs.getString("statut"),
                    rs.getInt("count")));
            }
        }
    }
    
    private void generateWalletBalanceReport(StringBuilder report, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Statistiques générales
            String sql = "SELECT " +
                        "COUNT(*) as total, " +
                        "SUM(solde) as total_solde, " +
                        "AVG(solde) as solde_moyen, " +
                        "MAX(solde) as solde_max, " +
                        "MIN(solde) as solde_min " +
                        "FROM portefeuille " +
                        "WHERE statut = 'ACTIF'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                report.append("# STATISTIQUES DES PORTEFEUILLES\n");
                report.append(String.format("Nombre total: %,d\n", rs.getInt("total")));
                report.append(String.format("Solde total: %,.0f GNF\n", rs.getDouble("total_solde")));
                report.append(String.format("Solde moyen: %,.0f GNF\n", rs.getDouble("solde_moyen")));
                report.append(String.format("Solde maximum: %,.0f GNF\n", rs.getDouble("solde_max")));
                report.append(String.format("Solde minimum: %,.0f GNF\n\n", rs.getDouble("solde_min")));
            }
            
            // Top 10 portefeuilles
            report.append("# TOP 10 PORTEFEUILLES (PAR SOLDE)\n");
            sql = "SELECT p.numero_portefeuille, p.solde, u.email, u.nom, u.prenom " +
                  "FROM portefeuille p " +
                  "JOIN utilisateur u ON p.utilisateur_id = u.utilisateur_id " +
                  "WHERE p.statut = 'ACTIF' " +
                  "ORDER BY p.solde DESC LIMIT 10";
            
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            int rank = 1;
            while (rs.next()) {
                report.append(String.format("%d. %s - %s %s (%s): %,.0f GNF\n",
                    rank++,
                    rs.getString("numero_portefeuille"),
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("email"),
                    rs.getDouble("solde")));
            }
        }
    }
    
    private void generateCommissionReport(StringBuilder report, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Récapitulatif commissions
            String sql = "SELECT " +
                        "COUNT(*) as total, " +
                        "SUM(montant_commission) as total_commissions, " +
                        "AVG(montant_commission) as commission_moyenne " +
                        "FROM commission " +
                        "WHERE DATE(date_creation) BETWEEN ? AND ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                report.append("# RÉCAPITULATIF DES COMMISSIONS\n");
                report.append(String.format("Nombre total: %,d\n", rs.getInt("total")));
                report.append(String.format("Total commissions: %,.0f GNF\n", rs.getDouble("total_commissions")));
                report.append(String.format("Commission moyenne: %,.0f GNF\n\n", rs.getDouble("commission_moyenne")));
            }
            
            // Par statut
            report.append("# COMMISSIONS PAR STATUT\n");
            sql = "SELECT statut, COUNT(*) as count, SUM(montant_commission) as total " +
                  "FROM commission " +
                  "WHERE DATE(date_creation) BETWEEN ? AND ? " +
                  "GROUP BY statut";
            
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();
            
            while (rs.next()) {
                report.append(String.format("- %s: %,d commissions, %,.0f GNF\n",
                    rs.getString("statut"),
                    rs.getInt("count"),
                    rs.getDouble("total")));
            }
            
            // Top agents par commissions
            report.append("\n# TOP 5 AGENTS PAR COMMISSIONS\n");
            sql = "SELECT u.email, u.nom, u.prenom, " +
                  "COUNT(c.commission_id) as nb_commissions, " +
                  "SUM(c.montant_commission) as total_commissions " +
                  "FROM commission c " +
                  "JOIN utilisateur u ON c.agent_id = u.utilisateur_id " +
                  "WHERE DATE(c.date_creation) BETWEEN ? AND ? " +
                  "GROUP BY u.utilisateur_id, u.email, u.nom, u.prenom " +
                  "ORDER BY total_commissions DESC LIMIT 5";
            
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();
            
            int rank = 1;
            while (rs.next()) {
                report.append(String.format("%d. %s %s (%s): %,d commissions, %,.0f GNF\n",
                    rank++,
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("email"),
                    rs.getInt("nb_commissions"),
                    rs.getDouble("total_commissions")));
            }
        }
    }
    
    private void generateDailyActivityReport(StringBuilder report, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            // Activité quotidienne
            report.append("# ACTIVITÉ QUOTIDIENNE\n");
            String sql = "SELECT DATE(date_transaction) as date, " +
                        "COUNT(*) as nb_transactions, " +
                        "SUM(ABS(montant)) as volume " +
                        "FROM transaction " +
                        "WHERE DATE(date_transaction) BETWEEN ? AND ? " +
                        "AND statut = 'CONFIRME' " +
                        "GROUP BY DATE(date_transaction) " +
                        "ORDER BY date";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            
            double totalVolume = 0;
            int totalTransactions = 0;
            
            while (rs.next()) {
                java.sql.Date date = rs.getDate("date");
                int count = rs.getInt("nb_transactions");
                double volume = rs.getDouble("volume");
                
                totalTransactions += count;
                totalVolume += volume;
                
                report.append(String.format("- %s: %,d transactions, %,.0f GNF\n",
                    date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    count,
                    volume));
            }
            
            report.append("\n# TOTAL PÉRIODE\n");
            report.append(String.format("Transactions: %,d\n", totalTransactions));
            report.append(String.format("Volume: %,.0f GNF\n", totalVolume));
            report.append(String.format("Moyenne quotidienne: %,.0f GNF\n", 
                totalVolume / Math.max(1, totalTransactions)));
        }
    }
}