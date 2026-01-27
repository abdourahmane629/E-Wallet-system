package com.ewallet.core.models;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private String numeroTransaction;
    private int portefeuilleId;
    private double montant;
    private String type; // DEPOT, RETRAIT, TRANSFERT
    private String description;
    private LocalDateTime dateTransaction;
    private String statut; // EN_ATTENTE, CONFIRME, REFUSE, ANNULE
    private int agentId;

    public Transaction() {}

    public Transaction(int portefeuilleId, double montant, String type, String description) {
        this.portefeuilleId = portefeuilleId;
        this.montant = montant;
        this.type = type;
        this.description = description;
        this.dateTransaction = LocalDateTime.now();
        this.statut = "CONFIRME";
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroTransaction() { return numeroTransaction; }
    public void setNumeroTransaction(String numeroTransaction) { this.numeroTransaction = numeroTransaction; }

    public int getPortefeuilleId() { return portefeuilleId; }
    public void setPortefeuilleId(int portefeuilleId) { this.portefeuilleId = portefeuilleId; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(LocalDateTime dateTransaction) { this.dateTransaction = dateTransaction; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getAgentId() { return agentId; }
    public void setAgentId(int agentId) { this.agentId = agentId; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", numeroTransaction='" + numeroTransaction + '\'' +
                ", portefeuilleId=" + portefeuilleId +
                ", montant=" + montant +
                ", type='" + type + '\'' +
                ", dateTransaction=" + dateTransaction +
                ", statut='" + statut + '\'' +
                '}';
    }
}