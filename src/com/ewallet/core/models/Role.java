package com.ewallet.core.models;

public enum Role {
    ADMIN(1, "ADMIN", "Administrateur système"),
    AGENT(2, "AGENT", "Agent pour dépôts et retraits"),
    USER(3, "USER", "Client / Utilisateur normal");

    private final int id;
    private final String nom;
    private final String description;

    Role(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }

    // Méthodes utilitaires
    public static Role getById(int id) {
        for (Role role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        return USER; // Par défaut
    }

    public static Role getByNom(String nom) {
        for (Role role : values()) {
            if (role.nom.equalsIgnoreCase(nom)) {
                return role;
            }
        }
        return USER; // Par défaut
    }

    public static String getDisplayName(String nomRole) {
        if ("USER".equalsIgnoreCase(nomRole)) {
            return "Client";
        }
        return nomRole;
    }

    public String getDisplayName() {
        if (this == USER) {
            return "Client";
        }
        return this.nom;
    }
}