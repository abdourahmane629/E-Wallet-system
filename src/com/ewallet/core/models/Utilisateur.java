package com.ewallet.core.models;

import java.time.LocalDateTime;

public class Utilisateur {
    private int utilisateurId;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String motDePasseHash;
    private String pinHash;
    private int roleId;
    private String roleName;
    private StatutUtilisateur statut;
    private int tentativesEchecs;
    private boolean compteVerrouille;
    private LocalDateTime dateVerrouillage;
    private LocalDateTime dateInscription;
    private LocalDateTime dateModification;
    private LocalDateTime lastLogin;

    public Utilisateur() {}

    public Utilisateur(int utilisateurId, String nom, String prenom, String email, 
                       String motDePasseHash, int roleId, StatutUtilisateur statut) {
        this.utilisateurId = utilisateurId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.roleId = roleId;
        this.statut = statut;
        this.tentativesEchecs = 0;
        this.compteVerrouille = false;
        this.dateInscription = LocalDateTime.now();
    }

    // ==================== GETTERS ET SETTERS ====================

    public int getId() { 
        return utilisateurId; 
    }
    
    public void setId(int utilisateurId) { 
        this.utilisateurId = utilisateurId; 
    }
    
    public int getUtilisateurId() { 
        return utilisateurId; 
    }
    
    public void setUtilisateurId(int utilisateurId) { 
        this.utilisateurId = utilisateurId; 
    }

    public String getNom() { 
        return nom; 
    }
    
    public void setNom(String nom) { 
        this.nom = nom; 
    }

    public String getPrenom() { 
        return prenom; 
    }
    
    public void setPrenom(String prenom) { 
        this.prenom = prenom; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getTelephone() { 
        return telephone; 
    }
    
    public void setTelephone(String telephone) { 
        this.telephone = telephone; 
    }

    public String getAdresse() { 
        return adresse; 
    }
    
    public void setAdresse(String adresse) { 
        this.adresse = adresse; 
    }

    public String getMotDePasseHash() { 
        return motDePasseHash; 
    }
    
    public void setMotDePasseHash(String motDePasseHash) { 
        this.motDePasseHash = motDePasseHash; 
    }

    public String getPinHash() { 
        return pinHash; 
    }
    
    public void setPinHash(String pinHash) { 
        this.pinHash = pinHash; 
    }

    public int getRoleId() { 
        return roleId; 
    }
    
    public void setRoleId(int roleId) { 
        this.roleId = roleId; 
    }

    public String getRoleName() { 
        return roleName; 
    }
    
    public void setRoleName(String roleName) { 
        this.roleName = roleName; 
    }

    
    public StatutUtilisateur getStatut() { 
        return statut; 
    }
    
    public void setStatut(StatutUtilisateur statut) { 
        this.statut = statut; 
    }

    public int getTentativesEchecs() { 
        return tentativesEchecs; 
    }
    
    public void setTentativesEchecs(int tentativesEchecs) { 
        this.tentativesEchecs = tentativesEchecs; 
    }

    public boolean isCompteVerrouille() { 
        return compteVerrouille; 
    }
    
    public void setCompteVerrouille(boolean compteVerrouille) { 
        this.compteVerrouille = compteVerrouille; 
    }

    public LocalDateTime getDateVerrouillage() { 
        return dateVerrouillage; 
    }
    
    public void setDateVerrouillage(LocalDateTime dateVerrouillage) { 
        this.dateVerrouillage = dateVerrouillage; 
    }

    public LocalDateTime getDateInscription() { 
        return dateInscription; 
    }
    
    public void setDateInscription(LocalDateTime dateInscription) { 
        this.dateInscription = dateInscription; 
    }

    public LocalDateTime getDateModification() { 
        return dateModification; 
    }
    
    public void setDateModification(LocalDateTime dateModification) { 
        this.dateModification = dateModification; 
    }
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    public void incrementerTentatives() {
        this.tentativesEchecs++;
        if (this.tentativesEchecs >= 3) {
            this.compteVerrouille = true;
            this.dateVerrouillage = LocalDateTime.now();
        }
    }

    public void reinitialiserTentatives() {
        this.tentativesEchecs = 0;
        this.compteVerrouille = false;
        this.dateVerrouillage = null;
    }

    public boolean estActif() {
        return statut == StatutUtilisateur.ACTIF && !compteVerrouille;
    }

    public boolean isAdmin() {
        return this.roleId == 1 || "ADMIN".equalsIgnoreCase(roleName);
    }

    public boolean isAgent() {
        return this.roleId == 2 || "AGENT".equalsIgnoreCase(roleName);
    }

    public boolean isClient() {
        return this.roleId == 3 || "USER".equalsIgnoreCase(roleName) || "CLIENT".equalsIgnoreCase(roleName);
    }

    public void setRoleAsAdmin() {
        this.roleId = 1;
        this.roleName = "ADMIN";
    }
    
    public void setRoleAsAgent() {
        this.roleId = 2;
        this.roleName = "AGENT";
    }
    
    public void setRoleAsClient() {
        this.roleId = 3;
        this.roleName = "USER";
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public String getRoleDisplayName() {
        if (roleName != null) {
            if ("USER".equalsIgnoreCase(roleName)) {
                return "Client";
            }
            return roleName;
        }
        
        switch (roleId) {
            case 1: return "Administrateur";
            case 2: return "Agent";
            case 3: return "Client";
            default: return "Utilisateur";
        }
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + utilisateurId +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", roleDisplay='" + getRoleDisplayName() + '\'' +
                ", statut=" + statut +
                ", compteVerrouille=" + compteVerrouille +
                '}';
    }
}