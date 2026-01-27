package com.ewallet.core.services;

import java.sql.SQLException;
import java.time.LocalDateTime;

import com.ewallet.core.dao.UtilisateurDAO;
import com.ewallet.core.models.Utilisateur;
import com.ewallet.core.models.StatutUtilisateur;
import com.ewallet.core.utils.SecurityUtil;
import com.ewallet.core.DatabaseConfig;


public class AuthService {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final NotificationService notificationService;
    
    public AuthService() {
        try {
            this.notificationService = new NotificationService(DatabaseConfig.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur initialisation NotificationService", e);
        }
    }

    /**
     * Authentifier un utilisateur (connexion)
     */
    public Utilisateur login(String email, String motDePasse) {
        System.out.println("[AUTH] Tentative de connexion: " + email);
        
        Utilisateur user = utilisateurDAO.findByEmail(email);
        
        if (user == null) {
            System.err.println("[AUTH] Utilisateur non trouvé: " + email);
            return null;
        }

        System.out.println("[AUTH] Utilisateur trouvé: " + user.getNomComplet());
        System.out.println("[AUTH] Statut: " + user.getStatut());
        System.out.println("[AUTH] Compte verrouillé: " + user.isCompteVerrouille());

        // Vérifier si le compte est verrouillé
        if (user.isCompteVerrouille()) {
            System.err.println("[AUTH] Compte verrouillé: " + email);
            return null;
        }

        // ✅ CORRECTION LIGNES 36, 94, 131, 191, 249: Comparer correctement StatutUtilisateur
        // Vérifier si l'utilisateur est ACTIF
        if (user.getStatut() != StatutUtilisateur.ACTIF) {
            System.err.println("[AUTH] Utilisateur inactif ou suspendu: " + email + " (Statut: " + user.getStatut() + ")");
            return null;
        }

        // Vérifier le mot de passe
        System.out.println("[AUTH] Vérification du mot de passe...");
        boolean passwordOk = SecurityUtil.verifyPassword(motDePasse, user.getMotDePasseHash());
        
        if (passwordOk) {
            // Mot de passe correct
            System.out.println("[AUTH] ✓ Authentification réussie: " + email);
            
            // Réinitialiser les tentatives
            utilisateurDAO.updateTentatives(user.getId(), 0, false);
            
            // Mettre à jour la dernière connexion
            utilisateurDAO.updateLastLogin(user.getId());
            user.setLastLogin(LocalDateTime.now());

            // Notification de connexion réussie
            notificationService.notifyLoginSuccess(user.getId());
            return user;
        } else {
            // Mot de passe incorrect
            System.err.println("[AUTH] ✗ Mot de passe incorrect: " + email);
            
            // Incrémenter les tentatives
            int nouvelleTentative = user.getTentativesEchecs() + 1;
            boolean verrouille = nouvelleTentative >= 3;
            
            utilisateurDAO.updateTentatives(user.getId(), nouvelleTentative, verrouille);
            
            //  Notification de tentative échouée
            notificationService.notifyLoginFailed(user.getId(), "127.0.0.1");
            if (verrouille) {
                System.err.println("[AUTH] Compte verrouillé après 3 tentatives: " + email);
            }
            
            return null;
        }
    }

    /**
     * Vérifier un mot de passe sans authentifier complètement
     */
    public boolean verifyPassword(String email, String password) {
        System.out.println("[AUTH] Vérification mot de passe pour: " + email);
        
        Utilisateur user = utilisateurDAO.findByEmail(email);
        
        if (user == null) {
            System.err.println("[AUTH] Utilisateur non trouvé pour vérification: " + email);
            return false;
        }

        // Vérifier si le compte est verrouillé
        if (user.isCompteVerrouille()) {
            System.err.println("[AUTH] Compte verrouillé: " + email);
            return false;
        }

        // ✅ CORRECTION: Comparer correctement avec StatutUtilisateur enum
        // Vérifier si l'utilisateur est ACTIF
        if (user.getStatut() != StatutUtilisateur.ACTIF) {
            System.err.println("[AUTH] Utilisateur inactif ou suspendu: " + email);
            return false;
        }

        // Vérifier le mot de passe
        boolean passwordOk = SecurityUtil.verifyPassword(password, user.getMotDePasseHash());
        
        if (passwordOk) {
            System.out.println("[AUTH] ✓ Mot de passe correct pour: " + email);
            return true;
        } else {
            System.err.println("[AUTH] ✗ Mot de passe incorrect pour: " + email);
            return false;
        }
    }

    /**
     * Vérifier un PIN
     */
    public boolean verifyPin(String email, String pin) {
        System.out.println("[AUTH] Vérification PIN pour: " + email);
        
        Utilisateur user = utilisateurDAO.findByEmail(email);
        
        if (user == null) {
            System.err.println("[AUTH] Utilisateur non trouvé pour vérification PIN: " + email);
            return false;
        }

        // Vérifier si le compte est verrouillé
        if (user.isCompteVerrouille()) {
            System.err.println("[AUTH] Compte verrouillé: " + email);
            return false;
        }

        // ✅ CORRECTION: Comparer correctement avec StatutUtilisateur enum
        // Vérifier si l'utilisateur est ACTIF
        if (user.getStatut() != StatutUtilisateur.ACTIF) {
            System.err.println("[AUTH] Utilisateur inactif ou suspendu: " + email);
            return false;
        }

        // Vérifier si un PIN est configuré
        if (user.getPinHash() == null || user.getPinHash().isEmpty()) {
            System.err.println("[AUTH] Aucun PIN configuré pour: " + email);
            return false;
        }

        // Vérifier le PIN
        boolean pinOk = SecurityUtil.verifyPin(pin, user.getPinHash());
        
        if (pinOk) {
            System.out.println("[AUTH] ✓ PIN correct pour: " + email);
            return true;
        } else {
            System.err.println("[AUTH] ✗ PIN incorrect pour: " + email);
            return false;
        }
    }

    /**
     * Vérifier si un utilisateur a un PIN configuré
     */
    public boolean hasPinConfigured(String email) {
        Utilisateur user = utilisateurDAO.findByEmail(email);
        
        if (user == null) {
            System.err.println("[AUTH] Utilisateur non trouvé pour vérification PIN configuré: " + email);
            return false;
        }

        boolean hasPin = user.getPinHash() != null && !user.getPinHash().isEmpty();
        System.out.println("[AUTH] PIN configuré pour " + email + ": " + hasPin);
        
        return hasPin;
    }

    /**
     * Changer le mot de passe d'un utilisateur
     */
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        System.out.println("[AUTH] Changement mot de passe pour: " + email);
        
        Utilisateur user = utilisateurDAO.findByEmail(email);
        
        if (user == null) {
            System.err.println("[AUTH] Utilisateur non trouvé pour changement MDP: " + email);
            return false;
        }

        // Vérifier si le compte est verrouillé
        if (user.isCompteVerrouille()) {
            System.err.println("[AUTH] Compte verrouillé: " + email);
            return false;
        }

        // ✅ CORRECTION: Comparer correctement avec StatutUtilisateur enum
        // Vérifier si l'utilisateur est ACTIF
        if (user.getStatut() != StatutUtilisateur.ACTIF) {
            System.err.println("[AUTH] Utilisateur inactif ou suspendu: " + email);
            return false;
        }

        // Vérifier le mot de passe actuel
        boolean currentPasswordOk = SecurityUtil.verifyPassword(currentPassword, user.getMotDePasseHash());
        if (!currentPasswordOk) {
            System.err.println("[AUTH] Mot de passe actuel incorrect pour: " + email);
            return false;
        }

        // Vérifier que le nouveau mot de passe est différent
        boolean samePassword = SecurityUtil.verifyPassword(newPassword, user.getMotDePasseHash());
        if (samePassword) {
            System.err.println("[AUTH] Nouveau mot de passe identique à l'ancien pour: " + email);
            return false;
        }

        // Vérifier la longueur du nouveau mot de passe
        if (newPassword.length() < 6) {
            System.err.println("[AUTH] Nouveau mot de passe trop court (min 6 caractères) pour: " + email);
            return false;
        }

        // Changer le mot de passe
        String newPasswordHash = SecurityUtil.hashPassword(newPassword);
        boolean success = utilisateurDAO.changePassword(user.getId(), newPasswordHash);
        
        if (success) {
            System.out.println("[AUTH] ✓ Mot de passe changé avec succès pour: " + email);
            // Notification de changement de MDP
            notificationService.notifyPasswordChange(user.getId());
        } else {
            System.err.println("[AUTH] ✗ Échec changement mot de passe pour: " + email);
        }
        
        return success;
    }

