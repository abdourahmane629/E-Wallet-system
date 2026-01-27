package com.ewallet.core.dao;

import com.ewallet.core.DatabaseConfig;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TypeTransactionDAO {
    
    /**
     * Récupérer tous les types de transaction sous forme de Map
     */
    public Map<String, Integer> getAllTypesMap() {
        Map<String, Integer> types = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT type_id, nom_type FROM type_transaction";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                types.put(rs.getString("nom_type").toUpperCase(), rs.getInt("type_id"));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer les types de transaction");
            e.printStackTrace();
        }
        return types;
    }
    
    /**
     * Récupérer l'ID d'un type par son nom
     */
    public int findIdByNom(String nomType) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT type_id FROM type_transaction WHERE nom_type = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nomType);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("type_id");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de trouver le type: " + nomType);
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Créer un type de transaction (pour l'initialisation)
     */
    public boolean createType(String nomType, String description) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Vérifier si le type existe déjà
            String checkSql = "SELECT type_id FROM type_transaction WHERE nom_type = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, nomType);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                return true; // Type existe déjà
            }
            
            // Créer le type
            String sql = "INSERT INTO type_transaction (nom_type, description) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nomType);
            ps.setString(2, description);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créer le type: " + nomType);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Initialiser les types de transaction par défaut
     */
    public boolean initializeDefaultTypes() {
        boolean success = true;
        success &= createType("DEPOT", "Dépôt d'argent");
        success &= createType("RETRAIT", "Retrait d'argent");
        success &= createType("TRANSFERT", "Transfert entre comptes");
        success &= createType("COMMISSION", "Commission agent");
        success &= createType("FRAIS", "Frais de service");
        return success;
    }
}