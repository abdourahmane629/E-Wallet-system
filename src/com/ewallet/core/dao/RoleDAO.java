package com.ewallet.core.dao;

import com.ewallet.core.DatabaseConfig;
import java.sql.*;

public class RoleDAO {
    
    /**
     * Obtenir l'ID d'un rôle par son nom
     */
    public int getRoleIdByName(String roleName) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT role_id FROM role WHERE nom_role = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // Convertir en majuscule pour correspondre à votre BD
            String nomRole = roleName.toUpperCase();
            ps.setString(1, nomRole);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("role_id");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR ROLE DAO] Impossible de récupérer l'ID du rôle: " + roleName);
            e.printStackTrace();
        }
        return -1; // Rôle non trouvé
    }
    
    /**
     * Obtenir le nom d'un rôle par son ID
     */
    public String getRoleNameById(int roleId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT nom_role FROM role WHERE role_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roleId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("nom_role");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR ROLE DAO] Impossible de récupérer le nom du rôle ID: " + roleId);
            e.printStackTrace();
        }
        return "INCONNU";
    }
    
    /**
     * Vérifier si un rôle existe
     */
    public boolean roleExists(String roleName) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM role WHERE nom_role = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName.toUpperCase());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR ROLE DAO] Impossible de vérifier l'existence du rôle");
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Obtenir tous les rôles
     */
    public java.util.List<String> getAllRoles() {
        java.util.List<String> roles = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT nom_role FROM role ORDER BY role_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                roles.add(rs.getString("nom_role"));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR ROLE DAO] Impossible de récupérer tous les rôles");
            e.printStackTrace();
        }
        return roles;
    }
}