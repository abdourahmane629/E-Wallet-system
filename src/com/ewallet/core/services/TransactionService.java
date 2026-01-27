package com.ewallet.core.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.dao.*;
import com.ewallet.core.models.*;
import com.ewallet.core.utils.SecurityUtil;
public class TransactionService {
    
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final PortefeuilleDAO portefeuilleDAO = new PortefeuilleDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private final NotificationService notificationService; 

    private final double commissionRate = 1.0; // 1% de commission
    //CONSTRUCTEUR
    public TransactionService() {
        try {
            this.notificationService = new NotificationService(DatabaseConfig.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur initialisation NotificationService", e);
        }
    }
    /**
     * Effectuer un dépôt pour un client (par un agent)
     */
    public boolean effectuerDepot(int agentId, String clientEmail, double montant, String pinClient) {
        System.out.println("=== DÉBUT DÉPÔT ===");
        System.out.println("Agent ID: " + agentId + " pour client: " + clientEmail + " montant: " + montant);
        
        try {
            // 1. Vérifier l'agent
            Utilisateur agent = utilisateurDAO.findById(agentId);
            if (agent == null || !agent.isAgent()) { // CORRECTION : utiliser isAgent()
                System.err.println("[SERVICE] Agent invalide ou non agent: " + agentId);
                System.err.println("[SERVICE] Agent role: " + (agent != null ? agent.getRoleName() : "null"));
                return false;
            }
            
            // 2. Trouver le client
            Utilisateur client = utilisateurDAO.findByEmail(clientEmail);
            if (client == null || !client.isClient()) { // CORRECTION : utiliser isClient()
                System.err.println("[SERVICE] Client non trouvé ou non client: " + clientEmail);
                System.err.println("[SERVICE] Client role: " + (client != null ? client.getRoleName() : "null"));
                return false;
            }
            
            // 3. Vérifier le PIN du client (si défini)
            if (client.getPinHash() != null && !client.getPinHash().isEmpty()) {
                if (pinClient == null || pinClient.isEmpty() || 
                    !SecurityUtil.verifyPin(pinClient, client.getPinHash())) {
                    System.err.println("[SERVICE] PIN incorrect pour: " + clientEmail);
                    return false;
                }
            }
            
            // 4. Vérifier le montant
            if (montant <= 0) {
                System.err.println("[SERVICE] Montant invalide: " + montant);
                return false;
            }
            
            // Limite de dépôt (10 millions)
            if (montant > 10000000) {
                System.err.println("[SERVICE] Montant trop élevé (max 10M): " + montant);
                return false;
            }
            
            // 5. Récupérer le portefeuille du client
            Portefeuille portefeuille = portefeuilleDAO.findByUtilisateurId(client.getUtilisateurId());
            if (portefeuille == null) {
                // Créer le portefeuille s'il n'existe pas
                System.out.println("[SERVICE] Création portefeuille pour client: " + clientEmail);
                if (!portefeuilleDAO.create(client.getUtilisateurId())) {
                    System.err.println("[SERVICE] Impossible de créer le portefeuille");
                    return false;
                }
                portefeuille = portefeuilleDAO.findByUtilisateurId(client.getUtilisateurId());
                if (portefeuille == null) {
                    System.err.println("[SERVICE] Portefeuille toujours null après création");
                    return false;
                }
            }
            
            System.out.println("[SERVICE] Portefeuille trouvé: ID=" + portefeuille.getId() + 
                             ", Solde avant: " + portefeuille.getSolde());
            
            // 6. Calculer la commission
            double commission = montant * (commissionRate / 100);
            System.out.println("[SERVICE] Commission calculée: " + commission + " GNF");
            
            // 7. Effectuer le dépôt
            boolean success = portefeuilleDAO.credit(portefeuille.getId(), montant);
            if (!success) {
                System.err.println("[SERVICE] Échec du crédit");
                return false;
            }
            
            // Vérifier le nouveau solde
            Portefeuille portefeuilleApres = portefeuilleDAO.findByUtilisateurId(client.getUtilisateurId());
            System.out.println("[SERVICE] Solde après crédit: " + 
                             (portefeuilleApres != null ? portefeuilleApres.getSolde() : "N/A"));
            
            // 8. Enregistrer la transaction
            String description = String.format("Dépôt par agent %s (%s)", 
                agent.getNomComplet(), agent.getEmail());
            
            Transaction transaction = new Transaction();
            transaction.setPortefeuilleId(portefeuille.getId());
            transaction.setMontant(montant);
            transaction.setType("DEPOT");
            transaction.setDescription(description);
            transaction.setAgentId(agentId);
            
            int transactionId = transactionDAO.createTransaction(transaction);
            if (transactionId <= 0) {
                System.err.println("[SERVICE] Échec de création de transaction");
                return false;
            }
            
            System.out.println("[SERVICE] Transaction créée: ID=" + transactionId);
            
            // 9. Enregistrer la commission (si > 0)
            if (commission > 0) {
                Commission commissionObj = new Commission();
                commissionObj.setAgentId(agentId);
                commissionObj.setTransactionId(transactionId);
                commissionObj.setMontantCommission(commission);
                commissionObj.setPourcentage(commissionRate);
                commissionObj.setStatut("PENDING");
                
                if (!commissionDAO.create(commissionObj)) {
                    System.err.println("[SERVICE] Commission non enregistrée");
                } else {
                    System.out.println("[SERVICE] Commission enregistrée: " + commission + " GNF");
                }
            } else {
                System.out.println("[SERVICE] Aucune commission (montant trop faible)");
            }
            
            System.out.println("=== DÉPÔT RÉUSSI ===");
            System.out.println("[SERVICE] Dépôt réussi: " + montant + " GNF pour " + clientEmail);

            //  Notification au client ET à l'agent
            if (client != null) {
                notificationService.notifyDeposit(client.getUtilisateurId(), montant);
                notificationService.notifyTransactionReceived(client.getUtilisateurId(), montant, 
                    agent.getNomComplet(), String.valueOf(transactionId));
            }
            
            // Notification à l'agent
            if (agent != null) {
                notificationService.notifyTransactionValidated(agentId, transactionId);
                if (commission > 0) {
                    notificationService.notifyCommissionEarned(agentId, commission, transactionId);
                }
            }

            return true;
            
        } catch (Exception e) {
            System.err.println("[SERVICE] Exception lors du dépôt: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Effectuer un retrait pour un client (par un agent)
     */
    public boolean effectuerRetrait(int agentId, String clientEmail, double montant, String pinClient) {
        System.out.println("=== DÉBUT RETRAIT ===");
        System.out.println("Agent ID: " + agentId + " pour client: " + clientEmail + " montant: " + montant);
        
        try {
            // 1. Vérifier l'agent
            Utilisateur agent = utilisateurDAO.findById(agentId);
            if (agent == null || !agent.isAgent()) { // CORRECTION : utiliser isAgent()
                System.err.println("[SERVICE] Agent invalide ou non agent: " + agentId);
                System.err.println("[SERVICE] Agent role: " + (agent != null ? agent.getRoleName() : "null"));
                return false;
            }
            
            // 2. Trouver le client
            Utilisateur client = utilisateurDAO.findByEmail(clientEmail);
            if (client == null || !client.isClient()) { // CORRECTION : utiliser isClient()
                System.err.println("[SERVICE] Client non trouvé ou non client: " + clientEmail);
                System.err.println("[SERVICE] Client role: " + (client != null ? client.getRoleName() : "null"));
                return false;
            }
            
            // 3. Vérifier le PIN du client (OBLIGATOIRE pour retrait)
            if (client.getPinHash() == null || client.getPinHash().isEmpty()) {
                System.err.println("[SERVICE] Client n'a pas de PIN configuré: " + clientEmail);
                return false;
            }
            
            if (pinClient == null || pinClient.isEmpty() || 
                !SecurityUtil.verifyPin(pinClient, client.getPinHash())) {
                System.err.println("[SERVICE] PIN incorrect pour: " + clientEmail);
                return false;
            }
            
            // 4. Vérifier le montant
            if (montant <= 0) {
                System.err.println("[SERVICE] Montant invalide: " + montant);
                return false;
            }
            
            // Limite de retrait (5 millions)
            if (montant > 5000000) {
                System.err.println("[SERVICE] Montant trop élevé (max 5M): " + montant);
                return false;
            }
            
            // 5. Récupérer le portefeuille du client
            Portefeuille portefeuille = portefeuilleDAO.findByUtilisateurId(client.getUtilisateurId());
            if (portefeuille == null) {
                System.err.println("[SERVICE] Portefeuille non trouvé pour: " + clientEmail);
                return false;
            }
            
            System.out.println("[SERVICE] Portefeuille trouvé: ID=" + portefeuille.getId() + 
                             ", Solde: " + portefeuille.getSolde());
            
            // 6. Vérifier le solde
            if (portefeuille.getSolde() < montant) {
                System.err.println("[SERVICE] Solde insuffisant. Disponible: " + portefeuille.getSolde() + 
                                 " GNF, Demandé: " + montant + " GNF");
                return false;
            }
            
            // 7. Calculer la commission
            double commission = montant * (commissionRate / 100);
            System.out.println("[SERVICE] Commission calculée: " + commission + " GNF");
            
            // 8. Effectuer le retrait
            boolean success = portefeuilleDAO.debit(portefeuille.getId(), montant);
            if (!success) {
                System.err.println("[SERVICE] Échec du débit");
                return false;
            }
            
            // Vérifier le nouveau solde
            Portefeuille portefeuilleApres = portefeuilleDAO.findByUtilisateurId(client.getUtilisateurId());
            System.out.println("[SERVICE] Solde après débit: " + 
                             (portefeuilleApres != null ? portefeuilleApres.getSolde() : "N/A"));
            
            // 9. Enregistrer la transaction
            String description = String.format("Retrait par agent %s (%s)", 
                agent.getNomComplet(), agent.getEmail());
            
            Transaction transaction = new Transaction();
            transaction.setPortefeuilleId(portefeuille.getId());
            transaction.setMontant(montant);
            transaction.setType("RETRAIT");
            transaction.setDescription(description);
            transaction.setAgentId(agentId);
            
            int transactionId = transactionDAO.createTransaction(transaction);
            if (transactionId <= 0) {
                System.err.println("[SERVICE] Échec de création de transaction");
                return false;
            }
            
            System.out.println("[SERVICE] Transaction créée: ID=" + transactionId);
            
            // 10. Enregistrer la commission
            Commission commissionObj = new Commission();
            commissionObj.setAgentId(agentId);
            commissionObj.setTransactionId(transactionId);
            commissionObj.setMontantCommission(commission);
            commissionObj.setPourcentage(commissionRate);
            commissionObj.setStatut("PENDING");
            
            if (!commissionDAO.create(commissionObj)) {
                System.err.println("[SERVICE] Commission non enregistrée");
            } else {
                System.out.println("[SERVICE] Commission enregistrée: " + commission + " GNF");
            }
            
            System.out.println("=== RETRAIT RÉUSSI ===");
            System.out.println("[SERVICE] Retrait réussi: " + montant + " GNF pour " + clientEmail);


            // Notification au client ET à l'agent
            if (client != null) {
                notificationService.notifyWithdrawal(client.getUtilisateurId(), montant);
                notificationService.notifyTransactionSent(client.getUtilisateurId(), montant, 
                    "Agent: " + agent.getNomComplet(), String.valueOf(transactionId));
            }
            
            // Notification à l'agent
            if (agent != null) {
                notificationService.notifyTransactionValidated(agentId, transactionId);
                if (commission > 0) {
                    notificationService.notifyCommissionEarned(agentId, commission, transactionId);
                }
            }
            return true;
            
        } catch (Exception e) {
            System.err.println("[SERVICE] Exception lors du retrait: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtenir le solde d'un client (pour vérification avant retrait)
     */
    public double getSoldeClient(String clientEmail) {
        System.out.println("[SERVICE] Vérification solde pour: " + clientEmail);
        
        try {
            Utilisateur client = utilisateurDAO.findByEmail(clientEmail);
            if (client == null) {
                System.err.println("[SERVICE] Client non trouvé: " + clientEmail);
                return -1;
            }
            
            if (!client.isClient()) {
                System.err.println("[SERVICE] Utilisateur n'est pas un client: " + clientEmail);
                return -1;
            }
            
            Portefeuille portefeuille = portefeuilleDAO.findByUtilisateurId(client.getUtilisateurId());
            double solde = portefeuille != null ? portefeuille.getSolde() : 0.0;
            
            System.out.println("[SERVICE] Solde vérifié: " + clientEmail + " = " + solde + " GNF");
            return solde;
            
        } catch (Exception e) {
            System.err.println("[SERVICE] Exception vérification solde: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }


    /**
 * Effectuer un paiement de service
 */
    public boolean effectuerPaiementService(int utilisateurId, double montant, 
                                       String serviceType, String reference, String pin) {
        System.out.println("=== DÉBUT PAIEMENT SERVICE ===");
        System.out.println("Utilisateur: " + utilisateurId + ", Service: " + serviceType + 
                        ", Référence: " + reference + ", Montant: " + montant);
        
        try {
            // 1. Vérifier l'utilisateur
            Utilisateur utilisateur = utilisateurDAO.findById(utilisateurId);
            if (utilisateur == null || !utilisateur.isClient()) {
                System.err.println("[SERVICE] Utilisateur invalide ou non client: " + utilisateurId);
                return false;
            }
            
            // 2. Vérifier le PIN (OBLIGATOIRE)
            if (utilisateur.getPinHash() == null || utilisateur.getPinHash().isEmpty()) {
                System.err.println("[SERVICE] Utilisateur n'a pas de PIN configuré");
                return false;
            }
            
            if (!SecurityUtil.verifyPin(pin, utilisateur.getPinHash())) {
                System.err.println("[SERVICE] PIN incorrect");
                return false;
            }
            
            // 3. Vérifier le montant
            if (montant <= 0) {
                System.err.println("[SERVICE] Montant invalide: " + montant);
                return false;
            }
            
            // Limite de paiement service (1 million)
            if (montant > 1000000) {
                System.err.println("[SERVICE] Montant trop élevé (max 1M): " + montant);
                return false;
            }
            
            // 4. Récupérer le portefeuille
            Portefeuille portefeuille = portefeuilleDAO.findByUtilisateurId(utilisateurId);
            if (portefeuille == null) {
                System.err.println("[SERVICE] Portefeuille non trouvé pour utilisateur: " + utilisateurId);
                return false;
            }
            
            System.out.println("[SERVICE] Portefeuille trouvé: ID=" + portefeuille.getId() + 
                            ", Solde: " + portefeuille.getSolde());
            
            // 5. Vérifier le solde
            if (portefeuille.getSolde() < montant) {
                System.err.println("[SERVICE] Solde insuffisant. Disponible: " + portefeuille.getSolde() + 
                                " GNF, Demandé: " + montant + " GNF");
                return false;
            }
            
            // 6. Effectuer le débit
            boolean debitSuccess = portefeuilleDAO.debit(portefeuille.getId(), montant);
            if (!debitSuccess) {
                System.err.println("[SERVICE] Échec du débit");
                return false;
            }
            
            // 7. Vérifier le nouveau solde
            Portefeuille portefeuilleApres = portefeuilleDAO.findByUtilisateurId(utilisateurId);
            System.out.println("[SERVICE] Solde après débit: " + 
                            (portefeuilleApres != null ? portefeuilleApres.getSolde() : "N/A"));
            
            // 8. Enregistrer la transaction
            String description = serviceType + " - Réf: " + reference + " - " + 
                            String.format("%.0f", montant) + " GNF";
            
            int transactionId = transactionDAO.createServicePayment(portefeuille.getId(), montant, 
                                                                serviceType, reference);
            if (transactionId <= 0) {
                System.err.println("[SERVICE] Échec de création de transaction");
                return false;
            }
            
            System.out.println("[SERVICE] Transaction créée: ID=" + transactionId);
            
            System.out.println("=== PAIEMENT SERVICE RÉUSSI ===");
            System.out.println("[SERVICE] Paiement réussi: " + montant + " GNF pour " + serviceType);


            // Notification au client
            if (utilisateur != null) {
                notificationService.notifyTransactionSent(utilisateurId, montant, 
                    serviceType, String.valueOf(transactionId));
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("[SERVICE] Exception lors du paiement service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
 * Récupérer les transactions effectuées par un agent
 */
    public List<Transaction> getTransactionsByAgent(int agentId, java.util.Date startDate, java.util.Date endDate) {
        System.out.println("[SERVICE] Récupération transactions pour agent: " + agentId);
        return transactionDAO.findByAgentId(agentId, startDate, endDate);
    }

    /**
     * Obtenir les statistiques d'un agent
     */
    public Map<String, Object> getAgentStatistics(int agentId, String period) {
        System.out.println("[SERVICE] Récupération statistiques pour agent: " + agentId + ", période: " + period);
        return transactionDAO.getAgentStatistics(agentId, period);
    }
}