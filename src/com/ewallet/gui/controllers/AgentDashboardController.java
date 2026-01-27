package com.ewallet.gui.controllers;

import com.ewallet.core.models.Utilisateur;
import com.ewallet.core.dao.CommissionDAO;
import com.ewallet.core.services.TransactionService;
import com.ewallet.core.utils.SessionManager;
import com.ewallet.gui.MainApp;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import com.ewallet.core.services.NotificationService;
import com.ewallet.core.services.ProfileService;
import com.ewallet.core.models.Notification;
import com.ewallet.core.models.Transaction;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.scene.Scene;



public class AgentDashboardController {
    
    // Palette de couleurs inspirée du template Django
    private static class ColorScheme {
        // Couleurs principales (dégradés bleu/violet)
        static final String PRIMARY = "#4361ee";
        static final String PRIMARY_DARK = "#3a56d4";
        static final String SECONDARY = "#7209b7";
        static final String ACCENT = "#4cc9f0";
        static final String SUCCESS = "#28a745";
        static final String WARNING = "#ffc107";
        static final String DANGER = "#dc3545";
        static final String INFO = "#17a2b8";
        static final String DARK = "#1a1a2e";
        static final String LIGHT = "#f8fafc";
        static final String NEUTRAL = "#718096";
        static final String CARD_BG = "#ffffff";
        static final String BORDER = "#e2e8f0";
        static final String SIDEBAR_BG = "#16213e";
        
        // Dégradés
        static final String GRADIENT_PRIMARY = "linear-gradient(90deg, #4361ee 0%, #7209b7 100%)";
        static final String GRADIENT_SIDEBAR = "linear-gradient(180deg, #1a1a2e 0%, #16213e 100%)";
        static final String GRADIENT_SUCCESS = "linear-gradient(135deg, #28a745 0%, #20c997 100%)";
        static final String GRADIENT_WARNING = "linear-gradient(135deg, #ffc107 0%, #fd7e14 100%)";
        static final String GRADIENT_DANGER = "linear-gradient(135deg, #dc3545 0%, #e83e8c 100%)";
        static final String GRADIENT_INFO = "linear-gradient(135deg, #17a2b8 0%, #20c997 100%)";
        
        // Couleurs d'arrière-plan
        static final String BG_LIGHT = "linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)";
        static final String BG_CARD = "#ffffff";
        
        // Couleurs pour statistiques
        static final Map<String, String[]> STAT_COLORS = new HashMap<>();
        static {
            STAT_COLORS.put("today", new String[]{"#4361ee", "#3a56d4", "#eef2ff"});
            STAT_COLORS.put("week", new String[]{"#7209b7", "#5a08a1", "#f5f3ff"});
            STAT_COLORS.put("month", new String[]{"#28a745", "#218838", "#f0fdf4"});
            STAT_COLORS.put("commission", new String[]{"#ffc107", "#e0a800", "#fef7cd"});
        }
    }
    
    @FXML private Label welcomeLabel;
    @FXML private Label commissionsLabel;
    @FXML private Label pendingCommissionsLabel;
    @FXML private Label totalCommissionsLabel;
    @FXML private Button depositButton;
    @FXML private Button withdrawButton;
    @FXML private Button operationsButton;
    @FXML private Button commissionsButton;
    @FXML private Button logoutButton;
    @FXML private VBox mainContent;
    @FXML private Button notificationsButton;
    @FXML private Label todayTransLabel;
    @FXML private Label todayAmountLabel;
    @FXML private Button profileButton;
    @FXML private Button helpButton;
    @FXML private VBox dashboardWelcomeSection;
    @FXML private VBox dashboardInstructionsSection;
    @FXML private Button dashboardButton;
    @FXML private ScrollPane mainScrollPane;

    private Utilisateur currentUser;
    private TransactionService transactionService;
    private CommissionDAO commissionDAO;
    private NotificationService notificationService;
    private int unreadNotificationCount = 0;
    private ProfileService profileService;
    
    @FXML
    public void initialize() {
        System.out.println("[AGENT] Dashboard initialisé pour l'agent");
        transactionService = new TransactionService();
        commissionDAO = new CommissionDAO();
        profileService = new ProfileService();
        currentUser = SessionManager.getCurrentUser();
        mainContent.setStyle("-fx-background-color: transparent;");
        
        if (currentUser != null) {
            try {
                this.notificationService = new NotificationService(com.ewallet.core.DatabaseConfig.getConnection());


            } catch (Exception e) {
                System.err.println("[AGENT] Erreur initialisation NotificationService: " + e.getMessage());
            }
            
            // Configurer le ScrollPane
        if (mainScrollPane != null) {
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
        
            // Appliquer le style général
            applyMainStyles();
            
            // Configurer les effets hover du sidebar
            setupSidebarHoverEffects();
                
            
            updateUI();
            initializeMainContent();
        } else {
            navigateToLogin();
        }
    }
    @FXML
    private void handleDashboard() {
        System.out.println("[AGENT] Bouton Dashboard cliqué");
        initializeMainContent();
    }
    private void setupSidebarHoverEffects() {
        // Appliquer les effets hover à tous les boutons du sidebar
        Button[] sidebarButtons = {dashboardButton, depositButton, withdrawButton, 
                                operationsButton, commissionsButton, notificationsButton,
                                profileButton, helpButton, logoutButton};
        
        for (Button button : sidebarButtons) {
            if (button != null) {
                String originalStyle = button.getStyle();
                
                // Effet hover
                button.setOnMouseEntered(e -> {
                    if (!button.getStyle().contains("-fx-background-color: rgba(67, 97, 238, 0.25)")) {
                        button.setStyle(originalStyle + " -fx-background-color: rgba(67, 97, 238, 0.15); " +
                                    "-fx-text-fill: white; -fx-translate-x: 4;");
                    }
                });
                
                button.setOnMouseExited(e -> {
                    if (!button.getStyle().contains("-fx-background-color: rgba(67, 97, 238, 0.25)")) {
                        button.setStyle(originalStyle);
                    }
                });
            }
        }
    }
    
    private void applyMainStyles() {
        // Style pour le VBox principal
        mainContent.setStyle("-fx-background-color: transparent; -fx-padding: 20;");
        
        // Styles pour les boutons principaux
        String buttonStyle = "-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 14px; " +
                           "-fx-padding: 12 24; -fx-background-radius: 10; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.5, 0, 2);";
        
        depositButton.setStyle(buttonStyle + " -fx-background-color: linear-gradient(to right, " + 
                             ColorScheme.PRIMARY + ", " + ColorScheme.SECONDARY + ");");
        
        withdrawButton.setStyle(buttonStyle + " -fx-background-color: linear-gradient(to right, " + 
                              ColorScheme.SUCCESS + ", #20c997);");
        
        operationsButton.setStyle(buttonStyle + " -fx-background-color: linear-gradient(to right, " + 
                                ColorScheme.INFO + ", #17a2b8);");
        
        commissionsButton.setStyle(buttonStyle + " -fx-background-color: linear-gradient(to right, " + 
                                 ColorScheme.WARNING + ", #fd7e14);");
        
        logoutButton.setStyle(buttonStyle + " -fx-background-color: " + ColorScheme.DANGER + ";");
        
        // Style pour le label de bienvenue
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + 
                            ColorScheme.DARK + "; -fx-font-family: 'Inter', sans-serif;");
        
        // Styles pour les labels de commissions
        String commissionLabelStyle = "-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: " + 
                                    ColorScheme.NEUTRAL + "; -fx-padding: 5 0;";
        
        commissionsLabel.setStyle(commissionLabelStyle);
        pendingCommissionsLabel.setStyle(commissionLabelStyle);
        totalCommissionsLabel.setStyle(commissionLabelStyle);
        
        // Effets hover pour les boutons
        setupButtonHoverEffects(depositButton);
        setupButtonHoverEffects(withdrawButton);
        setupButtonHoverEffects(operationsButton);
        setupButtonHoverEffects(commissionsButton);
        setupButtonHoverEffects(logoutButton);
    }
    
