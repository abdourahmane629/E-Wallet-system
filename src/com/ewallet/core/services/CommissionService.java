package com.ewallet.core.services;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.dao.CommissionDAO;
import com.ewallet.core.dao.JournalAuditDAO;
import com.ewallet.core.dao.UtilisateurDAO;
import com.ewallet.core.models.Commission;
import com.ewallet.core.models.Utilisateur;
import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CommissionService {
    
    private final CommissionDAO commissionDAO;
    private final JournalAuditDAO journalAuditDAO;
    private final UtilisateurDAO utilisateurDAO;
    private final NotificationService notificationService;

    public CommissionService() {
        this.commissionDAO = new CommissionDAO();
        this.journalAuditDAO = new JournalAuditDAO();
        this.utilisateurDAO = new UtilisateurDAO();

        try {
            this.notificationService = new NotificationService(DatabaseConfig.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur initialisation NotificationService", e);
        }
    }
    
    /**
     * Payer une commission (Admin seulement)
     */
    public boolean payerCommission(int commissionId, int adminId, String referencePaiement) {
        System.out.println("[COMMISSION] Paiement commission ID: " + commissionId + " par admin: " + adminId);
        
        try {
            // 1. Vérifier que la commission existe et est en PENDING
            Commission commission = getCommissionById(commissionId);
            if (commission == null) {
                System.err.println("[COMMISSION] Commission non trouvée: " + commissionId);
                return false;
            }
            
            if (!"PENDING".equals(commission.getStatut())) {
                System.err.println("[COMMISSION] Commission déjà payée ou annulée: " + commissionId);
                return false;
            }
            
            // 2. Vérifier que l'admin a les droits
            Utilisateur admin = utilisateurDAO.findById(adminId);
            if (admin == null || !admin.isAdmin()) {
                System.err.println("[COMMISSION] Utilisateur non admin: " + adminId);
                return false;
            }
            
            // 3. Changer le statut
            boolean success = commissionDAO.updateStatus(commissionId, "PAID");
            
            if (success) {
                // 4. Log dans journal d'audit
                String description = String.format("Commission payée: ID=%d, Montant=%,.0f GNF, Réf=%s", 
                    commissionId, commission.getMontantCommission(), referencePaiement);
                
                journalAuditDAO.logSimpleAction(adminId, "PAYER_COMMISSION", description);

                // Notification à l'agent
                notificationService.createNotification(commission.getAgentId(),
                    "Commission payée",
                    String.format("Votre commission de %,.0f GNF a été payée (Réf: %s)", 
                        commission.getMontantCommission(), referencePaiement),
                    NotificationService.TYPE_COMMISSION);
                
                System.out.println("[COMMISSION] Commission payée: " + commissionId);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("[COMMISSION] Erreur paiement commission: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Payer plusieurs commissions en lot
     */
    public boolean payerCommissionsEnLot(List<Integer> commissionIds, int adminId, String referencePaiement) {
        System.out.println("[COMMISSION] Paiement lot de " + commissionIds.size() + " commissions");
        
        boolean allSuccess = true;
        int succesCount = 0;
        
        for (int commissionId : commissionIds) {
            if (payerCommission(commissionId, adminId, referencePaiement + "-" + succesCount)) {
                succesCount++;
            } else {
                allSuccess = false;
            }
        }
        
        System.out.println("[COMMISSION] " + succesCount + "/" + commissionIds.size() + " commissions payées");
        return allSuccess;
    }
    
    /**
     * Annuler une commission (Admin seulement)
     */
    public boolean annulerCommission(int commissionId, int adminId, String raison) {
        System.out.println("[COMMISSION] Annulation commission ID: " + commissionId);
        
        try {
            Commission commission = getCommissionById(commissionId);
            if (commission == null) return false;
            
            // Vérifier que l'admin a les droits
            Utilisateur admin = utilisateurDAO.findById(adminId);
            if (admin == null || !admin.isAdmin()) {
                return false;
            }
            
            // On peut annuler seulement si PENDING
            if (!"PENDING".equals(commission.getStatut())) {
                System.err.println("[COMMISSION] Commission non annulable (statut: " + commission.getStatut() + ")");
                return false;
            }
            
            boolean success = commissionDAO.updateStatus(commissionId, "CANCELLED");
            
            if (success) {
                String description = String.format("Commission annulée: ID=%d, Montant=%,.0f GNF, Raison=%s", 
                    commissionId, commission.getMontantCommission(), raison);
                
                journalAuditDAO.logSimpleAction(adminId, "ANNULER_COMMISSION", description);

                // Notification à l'agent
                notificationService.createNotification(commission.getAgentId(),
                    "Commission annulée",
                    String.format("Votre commission de %,.0f GNF a été annulée. Raison: %s", 
                        commission.getMontantCommission(), raison),
                    NotificationService.TYPE_COMMISSION);
                return true;

            }
            return false;
            
        } catch (Exception e) {
            System.err.println("[COMMISSION] Erreur annulation commission: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtenir une commission par ID
     */
    public Commission getCommissionById(int commissionId) {
        try {
            // Implémenter une méthode findById dans CommissionDAO
            return commissionDAO.findById(commissionId);
        } catch (Exception e) {
            System.err.println("[COMMISSION] Erreur récupération commission: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtenir toutes les commissions (pour admin)
     */
    public List<Commission> getAllCommissions() {
        return commissionDAO.findAll();
    }
    
    /**
     * Obtenir les commissions par statut
     */
    public List<Commission> getCommissionsByStatus(String statut) {
        List<Commission> allCommissions = getAllCommissions();
        List<Commission> filtered = new ArrayList<>();
        
        for (Commission c : allCommissions) {
            if (statut.equals("TOUS") || statut.equals(c.getStatut())) {
                filtered.add(c);
            }
        }
        
        return filtered;
    }
    
    /**
     * Obtenir les commissions par agent
     */
    public List<Commission> getCommissionsByAgent(int agentId) {
        return commissionDAO.findByAgentId(agentId);
    }
    
    /**
     * Obtenir les statistiques des commissions
     */
    public Map<String, Object> getCommissionStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Commission> allCommissions = getAllCommissions();
        
        double total = 0;
        double totalPending = 0;
        double totalPaid = 0;
        double totalCancelled = 0;
        int countPending = 0;
        int countPaid = 0;
        int countCancelled = 0;
        
        for (Commission c : allCommissions) {
            total += c.getMontantCommission();
            
            switch (c.getStatut()) {
                case "PENDING":
                    totalPending += c.getMontantCommission();
                    countPending++;
                    break;
                case "PAID":
                    totalPaid += c.getMontantCommission();
                    countPaid++;
                    break;
                case "CANCELLED":
                    totalCancelled += c.getMontantCommission();
                    countCancelled++;
                    break;
            }
        }
        
        stats.put("total", total);
        stats.put("total_pending", totalPending);
        stats.put("total_paid", totalPaid);
        stats.put("total_cancelled", totalCancelled);
        stats.put("count_total", allCommissions.size());
        stats.put("count_pending", countPending);
        stats.put("count_paid", countPaid);
        stats.put("count_cancelled", countCancelled);
        
        return stats;
    }
    
    /**
     * Obtenir les commissions par agent avec statut
     */
    public Map<String, Object> getCommissionsByAgentWithStats(int agentId) {
        Map<String, Object> result = new HashMap<>();
        List<Commission> commissions = getCommissionsByAgent(agentId);
        
        double total = 0;
        double totalPending = 0;
        double totalPaid = 0;
        double totalCancelled = 0;
        
        for (Commission c : commissions) {
            total += c.getMontantCommission();
            
            switch (c.getStatut()) {
                case "PENDING":
                    totalPending += c.getMontantCommission();
                    break;
                case "PAID":
                    totalPaid += c.getMontantCommission();
                    break;
                case "CANCELLED":
                    totalCancelled += c.getMontantCommission();
                    break;
            }
        }
        
        result.put("commissions", commissions);
        result.put("total", total);
        result.put("total_pending", totalPending);
        result.put("total_paid", totalPaid);
        result.put("total_cancelled", totalCancelled);
        
        return result;
    }
}