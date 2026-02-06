package com.ewallet.core.services;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.dao.*;
import com.ewallet.core.models.*;
import com.ewallet.core.utils.SecurityUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


public class WalletService {
    
    private final PortefeuilleDAO portefeuilleDAO = new PortefeuilleDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final JournalAuditDAO auditDAO = new JournalAuditDAO();
    private final NotificationService notificationService;

    public WalletService() {
        try {
            this.notificationService = new NotificationService(DatabaseConfig.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur initialisation NotificationService", e);
        }
    }
    // Récupérer le portefeuille d'un utilisateur
    public Portefeuille getPortefeuille(int utilisateurId) {
        System.out.println("[WALLET SERVICE] Récupération du portefeuille pour utilisateur: " + utilisateurId);
        return portefeuilleDAO.findByUtilisateurId(utilisateurId);
    }
    
    // Obtenir le solde d'un utilisateur
    public double getSolde(int utilisateurId) {
        Portefeuille p = portefeuilleDAO.findByUtilisateurId(utilisateurId);
        if (p != null) {
            System.out.println("[WALLET SERVICE] Solde récupéré: " + p.getSolde() + " GNF");
            return p.getSolde();
        }
        return 0.0;
    }
    
    
    /**
     * TRANSFERT ENTRE CLIENTS ( montant > 0)
     */
    public boolean transfer(int utilisateurSourceId, String emailDestinataire, 
                           double montant, String pin, String description) {
        System.out.println("=== TRANSFERT ENTRE CLIENTS ===");
        System.out.println("[WALLET SERVICE] Transfert de " + montant + " GNF de " + 
                         utilisateurSourceId + " vers " + emailDestinataire);
        
        Connection conn = null;
        try {
            conn = com.ewallet.core.DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Démarrer transaction
            
            // 1. Vérifier l'expéditeur
            Utilisateur source = UtilisateurDAO.findById(utilisateurSourceId);
            if (source == null || !source.isClient()) {
                System.err.println("[WALLET SERVICE] Expéditeur invalide ou non client");
                conn.rollback();
                return false;
            }
            
            // 2. Vérifier le destinataire
            Utilisateur destinataire = utilisateurDAO.findByEmail(emailDestinataire);
            if (destinataire == null || !destinataire.isClient()) {
                System.err.println("[WALLET SERVICE] Destinataire non trouvé ou non client: " + emailDestinataire);
                conn.rollback();
                return false;
            }
            
            // 3. Vérifier qu'on ne se transfert pas à soi-même
            if (source.getEmail().equalsIgnoreCase(emailDestinataire)) {
                System.err.println("[WALLET SERVICE] Impossible de se transférer à soi-même");
                conn.rollback();
                return false;
            }
            
            // 4. Vérifier le PIN
            if (source.getPinHash() == null || source.getPinHash().isEmpty()) {
                System.err.println("[WALLET SERVICE] Expéditeur n'a pas de PIN configuré");
                conn.rollback();
                return false;
            }
            
            if (!SecurityUtil.verifyPin(pin, source.getPinHash())) {
                System.err.println("[WALLET SERVICE] PIN incorrect");
                auditDAO.logAction(source.getUtilisateurId(), "TRANSFER_FAILED_PIN", 
                                 "PORTEFEUILLE", null, null, 
                                 "PIN incorrect pour transfert", "127.0.0.1");
                conn.rollback();
                return false;
            }
            
            // 5. Vérifier le montant
            if (montant <= 0) {
                System.err.println("[WALLET SERVICE] Montant invalide: " + montant);
                conn.rollback();
                return false;
            }
            
            // 6. Vérifier les limites de transfert
            Portefeuille portefeuilleSource = portefeuilleDAO.findByUtilisateurId(utilisateurSourceId);
            if (portefeuilleSource == null) {
                System.err.println("[WALLET SERVICE] Portefeuille source non trouvé");
                conn.rollback();
                return false;
            }
            
            if (montant > portefeuilleSource.getLimiteTransfert()) {
                System.err.println("[WALLET SERVICE] Montant dépasse la limite de transfert: " + 
                                 portefeuilleSource.getLimiteTransfert());
                conn.rollback();
                return false;
            }
            
            // 7. Vérifier le solde
            if (portefeuilleSource.getSolde() < montant) {
                System.err.println("[WALLET SERVICE] Solde insuffisant: " + 
                                 portefeuilleSource.getSolde() + " < " + montant);
                auditDAO.logAction(source.getUtilisateurId(), "TRANSFER_FAILED_INSUFFICIENT", 
                                 "PORTEFEUILLE", portefeuilleSource.getId(), 
                                 String.valueOf(portefeuilleSource.getSolde()), 
                                 "Tentative transfert: " + montant, "127.0.0.1");
                conn.rollback();
                return false;
            }
            
            // 8. Récupérer le portefeuille destinataire
            Portefeuille portefeuilleDestinataire = portefeuilleDAO.findByUtilisateurId(destinataire.getUtilisateurId());
            if (portefeuilleDestinataire == null) {
                System.err.println("[WALLET SERVICE] Portefeuille destinataire non trouvé");
                conn.rollback();
                return false;
            }
            
            // 9. Calculer la commission (1%)
            double commission = montant * 0.01;
            double montantNet = montant - commission;
            
            System.out.println("[WALLET SERVICE] Commission: " + commission + " GNF, Montant net: " + montantNet + " GNF");
            
            // 10. Effectuer le transfert atomique
            if (!portefeuilleDAO.transfer(portefeuilleSource.getId(), portefeuilleDestinataire.getId(), montantNet)) {
                System.err.println("[WALLET SERVICE] Échec du transfert atomique");
                conn.rollback();
                return false;
            }
            
            // 11. Récupérer le type de transaction "TRANSFERT" (type_id = 3)
            int typeTransfertId = 3; // TRANSFERT dans type_transaction
            
            // 12. Enregistrer la transaction source (débit) - utiliser valeur absolue
            String numeroTransaction = generateTransactionNumber();
            
            // Transaction source (débit) - ENREGISTRER LA VALEUR ABSOLUE
            String insertSourceSQL = "INSERT INTO transaction " +
                    "(numero_transaction, type_id, montant, portefeuille_source_id, " +
                    "portefeuille_destination_id, statut, description) " +
                    "VALUES (?, ?, ?, ?, ?, 'CONFIRME', ?)";
            
            PreparedStatement psSource = conn.prepareStatement(insertSourceSQL, Statement.RETURN_GENERATED_KEYS);
            psSource.setString(1, numeroTransaction);
            psSource.setInt(2, typeTransfertId);
            psSource.setDouble(3, montant); // VALEUR ABSOLUE (positive) pour éviter la contrainte
            psSource.setInt(4, portefeuilleSource.getId());
            psSource.setInt(5, portefeuilleDestinataire.getId());
            psSource.setString(6, "Transfert vers " + destinataire.getEmail() + " - " + description);
            psSource.executeUpdate();
            
            ResultSet rsSource = psSource.getGeneratedKeys();
            int transSourceId = -1;
            if (rsSource.next()) {
                transSourceId = rsSource.getInt(1);
            }
            
            // 13. Enregistrer la transaction destinataire (crédit)
            String numeroTransactionDest = generateTransactionNumber();
            
            String insertDestSQL = "INSERT INTO transaction " +
                    "(numero_transaction, type_id, montant, portefeuille_source_id, " +
                    "portefeuille_destination_id, statut, description) " +
                    "VALUES (?, ?, ?, ?, ?, 'CONFIRME', ?)";
            
            PreparedStatement psDest = conn.prepareStatement(insertDestSQL, Statement.RETURN_GENERATED_KEYS);
            psDest.setString(1, numeroTransactionDest);
            psDest.setInt(2, typeTransfertId);
            psDest.setDouble(3, montantNet); // Positif pour crédit (après commission)
            psDest.setInt(4, portefeuilleSource.getId());
            psDest.setInt(5, portefeuilleDestinataire.getId());
            psDest.setString(6, "Transfert reçu de " + source.getEmail() + " - " + description);
            psDest.executeUpdate();
            
            ResultSet rsDest = psDest.getGeneratedKeys();
            int transDestId = -1;
            if (rsDest.next()) {
                transDestId = rsDest.getInt(1);
            }
            
            // 14. Mettre à jour les transactions pour indiquer le signe (nous gérerons le signe côté application)
            // Pour la transaction source, nous pourrions ajouter un champ supplémentaire ou gérer via la description
            
            // 15. Enregistrer l'audit
            auditDAO.logAction(source.getUtilisateurId(), "TRANSFER_SENT", 
                             "TRANSACTION", transSourceId, 
                             String.valueOf(portefeuilleSource.getSolde() + montant), 
                             String.valueOf(portefeuilleSource.getSolde()), 
                             "127.0.0.1");
            
            auditDAO.logAction(destinataire.getUtilisateurId(), "TRANSFER_RECEIVED", 
                             "TRANSACTION", transDestId, 
                             String.valueOf(portefeuilleDestinataire.getSolde() - montantNet), 
                             String.valueOf(portefeuilleDestinataire.getSolde()), 
                             "127.0.0.1");
            
             // 16. Envoyer les notifications aux utilisateurs
            if (source != null && destinataire != null) {
                // Notification à l'expéditeur
                notificationService.notifyTransactionSent(source.getUtilisateurId(), montant, 
                    destinataire.getEmail(), String.valueOf(transSourceId));
                
                // Notification au destinataire
                notificationService.notifyTransactionReceived(destinataire.getUtilisateurId(), montantNet, 
                    source.getEmail(), String.valueOf(transDestId));
                
                // Notification d'approche de limite si nécessaire
                double retraitQuotidien = getRetraitQuotidien(portefeuilleSource.getId());
                if (retraitQuotidien + montant > portefeuilleSource.getLimiteRetraitQuotidien() * 0.9) {
                    notificationService.notifyLimitWarning(source.getUtilisateurId(), 
                        "retrait quotidien", retraitQuotidien, portefeuilleSource.getLimiteRetraitQuotidien());
                }
            }
            
            // 17. Valider la transaction
            conn.commit();
            
            System.out.println("=== TRANSFERT RÉUSSI ===");
            System.out.println("[WALLET SERVICE] Transfert de " + montant + " GNF (" + montantNet + " net) " +
                             "de " + source.getEmail() + " à " + destinataire.getEmail());
            System.out.println("[WALLET SERVICE] Commission prélevée: " + commission + " GNF");
            System.out.println("[WALLET SERVICE] Transactions ID: " + transSourceId + ", " + transDestId);
            
            return true;
            
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.err.println("[WALLET SERVICE] Exception SQL lors du transfert: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.err.println("[WALLET SERVICE] Exception lors du transfert: " + e.getMessage());
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
     * Vérifier si un transfert est possible
     */
    public Map<String, Object> checkTransferPossibility(int utilisateurId, double montant) {
        Map<String, Object> result = new HashMap<>();
        
        double solde = getSolde(utilisateurId);
        Portefeuille portefeuille = portefeuilleDAO.findByUtilisateurId(utilisateurId);
        
        boolean possible = true;
        List<String> erreurs = new ArrayList<>();
        
        if (montant <= 0) {
            possible = false;
            erreurs.add("Montant invalide");
        }
        
        if (solde < montant) {
            possible = false;
            erreurs.add("Solde insuffisant");
        }
        
        if (portefeuille != null && montant > portefeuille.getLimiteTransfert()) {
            possible = false;
            erreurs.add("Dépasse la limite de transfert (" + portefeuille.getLimiteTransfert() + " GNF)");
        }
        
        double retraitQuotidien = 0;
        if (portefeuille != null) {
            retraitQuotidien = getRetraitQuotidien(portefeuille.getId());
            if (retraitQuotidien + montant > portefeuille.getLimiteRetraitQuotidien()) {
                possible = false;
                erreurs.add("Dépasse la limite de retrait quotidien");
            }
        }
        
        result.put("possible", possible);
        result.put("solde", solde);
        result.put("limite_transfert", portefeuille != null ? portefeuille.getLimiteTransfert() : 0);
        result.put("limite_retrait_quotidien", portefeuille != null ? portefeuille.getLimiteRetraitQuotidien() : 0);
        result.put("retrait_quotidien_actuel", retraitQuotidien);
        result.put("erreurs", erreurs);
        
        return result;
    }
    
    /**
     * Obtenir l'historique des transactions
     */
    public List<Transaction> getHistorique(int utilisateurId) {
        Portefeuille p = portefeuilleDAO.findByUtilisateurId(utilisateurId);
        if (p != null) {
            return transactionDAO.findByPortefeuilleId(p.getId());
        }
        return new ArrayList<>();
    }
    
    /**
     * Obtenir les X dernières transactions
     */
    public List<Transaction> getLastTransactions(int utilisateurId, int limit) {
        Portefeuille p = portefeuilleDAO.findByUtilisateurId(utilisateurId);
        if (p != null) {
            return transactionDAO.findLatestByPortefeuilleId(p.getId(), limit);
        }
        return new ArrayList<>();
    }
    
    /**
     * Mettre à jour les limites du portefeuille
     */
    public boolean updateLimites(int utilisateurId, double limiteRetrait, double limiteTransfert) {
        Portefeuille p = portefeuilleDAO.findByUtilisateurId(utilisateurId);
        if (p != null) {
            boolean success = portefeuilleDAO.updateLimites(p.getId(), limiteRetrait, limiteTransfert);
            if (success) {
                auditDAO.logAction(utilisateurId, "UPDATE_LIMITS", "PORTEFEUILLE", p.getId(),
                                 "Retrait: " + p.getLimiteRetraitQuotidien() + ", Transfert: " + p.getLimiteTransfert(),
                                 "Retrait: " + limiteRetrait + ", Transfert: " + limiteTransfert,
                                 "127.0.0.1");
            }
            return success;
        }
        return false;
    }
    
    /**
     * Obtenir le solde disponible pour transfert
     */
    public double getSoldeDisponibleTransfert(int utilisateurId) {
        double solde = getSolde(utilisateurId);
        // Garder au moins 1000 GNF sur le compte
        double minimum = 1000.0;
        return Math.max(0, solde - minimum);
    }
    
    /**
     * Vérifier l'état du portefeuille
     */
    public String checkPortefeuilleStatus(int utilisateurId) {
        Portefeuille p = portefeuilleDAO.findByUtilisateurId(utilisateurId);
        if (p == null) {
            return "NON_TROUVE";
        }
        
        String statut = p.getStatut();
        if (!"ACTIF".equalsIgnoreCase(statut)) {
            return statut;
        }
        
        // Vérifier si le solde est trop bas
        if (p.getSolde() < 1000) {
            return "SOLDE_FAIBLE";
        }
        
        // Vérifier les limites
        double retraitQuotidien = getRetraitQuotidien(p.getId());
        if (retraitQuotidien >= p.getLimiteRetraitQuotidien() * 0.9) {
            return "LIMITE_RETRAIT_ATTEINT";
        }
        
        return "ACTIF";
    }
    
    // ==================== MÉTHODES PRIVÉES UTILITAIRES ====================
    
    private String generateTransactionNumber() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    private double getRetraitQuotidien(int portefeuilleId) {
        // Calculer le total des retraits aujourd'hui
        try (Connection conn = com.ewallet.core.DatabaseConfig.getConnection()) {
            String sql = "SELECT COALESCE(SUM(ABS(montant)), 0) as total_retrait " +
                        "FROM transaction t " +
                        "JOIN type_transaction tt ON t.type_id = tt.type_id " +
                        "WHERE t.portefeuille_source_id = ? " +
                        "AND DATE(t.date_transaction) = CURDATE() " +
                        "AND tt.nom_type IN ('RETRAIT', 'DEBIT') " +
                        "AND t.statut = 'CONFIRME'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, portefeuilleId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total_retrait");
            }
        } catch (SQLException e) {
            System.err.println("[WALLET SERVICE] Impossible de calculer le retrait quotidien");
        }
        return 0.0;
    }
    
    // ==================== MÉTHODES DE RAPPORT ====================
    
    public Map<String, Object> getStatistiquesUtilisateur(int utilisateurId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Portefeuille p = portefeuilleDAO.findByUtilisateurId(utilisateurId);
            if (p == null) {
                return stats;
            }
            
            // Solde
            stats.put("solde", p.getSolde());
            stats.put("devise", p.getDevise());
            stats.put("statut_portefeuille", p.getStatut());
            
            // Transactions du mois
            LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            
            // Note: Nous devons récupérer toutes les transactions puis filtrer
            List<Transaction> allTransactions = getHistorique(utilisateurId);
            List<Transaction> transactionsMois = new ArrayList<>();
            
            for (Transaction t : allTransactions) {
                if (t.getDateTransaction() != null && 
                    !t.getDateTransaction().isBefore(debutMois)) {
                    transactionsMois.add(t);
                }
            }
            
            double totalDepots = 0;
            double totalRetraits = 0;
            int nbTransactions = transactionsMois.size();
            
            for (Transaction t : transactionsMois) {
                if (t.getMontant() > 0) {
                    totalDepots += t.getMontant();
                } else {
                    totalRetraits += Math.abs(t.getMontant());
                }
            }
            
            stats.put("total_depots_mois", totalDepots);
            stats.put("total_retraits_mois", totalRetraits);
            stats.put("nb_transactions_mois", nbTransactions);
            stats.put("solde_moyen", totalDepots - totalRetraits);
            
            // Limites
            stats.put("limite_retrait_quotidien", p.getLimiteRetraitQuotidien());
            stats.put("limite_transfert", p.getLimiteTransfert());
            stats.put("retrait_quotidien_utilise", getRetraitQuotidien(p.getId()));
            
        } catch (Exception e) {
            System.err.println("[WALLET SERVICE] Erreur calcul statistiques: " + e.getMessage());
        }
        
        return stats;
    }
}