    private void setupButtonHoverEffects(Button button) {
        String originalStyle = button.getStyle();
        button.setOnMouseEntered(e -> {
            button.setStyle(originalStyle + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.5, 0, 3); " +
                          "-fx-translate-y: -2;");
        });
        button.setOnMouseExited(e -> {
            button.setStyle(originalStyle);
        });
    }
    
    private void updateUI() {
        welcomeLabel.setText("👋 Bienvenue, Agent " + currentUser.getPrenom() + " " + currentUser.getNom());
        updateCommissionsStats();
        updateNotificationBadge();
        updateDashboardStats();
    }
    
    private void updateDashboardStats() {
        try {
            Map<String, Object> todayStats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "TODAY");
            
            int todayTransactions = (int) todayStats.getOrDefault("total_transactions", 0);
            double todayAmount = (double) todayStats.getOrDefault("total_amount", 0.0);
            
            if (todayTransLabel != null) {
                todayTransLabel.setText(String.valueOf(todayTransactions));
                todayTransLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + 
                                       ColorScheme.PRIMARY + ";");
            }
            
            if (todayAmountLabel != null) {
                todayAmountLabel.setText(String.format("%,.0f GNF", todayAmount));
                todayAmountLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: " + 
                                        ColorScheme.SUCCESS + ";");
            }
            
        } catch (Exception e) {
            System.err.println("[AGENT] Erreur mise à jour statistiques dashboard: " + e.getMessage());
        }
    }
 
    @FXML
    private void handleNotifications() {
        showNotificationsInterface();
    }
    private static final double SCREEN_WIDTH = 900;
    private static final double SCREEN_HEIGHT = 700;
    
    private void showNotificationsInterface() {
        Stage notificationsStage = new Stage();
        notificationsStage.setTitle("🔔 Notifications Agent");
        notificationsStage.initModality(Modality.WINDOW_MODAL);
        notificationsStage.initOwner(MainApp.getPrimaryStage());
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setStyle("-fx-background: " + ColorScheme.BG_LIGHT + ";");
        
        // Header avec style de navbar
        HBox header = new HBox();
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 15 0; -fx-border-color: " + ColorScheme.BORDER + "; -fx-border-width: 0 0 1 0;");
        
        Label title = new Label("🔔 Mes Notifications");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                      "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Badge pour notifications non lues
        Label badgeLabel = new Label();
        badgeLabel.setStyle("-fx-background-color: " + ColorScheme.DANGER + "; -fx-text-fill: white; " +
                          "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 2 8; " +
                          "-fx-background-radius: 12;");
        
        header.getChildren().addAll(title, spacer, badgeLabel);
        
        // Tableau des notifications avec style de template
        TableView<Notification> notificationsTable = new TableView<>();
        notificationsTable.setPrefHeight(400);
        notificationsTable.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                                  "-fx-border-color: " + ColorScheme.BORDER + "; " +
                                  "-fx-border-radius: 12; -fx-background-radius: 12; " +
                                  "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0.5, 0, 2);");
        
        // Compteur
        Label unreadCountLabel = new Label();
        unreadCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                                "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        // Boutons d'action avec style gradient
        HBox actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        actionButtons.setPadding(new Insets(0, 0, 15, 0));
        
        Button markAllReadButton = createStyledButton("📌 Tout marquer comme lu", "primary");
        markAllReadButton.setOnAction(e -> markAllNotificationsAsRead());
        
        Button refreshButton = createStyledButton("🔄 Rafraîchir", "success");
        refreshButton.setOnAction(e -> loadNotificationsForTable(notificationsTable, unreadCountLabel, badgeLabel));
        
        actionButtons.getChildren().addAll(markAllReadButton, refreshButton);
        
        // Configuration des colonnes avec style de template
        String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-padding: 12 15; -fx-font-family: 'Inter';";
        String headerStyle = "-fx-background-color: #f8fafc; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: " + ColorScheme.DARK + "; -fx-border-color: " + ColorScheme.BORDER + ";";
        
        TableColumn<Notification, String> statusCol = new TableColumn<>("");
        statusCol.setPrefWidth(50);
        statusCol.setStyle(headerStyle);
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
                    setStyle(columnStyle);
                    if (item.equals("🆕")) {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.DANGER + "; -fx-font-size: 16px;");
                    } else {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.NEUTRAL + "; -fx-font-size: 16px;");
                    }
                }
            }
        });
        
        TableColumn<Notification, String> titleCol = new TableColumn<>("Titre");
        titleCol.setPrefWidth(200);
        titleCol.setStyle(headerStyle);
        titleCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        titleCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitre())
        );
        
        TableColumn<Notification, String> messageCol = new TableColumn<>("Message");
        messageCol.setPrefWidth(350);
        messageCol.setStyle(headerStyle);
        messageCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        messageCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMessage())
        );
        
        TableColumn<Notification, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(150);
        dateCol.setStyle(headerStyle);
        dateCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateCreation();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        TableColumn<Notification, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(120);
        typeCol.setStyle(headerStyle);
        typeCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        typeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType())
        );
        
        notificationsTable.getColumns().addAll(statusCol, titleCol, messageCol, dateCol, typeCol);
        
        // Lignes alternées comme dans le template
        notificationsTable.setRowFactory(tv -> new TableRow<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    int index = getIndex();
                    if (index % 2 == 0) {
                        setStyle("-fx-background-color: white;");
                    } else {
                        setStyle("-fx-background-color: #f8fafc;");
                    }
                    
                    // Style pour notifications non lues
                    if (!item.isEstLue()) {
                        setStyle("-fx-background-color: rgba(176, 48, 205, 0.05);");
                    }
                    
                    // Effet hover
                    setOnMouseEntered(e -> setStyle("-fx-background-color: #e0f2fe; -fx-cursor: hand;"));
                    setOnMouseExited(e -> {
                        if (index % 2 == 0) {
                            setStyle("-fx-background-color: white;");
                        } else {
                            setStyle("-fx-background-color: #f8fafc;");
                        }
                        if (!item.isEstLue()) {
                            setStyle("-fx-background-color: rgba(176, 48, 205, 0.05);");
                        }
                    });
                    
                    // Double-clic pour marquer comme lu
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !isEmpty()) {
                            Notification notif = getItem();
                            if (!notif.isEstLue()) {
                                markNotificationAsRead(notif);
                                loadNotificationsForTable(notificationsTable, unreadCountLabel, badgeLabel);
                            }
                        }
                    });
                }
            }
        });
        
        // Bouton fermer
        Button closeButton = createStyledButton("Fermer", "neutral");
        closeButton.setOnAction(e -> notificationsStage.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        buttonBox.getChildren().add(closeButton);
        
        mainLayout.getChildren().addAll(header, actionButtons, notificationsTable, unreadCountLabel, buttonBox);
        
        Scene scene = new Scene(mainLayout, 950, 600);
        notificationsStage.setScene(scene);
        
        loadNotificationsForTable(notificationsTable, unreadCountLabel, badgeLabel);
        notificationsStage.showAndWait();

        
    }
    
    private void loadNotificationsForTable(TableView<Notification> table, Label countLabel, Label badgeLabel) {
        try {
            if (notificationService == null || currentUser == null) {
                countLabel.setText("Service de notifications non disponible");
                return;
            }
            
            List<Notification> notifications = notificationService.getUserNotifications(
                currentUser.getUtilisateurId(), false
            );
            
            ObservableList<Notification> observableList = FXCollections.observableArrayList(notifications);
            table.setItems(observableList);
            
            long unreadCount = notifications.stream()
                .filter(n -> !n.isEstLue())
                .count();
            
            unreadNotificationCount = (int) unreadCount;
            
            if (unreadCount > 0) {
                countLabel.setText("📊 " + unreadCount + " notification(s) non lue(s)");
                countLabel.setStyle("-fx-text-fill: " + ColorScheme.DANGER + "; -fx-font-weight: bold; -fx-font-family: 'Inter';");
                
                badgeLabel.setText(String.valueOf(unreadCount));
                badgeLabel.setVisible(true);
            } else {
                countLabel.setText("✅ Toutes vos notifications sont lues");
                countLabel.setStyle("-fx-text-fill: " + ColorScheme.SUCCESS + "; -fx-font-weight: bold; -fx-font-family: 'Inter';");
                
                badgeLabel.setVisible(false);
            }
            
        } catch (Exception e) {
            countLabel.setText("❌ Erreur chargement: " + e.getMessage());
            System.err.println("[AGENT] Erreur chargement notifications: " + e.getMessage());
        }
    }
    
    private void markNotificationAsRead(Notification notification) {
        try {
            if (notificationService != null) {
                boolean success = notificationService.markAsRead(notification.getNotificationId());
                if (success) {
                    System.out.println("[AGENT] Notification marquée comme lue: " + notification.getTitre());
                    unreadNotificationCount--;
                }
            }
        } catch (Exception e) {
            System.err.println("[AGENT] Erreur marquage notification: " + e.getMessage());
        }
    }
    
    private void markAllNotificationsAsRead() {
        try {
            if (notificationService != null && currentUser != null) {
                boolean success = notificationService.markAllAsRead(currentUser.getUtilisateurId());
                if (success) {
                    System.out.println("[AGENT] Toutes les notifications marquées comme lues");
                    unreadNotificationCount = 0;
                    updateNotificationBadge();
                    showStyledAlert("Succès", "Toutes les notifications ont été marquées comme lues.", "success");
                }
            }
        } catch (Exception e) {
            System.err.println("[AGENT] Erreur marquage toutes notifications: " + e.getMessage());
        }
    }
    
    private void updateNotificationBadge() {
        if (notificationsButton != null) {
            if (unreadNotificationCount > 0) {
                notificationsButton.setText("🔔 Notifications (" + unreadNotificationCount + ")");
                notificationsButton.setStyle("-fx-background-color: " + ColorScheme.GRADIENT_PRIMARY + "; " +
                                          "-fx-text-fill: white; -fx-font-size: 14px; " +
                                          "-fx-font-weight: 600; -fx-padding: 10 20; " +
                                          "-fx-background-radius: 8; -fx-cursor: hand; " +
                                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.5, 0, 2);");
            } else {
                notificationsButton.setText("🔔 Notifications");
                notificationsButton.setStyle("-fx-background-color: " + ColorScheme.GRADIENT_INFO + "; " +
                                          "-fx-text-fill: white; -fx-font-size: 14px; " +
                                          "-fx-font-weight: 600; -fx-padding: 10 20; " +
                                          "-fx-background-radius: 8; -fx-cursor: hand; " +
                                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.5, 0, 2);");
            }
        }
    }
    
    private void updateCommissionsStats() {
        try {
            double total = commissionDAO.getTotalCommissions(currentUser.getUtilisateurId());
            double pending = commissionDAO.getPendingCommissions(currentUser.getUtilisateurId());
            double paid = commissionDAO.getPaidCommissions(currentUser.getUtilisateurId());
            
            String labelStyle = "-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: " + 
                          ColorScheme.DARK + "; -fx-padding: 5 0;";
            commissionsLabel.setText("💰 Total commissions: " + String.format("%,.0f GNF", total));
            pendingCommissionsLabel.setText("⏳ En attente: " + String.format("%,.0f GNF", pending));
            totalCommissionsLabel.setText("✅ Payées: " + String.format("%,.0f GNF", paid));
        } catch (Exception e) {
            commissionsLabel.setText("Commissions: Erreur");
            pendingCommissionsLabel.setText("En attente: Erreur");
            totalCommissionsLabel.setText("Payées: Erreur");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeposit() {
        System.out.println("[AGENT] Bouton Dépôt cliqué");
        showDepositInterface();
    }
    
    @FXML
    private void handleWithdraw() {
        System.out.println("[AGENT] Bouton Retrait cliqué");
        showWithdrawInterface();
    }
    
    @FXML
    private void handleOperations() {
        System.out.println("[AGENT] Bouton Opérations cliqué");
        showOperationsInterface();
    }
    
    @FXML
    private void handleCommissions() {
        System.out.println("[AGENT] Bouton Commissions cliqué");
        showCommissionsInterface();
    }
    
    @FXML
    private void handleLogout() {
        System.out.println("[AGENT] Déconnexion de l'agent " + currentUser.getEmail());
        SessionManager.clearSession();
        MainApp.showWelcome();
    }
    
    @FXML
    private void handleProfile() {
        System.out.println("[AGENT] Bouton Profil cliqué pour: " + currentUser.getEmail());
        showProfileInterface();

        // Configurer le ScrollPane
        if (mainScrollPane != null) {
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
    }
    
    @FXML
    private void handleHelp() {
        System.out.println("[AGENT] Bouton Aide cliqué");
        showHelpInterface();
    }
   

    private void showDepositInterface() {
        System.out.println("[AGENT] Affichage interface dépôt");
        mainContent.getChildren().clear();
        
        VBox depositBox = new VBox(20);
        depositBox.setPadding(new Insets(30));
        depositBox.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                          "-fx-border-color: " + ColorScheme.BORDER + "; " +
                          "-fx-border-radius: 16; -fx-background-radius: 16; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.5, 0, 5);");
        
        // Header avec style de template
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label title = new Label("💰 Dépôt pour Client");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                      "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        Label subtitle = new Label("Créditez le compte d'un client");
        subtitle.setStyle("-fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(titleBox);
        
        // Formulaire
        VBox formBox = new VBox(20);
        formBox.setPadding(new Insets(25, 0, 0, 0));
        
        // Email client
        VBox emailBox = new VBox(8);
        Label emailLabel = new Label("📧 Email du client *");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        TextField clientEmailField = createStyledTextField("exemple@client.com");
        emailBox.getChildren().addAll(emailLabel, clientEmailField);
        
        // Montant
        VBox amountBox = new VBox(8);
        Label amountLabel = new Label("💰 Montant en GNF *");
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        TextField amountField = createStyledTextField("Ex: 100000");
        amountBox.getChildren().addAll(amountLabel, amountField);
        
        // PIN client
        VBox pinBox = new VBox(8);
        Label pinLabel = new Label("🔒 PIN du client (optionnel)");
        pinLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        PasswordField clientPinField = createStyledPasswordField("4 chiffres, optionnel");
        pinBox.getChildren().addAll(pinLabel, clientPinField);
        
        // Bouton d'action
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(25, 0, 0, 0));
        
        Button cancelButton = createStyledButton("Annuler", "neutral");
        cancelButton.setOnAction(e -> {
            mainContent.getChildren().clear();
            initializeMainContent();
        });
        
        Button confirmButton = createStyledButton("💳 Confirmer le Dépôt", "success");
        confirmButton.setStyle(confirmButton.getStyle() + " -fx-font-size: 16px; -fx-padding: 12 40;");
        confirmButton.setOnAction(e -> {
            System.out.println("[AGENT] Bouton Confirmer Dépôt cliqué");
            processDeposit(clientEmailField.getText(), amountField.getText(), clientPinField.getText());
        });
        
        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        
        // Notes informatives avec style d'alerte
        VBox notesBox = new VBox(10);
        notesBox.setPadding(new Insets(20, 0, 0, 0));
        notesBox.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 10; -fx-padding: 20; " +
                         "-fx-border-color: #7dd3fc; -fx-border-radius: 10; -fx-border-width: 1;");
        
        Label notesTitle = new Label("ℹ️ Informations importantes");
        notesTitle.setStyle("-fx-font-weight: bold; -fx-font-family: 'Inter'; -fx-text-fill: #0369a1;");
        
        Label note1 = new Label("• Le PIN est optionnel si le client n'en a pas configuré");
        note1.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: #475569; -fx-font-size: 13px;");
        
        Label note2 = new Label("• Commission agent: 1% du montant déposé");
        note2.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: #475569; -fx-font-size: 13px;");
        
        Label note3 = new Label("• Limite maximale par dépôt: 10 000 000 GNF");
        note3.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: #475569; -fx-font-size: 13px;");
        
        notesBox.getChildren().addAll(notesTitle, note1, note2, note3);
        
        formBox.getChildren().addAll(emailBox, amountBox, pinBox);
        depositBox.getChildren().addAll(header, formBox, buttonBox, notesBox);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(depositBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0));
        
        mainContent.getChildren().add(scrollPane);
    }
    
    private void showWithdrawInterface() {
        System.out.println("[AGENT] Affichage interface retrait");
        mainContent.getChildren().clear();
        
        VBox withdrawBox = new VBox(20);
        withdrawBox.setPadding(new Insets(30));
        withdrawBox.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                           "-fx-border-color: " + ColorScheme.BORDER + "; " +
                           "-fx-border-radius: 16; -fx-background-radius: 16; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.5, 0, 5);");
        
        // Header avec style de template
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label title = new Label("🏧 Retrait pour Client");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                      "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        Label subtitle = new Label("Débitez le compte d'un client");
        subtitle.setStyle("-fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(titleBox);
        
        // Formulaire
        VBox formBox = new VBox(20);
        formBox.setPadding(new Insets(25, 0, 0, 0));
        
        // Email client
        VBox emailBox = new VBox(8);
        Label emailLabel = new Label("📧 Email du client *");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        TextField clientEmailField = createStyledTextField("exemple@client.com");
        emailBox.getChildren().addAll(emailLabel, clientEmailField);
        
        // Montant
        VBox amountBox = new VBox(8);
        Label amountLabel = new Label("💰 Montant en GNF *");
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        TextField amountField = createStyledTextField("Ex: 50000");
        amountBox.getChildren().addAll(amountLabel, amountField);
        
        // PIN client
        VBox pinBox = new VBox(8);
        Label pinLabel = new Label("🔒 PIN du client *");
        pinLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        PasswordField clientPinField = createStyledPasswordField("4 chiffres, obligatoire");
        pinBox.getChildren().addAll(pinLabel, clientPinField);
        
        // Boutons d'action
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));
        
        Button checkBalanceButton = createStyledButton("💰 Vérifier le Solde", "info");
        checkBalanceButton.setOnAction(e -> {
            System.out.println("[AGENT] Bouton Vérifier Solde cliqué");
            checkClientBalance(clientEmailField.getText());
        });
        
        actionButtons.getChildren().addAll(checkBalanceButton);
        
        // Boutons de confirmation
        HBox confirmButtons = new HBox(15);
        confirmButtons.setAlignment(Pos.CENTER);
        confirmButtons.setPadding(new Insets(25, 0, 0, 0));
        
        Button cancelButton = createStyledButton("Annuler", "neutral");
        cancelButton.setOnAction(e -> {
            mainContent.getChildren().clear();
            initializeMainContent();
        });
        
        Button confirmButton = createStyledButton("💸 Confirmer le Retrait", "warning");
        confirmButton.setStyle(confirmButton.getStyle() + " -fx-font-size: 16px; -fx-padding: 12 40;");
        confirmButton.setOnAction(e -> {
            System.out.println("[AGENT] Bouton Confirmer Retrait cliqué");
            processWithdrawal(clientEmailField.getText(), amountField.getText(), clientPinField.getText());
        });
        
        confirmButtons.getChildren().addAll(cancelButton, confirmButton);
        
        formBox.getChildren().addAll(emailBox, amountBox, pinBox, actionButtons);
        
        // Notes importantes avec style d'alerte
        VBox notesBox = new VBox(10);
        notesBox.setPadding(new Insets(20, 0, 0, 0));
        notesBox.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 10; -fx-padding: 20; " +
                        "-fx-border-color: #fca5a5; -fx-border-radius: 10; -fx-border-width: 1;");
        
        Label notesTitle = new Label("⚠️ Instructions importantes");
        notesTitle.setStyle("-fx-font-weight: bold; -fx-font-family: 'Inter'; -fx-text-fill: #dc2626;");
        
        Label note1 = new Label("• Le PIN est OBLIGATOIRE pour le retrait");
        note1.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: #7f1d1d; -fx-font-size: 13px;");
        
        Label note2 = new Label("• Commission agent: 1% du montant retiré");
        note2.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: #7f1d1d; -fx-font-size: 13px;");
        
        Label note3 = new Label("• Limite maximale par retrait: 5 000 000 GNF");
        note3.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: #7f1d1d; -fx-font-size: 13px;");
        
        notesBox.getChildren().addAll(notesTitle, note1, note2, note3);
        
        withdrawBox.getChildren().addAll(header, formBox, confirmButtons, notesBox);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(withdrawBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        mainContent.getChildren().add(scrollPane);
    }
    
    private void showOperationsInterface() {
        System.out.println("[AGENT] Affichage interface opérations");
        mainContent.getChildren().clear();
        
        VBox operationsBox = new VBox(20);
        operationsBox.setPadding(new Insets(30));
        operationsBox.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                             "-fx-border-color: " + ColorScheme.BORDER + "; " +
                             "-fx-border-radius: 16; -fx-background-radius: 16; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.5, 0, 5);");
        
        // Header avec style de template
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label title = new Label("📋 Historique de Mes Opérations");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                      "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        Label subtitle = new Label("Suivez toutes vos transactions effectuées");
        subtitle.setStyle("-fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(titleBox);
        
        // Statistiques avec style de cartes
        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; " +
                        "-fx-border-color: " + ColorScheme.BORDER + "; -fx-border-radius: 12;");
        
        VBox todayBox = createModernStatBox("Aujourd'hui", "0", "0 GNF", "today");
        VBox weekBox = createModernStatBox("7 derniers jours", "0", "0 GNF", "week");
        VBox monthBox = createModernStatBox("30 derniers jours", "0", "0 GNF", "month");
        VBox commissionBox = createModernStatBox("Commissions", "0 GNF", "En attente: 0 GNF", "commission");
        
        statsBox.getChildren().addAll(todayBox, weekBox, monthBox, commissionBox);
        
        // Filtres avec style de template
        VBox filtersContainer = new VBox(15);
        filtersContainer.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-padding: 20;");
        
        Label filtersLabel = new Label("🔍 Filtres de recherche");
        filtersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                            "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        HBox filtersBox = new HBox(20);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        
        // Période
        VBox periodBox = new VBox(8);
        Label periodLabel = new Label("Période");
        periodLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        ComboBox<String> periodCombo = createStyledComboBox();
        periodCombo.getItems().addAll("Aujourd'hui", "7 derniers jours", "30 derniers jours", "Tous");
        periodCombo.setValue("Aujourd'hui");
        periodCombo.setPrefWidth(180);
        periodBox.getChildren().addAll(periodLabel, periodCombo);
        
        // Type
        VBox typeBox = new VBox(8);
        Label typeLabel = new Label("Type d'opération");
        typeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                         "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        ComboBox<String> typeCombo = createStyledComboBox();
        typeCombo.getItems().addAll("Tous", "Dépôt", "Retrait");
        typeCombo.setValue("Tous");
        typeCombo.setPrefWidth(150);
        typeBox.getChildren().addAll(typeLabel, typeCombo);
        
        // Recherche
        VBox searchBox = new VBox(8);
        Label searchLabel = new Label("Recherche par client");
        searchLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        TextField searchField = createStyledTextField("Email du client...");
        searchField.setPrefWidth(250);
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Boutons de filtres
        HBox filterButtons = new HBox(12);
        filterButtons.setAlignment(Pos.CENTER_LEFT);
        filterButtons.setPadding(new Insets(25, 0, 0, 0));
        
        Button filterButton = createStyledButton("🔍 Appliquer", "primary");
        Button clearButton = createStyledButton("🔄 Réinitialiser", "neutral");
        
        filterButtons.getChildren().addAll(filterButton, clearButton);
        
        filtersBox.getChildren().addAll(periodBox, typeBox, searchBox);
        filtersContainer.getChildren().addAll(filtersLabel, filtersBox, filterButtons);
        
        // Tableau avec style de template
        TableView<Transaction> transactionsTable = new TableView<>();
        transactionsTable.setPrefHeight(400);
        transactionsTable.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                                 "-fx-border-color: " + ColorScheme.BORDER + "; " +
                                 "-fx-border-radius: 10; -fx-background-radius: 10; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.5, 0, 2);");
        transactionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-padding: 12 15; -fx-font-family: 'Inter';";
        String headerStyle = "-fx-background-color: " + ColorScheme.GRADIENT_PRIMARY + "; " +
                           "-fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: white; -fx-border-color: transparent;";
        
        // Configuration des colonnes
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date/Heure");
        dateCol.setPrefWidth(150);
        dateCol.setStyle(headerStyle);
        dateCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateTransaction();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        TableColumn<Transaction, String> idCol = new TableColumn<>("N° Transaction");
        idCol.setPrefWidth(140);
        idCol.setStyle(headerStyle);
        idCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        idCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNumeroTransaction())
        );
        
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        typeCol.setStyle(headerStyle);
        typeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType())
        );
        typeCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(columnStyle);
                    if ("DEPOT".equalsIgnoreCase(item)) {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.SUCCESS + 
                                "; -fx-font-weight: bold;");
                    } else if ("RETRAIT".equalsIgnoreCase(item)) {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.DANGER + 
                                "; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        TableColumn<Transaction, String> clientCol = new TableColumn<>("Client");
        clientCol.setPrefWidth(200);
        clientCol.setStyle(headerStyle);
        clientCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        clientCol.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            if (desc.contains("Client: ")) {
                int start = desc.indexOf("Client: ");
                return new javafx.beans.property.SimpleStringProperty(
                    desc.substring(start + 8)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("Client non spécifié");
        });
        
        TableColumn<Transaction, String> amountCol = new TableColumn<>("Montant");
        amountCol.setPrefWidth(120);
        amountCol.setStyle(headerStyle);
        amountCol.setCellValueFactory(cellData -> {
            double montant = cellData.getValue().getMontant();
            String sign = montant >= 0 ? "+" : "";
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%s%,.0f GNF", sign, Math.abs(montant))
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
                    setStyle(columnStyle);
                    if (item.startsWith("+")) {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.SUCCESS + 
                                "; -fx-font-weight: bold;");
                    } else {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.DANGER + 
                                "; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        TableColumn<Transaction, String> commissionCol = new TableColumn<>("Commission");
        commissionCol.setPrefWidth(120);
        commissionCol.setStyle(headerStyle);
        commissionCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        commissionCol.setCellValueFactory(cellData -> {
            double montant = Math.abs(cellData.getValue().getMontant());
            double commission = montant * 0.01;
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f GNF", commission)
            );
        });
        
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(250);
        descCol.setStyle(headerStyle);
        descCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        descCol.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            if (desc.contains("(Client: ")) {
                desc = desc.substring(0, desc.indexOf("(Client: "));
            }
            return new javafx.beans.property.SimpleStringProperty(desc);
        });
        
        transactionsTable.getColumns().addAll(dateCol, idCol, typeCol, clientCol, amountCol, commissionCol, descCol);
        
        // Info label avec style
        HBox infoBox = new HBox();
        infoBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label infoLabel = new Label("Total affiché: 0 transactions - Montant total: 0 GNF");
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + ColorScheme.DARK + "; -fx-font-size: 14px; " +
                          "-fx-padding: 10 15; -fx-background-color: #f8fafc; " +
                          "-fx-background-radius: 8; -fx-border-color: " + ColorScheme.BORDER + ";");
        
        infoBox.getChildren().add(infoLabel);
        
        // Boutons d'action
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));
        
        Button refreshButton = createStyledButton("🔄 Actualiser", "primary");
        Button exportButton = createStyledButton("📊 Exporter", "success");
        Button printButton = createStyledButton("🖨️ Imprimer", "neutral");
        
        actionButtons.getChildren().addAll(refreshButton, exportButton, printButton);
        
        // Actions
        filterButton.setOnAction(e -> {
            loadAgentTransactions(transactionsTable, infoLabel, periodCombo.getValue(), 
                                typeCombo.getValue(), searchField.getText());
            updateAgentStatistics(todayBox, weekBox, monthBox, commissionBox);
        });
        
        clearButton.setOnAction(e -> {
            periodCombo.setValue("Aujourd'hui");
            typeCombo.setValue("Tous");
            searchField.clear();
            loadAgentTransactions(transactionsTable, infoLabel, "Aujourd'hui", "Tous", "");
            updateAgentStatistics(todayBox, weekBox, monthBox, commissionBox);
        });
        
        refreshButton.setOnAction(e -> {
            loadAgentTransactions(transactionsTable, infoLabel, periodCombo.getValue(), 
                                typeCombo.getValue(), searchField.getText());
            updateAgentStatistics(todayBox, weekBox, monthBox, commissionBox);
        });
        
        exportButton.setOnAction(e -> {
            showStyledAlert("Info", "Export Excel en développement", "info");
        });
        
        printButton.setOnAction(e -> {
            showStyledAlert("Info", "Impression en développement", "info");
        });
        
        // Assemblage
        operationsBox.getChildren().addAll(
            header, statsBox, filtersContainer, 
            transactionsTable, infoBox, actionButtons
        );
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(operationsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        mainContent.getChildren().add(scrollPane);
        
        // Chargement initial
        loadAgentTransactions(transactionsTable, infoLabel, "Aujourd'hui", "Tous", "");
        updateAgentStatistics(todayBox, weekBox, monthBox, commissionBox);
    }
    
    private void showCommissionsInterface() {
        System.out.println("[AGENT] Affichage interface commissions");
        mainContent.getChildren().clear();
        
        VBox commissionsBox = new VBox(20);
        commissionsBox.setPadding(new Insets(30));
        commissionsBox.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                              "-fx-border-color: " + ColorScheme.BORDER + "; " +
                              "-fx-border-radius: 16; -fx-background-radius: 16; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.5, 0, 5);");
        
        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label title = new Label("💵 Mes Commissions");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                      "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        Label subtitle = new Label("Suivez vos revenus de commission");
        subtitle.setStyle("-fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(titleBox);
        
        // Statistiques avec style de cartes
        HBox statsBox = new HBox(25);
        statsBox.setPadding(new Insets(25));
        statsBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; " +
                        "-fx-border-color: " + ColorScheme.BORDER + "; -fx-border-radius: 12;");
        
        VBox totalBox = createCommissionStatBox("Total commissions", 
            commissionDAO.getTotalCommissions(currentUser.getUtilisateurId()), "#8b5cf6");
        
        VBox pendingBox = createCommissionStatBox("En attente", 
            commissionDAO.getPendingCommissions(currentUser.getUtilisateurId()), "#f59e0b");
        
        VBox paidBox = createCommissionStatBox("Payées", 
            commissionDAO.getPaidCommissions(currentUser.getUtilisateurId()), "#10b981");
        
        statsBox.getChildren().addAll(totalBox, pendingBox, paidBox);
        
        // Tableau
        Label tableTitle = new Label("📊 Historique des commissions");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + ColorScheme.DARK + "; -fx-padding: 0 0 15 0;");
        
        TableView<com.ewallet.core.models.Commission> table = new TableView<>();
        table.setPrefHeight(350);
        table.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                     "-fx-border-color: " + ColorScheme.BORDER + "; " +
                     "-fx-border-radius: 10; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.5, 0, 2);");
        
        // Configuration des colonnes pour commissions
        String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-padding: 12 15; -fx-font-family: 'Inter';";
        String headerStyle = "-fx-background-color: " + ColorScheme.GRADIENT_PRIMARY + "; " +
                           "-fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: white; -fx-border-color: transparent;";
        
        TableColumn<com.ewallet.core.models.Commission, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);
        idCol.setStyle(headerStyle);
        idCol.setCellFactory(col -> new TableCell<com.ewallet.core.models.Commission, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    setStyle(columnStyle);
                }
            }
        });
        
        TableColumn<com.ewallet.core.models.Commission, Double> montantCol = new TableColumn<>("Montant");
        montantCol.setCellValueFactory(new PropertyValueFactory<>("montantCommission"));
        montantCol.setPrefWidth(130);
        montantCol.setStyle(headerStyle);
        montantCol.setCellFactory(col -> new TableCell<com.ewallet.core.models.Commission, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%,.0f GNF", item));
                    setStyle(columnStyle + " -fx-text-fill: #059669; -fx-font-weight: bold;");
                }
            }
        });
        
        TableColumn<com.ewallet.core.models.Commission, Double> pourcentageCol = new TableColumn<>("%");
        pourcentageCol.setCellValueFactory(new PropertyValueFactory<>("pourcentage"));
        pourcentageCol.setPrefWidth(80);
        pourcentageCol.setStyle(headerStyle);
        pourcentageCol.setCellFactory(col -> new TableCell<com.ewallet.core.models.Commission, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", item));
                    setStyle(columnStyle);
                }
            }
        });
        
        TableColumn<com.ewallet.core.models.Commission, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(160);
        dateCol.setStyle(headerStyle);
        dateCol.setCellFactory(col -> createStyledTableCell(columnStyle));
        dateCol.setCellValueFactory(cellData -> {
            com.ewallet.core.models.Commission c = cellData.getValue();
            if (c.getDateCommission() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    c.getDateCommission().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        TableColumn<com.ewallet.core.models.Commission, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutCol.setPrefWidth(110);
        statutCol.setStyle(headerStyle);
        statutCol.setCellFactory(col -> new TableCell<com.ewallet.core.models.Commission, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(columnStyle);
                    if ("PENDING".equals(item)) {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.WARNING + 
                                "; -fx-font-weight: bold;");
                    } else if ("PAID".equals(item)) {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.SUCCESS + 
                                "; -fx-font-weight: bold;");
                    } else if ("CANCELLED".equals(item)) {
                        setStyle(columnStyle + " -fx-text-fill: " + ColorScheme.DANGER + 
                                "; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        table.getColumns().addAll(idCol, montantCol, pourcentageCol, dateCol, statutCol);
        
        // Charger les commissions
        try {
            List<com.ewallet.core.models.Commission> commissions = 
                commissionDAO.findByAgentId(currentUser.getUtilisateurId());
            ObservableList<com.ewallet.core.models.Commission> observableList = 
                FXCollections.observableArrayList(commissions);
            table.setItems(observableList);
        } catch (Exception e) {
            System.err.println("[AGENT] Erreur chargement commissions: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button refreshButton = createStyledButton("🔄 Rafraîchir", "primary");
        refreshButton.setOnAction(e -> {
            try {
                List<com.ewallet.core.models.Commission> newCommissions = 
                    commissionDAO.findByAgentId(currentUser.getUtilisateurId());
                table.setItems(FXCollections.observableArrayList(newCommissions));
                updateCommissionsStats();
                showStyledAlert("Succès", "Liste des commissions rafraîchie !", "success");
            } catch (Exception ex) {
                System.err.println("[AGENT] Erreur rafraîchissement: " + ex.getMessage());
                showError("Erreur", "Impossible de rafraîchir la liste des commissions");
            }
        });
        
        buttonBox.getChildren().add(refreshButton);
        
        // Info avec style d'alerte
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(20, 0, 0, 0));
        infoBox.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 10; -fx-padding: 20; " +
                       "-fx-border-color: #7dd3fc; -fx-border-radius: 10; -fx-border-width: 1;");
        
        Label infoTitle = new Label("ℹ️ Informations sur les commissions");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-font-family: 'Inter'; -fx-text-fill: #0369a1;");
        
        Label infoLabel = new Label("Les commissions sont payées par l'administrateur mensuellement. " +
                                  "Contactez le support pour toute question concernant vos paiements.");
        infoLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 13px; -fx-text-fill: #475569;");
        infoLabel.setWrapText(true);
        
        infoBox.getChildren().addAll(infoTitle, infoLabel);
        
        commissionsBox.getChildren().addAll(
            header, statsBox, tableTitle, table, buttonBox, infoBox
        );
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(commissionsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        mainContent.getChildren().add(scrollPane);
    }
    
    private void showProfileInterface() {
        Stage profileStage = new Stage();
        profileStage.setTitle("👤 Mon Profil");
        profileStage.initModality(Modality.WINDOW_MODAL);
        profileStage.initOwner(MainApp.getPrimaryStage());
        
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background: " + ColorScheme.BG_LIGHT + ";");
        
        // ===== HEADER AVEC INFOS =====
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10;");
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 48px;");
        avatarBox.getChildren().add(avatar);
        
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(currentUser.getPrenom() + " " + currentUser.getNom());
        nameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + ColorScheme.DARK + ";");
        Label roleLabel = new Label("Agent E-Wallet");
        roleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        Label emailLabel = new Label(currentUser.getEmail());
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        infoBox.getChildren().addAll(nameLabel, roleLabel, emailLabel);
        header.getChildren().addAll(avatarBox, infoBox);
        
        // ===== FORMULAIRE =====
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(25));
        formBox.setStyle("-fx-background-color: white; -fx-border-radius: 10; " +
                    "-fx-border-color: " + ColorScheme.BORDER + ";");
        
        Label formTitle = new Label("📋 Informations Personnelles");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + ColorScheme.DARK + ";");
        
        // Champ Nom
        Label nomLabel = new Label("Nom *");
        nomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ColorScheme.DARK + ";");
        TextField nomField = createStyledTextField(currentUser.getNom());
        
        // Champ Prénom
        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ColorScheme.DARK + ";");
        TextField prenomField = createStyledTextField(currentUser.getPrenom());
        
        // Champ Email (non-éditable)
        Label emailLabel2 = new Label("Email (non-modifiable)");
        emailLabel2.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ColorScheme.DARK + ";");
        TextField emailField = createStyledTextField(currentUser.getEmail());
        emailField.setEditable(false);
        emailField.setStyle("-fx-text-fill: #999999;");
        
        // Champ Téléphone
        Label phoneLabel = new Label("Téléphone");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ColorScheme.DARK + ";");
        TextField phoneField = createStyledTextField(
            currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        
        // Champ Adresse
        Label addressLabel = new Label("Adresse");
        addressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ColorScheme.DARK + ";");
        TextArea addressField = new TextArea(
            currentUser.getAdresse() != null ? currentUser.getAdresse() : "");
        addressField.setPrefHeight(80);
        addressField.setStyle("-fx-control-inner-background: white; -fx-border-color: " + 
                            ColorScheme.BORDER + "; -fx-padding: 10;");
        addressField.setWrapText(true);
        
        formBox.getChildren().addAll(
            formTitle,
            nomLabel, nomField,
            prenomLabel, prenomField,
            emailLabel2, emailField,
            phoneLabel, phoneField,
            addressLabel, addressField
        );
        
        // ===== SECTION MOT DE PASSE =====
        VBox passwordBox = new VBox(15);
        passwordBox.setPadding(new Insets(25));
        passwordBox.setStyle("-fx-background-color: #fef7cd; -fx-border-radius: 10; " +
                            "-fx-border-color: #fde047;");
        
        Label passwordTitle = new Label("🔒 Changer le mot de passe");
        passwordTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #854d0e;");
        
        Label currentPassLabel = new Label("Mot de passe actuel *");
        currentPassLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #854d0e;");
        PasswordField currentPassField = createStyledPasswordField("Votre mot de passe actuel");
        
        Label newPassLabel = new Label("Nouveau mot de passe *");
        newPassLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #854d0e;");
        PasswordField newPassField = createStyledPasswordField("Minimum 8 caractères");
        
        Label confirmPassLabel = new Label("Confirmer le mot de passe *");
        confirmPassLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #854d0e;");
        PasswordField confirmPassField = createStyledPasswordField("Confirmez le nouveau");
        
        Label passwordHint = new Label("⚠️ Laisser vide pour ne pas modifier");
        passwordHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #854d0e; -fx-font-style: italic;");
        
        passwordBox.getChildren().addAll(
            passwordTitle,
            currentPassLabel, currentPassField,
            newPassLabel, newPassField,
            confirmPassLabel, confirmPassField,
            passwordHint
        );
        
        // ===== BOUTONS =====
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button saveButton = createStyledButton("💾 Sauvegarder", "success");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> {
            // Validation
            if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty()) {
                showError("Validation", "Le nom et prénom sont obligatoires");
                return;
            }
            
            // Mettre à jour le profil
            Map<String, Object> profileResult = profileService.updateProfile(
                currentUser,
                nomField.getText().trim(),
                prenomField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
            );
            
            if ((boolean) profileResult.get("success")) {
                // Le profil a été mis à jour
                currentUser = (Utilisateur) profileResult.get("utilisateur");
                SessionManager.updateCurrentUser(currentUser);
                
                // Afficher les changements
                StringBuilder message = new StringBuilder("✅ Profil mis à jour avec succès!\n\n");
                Map<String, String> changes = (Map<String, String>) profileResult.get("changes");
                if (changes != null && !changes.isEmpty()) {
                    for (String change : changes.values()) {
                        message.append("• ").append(change).append("\n");
                    }
                }
                
                // Mettre à jour le mot de passe si fourni
                if (!newPassField.getText().isEmpty()) {
                    Map<String, Object> passResult = profileService.changePassword(
                        currentUser.getEmail(),
                        currentPassField.getText(),
                        newPassField.getText(),
                        confirmPassField.getText()
                    );
                    
                    if ((boolean) passResult.get("success")) {
                        message.append("• Mot de passe modifié avec succès\n");
                        message.append("⚠️ Vous devrez vous reconnecter avec le nouveau mot de passe\n");
                    } else {
                        showError("Erreur mot de passe", (String) passResult.get("message"));
                        return;
                    }
                }
                
                showSuccess("Succès", message.toString());
                welcomeLabel.setText("👋 Bienvenue, Agent " + currentUser.getPrenom() + " " + currentUser.getNom());
                profileStage.close();
            } else {
                showError("Erreur", (String) profileResult.get("message"));
            }
        });
        
        Button closeButton = createStyledButton("Fermer", "neutral");
        closeButton.setPrefWidth(150);
        closeButton.setOnAction(e -> profileStage.close());
        
        buttonBox.getChildren().addAll(saveButton, closeButton);
        
        // ===== ASSEMBLAGE =====
        ScrollPane scrollPane = new ScrollPane();
        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(20));
        scrollContent.getChildren().addAll(header, formBox, passwordBox, buttonBox);
        scrollPane.setContent(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + ColorScheme.BG_LIGHT + ";");
        
        double width = Math.min(700, Screen.getPrimary().getBounds().getWidth() * 0.9);
        double height = Math.min(900, Screen.getPrimary().getBounds().getHeight() * 0.9);
        
        Scene scene = new Scene(scrollPane, width, height);
        profileStage.setScene(scene);
        profileStage.showAndWait();
    }
    
    private void saveProfileChanges(String nouveauNom, String nouveauPrenom, 
                                  String nouveauTelephone, String nouvelleAdresse,
                                  String currentPassword, String newPassword, 
                                  String confirmPassword) {
        try {
            ProfileService profileService = new ProfileService();
            
            boolean passwordUpdate = false;
            Map<String, Object> passwordResult = null;
            
            // 1. Gérer le changement de mot de passe si demandé
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                passwordResult = profileService.changePassword(
                    currentUser.getEmail(), 
                    currentPassword, 
                    newPassword, 
                    confirmPassword
                );
                
                if (!(boolean) passwordResult.get("success")) {
                    showError("Erreur mot de passe", (String) passwordResult.get("message"));
                    return;
                }
                
                passwordUpdate = true;
            }
            
            // 2. Mettre à jour les informations de profil
            Map<String, Object> profileResult = profileService.updateProfile(
                currentUser, 
                nouveauNom, 
                nouveauPrenom, 
                nouveauTelephone, 
                nouvelleAdresse
            );
            
            // 3. Afficher les résultats
            if ((boolean) profileResult.get("success")) {
                String message = "✅ Modifications enregistrées:\n\n";
                
                Map<String, String> changes = (Map<String, String>) profileResult.get("changes");
                if (changes != null && !changes.isEmpty()) {
                    for (Map.Entry<String, String> entry : changes.entrySet()) {
                        message += "• " + entry.getValue() + "\n";
                    }
                }
                
                if (passwordUpdate) {
                    message += "• Mot de passe modifié avec succès\n";
                    message += "⚠️ Vous devrez utiliser votre nouveau mot de passe à la prochaine connexion.\n";
                }
                
                welcomeLabel.setText("👋 Bienvenue, Agent " + currentUser.getPrenom() + " " + currentUser.getNom());
                SessionManager.updateCurrentUser(currentUser);
                
                showSuccess("Profil mis à jour", message);
            } else {
                String errorMsg = (String) profileResult.get("message");
                if ("Aucune modification détectée".equals(errorMsg)) {
                    if (passwordUpdate) {
                        showInfo("Information", "Seul le mot de passe a été modifié.");
                    } else {
                        showInfo("Information", "Aucune modification détectée.");
                    }
                } else {
                    showError("Erreur", errorMsg);
                }
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de sauvegarder les modifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private double calculateAgentRating() {
        try {
            Map<String, Object> monthStats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "MONTH");
            
            int totalTransactions = (int) monthStats.getOrDefault("total_transactions", 0);
            double totalAmount = (double) monthStats.getOrDefault("total_amount", 0.0);
            
            double noteTransactions = Math.min(2.0, totalTransactions / 10.0);
            double noteAmount = Math.min(2.0, totalAmount / 1000000.0);
            double baseNote = 4.0;
            
            return Math.min(5.0, baseNote + noteTransactions + noteAmount);
            
        } catch (Exception e) {
            return 4.0;
        }
    }
    
    private void showHelpInterface() {
        Stage helpStage = new Stage();
        helpStage.setTitle("❓ Aide & Support Agent");
        helpStage.initModality(Modality.WINDOW_MODAL);
        helpStage.initOwner(MainApp.getPrimaryStage());
        
        TabPane mainTabs = new TabPane();
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mainTabs.setStyle("-fx-background-color: white;");
        
        // Tab 1: Guide rapide
        Tab guideTab = new Tab("📋 Guide Rapide");
        guideTab.setClosable(false);
        
        ScrollPane guideScroll = new ScrollPane();
        guideScroll.setFitToWidth(true);
        guideScroll.setStyle("-fx-background: transparent; -fx-background-color: #f8fafc;");
        
        VBox guideContent = new VBox(20);
        guideContent.setPadding(new Insets(25));
        guideContent.setStyle("-fx-background-color: #f8fafc;");
        
        // Section Dépôt
        VBox depositGuide = createHelpSection("💰 DÉPÔT CLIENT",
            "PROCÉDURE EXACTE:\n\n" +
            "1. Vérifier identité client (Carte nationale obligatoire)\n" +
            "2. Saisir l'email EXACT du client (complet avec @)\n" +
            "3. Confirmer montant À VOIX HAUTE avec le client\n" +
            "4. PIN client: Optionnel pour dépôt\n" +
            "5. Commission: 1% DU MONTANT DÉPOSÉ\n" +
            "6. Limites système: Maximum 10 000 000 GNF\n" +
            "7. Reçu obligatoire avec numéro transaction unique",
            ColorScheme.PRIMARY);
        
        // Section Retrait
        VBox withdrawGuide = createHelpSection("🏧 RETRAIT CLIENT",
            "PROCÉDURE EXACTE - OBLIGATOIRE:\n\n" +
            "1. Vérifications: Carte identité + photo\n" +
            "2. PIN client: OBLIGATOIRE pour retrait\n" +
            "3. Commission: 1% DU MONTANT RETIRÉ\n" +
            "4. Limites système: Maximum 5 000 000 GNF\n" +
            "5. Distribution argent: Compter DEVANT client\n" +
            "6. Sécurité argent: Caisse verrouillée",
            ColorScheme.SECONDARY);
        
        // Section Sécurité
        VBox securityGuide = createHelpSection("🔒 SÉCURITÉ & INCIDENTS",
            "PROCÉDURES D'URGENCE:\n\n" +
            "🆘 PROBLÈME TRANSACTION:\n" +
            "• Noter N° transaction\n" +
            "• Appeler SUPPORT URGENCE\n\n" +
            "🔴 CLIENT AGRESSIF:\n" +
            "• Ne jamais argumenter\n" +
            "• Appeler SUPERVISEUR immédiatement\n\n" +
            "⚠️ DOUTES SUR BILLETS:\n" +
            "• \"À vérifier avec superviseur\"\n" +
            "• Signaler SANS faire attendre client",
            ColorScheme.DANGER);
        
        // Section FAQ
        VBox faqGuide = createHelpSection("❓ QUESTIONS FRÉQUENTES",
            "RÉPONSES PRÉCISES À DONNER AUX CLIENTS:\n\n" +
            "Q: Pourquoi la commission de 1%?\n" +
            "R: Frais de service pour opérations physiques.\n\n" +
            "Q: Délai de crédit après dépôt?\n" +
            "R: IMMÉDIAT (visible dans 30 secondes).\n\n" +
            "Q: Montant maximum dépôt/retrait?\n" +
            "R: Dépôt: 10M GNF | Retrait: 5M GNF.\n\n" +
            "Q: Commissions payées quand?\n" +
            "R: Tous les 25 du mois.\n\n",
            ColorScheme.SUCCESS);
        
        guideContent.getChildren().addAll(depositGuide, withdrawGuide, securityGuide, faqGuide);
        guideScroll.setContent(guideContent);
        guideTab.setContent(guideScroll);
        
        // Tab 2: Contacts
        Tab contactsTab = new Tab("📞 Contacts");
        contactsTab.setClosable(false);
        
        VBox contactsContent = new VBox(20);
        contactsContent.setPadding(new Insets(25));
        contactsContent.setStyle("-fx-background-color: #f8fafc;");
        
        // Contacts d'urgence
        VBox emergencyBox = new VBox(15);
        emergencyBox.setPadding(new Insets(20));
        emergencyBox.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 10; " +
                            "-fx-border-color: #fca5a5; -fx-border-radius: 10; -fx-border-width: 1;");
        
        Label emergencyTitle = new Label("🆘 CONTACTS URGENCE 24/7");
        emergencyTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                              "-fx-text-fill: #dc2626;");
        
        VBox emergencyList = new VBox(10);
        
        String[] emergencies = {
            "🔴 Urgence technique: +224 627 68 54 10",
            "🔴 Superviseur direct: Mme. Hadiatou",
            "🔴 Sécurité/Incident: +224 666 777 888",
            "🔴 Numéro d'urgence: +224 627 00 11 22"
        };
        
        for (String contact : emergencies) {
            Label contactLabel = new Label(contact);
            contactLabel.setStyle("-fx-font-size: 13px; -fx-font-family: 'Inter'; " +
                                "-fx-text-fill: #7f1d1d; -fx-font-weight: 600;");
            contactLabel.setWrapText(true);
            emergencyList.getChildren().add(contactLabel);
        }
        
        emergencyBox.getChildren().addAll(emergencyTitle, emergencyList);
        contactsContent.getChildren().addAll(emergencyBox);
        
        contactsTab.setContent(contactsContent);
        
        // Tab 3: Statistiques
        Tab statsTab = new Tab("📊 Mes Statistiques");
        statsTab.setClosable(false);
        
        VBox statsContent = new VBox(20);
        statsContent.setPadding(new Insets(25));
        statsContent.setStyle("-fx-background-color: #f8fafc;");
        
        Label statsTitle = new Label("📈 PERFORMANCE AGENT");
        statsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        try {
            Map<String, Object> todayStats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "TODAY");
            Map<String, Object> monthStats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "MONTH");
            
            int todayTransactions = (int) todayStats.getOrDefault("total_transactions", 0);
            double todayAmount = (double) todayStats.getOrDefault("total_amount", 0.0);
            int monthTransactions = (int) monthStats.getOrDefault("total_transactions", 0);
            double monthAmount = (double) monthStats.getOrDefault("total_amount", 0.0);
            double monthCommission = (double) monthStats.getOrDefault("total_commission", 0.0);
            
            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(20);
            statsGrid.setVgap(20);
            statsGrid.setPadding(new Insets(20, 0, 0, 0));
            
            VBox todayStatBox = createDetailedStatBox("Aujourd'hui", 
                String.valueOf(todayTransactions), 
                String.format("%,.0f GNF", todayAmount),
                String.format("%,.0f GNF", todayAmount * 0.01),
                ColorScheme.PRIMARY);
            
            VBox monthStatBox = createDetailedStatBox("Ce mois", 
                String.valueOf(monthTransactions), 
                String.format("%,.0f GNF", monthAmount),
                String.format("%,.0f GNF", monthCommission),
                ColorScheme.SUCCESS);
            
            double targetAmount = 50000000;
            double progress = (monthAmount / targetAmount) * 100;
            
            VBox targetBox = new VBox(10);
            targetBox.setPadding(new Insets(20));
            targetBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                             "-fx-border-color: " + ColorScheme.BORDER + "; -fx-border-radius: 10;");
            
            Label targetTitle = new Label("🎯 Objectif mensuel");
            targetTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                               "-fx-text-fill: " + ColorScheme.WARNING + ";");
            
            ProgressBar progressBar = new ProgressBar(progress / 100);
            progressBar.setPrefWidth(300);
            progressBar.setStyle("-fx-accent: " + ColorScheme.WARNING + "; -fx-pref-height: 10px;");
            
            Label progressLabel = new Label(String.format("%,.0f GNF / %,.0f GNF (%.1f%%)", 
                monthAmount, targetAmount, progress));
            progressLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                                 "-fx-text-fill: #854d0e; -fx-font-weight: bold;");
            
            targetBox.getChildren().addAll(targetTitle, progressBar, progressLabel);
            
            statsGrid.add(todayStatBox, 0, 0);
            statsGrid.add(monthStatBox, 1, 0);
            statsGrid.add(targetBox, 0, 1, 2, 1);
            
            statsContent.getChildren().addAll(statsTitle, statsGrid);
            
        } catch (Exception e) {
            Label errorLabel = new Label("❌ Impossible de charger les statistiques: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px; -fx-font-family: 'Inter';");
            statsContent.getChildren().addAll(statsTitle, errorLabel);
        }
        
        statsTab.setContent(statsContent);
        
        mainTabs.getTabs().addAll(guideTab, contactsTab, statsTab);
        
        // Bouton fermer
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));
        buttonBox.setStyle("-fx-background-color: white;");
        
        Button closeButton = createStyledButton("Fermer", "neutral");
        closeButton.setOnAction(e -> helpStage.close());
        buttonBox.getChildren().add(closeButton);
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(mainTabs);
        mainLayout.setBottom(buttonBox);
        mainLayout.setStyle("-fx-background-color: white;");
        
        Scene scene = new Scene(mainLayout, 850, 700);
        helpStage.setScene(scene);
        helpStage.showAndWait();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    private void initializeMainContent() {
       
        mainContent.getChildren().clear();
        
        VBox dashboardBox = new VBox(20);
        dashboardBox.setPadding(new Insets(20));
        dashboardBox.setStyle("-fx-background-color: transparent;");
        
        // Header avec date
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-padding: 0 0 20 0; -fx-border-color: " + ColorScheme.BORDER + 
                         "; -fx-border-width: 0 0 1 0;");
        
        VBox titleBox = new VBox(5);
        Label title = new Label("📊 Tableau de Bord Agent");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                      "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        Label dateLabel = new Label("Connecté en tant que " + currentUser.getEmail());
        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                         "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        
        titleBox.getChildren().addAll(title, dateLabel);
        headerBox.getChildren().add(titleBox);
        
        // Statistiques rapides
        HBox quickStats = new HBox(20);
        quickStats.setPadding(new Insets(20, 0, 0, 0));
        
        VBox todayBox = createQuickStatBox("Aujourd'hui", 
            String.valueOf(getTodayTransactions()), 
            String.format("%,.0f GNF", getTodayAmount()),
            ColorScheme.PRIMARY);
        
        VBox commissionBox = createQuickStatBox("Commissions ce mois", 
            String.format("%,.0f GNF", getMonthCommissions()),
            "En attente: " + String.format("%,.0f GNF", 
                commissionDAO.getPendingCommissions(currentUser.getUtilisateurId())),
            ColorScheme.SUCCESS);
        
        quickStats.getChildren().addAll(todayBox, commissionBox);
        
        // Boutons d'action principaux
        GridPane actionGrid = new GridPane();
        actionGrid.setHgap(20);
        actionGrid.setVgap(20);
        actionGrid.setPadding(new Insets(30, 0, 0, 0));
        
        // Première ligne
        VBox depositAction = createActionBox("💰 Dépôt", "Créditer un compte client", "primary", depositButton);
        VBox withdrawAction = createActionBox("🏧 Retrait", "Débiter un compte client", "success", withdrawButton);
        
        // Deuxième ligne
        VBox operationsAction = createActionBox("📋 Opérations", "Voir l'historique", "info", operationsButton);
        VBox commissionsAction = createActionBox("💵 Commissions", "Suivre mes revenus", "warning", commissionsButton);
        
        actionGrid.add(depositAction, 0, 0);
        actionGrid.add(withdrawAction, 1, 0);
        actionGrid.add(operationsAction, 0, 1);
        actionGrid.add(commissionsAction, 1, 1);
        
        dashboardBox.getChildren().addAll(headerBox, quickStats, actionGrid);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(dashboardBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0));
        
        mainContent.getChildren().add(scrollPane);
    }
    
    private VBox createActionBox(String title, String description, String type, Button actionButton) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setPrefWidth(250);
        box.setStyle("-fx-background-color: " + ColorScheme.CARD_BG + "; " +
                   "-fx-border-color: " + ColorScheme.BORDER + "; " +
                   "-fx-border-radius: 12; -fx-background-radius: 12; " +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.5, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + ColorScheme.DARK + ";");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-font-family: 'Inter'; " +
                         "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        descLabel.setWrapText(true);
        
        VBox.setMargin(actionButton, new Insets(10, 0, 0, 0));
        
        box.getChildren().addAll(titleLabel, descLabel, actionButton);
        return box;
    }
    
    private VBox createQuickStatBox(String title, String value, String subValue, String color) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(20));
        box.setPrefWidth(200);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                   "-fx-border-color: " + color + "30; -fx-border-radius: 12; " +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.5, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + color + ";");
        
        Label subLabel = new Label(subValue);
        subLabel.setStyle("-fx-font-size: 12px; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: " + ColorScheme.NEUTRAL + ";");
        
        box.getChildren().addAll(titleLabel, valueLabel, subLabel);
        return box;
    }
    
    private Button createStyledButton(String text, String type) {
        Button button = new Button(text);
        String baseStyle = "-fx-text-fill: white; -fx-font-weight: 600; -fx-font-family: 'Inter'; " +
                          "-fx-padding: 10 20; -fx-background-radius: 8; " +
                          "-fx-cursor: hand; -fx-font-size: 14px; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0.5, 0, 1);";
        
        switch (type.toLowerCase()) {
            case "primary":
                button.setStyle(baseStyle + " -fx-background-color: " + ColorScheme.GRADIENT_PRIMARY + ";");
                break;
            case "success":
                button.setStyle(baseStyle + " -fx-background-color: " + ColorScheme.GRADIENT_SUCCESS + ";");
                break;
            case "warning":
                button.setStyle(baseStyle + " -fx-background-color: " + ColorScheme.GRADIENT_WARNING + ";");
                break;
            case "danger":
                button.setStyle(baseStyle + " -fx-background-color: " + ColorScheme.GRADIENT_DANGER + ";");
                break;
            case "info":
                button.setStyle(baseStyle + " -fx-background-color: " + ColorScheme.GRADIENT_INFO + ";");
                break;
            case "neutral":
                button.setStyle(baseStyle + " -fx-background-color: " + ColorScheme.NEUTRAL + ";");
                break;
            default:
                button.setStyle(baseStyle + " -fx-background-color: " + ColorScheme.PRIMARY + ";");
        }
        
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0.5, 0, 2); " +
                          "-fx-translate-y: -1;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle + " -fx-background-color: " + 
                (type.equals("primary") ? ColorScheme.GRADIENT_PRIMARY :
                 type.equals("success") ? ColorScheme.GRADIENT_SUCCESS :
                 type.equals("warning") ? ColorScheme.GRADIENT_WARNING :
                 type.equals("danger") ? ColorScheme.GRADIENT_DANGER :
                 type.equals("info") ? ColorScheme.GRADIENT_INFO :
                 type.equals("neutral") ? ColorScheme.NEUTRAL : ColorScheme.PRIMARY) + ";");
        });
        
        return button;
    }
    
    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setStyle("-fx-background-color: white; -fx-border-color: " + ColorScheme.BORDER + "; " +
                      "-fx-border-radius: 8; -fx-padding: 10 15; -fx-font-size: 14px; " +
                      "-fx-font-family: 'Inter';");
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-background-color: white; -fx-border-color: " + 
                             ColorScheme.PRIMARY + "; -fx-border-radius: 8; " +
                             "-fx-padding: 10 15; -fx-font-size: 14px; -fx-font-family: 'Inter';");
            } else {
                field.setStyle("-fx-background-color: white; -fx-border-color: " + ColorScheme.BORDER + "; " +
                             "-fx-border-radius: 8; -fx-padding: 10 15; -fx-font-size: 14px; " +
                             "-fx-font-family: 'Inter';");
            }
        });
        
        return field;
    }
    
    private PasswordField createStyledPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setStyle("-fx-background-color: white; -fx-border-color: " + ColorScheme.BORDER + "; " +
                      "-fx-border-radius: 8; -fx-padding: 10 15; -fx-font-size: 14px; " +
                      "-fx-font-family: 'Inter';");
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-background-color: white; -fx-border-color: " + 
                             ColorScheme.PRIMARY + "; -fx-border-radius: 8; " +
                             "-fx-padding: 10 15; -fx-font-size: 14px; -fx-font-family: 'Inter';");
            } else {
                field.setStyle("-fx-background-color: white; -fx-border-color: " + ColorScheme.BORDER + "; " +
                             "-fx-border-radius: 8; -fx-padding: 10 15; -fx-font-size: 14px; " +
                             "-fx-font-family: 'Inter';");
            }
        });
        
        return field;
    }
    
    private ComboBox<String> createStyledComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setStyle("-fx-background-color: white; -fx-border-color: " + ColorScheme.BORDER + "; " +
                      "-fx-border-radius: 8; -fx-padding: 8 12; -fx-font-family: 'Inter';");
        return combo;
    }
    
    private VBox createModernStatBox(String title, String value, String subValue, String type) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(20));
        box.setPrefWidth(200);
        box.setMinWidth(200);
        
        String[] colors = ColorScheme.STAT_COLORS.getOrDefault(type, 
            new String[]{ColorScheme.PRIMARY, ColorScheme.PRIMARY_DARK, "#eff6ff"});
        
        String boxStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-border-radius: 12; " +
            "-fx-border-color: %s40; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.5, 0, 2);",
            colors[2], colors[0]
        );
        box.setStyle(boxStyle);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: #6b7280; -fx-font-weight: 500;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + colors[0] + ";");
        
        Label subLabel = new Label(subValue);
        subLabel.setStyle("-fx-font-size: 12px; -fx-font-family: 'Inter'; " +
                        "-fx-text-fill: #9ca3af;");
        
        box.getChildren().addAll(titleLabel, valueLabel, subLabel);
        return box;
    }
    
    private VBox createCommissionStatBox(String title, double value, String color) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(20));
        box.setPrefWidth(180);
        box.setStyle("-fx-background-color: " + color + "10; " +
                    "-fx-border-radius: 12; -fx-border-color: " + color + "30; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.5, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: #6b7280;");
        
        Label valueLabel = new Label(String.format("%,.0f GNF", value));
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + color + ";");
        
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }
    
    private VBox createProfileStatBox(String title, String value, String color) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(15));
        box.setPrefWidth(120);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 10; " +
                    "-fx-border-color: " + color + "30; -fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.5, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: #64748b; -fx-font-weight: 500;");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + color + ";");
        
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }
    
    private VBox createDetailedStatBox(String periode, String transactions, String montant, String commission, String color) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setPrefWidth(200);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-border-color: " + color + "30; -fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.5, 0, 2);");
        
        Label periodeLabel = new Label(periode);
        periodeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                            "-fx-text-fill: " + color + ";");
        
        VBox transBox = new VBox(3);
        Label transLabel = new Label("Transactions:");
        transLabel.setStyle("-fx-font-size: 12px; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: #64748b;");
        Label transValue = new Label(transactions);
        transValue.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + color + ";");
        transBox.getChildren().addAll(transLabel, transValue);
        
        VBox amountBox = new VBox(3);
        Label amountLabel = new Label("Montant traité:");
        amountLabel.setStyle("-fx-font-size: 12px; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: #64748b;");
        Label amountValue = new Label(montant);
        amountValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                           "-fx-text-fill: #334155;");
        amountBox.getChildren().addAll(amountLabel, amountValue);
        
        VBox commBox = new VBox(3);
        Label commLabel = new Label("Commission:");
        commLabel.setStyle("-fx-font-size: 12px; -fx-font-family: 'Inter'; " +
                         "-fx-text-fill: #64748b;");
        Label commValue = new Label(commission);
        commValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                         "-fx-text-fill: #10b981;");
        commBox.getChildren().addAll(commLabel, commValue);
        
        box.getChildren().addAll(periodeLabel, transBox, amountBox, commBox);
        return box;
    }
    
    private VBox createHelpSection(String title, String content, String color) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-border-color: " + color + "30; -fx-border-radius: 10; " +
                        "-fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.5, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Inter'; " +
                          "-fx-text-fill: " + color + ";");
        
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-size: 12px; -fx-font-family: 'Inter'; " +
                            "-fx-text-fill: #475569; -fx-line-spacing: 4;");
        contentLabel.setWrapText(true);
        
        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }
    
    private <T> TableCell<T, String> createStyledTableCell(String style) {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(style);
                }
            }
        };
    }
    
    private void loadAgentTransactions(TableView<Transaction> table, Label infoLabel, 
                                    String period, String typeFilter, String searchTerm) {
        try {
            java.util.Date startDate = null;
            java.util.Date endDate = null;
            
            switch (period) {
                case "Aujourd'hui":
                    startDate = java.sql.Date.valueOf(java.time.LocalDate.now());
                    endDate = startDate;
                    break;
                case "7 derniers jours":
                    startDate = java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(7));
                    endDate = java.sql.Date.valueOf(java.time.LocalDate.now());
                    break;
                case "30 derniers jours":
                    startDate = java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(30));
                    endDate = java.sql.Date.valueOf(java.time.LocalDate.now());
                    break;
            }
            
            List<Transaction> allTransactions = transactionService.getTransactionsByAgent(
                currentUser.getUtilisateurId(), startDate, endDate
            );
            
            List<Transaction> filteredTransactions = new ArrayList<>();
            for (Transaction t : allTransactions) {
                if (!"Tous".equals(typeFilter)) {
                    if (typeFilter.equals("Dépôt") && !"DEPOT".equalsIgnoreCase(t.getType())) {
                        continue;
                    }
                    if (typeFilter.equals("Retrait") && !"RETRAIT".equalsIgnoreCase(t.getType())) {
                        continue;
                    }
                }
                
                if (searchTerm != null && !searchTerm.isEmpty()) {
                    String desc = t.getDescription().toLowerCase();
                    if (!desc.contains(searchTerm.toLowerCase())) {
                        continue;
                    }
                }
                
                filteredTransactions.add(t);
            }
            
            ObservableList<Transaction> observableList = FXCollections.observableArrayList(filteredTransactions);
            table.setItems(observableList);
            
            double totalAmount = filteredTransactions.stream()
                .mapToDouble(Transaction::getMontant)
                .sum();
            
            infoLabel.setText(String.format(
                "Total affiché: %,d transactions - Montant total: %,.0f GNF",
                filteredTransactions.size(), Math.abs(totalAmount)
            ));
            
        } catch (Exception e) {
            System.err.println("[AGENT] Erreur chargement transactions: " + e.getMessage());
            infoLabel.setText("❌ Erreur de chargement des données");
        }
    }
    
    private void updateAgentStatistics(VBox todayBox, VBox weekBox, VBox monthBox, VBox commissionBox) {
        try {
            Map<String, Object> todayStats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "TODAY");
            
            int todayTransactions = (int) todayStats.getOrDefault("total_transactions", 0);
            double todayAmount = (double) todayStats.getOrDefault("total_amount", 0.0);
            
            updateStatBox(todayBox, 
                String.valueOf(todayTransactions),
                String.format("%,.0f GNF", todayAmount)
            );
            
            Map<String, Object> weekStats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "WEEK");
            
            int weekTransactions = (int) weekStats.getOrDefault("total_transactions", 0);
            double weekAmount = (double) weekStats.getOrDefault("total_amount", 0.0);
            
            updateStatBox(weekBox,
                String.valueOf(weekTransactions),
                String.format("%,.0f GNF", weekAmount)
            );
            
            Map<String, Object> monthStats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "MONTH");
            
            int monthTransactions = (int) monthStats.getOrDefault("total_transactions", 0);
            double monthAmount = (double) monthStats.getOrDefault("total_amount", 0.0);
            double monthCommission = (double) monthStats.getOrDefault("total_commission", 0.0);
            
            updateStatBox(monthBox,
                String.valueOf(monthTransactions),
                String.format("%,.0f GNF", monthAmount)
            );
            
            double pendingCommissions = commissionDAO.getPendingCommissions(currentUser.getUtilisateurId());
            updateStatBox(commissionBox,
                String.format("%,.0f GNF", monthCommission),
                String.format("En attente: %,.0f GNF", pendingCommissions)
            );
            
        } catch (Exception e) {
            System.err.println("[AGENT] Erreur mise à jour statistiques: " + e.getMessage());
        }
    }
    
    private void updateStatBox(VBox box, String value, String subValue) {
        if (box.getChildren().size() >= 3) {
            Label valueLabel = (Label) box.getChildren().get(1);
            Label subLabel = (Label) box.getChildren().get(2);
            valueLabel.setText(value);
            subLabel.setText(subValue);
        }
    }
    
    private void processDeposit(String clientEmail, String amountStr, String pin) {
        try {
            if (clientEmail == null || clientEmail.trim().isEmpty()) {
                showError("Erreur", "Veuillez entrer l'email du client");
                return;
            }
            
            if (amountStr == null || amountStr.trim().isEmpty()) {
                showError("Erreur", "Veuillez entrer le montant");
                return;
            }
            
            double montant;
            try {
                montant = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                showError("Erreur", "Montant invalide. Entrez un nombre valide.");
                return;
            }
            
            if (montant <= 0) {
                showError("Erreur", "Le montant doit être supérieur à 0");
                return;
            }
            
            if (montant > 10000000) {
                showError("Erreur", "Le montant maximum pour un dépôt est de 10 000 000 GNF");
                return;
            }
            
            boolean success = transactionService.effectuerDepot(
                currentUser.getUtilisateurId(),
                clientEmail.trim(), 
                montant, 
                pin != null ? pin.trim() : ""
            );
            
            if (success) {
                String message = String.format(
                    "✅ Dépôt réussi !\n\n" +
                    "• Client: %s\n" +
                    "• Montant: %,.0f GNF\n" +
                    "• Commission agent: %,.0f GNF (1%%)\n" +
                    "• Heure: %s\n\n" +
                    "Le solde du client a été crédité avec succès.",
                    clientEmail, montant, montant * 0.01, new java.util.Date()
                );
                
                showSuccess("Dépôt Effectué", message);
                showDepositInterface();
            } else {
                showError("Échec du Dépôt", 
                    "❌ Le dépôt n'a pas pu être effectué.\n\n" +
                    "Vérifiez:\n" +
                    "• L'email du client\n" +
                    "• La connexion à la base de données\n" +
                    "• Le PIN du client (si configuré)");
            }
            
        } catch (Exception e) {
            System.err.println("[AGENT] Exception lors du dépôt: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur Système", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }
    
    private void processWithdrawal(String clientEmail, String amountStr, String pin) {
        try {
            if (clientEmail == null || clientEmail.trim().isEmpty()) {
                showError("Erreur", "Veuillez entrer l'email du client");
                return;
            }
            
            if (amountStr == null || amountStr.trim().isEmpty()) {
                showError("Erreur", "Veuillez entrer le montant");
                return;
            }
            
            if (pin == null || pin.trim().isEmpty()) {
                showError("Erreur", "Le PIN est obligatoire pour le retrait");
                return;
            }
            
            double montant;
            try {
                montant = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                showError("Erreur", "Montant invalide. Entrez un nombre valide.");
                return;
            }
            
            if (montant <= 0) {
                showError("Erreur", "Le montant doit être supérieur à 0");
                return;
            }
            
            if (montant > 5000000) {
                showError("Erreur", "Le montant maximum pour un retrait est de 5 000 000 GNF");
                return;
            }
            
            boolean success = transactionService.effectuerRetrait(
                currentUser.getUtilisateurId(),
                clientEmail.trim(), 
                montant, 
                pin.trim()
            );
            
            if (success) {
                String message = String.format(
                    "✅ Retrait réussi !\n\n" +
                    "• Client: %s\n" +
                    "• Montant retiré: %,.0f GNF\n" +
                    "• Commission agent: %,.0f GNF (1%%)\n" +
                    "• Heure: %s\n\n" +
                    "Veuillez remettre l'argent au client.",
                    clientEmail, montant, montant * 0.01, new java.util.Date()
                );
                
                showSuccess("Retrait Effectué", message);
                showWithdrawInterface();
            } else {
                showError("Échec du Retrait", 
                    "❌ Le retrait n'a pas pu être effectué.\n\n" +
                    "Raisons possibles:\n" +
                    "• Solde insuffisant\n" +
                    "• PIN incorrect\n" +
                    "• Client non trouvé\n" +
                    "• Problème technique");
            }
            
        } catch (Exception e) {
            System.err.println("[AGENT] Exception lors du retrait: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur Système", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }
    
    private void checkClientBalance(String clientEmail) {
        if (clientEmail == null || clientEmail.trim().isEmpty()) {
            showError("Erreur", "Veuillez entrer l'email du client");
            return;
        }
        
        double solde = transactionService.getSoldeClient(clientEmail.trim());
        
        if (solde >= 0) {
            String message = String.format(
                "💰 Informations Client\n\n" +
                "• Email: %s\n" +
                "• Solde disponible: %,.0f GNF\n\n" +
                "Le client peut effectuer un retrait jusqu'à cette limite.",
                clientEmail, solde
            );
            
            showInfo("Solde Client", message);
        } else {
            showError("Client Introuvable", 
                "❌ Impossible de trouver le client avec l'email: " + clientEmail + "\n\n" +
                "Vérifiez que:\n" +
                "• L'email est correct\n" +
                "• Le client est inscrit dans le système\n" +
                "• Le client a un portefeuille actif");
        }
    }
    
    private void showStyledAlert(String title, String message, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setWidth(500);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Inter';");
        
        switch (type.toLowerCase()) {
            case "success":
                dialogPane.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #86efac; " +
                                  "-fx-font-family: 'Inter';");
                break;
            case "error":
                dialogPane.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fca5a5; " +
                                  "-fx-font-family: 'Inter';");
                break;
            case "warning":
                dialogPane.setStyle("-fx-background-color: #fefce8; -fx-border-color: #fde047; " +
                                  "-fx-font-family: 'Inter';");
                break;
            case "info":
                dialogPane.setStyle("-fx-background-color: #f0f9ff; -fx-border-color: #7dd3fc; " +
                                  "-fx-font-family: 'Inter';");
                break;
            default:
                dialogPane.setStyle("-fx-background-color: #f8fafc; -fx-border-color: " + ColorScheme.BORDER + "; " +
                                  "-fx-font-family: 'Inter';");
        }
        
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message) {
        showStyledAlert(title, message, "info");
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setWidth(500);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fca5a5; " +
                          "-fx-font-family: 'Inter';");
        
        alert.showAndWait();
    }
    
    private void showSuccess(String title, String message) {
        showStyledAlert(title, message, "success");
    }
    
    private void showInfo(String title, String message) {
        showStyledAlert(title, message, "info");
    }
    
    private void navigateToLogin() {
        MainApp.showLogin();
    }
    
    private double getTodayTransactions() {
        try {
            Map<String, Object> stats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "TODAY");
            return (int) stats.getOrDefault("total_transactions", 0);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private double getTodayAmount() {
        try {
            Map<String, Object> stats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "TODAY");
            return (double) stats.getOrDefault("total_amount", 0.0);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private double getMonthCommissions() {
        try {
            Map<String, Object> stats = transactionService.getAgentStatistics(
                currentUser.getUtilisateurId(), "MONTH");
            return (double) stats.getOrDefault("total_commission", 0.0);
        } catch (Exception e) {
            return 0;
        }
    }
}