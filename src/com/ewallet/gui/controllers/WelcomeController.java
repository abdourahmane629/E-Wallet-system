package com.ewallet.gui.controllers;

import com.ewallet.gui.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class WelcomeController {

    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    public void handleLogin() {
        System.out.println("[OK] Affichage de la page de connexion");
        MainApp.showLogin();
    }

    @FXML
    public void handleRegister() {
        System.out.println("[OK] Affichage de la page d'inscription (CLIENT)");
        MainApp.showRegister();
    }
}