package com.ewallet.gui.controllers;
import com.ewallet.core.models.Utilisateur;
import com.ewallet.core.services.AuthService;
import com.ewallet.core.utils.SessionManager;
import com.ewallet.gui.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private AuthService authService = new AuthService();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        try {
            Utilisateur user = authService.login(email, password);

            if (user != null) {
                System.out.println("[SUCCESS] Connexion réussie pour: " + email);
                System.out.println("[ROLE] Rôle: " + user.getRoleName());
                
                SessionManager.setCurrentUser(user);
                
                // Redirection selon le rôle
                if (user.isAdmin()) {
                    MainApp.showAdminDashboard();
                } else if (user.isAgent()) {
                    MainApp.showAgentDashboard();
                } else {
                    MainApp.showClientDashboard();
                }
                
            } else {
                showError("Connexion échouée", "Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            System.err.println("ERREUR lors de l'authentification:");
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de la connexion.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}