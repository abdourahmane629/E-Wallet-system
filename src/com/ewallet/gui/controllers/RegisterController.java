package com.ewallet.gui.controllers;

import com.ewallet.core.services.RegistrationService;
import com.ewallet.core.utils.ValidationUtil;
import com.ewallet.gui.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;

    private RegistrationService registrationService = new RegistrationService();

    @FXML
    public void handleRegister() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!ValidationUtil.isValidName(nom)) {
            afficherErreur("Erreur", "Nom invalide (minimum 2 caractères)");
            return;
        }

        if (!ValidationUtil.isValidName(prenom)) {
            afficherErreur("Erreur", "Prénom invalide (minimum 2 caractères)");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            afficherErreur("Erreur", "Email invalide");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            afficherErreur("Erreur", "Le mot de passe doit avoir au moins 6 caractères");
            return;
        }

        if (!ValidationUtil.passwordsMatch(password, confirmPassword)) {
            afficherErreur("Erreur", "Les mots de passe ne correspondent pas");
            return;
        }

        if (!telephone.isEmpty()) {
            if (!ValidationUtil.isValidGuineanPhone(telephone)) {
                afficherErreur("Erreur", ValidationUtil.getPhoneErrorMessage());
                return;
            }
        }

        try {
            boolean success = registrationService.register(nom, prenom, email, telephone, adresse, password);

            if (success) {
                afficherInfo("Succès", "Inscription réussie !\n\nVous pouvez maintenant vous connecter avec votre email et votre mot de passe.");
                
                MainApp.showLogin();
            } else {
                afficherErreur("Erreur", "L'inscription a échoué.\n\nCet email ou ce téléphone est peut-être déjà utilisé.");
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] Erreur lors de l'inscription");
            e.printStackTrace();
            afficherErreur("Erreur", "Une erreur est survenue lors de l'inscription:\n" + e.getMessage());
        }
    }

    @FXML
    public void handleBack() {
        System.out.println("[OK] Retour à l'écran de bienvenue");
        MainApp.showWelcome();
    }

    private void afficherErreur(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}