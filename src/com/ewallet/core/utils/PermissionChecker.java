package com.ewallet.core.utils;

import com.ewallet.core.models.Utilisateur;

/**
 * Classe utilitaire pour vérifier les permissions
 * Utilisée dans les contrôleurs et services
 */
public class PermissionChecker {

    private final Utilisateur currentUser;

    public PermissionChecker(Utilisateur user) {
        this.currentUser = user;
    }

    /**
     * Vérifier si l'utilisateur a un rôle spécifique
     * Utilisez le code du rôle : "ADMIN", "AGENT", "CLIENT"
     */
    public boolean hasRole(String roleCode) {
        if (currentUser == null) return false;
        return currentUser.getRoleName().equals(roleCode);
    }

    /**
     * Vérifier si l'utilisateur est admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Vérifier si l'utilisateur est agent
     */
    public boolean isAgent() {
        return currentUser != null && currentUser.isAgent();
    }

    /**
     * Vérifier si l'utilisateur est client
     */
    public boolean isClient() {
        return currentUser != null && currentUser.isClient();
    }

    /**
     * Demander une permission avec vérification et log
     */
    public boolean requirePermission(String action) {
        if (currentUser == null) {
            logUnauthorized(action, "USER_NULL");
            return false;
        }

        System.out.println("[PERMISSION] Vérification: " + currentUser.getEmail() + " - Action: " + action);
        return true;
    }

    /**
     * Demander le rôle admin
     */
    public void requireAdmin(String action) throws SecurityException {
        if (!isAdmin()) {
            logUnauthorized(action, currentUser != null ? currentUser.getEmail() : "UNKNOWN");
            throw new SecurityException("Accès admin requis pour: " + action);
        }
    }

    /**
     * Demander au minimum le rôle agent
     */
    public void requireAgentOrAdmin(String action) throws SecurityException {
        if (!isAgent() && !isAdmin()) {
            logUnauthorized(action, currentUser != null ? currentUser.getEmail() : "UNKNOWN");
            throw new SecurityException("Accès agent ou admin requis pour: " + action);
        }
    }

    /**
     * Demander d'être propriétaire de la ressource
     */
    public void requireOwnership(int resourceOwnerId, String action) throws SecurityException {
        if (currentUser == null || currentUser.getId() != resourceOwnerId) {
            if (!isAdmin()) { // Admin peut toujours accéder
                logUnauthorized(action, currentUser != null ? currentUser.getEmail() : "UNKNOWN");
                throw new SecurityException("Vous n'êtes pas propriétaire de cette ressource");
            }
        }
    }

    /**
     * Logger un accès non autorisé
     */
    private void logUnauthorized(String action, String userId) {
        System.err.println("[SECURITY] Accès refusé");
        System.err.println("  - Utilisateur: " + userId);
        System.err.println("  - Action: " + action);
        System.err.println("  - Role: " + (currentUser != null ? currentUser.getRoleName() : "NONE"));
    }

    // ==================== VÉRIFICATIONS MÉTIER ====================

    /**
     * Vérifier si l'utilisateur peut effectuer un transfert
     */
    public boolean canTransfer() {
        return isAdmin() || isClient();
    }

    /**
     * Vérifier si l'utilisateur peut effectuer un dépôt
     */
    public boolean canDeposit() {
        return isAdmin() || isAgent();
    }

    /**
     * Vérifier si l'utilisateur peut effectuer un retrait
     */
    public boolean canWithdraw() {
        return isAdmin() || isAgent();
    }

    /**
     * Vérifier si l'utilisateur peut voir tous les utilisateurs
     */
    public boolean canViewAllUsers() {
        return isAdmin();
    }

    /**
     * Vérifier si l'utilisateur peut voir les rapports admin
     */
    public boolean canViewAdminReports() {
        return isAdmin();
    }

    /**
     * Vérifier si l'utilisateur peut voir les commissions
     */
    public boolean canViewCommissions() {
        return isAdmin() || isAgent();
    }

    /**
     * Vérifier si l'utilisateur peut voir le journal d'audit complet
     */
    public boolean canViewFullAudit() {
        return isAdmin();
    }

    /**
     * Obtenir le message d'accès refusé
     */
    public static String accessDenied(String action) {
        return "Accès refusé: Vous n'avez pas les permissions pour '" + action + "'";
    }
}