package com.ewallet.core.dao;

import com.ewallet.core.DatabaseConfig;
import com.ewallet.core.models.Utilisateur;
import com.ewallet.core.utils.SecurityUtil;
import com.ewallet.core.models.StatutUtilisateur;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("unused")
public class UtilisateurDAO {

    /**
     * Trouver un utilisateur par email
     */
    public Utilisateur findByEmail(String email) {
        Utilisateur utilisateur = null;
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT u.utilisateur_id, u.nom, u.prenom, u.email, u.telephone, u.adresse, " +
                         "u.mot_de_passe_hash, u.pin_hash, u.role_id, u.statut, u.tentatives_echecs, " +
                         "u.compte_verrouille, u.date_verrouillage, u.date_inscription, u.date_modification, " +
                         "r.nom_role " +
                         "FROM utilisateur u " +
                         "LEFT JOIN role r ON u.role_id = r.role_id " +
                         "WHERE u.email = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                utilisateur = mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher l'utilisateur par email: " + email);
            e.printStackTrace();
        }
        return utilisateur;
    }

    /**
     * Trouver un utilisateur par ID
     */
    public Utilisateur findById(int id) {
        Utilisateur utilisateur = null;
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT u.utilisateur_id, u.nom, u.prenom, u.email, u.telephone, u.adresse, " +
                         "u.mot_de_passe_hash, u.pin_hash, u.role_id, u.statut, u.tentatives_echecs, " +
                         "u.compte_verrouille, u.date_verrouillage, u.date_inscription, u.date_modification, " +
                         "r.nom_role " +
                         "FROM utilisateur u " +
                         "LEFT JOIN role r ON u.role_id = r.role_id " +
                         "WHERE u.utilisateur_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                utilisateur = mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher l'utilisateur par ID: " + id);
            e.printStackTrace();
        }
        return utilisateur;
    }

    /**
     * Trouver un utilisateur par téléphone
     */
    public Utilisateur findByTelephone(String telephone) {
        Utilisateur utilisateur = null;
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT u.utilisateur_id, u.nom, u.prenom, u.email, u.telephone, u.adresse, " +
                         "u.mot_de_passe_hash, u.pin_hash, u.role_id, u.statut, u.tentatives_echecs, " +
                         "u.compte_verrouille, u.date_verrouillage, u.date_inscription, u.date_modification, " +
                         "r.nom_role " +
                         "FROM utilisateur u " +
                         "LEFT JOIN role r ON u.role_id = r.role_id " +
                         "WHERE u.telephone = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, telephone);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                utilisateur = mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher l'utilisateur par téléphone: " + telephone);
            e.printStackTrace();
        }
        return utilisateur;
    }

    /**
     * Créer un nouvel utilisateur
     */
    public int create(String nom, String prenom, String email, String telephone, String adresse,
                      String motDePasseHash, int roleId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO utilisateur (nom, prenom, email, telephone, adresse, " +
                         "mot_de_passe_hash, role_id, statut, tentatives_echecs, compte_verrouille) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIF', 0, false)";
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, email);
            ps.setString(4, telephone);
            ps.setString(5, adresse);
            ps.setString(6, motDePasseHash);
            ps.setInt(7, roleId);
            
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            
            if (rs.next()) {
                int userId = rs.getInt(1);
                System.out.println("[DAO] Utilisateur créé avec ID: " + userId + " - Email: " + email);
                return userId;
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créer l'utilisateur: " + email);
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Créer un utilisateur complet
     */
    public boolean create(Utilisateur utilisateur) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO utilisateur (nom, prenom, email, telephone, adresse, " +
                         "mot_de_passe_hash, pin_hash, role_id, statut, tentatives_echecs, compte_verrouille) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail());
            ps.setString(4, utilisateur.getTelephone());
            ps.setString(5, utilisateur.getAdresse());
            ps.setString(6, utilisateur.getMotDePasseHash());
            ps.setString(7, utilisateur.getPinHash());
            ps.setInt(8, utilisateur.getRoleId());
            ps.setString(9, utilisateur.getStatut().toString());
            ps.setInt(10, utilisateur.getTentativesEchecs());
            ps.setBoolean(11, utilisateur.isCompteVerrouille());
            
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    utilisateur.setUtilisateurId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de créer l'utilisateur");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour les tentatives échouées
     */
    public boolean updateTentatives(int utilisateurId, int tentatives, boolean verrouille) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql;
            PreparedStatement ps;
            
            if (verrouille) {
                sql = "UPDATE utilisateur SET tentatives_echecs = ?, compte_verrouille = ?, " +
                      "date_verrouillage = CURRENT_TIMESTAMP WHERE utilisateur_id = ?";
            } else {
                sql = "UPDATE utilisateur SET tentatives_echecs = ?, compte_verrouille = ? " +
                      "WHERE utilisateur_id = ?";
            }
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, tentatives);
            ps.setBoolean(2, verrouille);
            ps.setInt(3, utilisateurId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour les tentatives");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mettre à jour la date de modification (dernière connexion)
     */
    public boolean updateLastLogin(int utilisateurId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE utilisateur SET date_modification = CURRENT_TIMESTAMP WHERE utilisateur_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, utilisateurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour la dernière connexion");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifier les infos d'un utilisateur
     */
    public boolean update(Utilisateur utilisateur) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, telephone = ?, " +
                         "adresse = ?, statut = ? WHERE utilisateur_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getPrenom());
            ps.setString(3, utilisateur.getEmail());
            ps.setString(4, utilisateur.getTelephone());
            ps.setString(5, utilisateur.getAdresse());
            ps.setString(6, utilisateur.getStatut().toString());
            ps.setInt(7, utilisateur.getUtilisateurId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de modifier l'utilisateur");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Changer le mot de passe
     */
    public boolean changePassword(int utilisateurId, String newPasswordHash) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE utilisateur SET mot_de_passe_hash = ?, date_modification = CURRENT_TIMESTAMP " +
                         "WHERE utilisateur_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newPasswordHash);
            ps.setInt(2, utilisateurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de changer le mot de passe");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Changer le PIN
     */
    public boolean changePIN(int utilisateurId, String newPinHash) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE utilisateur SET pin_hash = ?, date_modification = CURRENT_TIMESTAMP " +
                         "WHERE utilisateur_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newPinHash);
            ps.setInt(2, utilisateurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de changer le PIN");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Débloquer un compte
     */
    public boolean unlockAccount(int utilisateurId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE utilisateur SET compte_verrouille = false, tentatives_echecs = 0, " +
                         "date_verrouillage = NULL WHERE utilisateur_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, utilisateurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de débloquer le compte");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mettre à jour le rôle
     */
    public boolean updateRole(int utilisateurId, int roleId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE utilisateur SET role_id = ?, date_modification = CURRENT_TIMESTAMP " +
                         "WHERE utilisateur_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roleId);
            ps.setInt(2, utilisateurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour le rôle");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Trouver tous les utilisateurs par rôle
     */
    public List<Utilisateur> findByRoleId(int roleId) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT u.utilisateur_id, u.nom, u.prenom, u.email, u.telephone, u.adresse, " +
                         "u.mot_de_passe_hash, u.pin_hash, u.role_id, u.statut, u.tentatives_echecs, " +
                         "u.compte_verrouille, u.date_verrouillage, u.date_inscription, u.date_modification, " +
                         "r.nom_role " +
                         "FROM utilisateur u " +
                         "LEFT JOIN role r ON u.role_id = r.role_id " +
                         "WHERE u.role_id = ? ORDER BY u.nom, u.prenom";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roleId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de chercher les utilisateurs par rôle");
            e.printStackTrace();
        }
        return utilisateurs;
    }

    /**
     * Récupérer tous les utilisateurs (pour admin dashboard)
     */
    public List<Utilisateur> findAll() {
        List<Utilisateur> users = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT u.utilisateur_id, u.nom, u.prenom, u.email, u.telephone, u.adresse, " +
                         "u.mot_de_passe_hash, u.pin_hash, u.role_id, u.statut, u.tentatives_echecs, " +
                         "u.compte_verrouille, u.date_verrouillage, u.date_inscription, u.date_modification, " +
                         "r.nom_role " +
                         "FROM utilisateur u " +
                         "LEFT JOIN role r ON u.role_id = r.role_id " +
                         "ORDER BY u.date_inscription DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                users.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de récupérer tous les utilisateurs");
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Compter les utilisateurs par rôle
     */
    public int countByRole(String roleName) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM utilisateur u " +
                        "JOIN role r ON u.role_id = r.role_id " +
                        "WHERE r.nom_role = ? AND u.statut = 'ACTIF'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de compter par rôle");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mettre à jour le statut d'un utilisateur
     */
    public boolean updateStatus(int utilisateurId, String newStatus) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE utilisateur SET statut = ?, date_modification = CURRENT_TIMESTAMP " +
                        "WHERE utilisateur_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, utilisateurId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de mettre à jour le statut");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Rechercher des utilisateurs avec critères multiples
     */
    public List<Utilisateur> searchUsers(String searchTerm, String role, String status) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT u.utilisateur_id, u.nom, u.prenom, u.email, u.telephone, u.adresse, " +
                "u.mot_de_passe_hash, u.pin_hash, u.role_id, u.statut, u.tentatives_echecs, " +
                "u.compte_verrouille, u.date_verrouillage, u.date_inscription, u.date_modification, " +
                "r.nom_role " +
                "FROM utilisateur u " +
                "LEFT JOIN role r ON u.role_id = r.role_id " +
                "WHERE 1=1"
            );
            
            List<Object> params = new ArrayList<>();
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                sql.append(" AND (u.email LIKE ? OR u.nom LIKE ? OR u.prenom LIKE ? OR u.telephone LIKE ?)");
                String termPattern = "%" + searchTerm + "%";
                params.add(termPattern);
                params.add(termPattern);
                params.add(termPattern);
                params.add(termPattern);
            }
            
            if (role != null && !role.isEmpty() && !role.equals("TOUS")) {
                sql.append(" AND r.nom_role = ?");
                params.add(role);
            }
            
            if (status != null && !status.isEmpty() && !status.equals("TOUS")) {
                sql.append(" AND u.statut = ?");
                params.add(status);
            }
            
            sql.append(" ORDER BY u.date_inscription DESC");
            
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible de rechercher des utilisateurs");
            e.printStackTrace();
        }
        return utilisateurs;
    }

    /**
     * Obtenir des statistiques sur les utilisateurs
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Total utilisateurs
            String sql = "SELECT COUNT(*) as total FROM utilisateur";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
            }
            
            // Par rôle (USER = CLIENT dans votre BD)
            sql = "SELECT r.nom_role, COUNT(u.utilisateur_id) as count " +
                  "FROM utilisateur u " +
                  "JOIN role r ON u.role_id = r.role_id " +
                  "GROUP BY r.nom_role";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                String role = rs.getString("nom_role");
                int count = rs.getInt("count");
                if ("USER".equals(role)) {
                    stats.put("clients", count);
                } else if ("AGENT".equals(role)) {
                    stats.put("agents", count);
                } else if ("ADMIN".equals(role)) {
                    stats.put("admins", count);
                }
            }
            
            // Par statut
            sql = "SELECT statut, COUNT(*) as count FROM utilisateur GROUP BY statut";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map<String, Integer> statusStats = new HashMap<>();
            while (rs.next()) {
                statusStats.put(rs.getString("statut"), rs.getInt("count"));
            }
            stats.put("byStatus", statusStats);
            
            // Nouveaux utilisateurs ce mois
            sql = "SELECT COUNT(*) as nouveaux_mois FROM utilisateur " +
                  "WHERE MONTH(date_inscription) = MONTH(CURRENT_DATE()) " +
                  "AND YEAR(date_inscription) = YEAR(CURRENT_DATE())";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("newThisMonth", rs.getInt("nouveaux_mois"));
            }
            
        } catch (SQLException e) {
            System.err.println("[ERREUR DAO] Impossible d'obtenir les statistiques utilisateurs");
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Mapper un ResultSet vers un objet Utilisateur
     */
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        
        u.setUtilisateurId(rs.getInt("utilisateur_id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setAdresse(rs.getString("adresse"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setPinHash(rs.getString("pin_hash"));
        
        int roleId = rs.getInt("role_id");
        u.setRoleId(roleId);
        
        // Récupérer le nom du rôle
        String roleName = rs.getString("nom_role");
        u.setRoleName(roleName);
        
        String statut = rs.getString("statut");
        u.setStatut(StatutUtilisateur.valueOf(statut));
        
        u.setTentativesEchecs(rs.getInt("tentatives_echecs"));
        u.setCompteVerrouille(rs.getBoolean("compte_verrouille"));
        
        Timestamp dateVerrouillage = rs.getTimestamp("date_verrouillage");
        if (dateVerrouillage != null) {
            u.setDateVerrouillage(dateVerrouillage.toLocalDateTime());
        }
        
        Timestamp dateInscription = rs.getTimestamp("date_inscription");
        if (dateInscription != null) {
            u.setDateInscription(dateInscription.toLocalDateTime());
        }
        
        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            u.setDateModification(dateModification.toLocalDateTime());
        }
        
        return u;
    }


    /**
     * Mettre à jour le mot de passe d'un utilisateur
     */
    public boolean updatePassword(int userId, String newPassword) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Hasher le mot de passe
            String passwordHash = SecurityUtil.hashPassword(newPassword);
            
            String sql = "UPDATE utilisateur SET password_hash = ?, date_modification = NOW() " +
                        "WHERE utilisateur_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            System.err.println("[ERREUR] Impossible de mettre à jour le mot de passe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mettre à jour le rôle d'un utilisateur
     */
    public boolean updateRole(int userId, String roleName) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Récupérer l'ID du rôle
            String roleSql = "SELECT role_id FROM role WHERE nom_role = ?";
            PreparedStatement rolePs = conn.prepareStatement(roleSql);
            rolePs.setString(1, roleName);
            ResultSet roleRs = rolePs.executeQuery();
            
            if (roleRs.next()) {
                int roleId = roleRs.getInt("role_id");
                
                String sql = "UPDATE utilisateur SET role_id = ?, date_modification = NOW() " +
                            "WHERE utilisateur_id = ?";
                
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, roleId);
                ps.setInt(2, userId);
                
                int rowsUpdated = ps.executeUpdate();
                return rowsUpdated > 0;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("[ERREUR] Impossible de mettre à jour le rôle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Recherche avancée avec filtres
     */
    public List<Utilisateur> searchWithFilters(String searchText, String role, String status, 
                                               String phone, LocalDate dateFrom, LocalDate dateTo) {
        List<Utilisateur> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT u.*, r.nom_role as role_name FROM utilisateur u " +
                "JOIN role r ON u.role_id = r.role_id WHERE 1=1"
            );
            
            List<Object> params = new ArrayList<>();
            
            // Filtre texte
            if (searchText != null && !searchText.isEmpty()) {
                sql.append(" AND (u.nom LIKE ? OR u.prenom LIKE ? OR u.email LIKE ?)");
                String pattern = "%" + searchText + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }
            
            // Filtre rôle
            if (role != null && !role.isEmpty() && !role.equals("TOUS")) {
                sql.append(" AND r.nom_role = ?");
                params.add(role);
            }
            
            // Filtre statut
            if (status != null && !status.isEmpty() && !status.equals("TOUS")) {
                sql.append(" AND u.statut = ?");
                params.add(status);
            }
            
            // Filtre téléphone
            if (phone != null && !phone.isEmpty()) {
                sql.append(" AND u.telephone LIKE ?");
                params.add("%" + phone + "%");
            }
            
            // Filtre date
            if (dateFrom != null) {
                sql.append(" AND u.date_inscription >= ?");
                params.add(java.sql.Date.valueOf(dateFrom));
            }
            
            if (dateTo != null) {
                sql.append(" AND u.date_inscription <= ?");
                params.add(java.sql.Date.valueOf(dateTo));
            }
            
            sql.append(" ORDER BY u.date_inscription DESC");
            
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUtilisateur(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("[ERREUR] Recherche avancée: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }

    
}