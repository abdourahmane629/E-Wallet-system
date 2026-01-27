package com.ewallet.core.models;

import java.time.LocalDateTime;

public class Commission {
    private int id;
    private int agentId;
    private int transactionId;
    private double montantCommission;
    private double pourcentage;
    private LocalDateTime dateCommission;
    private String statut;
    
    // Champs supplémentaires pour l'affichage
    private String agentNom;
    private String agentPrenom;
    private String agentEmail;
    private String transactionNumero;

    // Constructeurs
    public Commission() {}

    public Commission(int agentId, int transactionId, double montantCommission) {
        this.agentId = agentId;
        this.transactionId = transactionId;
        this.montantCommission = montantCommission;
        this.pourcentage = 1.0; // 1% par défaut
        this.dateCommission = LocalDateTime.now();
        this.statut = "PENDING";
    }

    public Commission(int agentId, int transactionId, double montantCommission, double pourcentage) {
        this.agentId = agentId;
        this.transactionId = transactionId;
        this.montantCommission = montantCommission;
        this.pourcentage = pourcentage;
        this.dateCommission = LocalDateTime.now();
        this.statut = "PENDING";
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAgentId() { return agentId; }
    public void setAgentId(int agentId) { this.agentId = agentId; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public double getMontantCommission() { return montantCommission; }
    public void setMontantCommission(double montantCommission) { this.montantCommission = montantCommission; }

    public double getPourcentage() { return pourcentage; }
    public void setPourcentage(double pourcentage) { this.pourcentage = pourcentage; }

    public LocalDateTime getDateCommission() { return dateCommission; }
    public void setDateCommission(LocalDateTime dateCommission) { this.dateCommission = dateCommission; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    // Getters et setters pour les champs d'affichage
    public String getAgentNom() { return agentNom; }
    public void setAgentNom(String agentNom) { this.agentNom = agentNom; }
    
    public String getAgentPrenom() { return agentPrenom; }
    public void setAgentPrenom(String agentPrenom) { this.agentPrenom = agentPrenom; }
    
    public String getAgentEmail() { return agentEmail; }
    public void setAgentEmail(String agentEmail) { this.agentEmail = agentEmail; }
    
    public String getTransactionNumero() { return transactionNumero; }
    public void setTransactionNumero(String transactionNumero) { this.transactionNumero = transactionNumero; }
    
    // Méthode utilitaire pour l'affichage
    public String getAgentComplet() {
        if (agentPrenom != null && agentNom != null) {
            return agentPrenom + " " + agentNom;
        }
        return "Agent " + agentId;
    }
    
    public String getMontantFormatted() {
        return String.format("%,.0f GNF", montantCommission);
    }
    
    public String getPourcentageFormatted() {
        return String.format("%.1f%%", pourcentage);
    }
    
    public String getDateFormatted() {
        if (dateCommission != null) {
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dateCommission.format(formatter);
        }
        return "";
    }
    
    public String getStatutColor() {
        switch (statut) {
            case "PENDING": return "orange";
            case "PAID": return "green";
            case "CANCELLED": return "red";
            default: return "gray";
        }
    }

    @Override
    public String toString() {
        return "Commission{" +
                "id=" + id +
                ", agentId=" + agentId +
                ", transactionId=" + transactionId +
                ", montantCommission=" + montantCommission +
                ", pourcentage=" + pourcentage +
                ", dateCommission=" + dateCommission +
                ", statut='" + statut + '\'' +
                '}';
    }
}