    /**
     * Changer le PIN d'un utilisateur
     */
    public boolean changePin(String email, String password, String newPin) {
        System.out.println("[AUTH] Changement PIN pour: " + email);
        
        Utilisateur user = utilisateurDAO.findByEmail(email);
        
        if (user == null) {
            System.err.println("[AUTH] Utilisateur non trouvé pour changement PIN: " + email);
            return false;
        }

        // Vérifier si le compte est verrouillé
        if (user.isCompteVerrouille()) {
            System.err.println("[AUTH] Compte verrouillé: " + email);
            return false;
        }

        // ✅ CORRECTION: Comparer correctement avec StatutUtilisateur enum
        // Vérifier si l'utilisateur est ACTIF
        if (user.getStatut() != StatutUtilisateur.ACTIF) {
            System.err.println("[AUTH] Utilisateur inactif ou suspendu: " + email);
            return false;
        }

        // Vérifier le mot de passe
        boolean passwordOk = SecurityUtil.verifyPassword(password, user.getMotDePasseHash());
        if (!passwordOk) {
            System.err.println("[AUTH] Mot de passe incorrect pour changement PIN: " + email);
            return false;
        }

        // Vérifier que le PIN est valide (4 chiffres)
        if (!newPin.matches("\\d{4}")) {
            System.err.println("[AUTH] PIN invalide (doit être 4 chiffres): " + newPin);
            return false;
        }

        // Vérifier que le nouveau PIN est différent (si un PIN existe déjà)
        if (user.getPinHash() != null && !user.getPinHash().isEmpty()) {
            boolean samePin = SecurityUtil.verifyPin(newPin, user.getPinHash());
            if (samePin) {
                System.err.println("[AUTH] Nouveau PIN identique à l'ancien pour: " + email);
                return false;
            }
        }

        // Changer le PIN
        String newPinHash = SecurityUtil.hashPin(newPin);
        boolean success = utilisateurDAO.changePIN(user.getId(), newPinHash);
        
        if (success) {
            System.out.println("[AUTH] ✓ PIN changé avec succès pour: " + email);
            // Notification de changement de PIN
            notificationService.createNotification(user.getId(), 
                "PIN modifié", 
                "Votre code PIN a été modifié avec succès.", 
                NotificationService.TYPE_SECURITY);

        } else {
            System.err.println("[AUTH] ✗ Échec changement PIN pour: " + email);
        }
        
        return success;
    }

    /**
     * Débloquer un compte utilisateur
     */
    public boolean unlockAccount(String email) {
        System.out.println("[AUTH] Déblocage compte pour: " + email);
        
        Utilisateur user = utilisateurDAO.findByEmail(email);
        
        if (user == null) {
            System.err.println("[AUTH] Utilisateur non trouvé pour déblocage: " + email);
            return false;
        }

        // Débloquer le compte
        boolean success = utilisateurDAO.unlockAccount(user.getId());
        
        if (success) {
            System.out.println("[AUTH] ✓ Compte débloqué pour: " + email);
        } else {
            System.err.println("[AUTH] ✗ Échec déblocage pour: " + email);
        }
        
        return success;
    }

    /**
     * Déconnecter un utilisateur
     */
    public boolean logout(Utilisateur user) {
        if (user != null) {
            System.out.println("[AUTH] Déconnexion: " + user.getEmail());
            return true;
        }
        return false;
    }
}