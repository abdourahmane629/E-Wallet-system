package com.ewallet.gui;

import com.ewallet.core.models.Utilisateur;
import com.ewallet.core.utils.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import javafx.scene.text.Font;

@SuppressWarnings("unused")
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("E-Wallet");
        primaryStage.setWidth(600);
        primaryStage.setHeight(500);

        Font.loadFont(getClass().getResourceAsStream("/com/ewallet/gui/fonts/Inter-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/com/ewallet/gui/fonts/Inter-Bold.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/com/ewallet/gui/fonts/Inter-Medium.ttf"), 12);
        showWelcome();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
     
   
    // ==================== MÉTHODES DE NAVIGATION ====================

    public static void showWelcome() {
        try {
            // Charger depuis le chemin absolu du fichier
            File fxmlFile = new File("resources/welcome.fxml");
            System.out.println("[DEBUG] Chemin FXML: " + fxmlFile.getAbsolutePath());
            System.out.println("[DEBUG] Fichier existe: " + fxmlFile.exists());
            
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("E-Wallet - Accueil");
            primaryStage.setScene(scene);
            primaryStage.setWidth(600);
            primaryStage.setHeight(500);
            primaryStage.show();
            System.out.println("[OK] Écran de bienvenue affiché");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger welcome.fxml:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void showLogin() {
        try {
            File fxmlFile = new File("resources/login.fxml");
            System.out.println("[DEBUG] Chemin FXML: " + fxmlFile.getAbsolutePath());
            
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("E-Wallet - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setWidth(500);
            primaryStage.setHeight(350);
            primaryStage.show();
            System.out.println("[OK] Écran de connexion affiché");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger login.fxml:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void showRegister() {
        try {
            File fxmlFile = new File("resources/register.fxml");
            System.out.println("[DEBUG] Chemin FXML: " + fxmlFile.getAbsolutePath());
            
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("E-Wallet - Inscription");
            primaryStage.setScene(scene);
            primaryStage.setWidth(500);
            primaryStage.setHeight(600);
            primaryStage.show();
            System.out.println("[OK] Écran d'inscription affiché");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger register.fxml:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void showAdminDashboard() {
        try {
            System.out.println("[NAV] Redirection vers dashboard ADMIN");
            File fxmlFile = new File("resources/admin_dashboard.fxml");
            System.out.println("[DEBUG] Chemin FXML: " + fxmlFile.getAbsolutePath());
            
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("E-Wallet - Admin Dashboard");
            primaryStage.setWidth(1000);
            primaryStage.setHeight(800);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le dashboard admin");
        }
    }

    public static void showAgentDashboard() {
        try {
            System.out.println("[NAV] Redirection vers dashboard AGENT");
            File fxmlFile = new File("resources/agent_dashboard.fxml");
            System.out.println("[DEBUG] Chemin FXML: " + fxmlFile.getAbsolutePath());
            
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("E-Wallet - Agent Dashboard");
            primaryStage.setWidth(1000);
            primaryStage.setHeight(800);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le dashboard agent");
        }
    }

    public static void showClientDashboard() {
        try {
            System.out.println("[NAV] Redirection vers dashboard CLIENT");
            File fxmlFile = new File("resources/client_dashboard.fxml");
            System.out.println("[DEBUG] Chemin FXML: " + fxmlFile.getAbsolutePath());
            
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("E-Wallet - Client Dashboard");
            primaryStage.setWidth(900);
            primaryStage.setHeight(700);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le dashboard client");
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private static void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        // Vérifier que les fichiers FXML existent
        String[] requiredFiles = {
            "resources/welcome.fxml",
            "resources/login.fxml",
            "resources/register.fxml",
            "resources/admin_dashboard.fxml",
            "resources/agent_dashboard.fxml",
            "resources/client_dashboard.fxml"
        };
        
        boolean allFilesExist = true;
        for (String file : requiredFiles) {
            if (!new File(file).exists()) {
                System.err.println("[ERREUR] Fichier manquant: " + file);
                System.err.println("        Chemin absolu: " + new File(file).getAbsolutePath());
                allFilesExist = false;
            }
        }
        
        if (!allFilesExist) {
            System.err.println("[ERREUR CRITIQUE] Des fichiers FXML sont manquants!");
            System.exit(1);
        }
        
        System.out.println("[INFO] Tous les fichiers FXML sont présents");
        launch(args);
    }
}