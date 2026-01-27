package com.ewallet.core.models;

import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private int utilisateurId;
    private String titre;
    private String message;
    private String type;
    private boolean estLue;
    private LocalDateTime dateCreation;
    
    // Constructeurs
    public Notification() {}
    
    public Notification(int utilisateurId, String titre, String message, String type) {
        this.utilisateurId = utilisateurId;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.estLue = false;
        this.dateCreation = LocalDateTime.now();
    }
    
    // Getters et Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isEstLue() { return estLue; }
    public void setEstLue(boolean estLue) { this.estLue = estLue; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}