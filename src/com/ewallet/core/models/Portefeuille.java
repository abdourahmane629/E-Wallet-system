package com.ewallet.core.models;

import java.time.LocalDateTime;

public class Portefeuille {
    private int id;
    private int utilisateurId;
    private String numeroPortefeuille;
    private double solde;
    private String devise;
    private String statut;
    private Double limiteRetraitQuotidien;
    private Double limiteTransfert;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    
    // Constructeurs
    public Portefeuille() {}
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getNumeroPortefeuille() { return numeroPortefeuille; }
    public void setNumeroPortefeuille(String numeroPortefeuille) { this.numeroPortefeuille = numeroPortefeuille; }
    
    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }
    
    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public Double getLimiteRetraitQuotidien() { return limiteRetraitQuotidien; }
    public void setLimiteRetraitQuotidien(Double limiteRetraitQuotidien) { this.limiteRetraitQuotidien = limiteRetraitQuotidien; }
    
    public Double getLimiteTransfert() { return limiteTransfert; }
    public void setLimiteTransfert(Double limiteTransfert) { this.limiteTransfert = limiteTransfert; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    @Override
    public String toString() {
        return "Portefeuille{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", numeroPortefeuille='" + numeroPortefeuille + '\'' +
                ", solde=" + solde +
                ", devise='" + devise + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}