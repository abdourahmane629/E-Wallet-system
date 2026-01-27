package com.ewallet.core.models;

import java.time.LocalDateTime;

public class JournalAudit {
    private int journalId;
    private int utilisateurId;
    private String action;
    private String entite;
    private Integer entiteId;
    private String ancienneValeur;
    private String nouvelleValeur;
    private String adresseIp;
    private LocalDateTime dateAction;
    
    // Constructeurs
    public JournalAudit() {}
    
    public JournalAudit(int utilisateurId, String action, String entite, 
                       Integer entiteId, String ancienneValeur, 
                       String nouvelleValeur, String adresseIp) {
        this.utilisateurId = utilisateurId;
        this.action = action;
        this.entite = entite;
        this.entiteId = entiteId;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.adresseIp = adresseIp;
    }
    
    // Getters et Setters
    public int getJournalId() { return journalId; }
    public void setJournalId(int journalId) { this.journalId = journalId; }
    
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getEntite() { return entite; }
    public void setEntite(String entite) { this.entite = entite; }
    
    public Integer getEntiteId() { return entiteId; }
    public void setEntiteId(Integer entiteId) { this.entiteId = entiteId; }
    
    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }
    
    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }
    
    public String getAdresseIp() { return adresseIp; }
    public void setAdresseIp(String adresseIp) { this.adresseIp = adresseIp; }
    
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
    
    @Override
    public String toString() {
        return "JournalAudit{" +
               "journalId=" + journalId +
               ", action='" + action + '\'' +
               ", dateAction=" + dateAction +
               '}';
    }
}