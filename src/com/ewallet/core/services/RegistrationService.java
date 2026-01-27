package com.ewallet.core.services;

import com.ewallet.core.dao.UtilisateurDAO;

import java.sql.SQLException;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.dao.PortefeuilleDAO;
import com.ewallet.core.dao.RoleDAO;
import com.ewallet.core.utils.SecurityUtil;
import com.ewallet.core.utils.ValidationUtil;


public class RegistrationService {
    
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final PortefeuilleDAO portefeuilleDAO = new PortefeuilleDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final NotificationService notificationService;
    
    public RegistrationService() {
        try {
            this.notificationService = new NotificationService(DatabaseConfig.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur initialisation NotificationService", e);
        }
    }

    public boolean register(String nom, String prenom, String email, 
                           String telephone, String adresse, String motDePasse) {
        
        try {
            System.out.println("[REGISTRATION] Tentative d'inscription pour: " + email);
            
            // Validation
            if (!ValidationUtil.isValidName(nom) || !ValidationUtil.isValidName(prenom)) {
                System.err.println("[REGISTRATION] Nom ou prénom invalide");
                return false;
            }
            
            if (!ValidationUtil.isValidEmail(email)) {
                System.err.println("[REGISTRATION] Email invalide");
                return false;
            }
            
            if (!ValidationUtil.isValidPassword(motDePasse)) {
                System.err.println("[REGISTRATION] Mot de passe invalide");
                return false;
            }
            
            // Vérifier si l'email existe déjà
            if (utilisateurDAO.findByEmail(email) != null) {
                System.err.println("[REGISTRATION] Email déjà utilisé: " + email);
                return false;
            }
            
            // Vérifier si le téléphone existe déjà (si fourni)
            if (telephone != null && !telephone.isEmpty()) {
                if (utilisateurDAO.findByTelephone(telephone) != null) {
                    System.err.println("[REGISTRATION] Téléphone déjà utilisé: " + telephone);
                    return false;
                }
            }
            
            // Hasher le mot de passe
            String passwordHash = SecurityUtil.hashPassword(motDePasse);
            
            // Récupérer l'ID du rôle CLIENT (role_id = 3)
            int roleId = roleDAO.getRoleIdByName("CLIENT");
            if (roleId == -1) {
                // Si le rôle n'existe pas, créer par défaut
                roleId = 3; // ID du client dans ta table roles
            }
            
            // Créer l'utilisateur
            int userId = utilisateurDAO.create(nom, prenom, email, telephone, adresse, passwordHash, roleId);
            
            if (userId > 0) {
                System.out.println("[REGISTRATION] Utilisateur créé avec ID: " + userId);
                
                // CRÉER AUTOMATIQUEMENT UN PORTEFEUILLE POUR LE CLIENT
                boolean portefeuilleCree = portefeuilleDAO.create(userId);
                
                if (portefeuilleCree) {
                    System.out.println("[REGISTRATION] Portefeuille créé pour l'utilisateur ID: " + userId);
                    // Notification de bienvenue au client
                    notificationService.createNotification(userId,
                        "Bienvenue sur E-Wallet!",
                        "Votre compte a été créé avec succès. Votre portefeuille est maintenant actif.",
                        NotificationService.TYPE_SYSTEM);
                    return true;
                } else {
                    System.err.println("[REGISTRATION] Utilisateur créé mais portefeuille non créé");
                    return false;
                }
            } else {
                System.err.println("[REGISTRATION] Échec de création de l'utilisateur");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("[REGISTRATION] Exception lors de l'inscription");
            e.printStackTrace();
            return false;
        }
    }
}