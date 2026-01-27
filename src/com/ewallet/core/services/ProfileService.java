package com.ewallet.core.services;

import com.ewallet.core.dao.UtilisateurDAO;
import com.ewallet.core.dao.JournalAuditDAO;
import com.ewallet.core.dao.RoleDAO;
import com.ewallet.core.models.Utilisateur;
import com.ewallet.core.models.StatutUtilisateur;
import com.ewallet.core.DatabaseConfig;
import java.sql.*;
import java.util.*;

public class ProfileService {
    
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final JournalAuditDAO journalAuditDAO = new JournalAuditDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    
    /**
     * Mettre à jour les informations de profil
     */
    public Map<String, Object> updateProfile(Utilisateur utilisateur, String nouveauNom, 
                                           String nouveauPrenom, String nouveauTelephone, 
                                           String nouvelleAdresse) {
        System.out.println("[PROFILE] Mise à jour profil pour: " + utilisateur.getEmail());
        
        Map<String, Object> result = new HashMap<>();
        boolean modifications = false;
        Map<String, String> changes = new HashMap<>();
        
        try {
            // Vérifier et mettre à jour le nom
            if (nouveauNom != null && !nouveauNom.trim().isEmpty() && 
                !nouveauNom.equals(utilisateur.getNom())) {
                changes.put("nom", utilisateur.getNom() + " → " + nouveauNom);
                utilisateur.setNom(nouveauNom);
                modifications = true;
            }
            
            // Vérifier et mettre à jour le prénom
            if (nouveauPrenom != null && !nouveauPrenom.trim().isEmpty() && 
                !nouveauPrenom.equals(utilisateur.getPrenom())) {
                changes.put("prenom", utilisateur.getPrenom() + " → " + nouveauPrenom);
                utilisateur.setPrenom(nouveauPrenom);
                modifications = true;
            }
            
            // Vérifier et mettre à jour le téléphone
            if (nouveauTelephone != null && !nouveauTelephone.trim().isEmpty()) {
                if (utilisateur.getTelephone() == null || 
                    !nouveauTelephone.equals(utilisateur.getTelephone())) {
                    changes.put("telephone", 
                        (utilisateur.getTelephone() != null ? utilisateur.getTelephone() : "Non défini") + 
                        " → " + nouveauTelephone);
                    utilisateur.setTelephone(nouveauTelephone);
                    modifications = true;
                }
            }
            
            // Vérifier et mettre à jour l'adresse
            if (nouvelleAdresse != null && !nouvelleAdresse.trim().isEmpty()) {
                if (utilisateur.getAdresse() == null || 
                    !nouvelleAdresse.equals(utilisateur.getAdresse())) {
                    changes.put("adresse", 
                        (utilisateur.getAdresse() != null ? utilisateur.getAdresse() : "Non définie") + 
                        " → " + nouvelleAdresse);
                    utilisateur.setAdresse(nouvelleAdresse);
                    modifications = true;
                }
            }
            
            if (modifications) {
                // Sauvegarder dans la base de données
                boolean saved = utilisateurDAO.update(utilisateur);
                
                if (saved) {
                    System.out.println("[PROFILE] ✓ Profil mis à jour pour: " + utilisateur.getEmail());
                    result.put("success", true);
                    result.put("message", "Profil mis à jour avec succès");
                    result.put("changes", changes);
                    result.put("utilisateur", utilisateur);
                } else {
                    System.err.println("[PROFILE] ✗ Échec sauvegarde profil: " + utilisateur.getEmail());
                    result.put("success", false);
                    result.put("message", "Erreur lors de la sauvegarde en base de données");
                }
            } else {
                result.put("success", false);
                result.put("message", "Aucune modification détectée");
            }
            
        } catch (Exception e) {
            System.err.println("[PROFILE] Exception mise à jour profil: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Erreur système: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Changer le mot de passe avec validation
     */
    public Map<String, Object> changePassword(String email, String currentPassword, 
                                            String newPassword, String confirmPassword) {
        System.out.println("[PROFILE] Changement mot de passe pour: " + email);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validation des entrées
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Le mot de passe actuel est requis");
                return result;
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Le nouveau mot de passe est requis");
                return result;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                result.put("success", false);
                result.put("message", "Les mots de passe ne correspondent pas");
                return result;
            }
            
            // Vérifier la force du mot de passe
            if (newPassword.length() < 8) {
                result.put("success", false);
                result.put("message", "Le mot de passe doit contenir au moins 8 caractères");
                return result;
            }
            
            if (!newPassword.matches(".*[A-Z].*")) {
                result.put("success", false);
                result.put("message", "Le mot de passe doit contenir au moins une majuscule");
                return result;
            }
            
            if (!newPassword.matches(".*[0-9].*")) {
                result.put("success", false);
                result.put("message", "Le mot de passe doit contenir au moins un chiffre");
                return result;
            }
            
            if (!newPassword.matches(".*[!@#$%^&*()].*")) {
                result.put("success", false);
                result.put("message", "Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*())");
                return result;
            }
            
            // Utiliser AuthService pour changer le mot de passe
            AuthService authService = new AuthService();
            boolean success = authService.changePassword(email, currentPassword, newPassword);
            
            if (success) {
                result.put("success", true);
                result.put("message", "Mot de passe changé avec succès");
            } else {
                result.put("success", false);
                result.put("message", "Échec du changement de mot de passe. Vérifiez votre mot de passe actuel.");
            }
            
        } catch (Exception e) {
            System.err.println("[PROFILE] Exception changement mot de passe: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Erreur système: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Obtenir les informations de profil
     */
    public Map<String, Object> getProfileInfo(Utilisateur utilisateur) {
        Map<String, Object> info = new HashMap<>();
        
        info.put("nom", utilisateur.getNom());
        info.put("prenom", utilisateur.getPrenom());
        info.put("email", utilisateur.getEmail());
        info.put("telephone", utilisateur.getTelephone() != null ? utilisateur.getTelephone() : "Non défini");
        info.put("adresse", utilisateur.getAdresse() != null ? utilisateur.getAdresse() : "Non définie");
        info.put("role", utilisateur.getRoleName());
        info.put("statut", utilisateur.getStatut() != null ? utilisateur.getStatut().name() : "INCONNU");
        info.put("date_creation", utilisateur.getDateInscription());
        info.put("derniere_connexion", utilisateur.getLastLogin());
        info.put("matricule", "AG-" + utilisateur.getUtilisateurId());
        info.put("has_pin", utilisateur.getPinHash() != null && !utilisateur.getPinHash().isEmpty());
        
        return info;
    }
    
    /**
     * Vérifier si l'email est disponible (pour modification)
     */
    public boolean isEmailAvailable(String email, int userId) {
        try {
            Utilisateur existing = utilisateurDAO.findByEmail(email);
            return existing == null || existing.getUtilisateurId() == userId;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Mettre à jour un utilisateur (version admin avec audit)
     */
    public Map<String, Object> updateUserAsAdmin(int targetUserId, Map<String, String> updates, 
                                                 int adminId, String adminName, String ipAddress) {
        System.out.println("[PROFILE-ADMIN] Mise à jour admin pour utilisateur ID: " + targetUserId);
        
        Map<String, Object> result = new HashMap<>();
        Map<String, String> auditChanges = new HashMap<>();
        boolean modifications = false;
        
        try {
            // Récupérer l'utilisateur cible
            Utilisateur targetUser = utilisateurDAO.findById(targetUserId);
            if (targetUser == null) {
                result.put("success", false);
                result.put("message", "Utilisateur non trouvé");
                return result;
            }
            
            // Sauvegarder l'état avant modification
            Map<String, String> beforeState = getUserState(targetUser);
            
            // Vérifier et appliquer chaque mise à jour
            // Nom
            if (updates.containsKey("nom") && !updates.get("nom").trim().isEmpty() && 
                !updates.get("nom").equals(targetUser.getNom())) {
                auditChanges.put("nom", targetUser.getNom() + " → " + updates.get("nom"));
                targetUser.setNom(updates.get("nom"));
                modifications = true;
            }
            
            // Prénom
            if (updates.containsKey("prenom") && !updates.get("prenom").trim().isEmpty() && 
                !updates.get("prenom").equals(targetUser.getPrenom())) {
                auditChanges.put("prenom", targetUser.getPrenom() + " → " + updates.get("prenom"));
                targetUser.setPrenom(updates.get("prenom"));
                modifications = true;
            }
            
            // Email
            if (updates.containsKey("email") && !updates.get("email").trim().isEmpty() && 
                !updates.get("email").equals(targetUser.getEmail())) {
                
                // Vérifier si l'email est disponible
                if (isEmailAvailable(updates.get("email"), targetUserId)) {
                    auditChanges.put("email", targetUser.getEmail() + " → " + updates.get("email"));
                    targetUser.setEmail(updates.get("email"));
                    modifications = true;
                } else {
                    result.put("success", false);
                    result.put("message", "Cet email est déjà utilisé par un autre utilisateur");
                    return result;
                }
            }
            
            // Téléphone
            if (updates.containsKey("telephone")) {
                String newPhone = updates.get("telephone");
                String currentPhone = targetUser.getTelephone();
                
                if (newPhone != null && !newPhone.trim().isEmpty()) {
                    if (currentPhone == null || !newPhone.equals(currentPhone)) {
                        // Vérifier l'unicité du téléphone
                        Utilisateur existing = utilisateurDAO.findByTelephone(newPhone);
                        if (existing == null || existing.getUtilisateurId() == targetUserId) {
                            auditChanges.put("telephone", 
                                (currentPhone != null ? currentPhone : "Non défini") + 
                                " → " + newPhone);
                            targetUser.setTelephone(newPhone);
                            modifications = true;
                        }
                    }
                } else if (newPhone != null && newPhone.trim().isEmpty() && currentPhone != null) {
                    // Supprimer le téléphone
                    auditChanges.put("telephone", currentPhone + " → Supprimé");
                    targetUser.setTelephone(null);
                    modifications = true;
                }
            }
            
            // Adresse
            if (updates.containsKey("adresse")) {
                String newAddress = updates.get("adresse");
                String currentAddress = targetUser.getAdresse();
                
                if (newAddress != null && !newAddress.trim().isEmpty()) {
                    if (currentAddress == null || !newAddress.equals(currentAddress)) {
                        auditChanges.put("adresse", 
                            (currentAddress != null ? currentAddress : "Non définie") + 
                            " → " + newAddress);
                        targetUser.setAdresse(newAddress);
                        modifications = true;
                    }
                } else if (newAddress != null && newAddress.trim().isEmpty() && currentAddress != null) {
                    // Supprimer l'adresse
                    auditChanges.put("adresse", currentAddress + " → Supprimé");
                    targetUser.setAdresse(null);
                    modifications = true;
                }
            }
            
            // Rôle
            if (updates.containsKey("role") && !updates.get("role").trim().isEmpty() && 
                !updates.get("role").equals(targetUser.getRoleName())) {
                auditChanges.put("role", targetUser.getRoleName() + " → " + updates.get("role"));
                boolean roleUpdated = utilisateurDAO.updateRole(targetUserId, updates.get("role"));
                modifications = modifications || roleUpdated;
            }
            
            // Statut - CORRECTION : convertir String en StatutUtilisateur
            if (updates.containsKey("statut") && !updates.get("statut").trim().isEmpty()) {
                String newStatusStr = updates.get("statut");
                StatutUtilisateur currentStatus = targetUser.getStatut();
                StatutUtilisateur newStatus = null;
                
                try {
                    newStatus = StatutUtilisateur.valueOf(newStatusStr);
                } catch (IllegalArgumentException e) {
                    // Si la valeur n'est pas valide, essayer de la mapper
                    switch (newStatusStr.toUpperCase()) {
                        case "ACTIF": newStatus = StatutUtilisateur.ACTIF; break;
                        case "INACTIF": newStatus = StatutUtilisateur.INACTIF; break;
                        case "BLOQUE": newStatus = StatutUtilisateur.BLOQUE; break;
                        default: newStatus = StatutUtilisateur.INACTIF;
                    }
                }
                
                if (currentStatus != newStatus) {
                    auditChanges.put("statut", currentStatus.name() + " → " + newStatus.name());
                    targetUser.setStatut(newStatus);
                    modifications = true;
                }
            }
            
            if (modifications) {
                // Sauvegarder les modifications
                boolean saved = utilisateurDAO.update(targetUser);
                
                if (saved) {
                    // Enregistrer l'audit avec toutes les informations
                    String auditMessage = "Modification par l'administrateur " + adminName + 
                                         " (ID: " + adminId + ")";
                    
                    for (Map.Entry<String, String> change : auditChanges.entrySet()) {
                        auditMessage += "\n- " + change.getKey() + ": " + change.getValue();
                    }
                    
                    // Journaliser l'action
                    journalAuditDAO.logAction(
                        adminId,
                        "USER_UPDATE",
                        "UTILISATEUR",
                        targetUserId,
                        mapToJson(beforeState),
                        mapToJson(getUserState(targetUser)),
                        ipAddress
                    );
                    
                    System.out.println("[PROFILE-ADMIN] ✓ Utilisateur " + targetUserId + " mis à jour par admin " + adminName);
                    
                    result.put("success", true);
                    result.put("message", "Utilisateur mis à jour avec succès");
                    result.put("changes", auditChanges);
                    result.put("utilisateur", targetUser);
                } else {
                    result.put("success", false);
                    result.put("message", "Erreur lors de la sauvegarde");
                }
            } else {
                result.put("success", false);
                result.put("message", "Aucune modification détectée");
            }
            
        } catch (Exception e) {
            System.err.println("[PROFILE-ADMIN] Exception: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Erreur: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Réinitialiser le mot de passe (admin) avec audit
     */
    public Map<String, Object> resetPasswordAsAdmin(int targetUserId, String newPassword, 
                                                    int adminId, String adminName, String ipAddress) {
        System.out.println("[PROFILE-ADMIN] Réinitialisation MDP pour utilisateur: " + targetUserId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validation du mot de passe
            if (newPassword == null || newPassword.length() < 6) {
                result.put("success", false);
                result.put("message", "Le mot de passe doit contenir au moins 6 caractères");
                return result;
            }
            
            // Récupérer l'utilisateur
            Utilisateur targetUser = utilisateurDAO.findById(targetUserId);
            if (targetUser == null) {
                result.put("success", false);
                result.put("message", "Utilisateur non trouvé");
                return result;
            }
            
            // Sauvegarder l'état avant
            String beforeState = "Mot de passe précédent (hashé)";
            
            // Réinitialiser le mot de passe
            boolean success = utilisateurDAO.updatePassword(targetUserId, newPassword);
            
            if (success) {
                // Enregistrer l'audit
                String auditMessage = "Réinitialisation du mot de passe par l'administrateur " + 
                                    adminName + " (ID: " + adminId + ")";
                
                journalAuditDAO.logAction(
                    adminId,
                    "PASSWORD_RESET",
                    "UTILISATEUR",
                    targetUserId,
                    beforeState,
                    "Nouveau mot de passe défini",
                    ipAddress
                );
                
                System.out.println("[PROFILE-ADMIN] ✓ MDP réinitialisé pour: " + targetUser.getEmail());
                
                result.put("success", true);
                result.put("message", "Mot de passe réinitialisé avec succès");
            } else {
                result.put("success", false);
                result.put("message", "Échec de la réinitialisation");
            }
            
        } catch (Exception e) {
            System.err.println("[PROFILE-ADMIN] Exception réinitialisation MDP: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Erreur: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Recherche avancée des utilisateurs (vraies données)
     */
    public List<Utilisateur> searchUsersAdvanced(Map<String, String> filters) {
        System.out.println("[PROFILE] Recherche avancée avec filtres: " + filters);
        
        List<Utilisateur> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Construire la requête dynamique
            StringBuilder sql = new StringBuilder(
                "SELECT u.*, r.nom_role as role_name FROM utilisateur u " +
                "JOIN role r ON u.role_id = r.role_id WHERE 1=1"
            );
            
            List<Object> params = new ArrayList<>();
            
            // Filtre par recherche texte
            if (filters.containsKey("search") && !filters.get("search").isEmpty()) {
                String search = filters.get("search");
                sql.append(" AND (u.nom LIKE ? OR u.prenom LIKE ? OR u.email LIKE ? OR u.telephone LIKE ?)");
                String searchPattern = "%" + search + "%";
                params.add(searchPattern);
                params.add(searchPattern);
                params.add(searchPattern);
                params.add(searchPattern);
            }
            
            // Filtre par rôle
            if (filters.containsKey("role") && !filters.get("role").isEmpty() && 
                !filters.get("role").equals("TOUS")) {
                sql.append(" AND r.nom_role = ?");
                params.add(filters.get("role"));
            }
            
            // Filtre par statut - CORRECTION : utiliser le nom du statut (String)
            if (filters.containsKey("statut") && !filters.get("statut").isEmpty() && 
                !filters.get("statut").equals("TOUS")) {
                sql.append(" AND u.statut = ?");
                params.add(filters.get("statut"));
            }
            
            // Filtre par téléphone
            if (filters.containsKey("telephone") && !filters.get("telephone").isEmpty()) {
                sql.append(" AND u.telephone LIKE ?");
                params.add("%" + filters.get("telephone") + "%");
            }
            
            // Tri
            sql.append(" ORDER BY u.date_inscription DESC");
            
            // Exécuter la requête
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            
            // Map ResultSet vers objets Utilisateur
            while (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setUtilisateurId(rs.getInt("utilisateur_id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone"));
                user.setAdresse(rs.getString("adresse"));
                
                // CORRECTION : convertir String en StatutUtilisateur
                String statutStr = rs.getString("statut");
                if (statutStr != null) {
                    try {
                        user.setStatut(StatutUtilisateur.valueOf(statutStr));
                    } catch (IllegalArgumentException e) {
                        user.setStatut(StatutUtilisateur.INACTIF);
                    }
                }
                
                user.setDateInscription(rs.getTimestamp("date_inscription") != null ? 
                    rs.getTimestamp("date_inscription").toLocalDateTime() : null);
                user.setRoleName(rs.getString("role_name"));
                
                results.add(user);
            }
            
        } catch (Exception e) {
            System.err.println("[PROFILE] Erreur recherche avancée: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Obtenir l'historique des modifications d'un utilisateur (vraies données)
     */
    public List<Map<String, Object>> getUserAuditHistory(int userId) {
        List<Map<String, Object>> history = new ArrayList<>();
        
        try {
            // Utiliser JournalAuditDAO pour récupérer les vrais logs
            List<com.ewallet.core.models.JournalAudit> auditLogs = 
                journalAuditDAO.findByUtilisateurId(userId);
            
            for (com.ewallet.core.models.JournalAudit log : auditLogs) {
                Map<String, Object> entry = new HashMap<>();
                
                entry.put("date", log.getDateAction());
                entry.put("action", log.getAction());
                entry.put("entite", log.getEntite());
                entry.put("entiteId", log.getEntiteId());
                entry.put("ancienneValeur", log.getAncienneValeur());
                entry.put("nouvelleValeur", log.getNouvelleValeur());
                entry.put("adresseIp", log.getAdresseIp());
                
                // Formater la description pour l'affichage
                String description = log.getAction();
                if (log.getEntite() != null) {
                    description += " sur " + log.getEntite();
                    if (log.getEntiteId() != null) {
                        description += " #" + log.getEntiteId();
                    }
                }
                
                if (log.getNouvelleValeur() != null && !log.getNouvelleValeur().isEmpty()) {
                    description += ": " + log.getNouvelleValeur();
                }
                
                entry.put("description", description);
                
                history.add(entry);
            }
            
            // Si pas d'audit, on peut ajouter des informations basiques
            if (history.isEmpty()) {
                // Récupérer l'utilisateur pour sa date de création
                Utilisateur user = utilisateurDAO.findById(userId);
                if (user != null) {
                    Map<String, Object> creationEntry = new HashMap<>();
                    creationEntry.put("date", user.getDateInscription());
                    creationEntry.put("action", "USER_CREATED");
                    creationEntry.put("description", "Compte utilisateur créé");
                    history.add(creationEntry);
                }
            }
            
        } catch (Exception e) {
            System.err.println("[PROFILE] Erreur récupération historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    // Méthodes utilitaires privées
    private Map<String, String> getUserState(Utilisateur user) {
        Map<String, String> state = new HashMap<>();
        state.put("nom", user.getNom());
        state.put("prenom", user.getPrenom());
        state.put("email", user.getEmail());
        state.put("telephone", user.getTelephone() != null ? user.getTelephone() : "");
        state.put("adresse", user.getAdresse() != null ? user.getAdresse() : "");
        // Utiliser name() pour StatutUtilisateur
        state.put("statut", user.getStatut() != null ? user.getStatut().name() : "");
        state.put("role", user.getRoleName());
        return state;
    }
    
    private String mapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue().replace("\"", "\\\"")).append("\",");
        }
        if (json.length() > 1) {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
}