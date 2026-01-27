package com.ewallet.gui.controllers;

import com.ewallet.core.models.*;
import com.ewallet.core.dao.*;
import com.ewallet.core.services.WalletService;
import com.ewallet.core.services.AuthService;
import com.ewallet.core.services.TransactionService;
import com.ewallet.core.utils.SessionManager;
import com.ewallet.core.utils.NotificationUtil;
import com.ewallet.core.utils.SecurityUtil;
import com.ewallet.core.utils.ValidationUtil;
import com.ewallet.gui.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;
import com.ewallet.core.services.NotificationService;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class ClientDashboardController implements Initializable {
    
    // ==== DÉCLARATIONS DES COMPOSANTS FXML ====
    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private Label lastLoginLabel;
    @FXML private Label connectionStatus;
    @FXML private Button dashboardButton;
    @FXML private Button transferButton;
    @FXML private Button servicesButton;
    @FXML private Button historyButton;
    @FXML private Button notificationsButton;
    @FXML private Button profileButton;
    @FXML private Button changePasswordButton;
    @FXML private Button helpButton;
    @FXML private Button logoutButton;
    @FXML private Button quickProfileBtn;
    @FXML private VBox mainContent;
    @FXML private StackPane mainContentContainer;
    
    // Composants pour le tableau de bord
    private Label balanceLabelCenter;
    private Label dailyLimitLabel;
    private Label monthlySpentLabel;
    private GridPane quickActionsGrid;
    
    // Composants pour l'historique
    private TableView<Transaction> transactionsTable;
    private ObservableList<Transaction> transactionsList;
    private ComboBox<String> filterTypeCombo;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Label totalCreditsLabel;
    private Label totalDebitsLabel;
    private Label currentBalanceLabel;
    
    // Champs pour modification profil
    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private TextField telephoneField;
    private TextField adresseField;
    private PasswordField currentPasswordProfileField;
    private Label profileMessage;
    
    // Champs pour sécurité
    private PasswordField currentPasswordSecurityField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private PasswordField passwordForPinField;
    private PasswordField newPinField;
    private PasswordField confirmPinField;
    private Label securityMessage;
    
    // Champs pour transfert
    private TextField recipientEmailField;
    private TextField transferAmountField;
    private TextField transferDescriptionField;
    private PasswordField transferPinField;
    private Label transferMessage;
    private Label transferFeeLabel;
    
    // Composants pour les services
    private ComboBox<String> serviceTypeCombo;
    private TextField serviceReferenceField;
    private TextField serviceAmountField;
    private PasswordField servicePinField;
    private Label serviceMessage;
    private GridPane servicesGrid;
    private VBox paymentFormContainer; 
    
    // Composants pour les notifications
    private TableView<Notification> notificationsTable;
    private ObservableList<Notification> notificationsList;
    private Label unreadCountLabel;
    private ToggleButton filterUnreadToggle;
    
    // ==== VARIABLES D'INSTANCE ====
    private Utilisateur currentUser;
    private Portefeuille currentWallet;
    private WalletService walletService;
    private UtilisateurDAO utilisateurDAO;
    private AuthService authService;
    private TransactionDAO transactionDAO;
    private PortefeuilleDAO portefeuilleDAO;
    private TransactionService transactionService;
    private NotificationService notificationService; 
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    
    // Couleurs pour le thème
    private final String PRIMARY_COLOR = "#3281b3";
    private final String SECONDARY_COLOR = "#1f5a82";
    private final String SUCCESS_COLOR = "#4caf50";
    private final String WARNING_COLOR = "#ff9800";
    private final String ERROR_COLOR = "#f44336";
    private final String INFO_COLOR = "#2196f3";
    private final String TEXT_PRIMARY = "#263238";
    private final String TEXT_SECONDARY = "#546e7a";
    private final String BACKGROUND_LIGHT = "#f5f7fa";
    private final String BORDER_COLOR = "#e0e0e0";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[CLIENT] Dashboard professionnel initialisé");
        
        try {
            // Initialiser les services
            walletService = new WalletService();
            utilisateurDAO = new UtilisateurDAO();
            authService = new AuthService();
            transactionDAO = new TransactionDAO();
            portefeuilleDAO = new PortefeuilleDAO();
            transactionService = new TransactionService();
            
            try {
                this.notificationService = new NotificationService(com.ewallet.core.DatabaseConfig.getConnection());
            } catch (SQLException e) {
                System.err.println("[CLIENT] Erreur initialisation NotificationService: " + e.getMessage());
            }
            // Récupérer l'utilisateur courant
            currentUser = SessionManager.getCurrentUser();
            
            if (currentUser != null) {
                currentWallet = portefeuilleDAO.findByUtilisateurId(currentUser.getUtilisateurId());
                updateUI();
                showDashboard(); // Afficher le tableau de bord par défaut
                
                // Configurer les styles dynamiques
                setupDynamicStyles();
                updateConnectionStatus();
                
            } else {
                navigateToLogin();
            }
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur initialisation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le tableau de bord.");
        }
    }
    
    private void updateUI() {
        try {
            welcomeLabel.setText("Bonjour, " + currentUser.getPrenom() + " " + currentUser.getNom());
            updateBalanceHeader();
            
            // Dernière connexion
            if (currentUser.getLastLogin() != null) {
                lastLoginLabel.setText("Dernière connexion: " + 
                    currentUser.getLastLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } else {
                lastLoginLabel.setText("Dernière connexion: ");
            }
            
            // Mettre à jour le statut du PIN
            updatePinStatus();

            // Mettre à jour le badge de notifications
            updateNotificationBadge();
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur mise à jour UI: " + e.getMessage());
        }
    }
    
    private void updateBalanceHeader() {
        try {
            double solde = walletService.getSolde(currentUser.getId());
            String soldeStr = String.format("%,.0f", solde);
            balanceLabel.setText(soldeStr + " GNF");
            
            // Couleur selon le solde
            if (solde < 10000) {
                balanceLabel.setStyle("-fx-text-fill: " + WARNING_COLOR + "; -fx-font-size: 16px; -fx-font-weight: 800;");
            } else if (solde < 1000) {
                balanceLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 16px; -fx-font-weight: 800;");
            } else {
                balanceLabel.setStyle("-fx-text-fill: #ffd54f; -fx-font-size: 16px; -fx-font-weight: 800;");
            }
        } catch (Exception e) {
            balanceLabel.setText("Erreur de chargement");
            balanceLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 16px; -fx-font-weight: 800;");
            e.printStackTrace();
        }
    }
    
    private void updatePinStatus() {
        boolean hasPin = currentUser.getPinHash() != null && !currentUser.getPinHash().isEmpty();
        String pinStatus = hasPin ? "🔒 PIN configuré" : "⚠️ PIN non configuré";
        
        // Mettre à jour le tooltip du bouton de profil rapide
        String tooltipText = hasPin ? 
            "Compte sécurisé\nCliquez pour les options rapides" :
            "⚠️ Configurez votre PIN pour plus de sécurité";
        
        Tooltip.install(quickProfileBtn, new Tooltip(tooltipText));
    }
    
    private void setupDynamicStyles() {
        // Effet de survol pour les boutons du menu
        setupButtonHoverEffect(dashboardButton, "#e3f2fd");
        setupButtonHoverEffect(transferButton, "#f3e5f5");
        setupButtonHoverEffect(servicesButton, "#e8f5e8");
        setupButtonHoverEffect(historyButton, "#fff3e0");
        setupButtonHoverEffect(notificationsButton, "#fff8e1");
        setupButtonHoverEffect(profileButton, "#f1f8e9");
        setupButtonHoverEffect(changePasswordButton, "#fff3e0");
        setupButtonHoverEffect(helpButton, "#e0f7fa");
        
        // Bouton de déconnexion
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle(
            "-fx-background-color: #ffebee; -fx-text-fill: #d32f2f; -fx-font-size: 14px; " +
            "-fx-font-weight: 600; -fx-alignment: CENTER_LEFT; -fx-padding: 15 20; " +
            "-fx-background-radius: 8; -fx-border-color: #ffcdd2; -fx-border-width: 2; " +
            "-fx-border-radius: 8; -fx-cursor: hand;"
        ));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle(
            "-fx-background-color: #f5f5f5; -fx-text-fill: #d32f2f; -fx-font-size: 14px; " +
            "-fx-font-weight: 600; -fx-alignment: CENTER_LEFT; -fx-padding: 15 20; " +
            "-fx-background-radius: 8; -fx-border-color: #ffcdd2; -fx-border-width: 1; " +
            "-fx-border-radius: 8; -fx-cursor: hand;"
        ));
    }
    
    private void setupButtonHoverEffect(Button button, String hoverColor) {
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: " + hoverColor + "; -fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-font-size: 14px; -fx-font-weight: 600; -fx-alignment: CENTER_LEFT; " +
            "-fx-padding: 15 20; -fx-background-radius: 0; -fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #546e7a; " +
            "-fx-font-size: 14px; -fx-font-weight: 600; -fx-alignment: CENTER_LEFT; " +
            "-fx-padding: 15 20; -fx-background-radius: 0; -fx-cursor: hand;"
        ));
    }
    
    private void updateConnectionStatus() {
        connectionStatus.setText("🟢 En ligne");
        connectionStatus.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-size: 11px; -fx-font-weight: 600;");
    }
    
    // ==== GESTION DES SECTIONS ====
    
    @FXML
    private void handleDashboard() {
        clearMenuSelection();
        dashboardButton.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showDashboard();
    }
    
    @FXML
    private void handleTransfer() {
        clearMenuSelection();
        transferButton.setStyle("-fx-background-color: #f3e5f5; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showTransferInterface();
    }
    
    @FXML
    private void handleServices() {
        clearMenuSelection();
        servicesButton.setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showServicesInterface();
    }
    
    @FXML
    private void handleHistory() {
        clearMenuSelection();
        historyButton.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showHistoryInterface();
    }
    
    @FXML
    private void handleNotifications() {
        clearMenuSelection();
        notificationsButton.setStyle("-fx-background-color: #fff8e1; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showNotificationsInterface();
    }
    
    @FXML
    private void handleProfile() {
        clearMenuSelection();
        profileButton.setStyle("-fx-background-color: #f1f8e9; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showProfileInterface();
    }
    
    @FXML
    private void handleSecurity() {
        clearMenuSelection();
        changePasswordButton.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showSecurityInterface();
    }
    
    @FXML
    private void handleHelp() {
        clearMenuSelection();
        helpButton.setStyle("-fx-background-color: #e0f7fa; -fx-text-fill: " + PRIMARY_COLOR + ";");
        showHelpInterface();
    }
    
    @FXML
    private void handleLogout() {
        System.out.println("[CLIENT] Déconnexion demandée");
        SessionManager.clearSession();
        MainApp.showWelcome();
    }
    
    @FXML
    private void handleQuickProfile() {
        // Menu contextuel rapide
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem viewProfile = new MenuItem("👤 Voir mon profil");
        viewProfile.setOnAction(e -> showProfileInterface());
        
        MenuItem securitySettings = new MenuItem("🔒 Paramètres de sécurité");
        securitySettings.setOnAction(e -> showSecurityInterface());
        
        MenuItem notifications = new MenuItem("🔔 Notifications");
        notifications.setOnAction(e -> showNotificationsInterface());
        
        MenuItem logoutItem = new MenuItem("🚪 Déconnexion");
        logoutItem.setOnAction(e -> handleLogout());
        
        contextMenu.getItems().addAll(viewProfile, securitySettings, notifications, 
                                      new SeparatorMenuItem(), logoutItem);
        contextMenu.show(quickProfileBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }
    
    private void clearMenuSelection() {
        Button[] buttons = {dashboardButton, transferButton, servicesButton, historyButton, 
                           notificationsButton, profileButton, changePasswordButton, helpButton};
        for (Button btn : buttons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #546e7a; " +
                        "-fx-font-size: 14px; -fx-font-weight: 600; -fx-alignment: CENTER_LEFT; " +
                        "-fx-padding: 15 20; -fx-background-radius: 0;");
        }
    }
    
    // ==== SECTION TABLEAU DE BORD PROFESSIONNEL ====
    
    private void showDashboard() {
        mainContent.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox dashboardContent = new VBox(20);
        dashboardContent.setPadding(new Insets(20));
        dashboardContent.setStyle("-fx-background-color: " + BACKGROUND_LIGHT + ";");
        
        // En-tête du tableau de bord
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label title = new Label("Tableau de Bord Client");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        Label subtitle = new Label("Vue d'ensemble de votre compte");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: " + INFO_COLOR + "; -fx-text-fill: white; " +
                           "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6;");
        refreshBtn.setOnAction(e -> refreshDashboard());
        
        headerBox.getChildren().addAll(titleBox, spacer, refreshBtn);
        
        // Carte de solde
        VBox balanceCard = new VBox(10);
        balanceCard.setPadding(new Insets(20));
        balanceCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                           "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; " +
                           "-fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
        balanceCard.setPrefWidth(400);
        balanceCard.setAlignment(Pos.CENTER);
        
        Label balanceTitle = new Label("Votre Solde");
        balanceTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        
        balanceLabelCenter = new Label();
        balanceLabelCenter.setStyle("-fx-font-size: 36px; -fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
        
        Label currencyLabel = new Label("GNF");
        currencyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        
        // Mettre à jour le solde
        try {
            double solde = walletService.getSolde(currentUser.getId());
            balanceLabelCenter.setText(String.format("%,.0f", solde));
        } catch (Exception e) {
            balanceLabelCenter.setText("Erreur");
        }
        
        balanceCard.getChildren().addAll(balanceTitle, balanceLabelCenter, currencyLabel);
        
        // Actions rapides
        GridPane quickActions = new GridPane();
        quickActions.setHgap(20);
        quickActions.setVgap(20);
        quickActions.setAlignment(Pos.CENTER);
        
        // Transfert rapide
        VBox transferCard = createQuickActionCard("💸", "Transférer", 
            "Envoyer de l'argent à un autre client", INFO_COLOR);
        transferCard.setOnMouseClicked(e -> showTransferInterface());
        
        // Services rapides
        VBox servicesCard = createQuickActionCard("🏦", "Services", 
            "Payer factures et services", "#9c27b0");
        servicesCard.setOnMouseClicked(e -> showServicesInterface());
        
        // Historique rapide
        VBox historyCard = createQuickActionCard("📜", "Historique", 
            "Voir toutes vos transactions", WARNING_COLOR);
        historyCard.setOnMouseClicked(e -> showHistoryInterface());
        
        // Profil rapide
        VBox profileCard = createQuickActionCard("👤", "Mon Profil", 
            "Modifier vos informations personnelles", SUCCESS_COLOR);
        profileCard.setOnMouseClicked(e -> showProfileInterface());
        
        // Sécurité rapide
        VBox securityCard = createQuickActionCard("🔐", "Sécurité", 
            "Changer mot de passe et PIN", ERROR_COLOR);
        securityCard.setOnMouseClicked(e -> showSecurityInterface());
        
        // Notifications rapides
        VBox notificationsCard = createQuickActionCard("🔔", "Notifications", 
            "Voir vos alertes et messages", "#ff9800");
        notificationsCard.setOnMouseClicked(e -> showNotificationsInterface());
        
        quickActions.add(transferCard, 0, 0);
        quickActions.add(servicesCard, 1, 0);
        quickActions.add(historyCard, 0, 1);
        quickActions.add(profileCard, 1, 1);
        quickActions.add(securityCard, 0, 2);
        quickActions.add(notificationsCard, 1, 2);
        
        dashboardContent.getChildren().addAll(headerBox, balanceCard, quickActions);
        scrollPane.setContent(dashboardContent);
        mainContent.getChildren().add(scrollPane);
    }
    
    private VBox createQuickActionCard(String icon, String title, String description, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 10; " +
                     "-fx-padding: 20; -fx-cursor: hand;");
        card.setPrefSize(180, 150);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descLabel.setMaxWidth(160);
        
        card.getChildren().addAll(iconLabel, titleLabel, descLabel);
        
        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + color + "15; -fx-border-color: " + color + "; -fx-border-width: 2; " +
            "-fx-border-radius: 10; -fx-padding: 20; -fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; " +
            "-fx-border-radius: 10; -fx-padding: 20; -fx-cursor: hand;"
        ));
        
        return card;
    }
    
    private void refreshDashboard() {
        currentWallet = portefeuilleDAO.findByUtilisateurId(currentUser.getUtilisateurId());
        updateBalanceHeader();
        showDashboard(); // Recharger
    }
    
    // ==== SECTION TRANSFERT (Votre code existant adapté) ====
    
    private void showTransferInterface() {
        mainContent.getChildren().clear();
        
        VBox transferContent = new VBox(15);
        transferContent.setPadding(new Insets(20));
        transferContent.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-border-color: " + BORDER_COLOR + ";");
        transferContent.setMaxWidth(500);
        
        Label title = new Label("💸 Effectuer un Transfert");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        // Formulaire de transfert
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        
        // Destinataire
        Label recipientLabel = new Label("Destinataire (Email):");
        recipientLabel.setPrefWidth(150);
        recipientEmailField = new TextField();
        recipientEmailField.setPromptText("email@exemple.com");
        recipientEmailField.setPrefWidth(300);
        recipientEmailField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        // Montant
        Label amountLabel = new Label("Montant (GNF):");
        transferAmountField = new TextField();
        transferAmountField.setPromptText("Ex: 50000");
        transferAmountField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        // Description
        Label descLabel = new Label("Description:");
        transferDescriptionField = new TextField();
        transferDescriptionField.setPromptText("Motif du transfert");
        transferDescriptionField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        // PIN
        Label pinLabel = new Label("Votre PIN (4 chiffres):");
        transferPinField = new PasswordField();
        transferPinField.setPromptText("Obligatoire pour le transfert");
        transferPinField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        // Bouton
        Button transferBtn = new Button("Effectuer le Transfert");
        transferBtn.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30;");
        transferBtn.setOnAction(e -> handleTransferMoney());
        
        // Message
        transferMessage = new Label();
        transferMessage.setStyle("-fx-font-size: 14px;");
        
        // Ajouter au formulaire
        form.add(recipientLabel, 0, 0);
        form.add(recipientEmailField, 1, 0);
        form.add(amountLabel, 0, 1);
        form.add(transferAmountField, 1, 1);
        form.add(descLabel, 0, 2);
        form.add(transferDescriptionField, 1, 2);
        form.add(pinLabel, 0, 3);
        form.add(transferPinField, 1, 3);
        form.add(transferBtn, 0, 4, 2, 1);
        form.add(transferMessage, 0, 5, 2, 1);
        
        // Information sur le solde
        try {
            double solde = walletService.getSolde(currentUser.getId());
            Label balanceInfo = new Label("💡 Votre solde disponible: " + String.format("%,.0f", solde) + " GNF");
            balanceInfo.setStyle("-fx-text-fill: " + INFO_COLOR + "; -fx-font-weight: bold; -fx-padding: 10 0;");
            transferContent.getChildren().addAll(title, balanceInfo, form);
        } catch (Exception e) {
            transferContent.getChildren().addAll(title, form);
        }
        
        mainContent.getChildren().add(transferContent);
    }
    
    private void handleTransferMoney() {
        System.out.println("[CLIENT] Transfert d'argent initié");
        
        String recipientEmail = recipientEmailField.getText().trim();
        String amountStr = transferAmountField.getText().trim();
        String description = transferDescriptionField.getText().trim();
        String pin = transferPinField.getText().trim();
        
        // Validation basique
        if (recipientEmail.isEmpty() || amountStr.isEmpty() || pin.isEmpty()) {
            showTransferError("Tous les champs sont obligatoires.");
            return;
        }
        
        if (!ValidationUtil.isValidEmail(recipientEmail)) {
            showTransferError("Email du destinataire invalide.");
            return;
        }
        
        if (recipientEmail.equalsIgnoreCase(currentUser.getEmail())) {
            showTransferError("Vous ne pouvez pas vous transférer de l'argent à vous-même.");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showTransferError("Le montant doit être supérieur à 0.");
                return;
            }
        
            // Vérifier la limite de transfert (500,000 GNF)
            if (amount > 500000) {
                showTransferError("Le montant maximum par transfert est de 500 000 GNF.");
                return;
            }
            
            // Vérifier le minimum de transfert (100 GNF)
            if (amount < 100) {
                showTransferError("Le montant minimum par transfert est de 100 GNF.");
                return;
            }
        } catch (NumberFormatException e) {
            showTransferError("Montant invalide. Entrez un nombre valide.");
            return;
        }
        
        if (!pin.matches("\\d{4}")) {
            showTransferError("Le PIN doit contenir exactement 4 chiffres.");
            return;
        }
        
        // Vérifier le PIN
        if (currentUser.getPinHash() == null || currentUser.getPinHash().isEmpty()) {
            showTransferError("Vous n'avez pas configuré de PIN. Veuillez le configurer dans la section Sécurité.");
            return;
        }
        
        if (!SecurityUtil.verifyPin(pin, currentUser.getPinHash())) {
            showTransferError("PIN incorrect. Veuillez réessayer.");
            return;
        }
        
        // Vérifier le solde avec commission incluse (1%)
        double commission = amount * 0.01;
        double totalDebit = amount;
        
        try {
            double solde = walletService.getSolde(currentUser.getUtilisateurId());
            if (solde < totalDebit) {
                showTransferError("Solde insuffisant. Solde disponible: " + 
                                String.format("%,.0f", solde) + " GNF\n" +
                                "Montant requis: " + String.format("%,.0f", totalDebit) + " GNF (inclut commission)");
                return;
            }
        } catch (Exception e) {
            showTransferError("Erreur lors de la vérification du solde.");
            return;
        }
        
        // Effectuer le transfert
        try {
            boolean success = walletService.transfer(
                currentUser.getUtilisateurId(),
                recipientEmail,
                amount,
                pin,
                description
            );
            
            if (success) {
                transferMessage.setText("✅ Transfert effectué avec succès !\n" +
                                      "• Montant envoyé: " + String.format("%,.0f", amount) + " GNF\n" +
                                      "• Commission (1%): " + String.format("%,.0f", commission) + " GNF\n" +
                                      "• Montant reçu: " + String.format("%,.0f", amount - commission) + " GNF");
                transferMessage.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                
                // Mettre à jour le solde affiché
                updateBalanceHeader();
                if (balanceLabelCenter != null) {
                    double newSolde = walletService.getSolde(currentUser.getUtilisateurId());
                    balanceLabelCenter.setText(String.format("%,.0f", newSolde));
                }
                
                // Réinitialiser les champs
                recipientEmailField.clear();
                transferAmountField.clear();
                transferDescriptionField.clear();
                transferPinField.clear();
                
                // Afficher une notification
                showAlert("Transfert réussi", 
                         "Transfert effectué avec succès !\n\n" +
                         "Destinataire: " + recipientEmail + "\n" +
                         "Montant envoyé: " + String.format("%,.0f", amount) + " GNF\n" +
                         "Commission: " + String.format("%,.0f", commission) + " GNF\n" +
                         "Montant reçu: " + String.format("%,.0f", amount - commission) + " GNF\n\n" +
                         "Le solde a été mis à jour.");
                
            } else {
                showTransferError("Échec du transfert. Veuillez vérifier:\n" +
                                 "- L'email du destinataire existe\n" +
                                 "- Le destinataire est bien un client\n" +
                                 "- Votre solde est suffisant\n" +
                                 "- Vous n'avez pas dépassé les limites");
            }
        } catch (Exception e) {
            showTransferError("Erreur technique: " + e.getMessage());
            System.err.println("[CLIENT] Erreur transfert: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showTransferError(String message) {
        transferMessage.setText("❌ " + message);
        transferMessage.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
    }
    
    // ==== SECTION SERVICES (Nouveau) ====
    
    private void showServicesInterface() {
        mainContent.getChildren().clear();
        
        // Créer un ScrollPane pour le contenu
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox servicesContent = new VBox(20);
        servicesContent.setPadding(new Insets(30));
        servicesContent.setAlignment(Pos.TOP_CENTER);
        servicesContent.setStyle("-fx-background-color: " + BACKGROUND_LIGHT + ";");
        
        // En-tête
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🏦 Paiement de Services");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button backBtn = new Button("← Retour au Dashboard");
        backBtn.setStyle("-fx-background-color: " + INFO_COLOR + "; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        backBtn.setOnAction(e -> showDashboard());
        
        headerBox.getChildren().addAll(title, spacer, backBtn);
        
        // Information solde
        HBox balanceBox = new HBox(10);
        balanceBox.setAlignment(Pos.CENTER_LEFT);
        balanceBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15 20;");
        
        try {
            double solde = walletService.getSolde(currentUser.getId());
            Label balanceLabel = new Label("💰 Solde disponible: ");
            balanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_SECONDARY + ";");
            
            Label balanceValue = new Label(String.format("%,.0f GNF", solde));
            balanceValue.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: " + 
                                (solde < 10000 ? WARNING_COLOR : SUCCESS_COLOR) + ";");
            
            balanceBox.getChildren().addAll(balanceLabel, balanceValue);
        } catch (Exception e) {
            balanceBox.getChildren().add(new Label("Erreur de chargement du solde"));
        }
        
        // Titre services
        Label servicesTitle = new Label("Sélectionnez un service à payer :");
        servicesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-padding: 0 0 10 0;");
        
        // Grille des services (2 colonnes seulement pour plus de simplicité)
        GridPane servicesGrid = new GridPane();
        servicesGrid.setHgap(20);
        servicesGrid.setVgap(20);
        servicesGrid.setPadding(new Insets(20, 0, 30, 0));
        servicesGrid.setAlignment(Pos.CENTER);
        
        // Services simplifiés (4 principaux)
        String[][] services = {
            {"⚡ Électricité (EDG)", "#ff9800", "Paiement facture EDG"},
            {"💧 Eau (SEG)", "#2196f3", "Paiement facture SEG"},
            {"📱 Orange Money", "#ff5722", "Recharge téléphonique"},
            {"📱 MTN Mobile Money", "#ffc107", "Recharge téléphonique"}
        };
        
        // Création des cartes
        for (int i = 0; i < services.length; i++) {
            final int index = i;
            VBox serviceCard = new VBox(15);
            serviceCard.setPadding(new Insets(20));
            serviceCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                            "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; " +
                            "-fx-border-radius: 12; -fx-cursor: hand;");
            serviceCard.setPrefSize(280, 160);
            serviceCard.setAlignment(Pos.CENTER);
            
            Label iconLabel = new Label(services[index][0].split(" ")[0]);
            iconLabel.setStyle("-fx-font-size: 24px;");
            
            Label titleLabel = new Label(services[index][0]);
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: " + TEXT_PRIMARY + ";");
            
            Label descLabel = new Label(services[index][2]);
            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_SECONDARY + ";");
            descLabel.setWrapText(true);
            descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            descLabel.setMaxWidth(240);
            
            Button payButton = new Button("Payer ce service");
            payButton.setStyle("-fx-background-color: " + services[index][1] + "; -fx-text-fill: white; " +
                            "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
            
            final String serviceName = services[index][0];
            payButton.setOnAction(e -> showPaymentForm(serviceName));
            
            // Effet hover
            final String color = services[index][1];
            serviceCard.setOnMouseEntered(mouseEvent -> serviceCard.setStyle(
                "-fx-background-color: " + color + "15; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 2; " +
                "-fx-border-radius: 12; -fx-cursor: hand;"
            ));
            serviceCard.setOnMouseExited(mouseEvent -> serviceCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; " +
                "-fx-border-radius: 12; -fx-cursor: hand;"
            ));
            
            serviceCard.getChildren().addAll(iconLabel, titleLabel, descLabel, payButton);
            
            // Ajout à la grille
            servicesGrid.add(serviceCard, index % 2, index / 2);
        }
        
        servicesContent.getChildren().addAll(headerBox, balanceBox, servicesTitle, servicesGrid);
        scrollPane.setContent(servicesContent);
        mainContent.getChildren().add(scrollPane);
        
        System.out.println("[CLIENT] Interface services affichée");
    }
    
    private VBox createServiceCard(String title, String color, String description) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                     "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; " +
                     "-fx-border-radius: 12; -fx-cursor: hand;");
        card.setPrefSize(250, 150);
        card.setAlignment(Pos.CENTER);
        
        // Icone
        Label iconLabel = new Label(title.split(" ")[0]);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        // Titre
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        // Description
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descLabel.setMaxWidth(220);
        
        // Bouton
        Button payButton = new Button("Payer");
        payButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                          "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6;");
        payButton.setOnAction(e -> showPaymentForm(title));
        
        card.getChildren().addAll(iconLabel, titleLabel, descLabel, payButton);
        
        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + color + "15; -fx-background-radius: 12; " +
            "-fx-border-color: " + color + "; -fx-border-width: 2; " +
            "-fx-border-radius: 12; -fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12; " +
            "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; " +
            "-fx-border-radius: 12; -fx-cursor: hand;"
        ));
        
        return card;
    }
    
    private VBox createPaymentForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; " +
                    "-fx-border-radius: 12;");
        form.setMaxWidth(500);
        form.setAlignment(Pos.TOP_CENTER);
        
        Label formTitle = new Label("Formulaire de Paiement");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        
        // Type de service
        Label serviceLabel = new Label("Service");
        serviceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + TEXT_PRIMARY + ";");
        serviceTypeCombo = new ComboBox<>();
        serviceTypeCombo.getItems().addAll(
            "Électricité (EDG)",
            "Eau (SEG)",
            "Orange Money",
            "MTN Mobile Money",
            "Internet",
            "TV Satellite",
            "Université",
            "Santé"
        );
        serviceTypeCombo.setPrefWidth(300);
        
        // Référence
        Label refLabel = new Label("Référence/N° Compte");
        refLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + TEXT_PRIMARY + ";");
        serviceReferenceField = new TextField();
        serviceReferenceField.setPromptText("Ex: 1234567890");
        serviceReferenceField.setPrefWidth(300);
        
        // Montant
        Label amountLabel = new Label("Montant (GNF)");
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + TEXT_PRIMARY + ";");
        serviceAmountField = new TextField();
        serviceAmountField.setPromptText("Ex: 75000");
        serviceAmountField.setPrefWidth(300);
        
        // PIN
        Label pinLabel = new Label("Code PIN");
        pinLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + TEXT_PRIMARY + ";");
        servicePinField = new PasswordField();
        servicePinField.setPromptText("4 chiffres");
        servicePinField.setPrefWidth(300);
        
        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button payButton = new Button("Payer maintenant");
        payButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                          "-fx-font-size: 16px; -fx-font-weight: 700; -fx-padding: 12 30; " +
                          "-fx-background-radius: 8; -fx-cursor: hand;");
        payButton.setOnAction(e -> handlePayService());
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: " + ERROR_COLOR + "; -fx-text-fill: white; " +
                            "-fx-font-weight: 600; -fx-padding: 12 30; " +
                            "-fx-background-radius: 8; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            // Cacher le formulaire
            if (paymentFormContainer != null) {
                paymentFormContainer.setVisible(false);
                paymentFormContainer.setManaged(false);
                paymentFormContainer.getChildren().clear();
            }
        });
        
        buttonBox.getChildren().addAll(payButton, cancelButton);
        
        // Message
        serviceMessage = new Label();
        serviceMessage.setStyle("-fx-font-size: 14px;");
        serviceMessage.setWrapText(true);
        
        // Ajout au grid
        grid.add(serviceLabel, 0, 0);
        grid.add(serviceTypeCombo, 1, 0);
        grid.add(refLabel, 0, 1);
        grid.add(serviceReferenceField, 1, 1);
        grid.add(amountLabel, 0, 2);
        grid.add(serviceAmountField, 1, 2);
        grid.add(pinLabel, 0, 3);
        grid.add(servicePinField, 1, 3);
        grid.add(buttonBox, 0, 4, 2, 1);
        grid.add(serviceMessage, 0, 5, 2, 1);
        
        form.getChildren().addAll(formTitle, grid);
        return form;
    }
    
    private void showPaymentForm(String serviceName) {
        System.out.println("[CLIENT] Affichage formulaire simplifié pour: " + serviceName);
        
        // Créer un nouveau conteneur pour le formulaire
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(30));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                            "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 2; " +
                            "-fx-border-radius: 12;");
        formContainer.setMaxWidth(500);
        formContainer.setAlignment(Pos.TOP_CENTER);
        
        Label formTitle = new Label("Formulaire de Paiement");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        
        // Service (lecture seule)
        Label serviceLabel = new Label("Service:");
        serviceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        
        TextField serviceField = new TextField(serviceName);
        serviceField.setEditable(false);
        serviceField.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 8;");
        serviceField.setPrefWidth(300);
        
        // Référence
        Label refLabel = new Label("Référence/N° Compte:");
        refLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        
        TextField referenceField = new TextField();
        referenceField.setPromptText("Ex: 1234567890");
        referenceField.setPrefWidth(300);
        
        // Montant
        Label amountLabel = new Label("Montant (GNF):");
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Ex: 75000");
        amountField.setPrefWidth(300);
        
        // PIN
        Label pinLabel = new Label("Code PIN (4 chiffres):");
        pinLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("Entrez votre PIN");
        pinField.setPrefWidth(300);
        
        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button payButton = new Button("Payer maintenant");
        payButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-font-weight: 700; -fx-padding: 12 30; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: " + ERROR_COLOR + "; -fx-text-fill: white; " +
                            "-fx-font-weight: 600; -fx-padding: 12 30; " +
                            "-fx-background-radius: 8; -fx-cursor: hand;");
        
        // Message
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 14px;");
        messageLabel.setWrapText(true);
        
        // Actions des boutons
        payButton.setOnAction(e -> {
            String reference = referenceField.getText().trim();
            String amountStr = amountField.getText().trim();
            String pin = pinField.getText().trim();
            
            // Validation
            if (reference.isEmpty()) {
                messageLabel.setText("❌ La référence est obligatoire.");
                messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                return;
            }
            
            if (amountStr.isEmpty() || pin.isEmpty()) {
                messageLabel.setText("❌ Tous les champs sont obligatoires.");
                messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    messageLabel.setText("❌ Le montant doit être > 0.");
                    messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                    return;
                }
                
                if (amount > 1000000) {
                    messageLabel.setText("❌ Montant maximum: 1,000,000 GNF.");
                    messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                    return;
                }
            } catch (NumberFormatException ex) {
                messageLabel.setText("❌ Montant invalide.");
                messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                return;
            }
            
            if (!pin.matches("\\d{4}")) {
                messageLabel.setText("❌ PIN doit avoir 4 chiffres.");
                messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                return;
            }
            
            // Vérifier le solde
            try {
                double solde = walletService.getSolde(currentUser.getUtilisateurId());
                if (solde < amount) {
                    messageLabel.setText("❌ Solde insuffisant. Disponible: " + 
                                    String.format("%,.0f", solde) + " GNF");
                    messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                    return;
                }
            } catch (Exception ex) {
                messageLabel.setText("❌ Erreur vérification solde.");
                messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                return;
            }
            
            // Appel au service
            try {
                boolean success = transactionService.effectuerPaiementService(
                    currentUser.getUtilisateurId(),
                    amount,
                    serviceName,
                    reference,
                    pin
                );
                
                if (success) {
                    messageLabel.setText("✅ Paiement réussi !\n" +
                                    "Service: " + serviceName + "\n" +
                                    "Référence: " + reference + "\n" +
                                    "Montant: " + String.format("%,.0f", amount) + " GNF");
                    messageLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                    
                    // Mettre à jour le solde
                    updateBalanceHeader();
                    
                    // Effacer les champs sauf le message
                    referenceField.clear();
                    amountField.clear();
                    pinField.clear();
                    
                    // Fermer le formulaire après 3 secondes
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            javafx.application.Platform.runLater(() -> {
                                showServicesInterface(); // Retour à la liste des services
                            });
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }).start();
                    
                } else {
                    messageLabel.setText("❌ Échec du paiement. Vérifiez vos informations.");
                    messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                }
                
            } catch (Exception ex) {
                messageLabel.setText("❌ Erreur technique: " + ex.getMessage());
                messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                System.err.println("[CLIENT] Erreur paiement: " + ex.getMessage());
            }
        });
        
        cancelButton.setOnAction(e -> showServicesInterface());
        
        buttonBox.getChildren().addAll(payButton, cancelButton);
        
        // Ajout au grid
        grid.add(serviceLabel, 0, 0);
        grid.add(serviceField, 1, 0);
        grid.add(refLabel, 0, 1);
        grid.add(referenceField, 1, 1);
        grid.add(amountLabel, 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(pinLabel, 0, 3);
        grid.add(pinField, 1, 3);
        grid.add(buttonBox, 0, 4, 2, 1);
        grid.add(messageLabel, 0, 5, 2, 1);
        
        formContainer.getChildren().addAll(formTitle, grid);
        
        // Remplacer le contenu principal par le formulaire
        mainContent.getChildren().clear();
        mainContent.getChildren().add(formContainer);
        
        // Focus sur le champ référence
        referenceField.requestFocus();
    }
    private void handlePayService() {
        String serviceType = serviceTypeCombo.getValue();
        String reference = serviceReferenceField.getText().trim();
        String amountStr = serviceAmountField.getText().trim();
        String pin = servicePinField.getText().trim();
        
        // Validation
        if (serviceType == null || serviceType.isEmpty()) {
            showServiceError("Veuillez sélectionner un service.");
            return;
        }
        
        if (reference.isEmpty()) {
            showServiceError("La référence compte est obligatoire.");
            return;
        }
        
        if (amountStr.isEmpty() || pin.isEmpty()) {
            showServiceError("Tous les champs sont obligatoires.");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showServiceError("Le montant doit être supérieur à 0.");
                return;
            }
            
            if (amount > 1000000) {
                showServiceError("Le montant maximum par paiement est de 1,000,000 GNF.");
                return;
            }
        } catch (NumberFormatException e) {
            showServiceError("Montant invalide. Entrez un nombre valide.");
            return;
        }
        
        if (!pin.matches("\\d{4}")) {
            showServiceError("Le PIN doit contenir exactement 4 chiffres.");
            return;
        }
        
        // Vérifier le PIN
        if (currentUser.getPinHash() == null || currentUser.getPinHash().isEmpty()) {
            showServiceError("Vous n'avez pas configuré de PIN.");
            return;
        }
        
        if (!SecurityUtil.verifyPin(pin, currentUser.getPinHash())) {
            showServiceError("PIN incorrect.");
            return;
        }
        
        // Vérifier le solde
        try {
            double solde = walletService.getSolde(currentUser.getUtilisateurId());
            if (solde < amount) {
                showServiceError("Solde insuffisant. Solde disponible: " + 
                            String.format("%,.0f", solde) + " GNF");
                return;
            }
        } catch (Exception e) {
            showServiceError("Erreur vérification solde.");
            return;
        }
        
        // Appel au service réel
        try {
            boolean success = transactionService.effectuerPaiementService(
                currentUser.getUtilisateurId(),
                amount,
                serviceType,
                reference,
                pin
            );
            
            if (success) {
                serviceMessage.setText("✅ Paiement effectué avec succès !\n" +
                                    "• Service: " + serviceType + "\n" +
                                    "• Référence: " + reference + "\n" +
                                    "• Montant: " + String.format("%,.0f", amount) + " GNF\n" +
                                    "• Date: " + LocalDateTime.now().format(dateFormatter));
                serviceMessage.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                
                // Mettre à jour le solde dans l'interface
                updateBalanceHeader();
                
                // Réinitialiser les champs
                serviceReferenceField.clear();
                serviceAmountField.clear();
                servicePinField.clear();
                
                // Afficher une notification
                showAlert("Paiement réussi", 
                        "Votre paiement a été effectué avec succès.\n\n" +
                        "Service: " + serviceType + "\n" +
                        "Référence: " + reference + "\n" +
                        "Montant: " + String.format("%,.0f", amount) + " GNF\n\n" +
                        "Vous recevrez une confirmation par SMS.");
                
            } else {
                showServiceError("Échec du paiement. Veuillez réessayer.");
            }
            
        } catch (Exception e) {
            showServiceError("Erreur technique: " + e.getMessage());
            System.err.println("[CLIENT] Erreur paiement service: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showServiceError(String message) {
        serviceMessage.setText("❌ " + message);
        serviceMessage.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
    }
    
    
    
    // ==== SECTION HISTORIQUE (Votre code existant adapté) ====
    
    @SuppressWarnings("unchecked")
    private void showHistoryInterface() {
        mainContent.getChildren().clear();
        
        VBox historyContent = new VBox(15);
        historyContent.setPadding(new Insets(20));
        
        Label title = new Label("📊 Historique des Transactions");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        // Filtres
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        
        filterTypeCombo = new ComboBox<>();
        filterTypeCombo.getItems().addAll("TOUS", "DÉPÔT", "RETRAIT", "TRANSFERT", "SERVICE");
        filterTypeCombo.setValue("TOUS");
        filterTypeCombo.setPromptText("Type de transaction");
        filterTypeCombo.setPrefWidth(150);
        
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Date début");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Date fin");
        endDatePicker.setValue(LocalDate.now());
        
        Button filterButton = new Button("Filtrer");
        filterButton.setOnAction(e -> loadTransactions());
        
        Button refreshButton = new Button("Rafraîchir");
        refreshButton.setOnAction(e -> {
            filterTypeCombo.setValue("TOUS");
            startDatePicker.setValue(LocalDate.now().minusMonths(1));
            endDatePicker.setValue(LocalDate.now());
            loadTransactions();
        });
        
        filters.getChildren().addAll(
            new Label("Filtres:"), filterTypeCombo, 
            new Label("Du:"), startDatePicker, 
            new Label("Au:"), endDatePicker, 
            filterButton, refreshButton
        );
        
        // Tableau des transactions
        transactionsTable = new TableView<>();
        transactionsTable.setPrefHeight(300);
        
        // Colonne Date
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateTransaction();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Colonne Type
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        typeCol.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType();
            String displayType = "Autre";
            if ("DEPOT".equalsIgnoreCase(type) || "CREDIT".equalsIgnoreCase(type)) {
                displayType = "Dépôt";
            } else if ("RETRAIT".equalsIgnoreCase(type) || "DEBIT".equalsIgnoreCase(type)) {
                displayType = "Retrait";
            } else if ("TRANSFERT".equalsIgnoreCase(type)) {
                displayType = "Transfert";
            } else if ("PAIEMENT_SERVICE".equalsIgnoreCase(type)) {
                displayType = "Service";
            }
            return new javafx.beans.property.SimpleStringProperty(displayType);
        });
        
        // Colonne Montant
        TableColumn<Transaction, String> amountCol = new TableColumn<>("Montant (GNF)");
        amountCol.setPrefWidth(120);
        amountCol.setCellValueFactory(cellData -> {
            double montant = cellData.getValue().getMontant();
            String signe = montant >= 0 ? "+" : "";
            return new javafx.beans.property.SimpleStringProperty(
                signe + String.format("%,.0f", Math.abs(montant))
            );
        });
        amountCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction.getMontant() >= 0) {
                        setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Colonne Description
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(250);
        descCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription())
        );
        
        // Colonne Statut
        TableColumn<Transaction, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut())
        );
        
        transactionsTable.getColumns().addAll(dateCol, typeCol, amountCol, descCol, statusCol);
        
        // Résumé
        HBox summary = new HBox(20);
        summary.setAlignment(Pos.CENTER);
        
        totalCreditsLabel = new Label("Total crédits: 0 GNF");
        totalCreditsLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
        
        totalDebitsLabel = new Label("Total débits: 0 GNF");
        totalDebitsLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-weight: bold;");
        
        currentBalanceLabel = new Label("Solde actuel: 0 GNF");
        currentBalanceLabel.setStyle("-fx-text-fill: " + INFO_COLOR + "; -fx-font-weight: bold;");
        
        summary.getChildren().addAll(totalCreditsLabel, totalDebitsLabel, currentBalanceLabel);
        
        historyContent.getChildren().addAll(title, filters, transactionsTable, summary);
        mainContent.getChildren().add(historyContent);
        
        // Charger les transactions
        loadTransactions();
    }
    
    private void loadTransactions() {
        try {
            // Récupérer le portefeuille
            Portefeuille portefeuille = portefeuilleDAO.findByUtilisateurId(currentUser.getUtilisateurId());
            if (portefeuille == null) {
                showAlert("Information", "Aucun portefeuille trouvé.");
                return;
            }
            
            // Récupérer les transactions
            List<Transaction> allTransactions = transactionDAO.findByPortefeuilleId(portefeuille.getId());
            
            // Appliquer les filtres
            List<Transaction> filtered = new ArrayList<>();
            String selectedType = filterTypeCombo.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            for (Transaction t : allTransactions) {
                // Filtrer par type
                boolean typeMatch = true;
                if (!"TOUS".equals(selectedType)) {
                    String type = t.getType();
                    switch (selectedType) {
                        case "DÉPÔT":
                            typeMatch = "DEPOT".equalsIgnoreCase(type) || "CREDIT".equalsIgnoreCase(type);
                            break;
                        case "RETRAIT":
                            typeMatch = "RETRAIT".equalsIgnoreCase(type) || "DEBIT".equalsIgnoreCase(type);
                            break;
                        case "TRANSFERT":
                            typeMatch = "TRANSFERT".equalsIgnoreCase(type);
                            break;
                        case "SERVICE":
                            typeMatch = "PAIEMENT_SERVICE".equalsIgnoreCase(type);
                            break;
                    }
                }
                
                // Filtrer par date
                boolean dateMatch = true;
                if (startDate != null && endDate != null) {
                    LocalDate transactionDate = t.getDateTransaction().toLocalDate();
                    dateMatch = !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
                }
                
                if (typeMatch && dateMatch) {
                    filtered.add(t);
                }
            }
            
            // Mettre à jour la table
            transactionsList = FXCollections.observableArrayList(filtered);
            transactionsTable.setItems(transactionsList);
            
            // Calculer les totaux
            double totalCredits = 0;
            double totalDebits = 0;
            
            for (Transaction t : filtered) {
                if (t.getMontant() >= 0) {
                    totalCredits += t.getMontant();
                } else {
                    totalDebits += Math.abs(t.getMontant());
                }
            }
            
            totalCreditsLabel.setText(String.format("Total crédits: %,.0f GNF", totalCredits));
            totalDebitsLabel.setText(String.format("Total débits: %,.0f GNF", totalDebits));
            currentBalanceLabel.setText(String.format("Solde actuel: %,.0f GNF", portefeuille.getSolde()));
            
            System.out.println("[CLIENT] " + filtered.size() + " transactions chargées");
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur chargement transactions: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'historique: " + e.getMessage());
        }
    }
    
    // ==== SECTION NOTIFICATIONS (Nouveau) ====
    
        private void showNotificationsInterface() {
        mainContent.getChildren().clear();
        
        VBox notificationsContent = new VBox(15);
        notificationsContent.setPadding(new Insets(20));
        notificationsContent.setStyle("-fx-background-color: " + BACKGROUND_LIGHT + ";");
        
        Label title = new Label("🔔 Mes Notifications");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        // Boutons d'action
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button markAllReadButton = new Button("📌 Tout marquer comme lu");
        markAllReadButton.setStyle("-fx-background-color: " + INFO_COLOR + "; -fx-text-fill: white; " +
                                  "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        markAllReadButton.setOnAction(e -> markAllNotificationsAsRead());
        
        Button refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white; " +
                              "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> loadRealNotificationsData());
        
        actionButtons.getChildren().addAll(markAllReadButton, refreshButton);
        
        // Tableau des notifications
        notificationsTable = new TableView<>();
        notificationsTable.setPrefHeight(400);
        notificationsTable.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER_COLOR + ";");
        
        // Colonne Statut (icône)
        TableColumn<Notification, String> statusCol = new TableColumn<>("");
        statusCol.setPrefWidth(40);
        statusCol.setCellValueFactory(cellData -> {
            boolean isRead = cellData.getValue().isEstLue();
            return new javafx.beans.property.SimpleStringProperty(isRead ? "📌" : "🆕");
        });
        statusCol.setCellFactory(col -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("🆕")) {
                        setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 16px;");
                    } else {
                        setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 16px;");
                    }
                }
            }
        });
        
        // Colonne Titre
        TableColumn<Notification, String> titleCol = new TableColumn<>("Titre");
        titleCol.setPrefWidth(200);
        titleCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitre())
        );
        
        // Colonne Message
        TableColumn<Notification, String> messageCol = new TableColumn<>("Message");
        messageCol.setPrefWidth(350);
        messageCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMessage())
        );
        
        // Colonne Date
        TableColumn<Notification, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateCreation();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    NotificationUtil.formatDateTime(date)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Colonne Type
        TableColumn<Notification, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        typeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType())
        );
        typeCol.setCellFactory(col -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = NotificationUtil.getColorClassByType(item);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        
        notificationsTable.getColumns().addAll(statusCol, titleCol, messageCol, dateCol, typeCol);
        
        // Double-clic pour marquer comme lu
        notificationsTable.setRowFactory(tv -> {
            TableRow<Notification> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Notification notif = row.getItem();
                    if (!notif.isEstLue()) {
                        markNotificationAsRead(notif);
                    }
                }
            });
            return row;
        });
        
        // Compteur
        unreadCountLabel = new Label();
        unreadCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        notificationsContent.getChildren().addAll(title, actionButtons, notificationsTable, unreadCountLabel);
        mainContent.getChildren().add(notificationsContent);
        
        // Charger les notifications réelles
        loadRealNotificationsData();
    }
    
        private void loadRealNotificationsData() {
        try {
            if (notificationService == null || currentUser == null) {
                showAlert("Erreur", "Service de notifications non disponible.");
                return;
            }
            
            // Charger les notifications depuis la base de données
            List<Notification> notifications = notificationService.getUserNotifications(
                currentUser.getUtilisateurId(), false
            );
            
            notificationsList = FXCollections.observableArrayList(notifications);
            notificationsTable.setItems(notificationsList);
            
            // Compter les non lus
            long unreadCount = notificationsList.stream()
                .filter(n -> !n.isEstLue())
                .count();
            
            if (unreadCount > 0) {
                unreadCountLabel.setText("📊 " + unreadCount + " notification(s) non lue(s)");
                unreadCountLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-weight: bold;");
            } else {
                unreadCountLabel.setText("✅ Toutes vos notifications sont lues");
                unreadCountLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
            }
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur chargement notifications: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les notifications: " + e.getMessage());
        }
    }
    
    private void markNotificationAsRead(Notification notification) {
        try {
            if (notificationService != null) {
                boolean success = notificationService.markAsRead(notification.getNotificationId());
                if (success) {
                    notification.setEstLue(true);
                    loadRealNotificationsData(); // Recharger
                    showAlert("Succès", "Notification marquée comme lue.");
                }
            }
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur marquage notification: " + e.getMessage());
        }
    }
    
    private void markAllNotificationsAsRead() {
        try {
            if (notificationService != null && currentUser != null) {
                boolean success = notificationService.markAllAsRead(currentUser.getUtilisateurId());
                if (success) {
                    loadRealNotificationsData();
                    showAlert("Succès", "Toutes les notifications ont été marquées comme lues.");
                }
            }
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur marquage toutes notifications: " + e.getMessage());
        }
    }
    
    private void updateNotificationBadge() {
        if (notificationService != null && currentUser != null) {
            try {
                int unreadCount = notificationService.getUnreadCount(currentUser.getUtilisateurId());
                if (unreadCount > 0) {
                    // Mettre à jour le bouton notifications avec un badge
                    if (notificationsButton != null) {
                        notificationsButton.setText("🔔 Notifications (" + unreadCount + ")");
                        notificationsButton.setStyle(
                            "-fx-background-color: #fff8e1; -fx-text-fill: " + PRIMARY_COLOR + "; " +
                            "-fx-font-size: 14px; -fx-font-weight: 600; -fx-alignment: CENTER_LEFT; " +
                            "-fx-padding: 15 20; -fx-background-radius: 0;"
                        );
                    }
                } else {
                    if (notificationsButton != null) {
                        notificationsButton.setText("🔔 Notifications");
                    }
                }
            } catch (Exception e) {
                System.err.println("[CLIENT] Erreur mise à jour badge: " + e.getMessage());
            }
        }
    }
    
    // ==== SECTION PROFIL (Votre code existant) ====
    
    private void showProfileInterface() {
        mainContent.getChildren().clear();
        
        VBox profileContent = new VBox(15);
        profileContent.setPadding(new Insets(20));
        profileContent.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-border-color: " + BORDER_COLOR + ";");
        profileContent.setMaxWidth(600);
        
        Label title = new Label("👤 Modification du Profil");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        Label instruction = new Label("Modifiez uniquement les champs que vous souhaitez changer.");
        instruction.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;");
        
        // Formulaire
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        
        // Nom
        Label nomLabel = new Label("Nom:");
        nomLabel.setPrefWidth(150);
        nomField = new TextField(currentUser.getNom());
        nomField.setPromptText("Votre nom");
        
        // Prénom
        Label prenomLabel = new Label("Prénom:");
        prenomField = new TextField(currentUser.getPrenom());
        prenomField.setPromptText("Votre prénom");
        
        // Email (lecture seule ou avec vérification spéciale)
        Label emailLabel = new Label("Email:");
        emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Votre email");
        emailField.setEditable(false); 
        emailField.setStyle("-fx-background-color: #ecf0f1;");
        
        // Téléphone
        Label telephoneLabel = new Label("Téléphone:");
        telephoneField = new TextField(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        telephoneField.setPromptText("Ex: 620123456");
        
        // Adresse
        Label adresseLabel = new Label("Adresse:");
        adresseField = new TextField(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");
        adresseField.setPromptText("Votre adresse");
        
        // Mot de passe pour vérification
        Label passwordLabel = new Label("Mot de passe actuel:");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ERROR_COLOR + ";");
        currentPasswordProfileField = new PasswordField();
        currentPasswordProfileField.setPromptText("Obligatoire pour valider les modifications");
        
        // Bouton
        Button updateButton = new Button("Enregistrer les modifications");
        updateButton.setStyle("-fx-background-color: " + INFO_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30;");
        updateButton.setOnAction(e -> handleUpdateProfile());
        
        // Message
        profileMessage = new Label();
        profileMessage.setStyle("-fx-font-size: 14px;");
        
        // Ajouter au formulaire
        form.add(nomLabel, 0, 0);
        form.add(nomField, 1, 0);
        form.add(prenomLabel, 0, 1);
        form.add(prenomField, 1, 1);
        form.add(emailLabel, 0, 2);
        form.add(emailField, 1, 2);
        form.add(telephoneLabel, 0, 3);
        form.add(telephoneField, 1, 3);
        form.add(adresseLabel, 0, 4);
        form.add(adresseField, 1, 4);
        form.add(passwordLabel, 0, 5);
        form.add(currentPasswordProfileField, 1, 5);
        form.add(updateButton, 0, 6, 2, 1);
        form.add(profileMessage, 0, 7, 2, 1);
        
        profileContent.getChildren().addAll(title, instruction, form);
        mainContent.getChildren().add(profileContent);
    }
    
    private void handleUpdateProfile() {
        System.out.println("[CLIENT] Mise à jour profil demandée");
        
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseField.getText().trim();
        String currentPassword = currentPasswordProfileField.getText();
        
        // Vérifier le mot de passe (obligatoire pour toute modification)
        if (currentPassword.isEmpty()) {
            showProfileError("Le mot de passe actuel est requis pour valider les modifications.");
            return;
        }
        
        if (!authService.verifyPassword(currentUser.getEmail(), currentPassword)) {
            showProfileError("Mot de passe incorrect. Veuillez réessayer.");
            return;
        }
        
        // Vérifier si des modifications ont été faites
        boolean modifications = false;
        StringBuilder modificationsList = new StringBuilder();
        
        // Vérifier chaque champ
        if (!nom.equals(currentUser.getNom()) && !nom.isEmpty()) {
            if (nom.length() < 2) {
                showProfileError("Le nom doit contenir au moins 2 caractères.");
                return;
            }
            currentUser.setNom(nom);
            modifications = true;
            modificationsList.append("- Nom\n");
        }
        
        if (!prenom.equals(currentUser.getPrenom()) && !prenom.isEmpty()) {
            if (prenom.length() < 2) {
                showProfileError("Le prénom doit contenir au moins 2 caractères.");
                return;
            }
            currentUser.setPrenom(prenom);
            modifications = true;
            modificationsList.append("- Prénom\n");
        }
        
        // Pour téléphone, permettre de vider le champ ou de mettre une nouvelle valeur
        String currentPhone = currentUser.getTelephone() != null ? currentUser.getTelephone() : "";
        if (!telephone.equals(currentPhone)) {
            if (!telephone.isEmpty()) {
                // Si un nouveau téléphone est fourni, le valider
                if (!ValidationUtil.isValidGuineanPhone(telephone)) {
                    showProfileError("Format de téléphone invalide. Format attendu: 6XX XXX XXX");
                    return;
                }
                
                // Vérifier si le téléphone existe déjà (sauf pour l'utilisateur actuel)
                Utilisateur existing = utilisateurDAO.findByTelephone(telephone);
                if (existing != null && existing.getUtilisateurId() != currentUser.getUtilisateurId()) {
                    showProfileError("Ce numéro de téléphone est déjà utilisé par un autre compte.");
                    return;
                }
            }
            currentUser.setTelephone(telephone.isEmpty() ? null : telephone);
            modifications = true;
            modificationsList.append("- Téléphone\n");
        }
        
        // Adresse
        String currentAddress = currentUser.getAdresse() != null ? currentUser.getAdresse() : "";
        if (!adresse.equals(currentAddress)) {
            currentUser.setAdresse(adresse.isEmpty() ? null : adresse);
            modifications = true;
            modificationsList.append("- Adresse\n");
        }
        
        if (!modifications) {
            showProfileInfo("Aucune modification détectée. Tous les champs sont identiques aux valeurs actuelles.");
            return;
        }
        
        // Mettre à jour dans la base de données
        try {
            boolean success = utilisateurDAO.update(currentUser);
            
            if (success) {
                // Mettre à jour la session
                SessionManager.setCurrentUser(currentUser);
                
                // Mettre à jour l'interface
                updateUI();
                
                profileMessage.setText("✅ Profil mis à jour avec succès !\n\nModifications enregistrées:\n" + modificationsList.toString());
                profileMessage.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                
                // Effacer le champ mot de passe
                currentPasswordProfileField.clear();
                
                System.out.println("[CLIENT] Profil mis à jour pour: " + currentUser.getEmail());
            } else {
                showProfileError("Erreur lors de la mise à jour. Veuillez réessayer.");
            }
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur mise à jour profil: " + e.getMessage());
            e.printStackTrace();
            showProfileError("Erreur technique: " + e.getMessage());
        }
    }
    
    private void showProfileError(String message) {
        profileMessage.setText("❌ " + message);
        profileMessage.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
    }
    
    private void showProfileInfo(String message) {
        profileMessage.setText("ℹ️ " + message);
        profileMessage.setStyle("-fx-text-fill: " + INFO_COLOR + ";");
    }
    
    // ==== SECTION SÉCURITÉ (Votre code existant) ====
    
    private void showSecurityInterface() {
        mainContent.getChildren().clear();
        
        VBox securityContent = new VBox(15);
        securityContent.setPadding(new Insets(20));
        securityContent.setStyle("-fx-background-color: #fff5f5; -fx-border-radius: 10; -fx-border-color: " + BORDER_COLOR + ";");
        securityContent.setMaxWidth(600);
        
        Label title = new Label("🔐 Sécurité du Compte");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + ERROR_COLOR + ";");
        
        // Changement de mot de passe
        VBox passwordSection = new VBox(10);
        passwordSection.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label passwordTitle = new Label("Changer le Mot de Passe");
        passwordTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 16px;");
        
        GridPane passwordForm = new GridPane();
        passwordForm.setHgap(10);
        passwordForm.setVgap(10);
        
        // Mot de passe actuel
        Label currentPassLabel = new Label("Mot de passe actuel:");
        currentPasswordSecurityField = new PasswordField();
        currentPasswordSecurityField.setPromptText("Mot de passe actuel");
        
        // Nouveau mot de passe
        Label newPassLabel = new Label("Nouveau mot de passe:");
        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Minimum 6 caractères");
        
        // Confirmation
        Label confirmPassLabel = new Label("Confirmer nouveau:");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Répéter le mot de passe");
        
        // Bouton
        Button changePassButton = new Button("Changer Mot de Passe");
        changePassButton.setStyle("-fx-background-color: " + ERROR_COLOR + "; -fx-text-fill: white;");
        changePassButton.setOnAction(e -> handleChangePassword());
        
        passwordForm.add(currentPassLabel, 0, 0);
        passwordForm.add(currentPasswordSecurityField, 1, 0);
        passwordForm.add(newPassLabel, 0, 1);
        passwordForm.add(newPasswordField, 1, 1);
        passwordForm.add(confirmPassLabel, 0, 2);
        passwordForm.add(confirmPasswordField, 1, 2);
        passwordForm.add(changePassButton, 0, 3, 2, 1);
        
        passwordSection.getChildren().addAll(passwordTitle, passwordForm);
        
        // Changement de PIN
        VBox pinSection = new VBox(10);
        pinSection.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label pinTitle = new Label("Changer le PIN (4 chiffres)");
        pinTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 16px;");
        
        GridPane pinForm = new GridPane();
        pinForm.setHgap(10);
        pinForm.setVgap(10);
        
        // Mot de passe pour vérification
        Label pinPassLabel = new Label("Mot de passe:");
        passwordForPinField = new PasswordField();
        passwordForPinField.setPromptText("Pour vérification");
        
        // Nouveau PIN
        Label newPinLabel = new Label("Nouveau PIN:");
        newPinField = new PasswordField();
        newPinField.setPromptText("4 chiffres");
        
        // Confirmation PIN
        Label confirmPinLabel = new Label("Confirmer PIN:");
        confirmPinField = new PasswordField();
        confirmPinField.setPromptText("Répéter le PIN");
        
        // Bouton
        Button changePinButton = new Button("Changer PIN");
        changePinButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        changePinButton.setOnAction(e -> handleChangePin());
        
        pinForm.add(pinPassLabel, 0, 0);
        pinForm.add(passwordForPinField, 1, 0);
        pinForm.add(newPinLabel, 0, 1);
        pinForm.add(newPinField, 1, 1);
        pinForm.add(confirmPinLabel, 0, 2);
        pinForm.add(confirmPinField, 1, 2);
        pinForm.add(changePinButton, 0, 3, 2, 1);
        
        pinSection.getChildren().addAll(pinTitle, pinForm);
        
        // Message
        securityMessage = new Label();
        securityMessage.setStyle("-fx-font-size: 14px;");
        
        // Statut du PIN
        Label pinStatus = new Label();
        if (currentUser.getPinHash() == null || currentUser.getPinHash().isEmpty()) {
            pinStatus.setText("⚠️ Vous n'avez pas configuré de PIN. Configurez-en un pour sécuriser votre compte.");
            pinStatus.setStyle("-fx-text-fill: " + WARNING_COLOR + "; -fx-font-weight: bold;");
        } else {
            pinStatus.setText("✅ PIN configuré. Vous pouvez le modifier ci-dessous.");
            pinStatus.setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
        }
        
        securityContent.getChildren().addAll(title, pinStatus, passwordSection, pinSection, securityMessage);
        mainContent.getChildren().add(securityContent);
    }
    
    private void handleChangePassword() {
        System.out.println("[CLIENT] Changement mot de passe demandé");
        
        String currentPassword = currentPasswordSecurityField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showSecurityError("Tous les champs sont obligatoires.");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showSecurityError("Les nouveaux mots de passe ne correspondent pas.");
            return;
        }
        
        if (newPassword.length() < 6) {
            showSecurityError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        
        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (newPassword.equals(currentPassword)) {
            showSecurityError("Le nouveau mot de passe doit être différent de l'actuel.");
            return;
        }
        
        // Vérifier le mot de passe actuel
        if (!authService.verifyPassword(currentUser.getEmail(), currentPassword)) {
            showSecurityError("Mot de passe actuel incorrect.");
            return;
        }
        
        try {
            // Changer le mot de passe
            boolean success = utilisateurDAO.changePassword(
                currentUser.getUtilisateurId(), 
                SecurityUtil.hashPassword(newPassword)
            );
            
            if (success) {
                securityMessage.setText("✅ Mot de passe changé avec succès !");
                securityMessage.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                
                // Effacer les champs
                currentPasswordSecurityField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                
                System.out.println("[CLIENT] Mot de passe changé pour: " + currentUser.getEmail());
            } else {
                showSecurityError("Erreur lors du changement de mot de passe.");
            }
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur changement mot de passe: " + e.getMessage());
            e.printStackTrace();
            showSecurityError("Erreur technique: " + e.getMessage());
        }
    }
    
    private void handleChangePin() {
        System.out.println("[CLIENT] Changement PIN demandé");
        
        String password = passwordForPinField.getText();
        String newPin = newPinField.getText();
        String confirmPin = confirmPinField.getText();
        
        // Validation
        if (password.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            showSecurityError("Tous les champs sont obligatoires.");
            return;
        }
        
        if (!newPin.equals(confirmPin)) {
            showSecurityError("Les nouveaux PIN ne correspondent pas.");
            return;
        }
        
        if (!newPin.matches("\\d{4}")) {
            showSecurityError("Le PIN doit contenir exactement 4 chiffres.");
            return;
        }
        
        // Vérifier que le nouveau PIN est différent de l'ancien (si existant)
        if (currentUser.getPinHash() != null && !currentUser.getPinHash().isEmpty()) {
            if (SecurityUtil.verifyPin(newPin, currentUser.getPinHash())) {
                showSecurityError("Le nouveau PIN doit être différent de l'actuel.");
                return;
            }
        }
        
        // Vérifier le mot de passe
        if (!authService.verifyPassword(currentUser.getEmail(), password)) {
            showSecurityError("Mot de passe incorrect.");
            return;
        }
        
        try {
            // Changer le PIN
            String pinHash = SecurityUtil.hashPin(newPin);
            boolean success = utilisateurDAO.changePIN(currentUser.getUtilisateurId(), pinHash);
            
            if (success) {
                // Mettre à jour l'utilisateur dans la session
                currentUser.setPinHash(pinHash);
                SessionManager.setCurrentUser(currentUser);
                
                securityMessage.setText("✅ PIN changé avec succès !");
                securityMessage.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                
                // Effacer les champs
                passwordForPinField.clear();
                newPinField.clear();
                confirmPinField.clear();
                
                System.out.println("[CLIENT] PIN changé pour: " + currentUser.getEmail());
            } else {
                showSecurityError("Erreur lors du changement de PIN.");
            }
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Erreur changement PIN: " + e.getMessage());
            e.printStackTrace();
            showSecurityError("Erreur technique: " + e.getMessage());
        }
    }
    
    private void showSecurityError(String message) {
        securityMessage.setText("❌ " + message);
        securityMessage.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
    }
    
    // ==== SECTION AIDE (Nouveau) ====
    
    private void showHelpInterface() {
        mainContent.getChildren().clear();
        
        VBox helpContent = new VBox(15);
        helpContent.setPadding(new Insets(20));
        helpContent.setStyle("-fx-background-color: #e8f4f8; -fx-border-radius: 10; -fx-border-color: " + INFO_COLOR + ";");
        
        Label title = new Label("❓ Centre d'Aide & FAQ");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        // Accordéon FAQ
        TitledPane faq1 = new TitledPane();
        faq1.setText("Comment effectuer un transfert d'argent ?");
        faq1.setContent(new Label("1. Cliquez sur 'Transférer' dans le menu\n" +
                                "2. Entrez l'email du destinataire\n" +
                                "3. Saisissez le montant\n" +
                                "4. Entrez votre PIN de 4 chiffres\n" +
                                "5. Cliquez sur 'Effectuer le Transfert'"));
        
        TitledPane faq2 = new TitledPane();
        faq2.setText("Comment changer mon mot de passe ou mon PIN ?");
        faq2.setContent(new Label("Rendez-vous dans la section 'Sécurité'\n" +
                                "Pour le mot de passe: entrez l'ancien, le nouveau, confirmez\n" +
                                "Pour le PIN: entrez votre mot de passe, le nouveau PIN, confirmez"));
        
        TitledPane faq3 = new TitledPane();
        faq3.setText("Quelles sont les limites de transaction ?");
        faq3.setContent(new Label("• Transfert maximum: 500,000 GNF\n" +
                                "• Retrait maximum: 5,000,000 GNF\n" +
                                "• Dépôt maximum: 10,000,000 GNF\n" +
                                "• Frais de transfert: 1% du montant"));
        
        TitledPane faq4 = new TitledPane();
        faq4.setText("Que faire si j'oublie mon PIN ?");
        faq4.setContent(new Label("Contactez notre service client au:\n" +
                                "📞 656-123-456\n" +
                                "✉️ support@ewallet-gn.com\n" +
                                "Un agent vous aidera à réinitialiser votre compte."));
        
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(faq1, faq2, faq3, faq4);
        accordion.setPrefWidth(600);
        
        // Contact
        VBox contactBox = new VBox(10);
        contactBox.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label contactTitle = new Label("📞 Contact et Support");
        contactTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label phoneLabel = new Label("Téléphone: 656-123-456 (7j/7, 8h-18h)");
        Label emailLabel = new Label("Email: support@ewallet-gn.com");
        Label addressLabel = new Label("Adresse: Rue KA 028, Kaloum, Conakry");
        
        contactBox.getChildren().addAll(contactTitle, phoneLabel, emailLabel, addressLabel);
        
        // Urgence
        VBox emergencyBox = new VBox(10);
        emergencyBox.setStyle("-fx-background-color: #fff5f5; -fx-border-radius: 5; -fx-padding: 15; -fx-border-color: " + ERROR_COLOR + ";");
        
        Label emergencyTitle = new Label("🚨 Urgence / Portefeuille bloqué");
        emergencyTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + ERROR_COLOR + ";");
        
        Label emergencyText = new Label("Si vous suspectez une activité frauduleuse:\n" +
                                   "1. Changez immédiatement votre mot de passe\n" +
                                   "2. Contactez le support URGENCE: 656-999-999\n" +
                                   "3. Ne communiquez JAMAIS votre PIN à qui que ce soit");
        emergencyText.setStyle("-fx-text-fill: #c0392b;");
        
        emergencyBox.getChildren().addAll(emergencyTitle, emergencyText);
        
        helpContent.getChildren().addAll(title, accordion, contactBox, emergencyBox);
        mainContent.getChildren().add(helpContent);
    }
    
    // ==== MÉTHODES UTILITAIRES ====
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void navigateToLogin() {
        MainApp.showLogin();
    }
}