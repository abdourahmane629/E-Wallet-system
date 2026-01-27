package com.ewallet.gui.controllers;

import com.ewallet.core.models.Utilisateur;
import com.ewallet.core.models.Transaction;
import com.ewallet.core.models.Commission;
import com.ewallet.core.models.Portefeuille;
import com.ewallet.core.models.StatutUtilisateur;
import com.ewallet.core.dao.UtilisateurDAO;
import com.ewallet.core.dao.RoleDAO;
import com.ewallet.core.dao.TransactionDAO;
import com.ewallet.core.dao.PortefeuilleDAO;
import com.ewallet.core.dao.CommissionDAO;
import com.ewallet.core.services.ReportService;
import com.ewallet.core.services.CommissionService;
import com.ewallet.core.services.ProfileService;
import com.ewallet.core.utils.ExportUtil;
import com.ewallet.core.utils.SecurityUtil;
import com.ewallet.core.utils.ValidationUtil;
import com.ewallet.gui.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.property.SimpleStringProperty;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import com.ewallet.core.services.NotificationService;
import com.ewallet.core.models.Notification;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class AdminDashboardController {

    private Utilisateur utilisateur;
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private RoleDAO roleDAO = new RoleDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    private PortefeuilleDAO portefeuilleDAO = new PortefeuilleDAO();
    private CommissionDAO commissionDAO = new CommissionDAO();
    private CommissionService commissionService = new CommissionService();
    private ReportService reportService = new ReportService();
    private NotificationService notificationService;
    private ProfileService profileService = new ProfileService();

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Button notificationsButton;
    // Onglet Créer utilisateur
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button createUserButton;
    
    // Onglet Gérer utilisateurs
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, Integer> userIdColumn;
    @FXML private TableColumn<Utilisateur, String> nomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, String> telephoneColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, String> statutColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsColumn;
    @FXML private Button refreshUsersButton;
    @FXML private Button exportExcelButton;
    @FXML private Button exportPDFButton;
    
    // Recherche avancée
    @FXML private TextField searchUserField;
    @FXML private ComboBox<String> filterRoleCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private Button btnSearch;
    @FXML private Button btnClearSearch;
    @FXML private TextField filterUserPhoneField;
    @FXML private Button btnSearchUsers;
    @FXML private Button btnClearUsers;
    // Onglet Transactions
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionIdColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> montantColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> agentColumn;
    @FXML private Button refreshTransactionsButton;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private TextField searchTransactionField;
    @FXML private ComboBox<String> filterTransactionStatusCombo;
    @FXML private Button btnSearchTransactions;
    @FXML private Button btnClearTransactions;
    @FXML private Button exportTransactionsButton;
    @FXML private Label transactionStatsLabel;

    // Onglet Portefeuilles
    @FXML private TableView<Portefeuille> walletsTable;
    @FXML private TableColumn<Portefeuille, String> walletIdColumn;
    @FXML private TableColumn<Portefeuille, String> clientColumn;
    @FXML private TableColumn<Portefeuille, Double> soldeColumn;
    @FXML private TableColumn<Portefeuille, String> statutColumnWallet;
    @FXML private TableColumn<Portefeuille, Void> actionsColumnWallet;
    @FXML private Button refreshWalletsButton;
    @FXML private TextField searchWalletField;
    @FXML private TextField searchWalletClientField;
    @FXML private ComboBox<String> filterWalletStatusCombo;
    @FXML private TextField filterMinBalanceField;
    @FXML private TextField filterMaxBalanceField;
    @FXML private Button btnSearchWallets;
    @FXML private Button btnClearWallets;
    @FXML private Button exportWalletsButton;
    @FXML private Label walletStatsLabel;
    @FXML private TableColumn<Portefeuille, Double> imiteRetraitQuotidienColumn;
    @FXML private TableColumn<Portefeuille, Double> limiteTransfertColumn;
    @FXML private TableColumn<Portefeuille, Double> limiteColumn;

    // Onglet Commissions
    @FXML private TableView<Commission> commissionsTable;
    @FXML private TableColumn<Commission, Integer> commissionIdColumn;
    @FXML private TableColumn<Commission, String> agentColumnCom;
    @FXML private TableColumn<Commission, Double> montantColumnCom;
    @FXML private TableColumn<Commission, String> pourcentageColumn;
    @FXML private TableColumn<Commission, String> dateColumnCom;
    @FXML private TableColumn<Commission, String> statutColumnCom;
    @FXML private TableColumn<Commission, Void> actionsColumnCom;
    @FXML private ComboBox<String> statutFilterCombo;
    @FXML private ComboBox<String> agentFilterCombo;
    @FXML private Button refreshCommissionsButton;
    @FXML private Button payerCommissionButton;
    @FXML private Button annulerCommissionButton;
    @FXML private Button exporterCommissionsButton;
    @FXML private DatePicker commissionStartDatePicker;
    @FXML private DatePicker commissionEndDatePicker;
    @FXML private Button btnSearchCommissions;
    @FXML private Button btnClearCommissions;
    @FXML private Label commissionStatsLabel;

    // Onglet Statistiques
    @FXML private Label totalUsersLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private Label totalVolumeLabel;
    @FXML private Label totalCommissionsLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label depositCountLabel;
    @FXML private Label withdrawalCountLabel;
    @FXML private Label transferCountLabel;
    @FXML private Label pendingCommissionsLabel;
    @FXML private Label paidCommissionsLabel;
    @FXML private Label cancelledCommissionsLabel;
    @FXML private Button refreshStatsButton;
    @FXML private Button exportStatsExcelButton;
    @FXML private Button exportStatsPDFButton;
    @FXML private Button generateReportButton;
    @FXML private Label dateLabel;
    @FXML private Button dashboardButton;

    @FXML
    public void initialize() {
        System.out.println("[OK] AdminDashboardController initialisé");
        
        // Récupérer l'utilisateur courant
        utilisateur = com.ewallet.core.utils.SessionManager.getCurrentUser();
        
        if (utilisateur != null && utilisateur.isAdmin()) {
            try {
                this.notificationService = new NotificationService(com.ewallet.core.DatabaseConfig.getConnection());
            } catch (SQLException e) {
                System.err.println("[ERROR] Erreur initialisation NotificationService: " + e.getMessage());
            }
            
            String nom = utilisateur.getPrenom() + " " + utilisateur.getNom();
            welcomeLabel.setText("Bienvenue Admin - " + nom);
            

            
            // Mettre à jour la date
            updateDate();
            
            // Appliquer les styles 
            applyMainStyles();
        
            // Initialiser les onglets
            initCreerUtilisateurTab();
            initGererUtilisateursTab();
            initTransactionsTab();
            initPortefeuillesTab();
            initCommissionsTab();
            
            // Charger les données initiales
            chargerUtilisateurs();
            chargerTransactions();
            chargerPortefeuilles();
            chargerCommissions();
            chargerStatistiques();
            updateNotificationBadge();
            
            System.out.println("[OK] Admin Dashboard affiché pour: " + utilisateur.getEmail());
        } else {
            afficherErreur("Erreur", "Accès refusé : Seuls les administrateurs peuvent accéder à cette page");
            MainApp.showLogin();
        }
    }


    
    // Méthode pour mettre à jour la date
    private void updateDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String formattedDate = today.format(formatter);
        dateLabel.setText("Aujourd'hui, " + formattedDate);
    }

    private void applyMainStyles() {
        // Appliquer la police Inter partout
        applyFontToAllNodes();
        
        ScrollPane mainScrollPane = null;
        // Configurer le ScrollPane principal si présent
        if (mainScrollPane != null) {
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Style de la scrollbar
            mainScrollPane.getStylesheets().add(getClass().getResource("/com/ewallet/gui/css/scrollbar.css").toExternalForm());
        }
        
        // Configurer les effets hover sur les boutons
        setupButtonHoverEffects();
        
        // Configurer les tableaux avec style alterné
        setupTableStyles();
    }

    private void applyFontToAllNodes() {
        // Cette méthode appliquerait la police Inter partout
        // Dans un projet réel, utiliseriez CSS
        System.out.println("[STYLE] Police Inter appliquée à l'interface Admin");
    }

    private void setupButtonHoverEffects() {
        // Appliquer des effets hover aux boutons principaux
        setupHoverEffectForButton(logoutButton, "#e74c3c", "#c0392b");
        setupHoverEffectForButton(notificationsButton, "#3498db", "#2980b9");
        
        // Appliquer aux autres boutons d'action
        if (createUserButton != null) {
            setupHoverEffectForGradientButton(createUserButton, "#4361ee", "#7209b7");
        }
        
        // Ajoutez d'autres boutons selon vos besoins
    }

    private void setupHoverEffectForButton(Button button, String baseColor, String hoverColor) {
        if (button == null) return;
        
        String originalStyle = button.getStyle();
        
        button.setOnMouseEntered(e -> {
            button.setStyle(originalStyle.replace(baseColor, hoverColor) + 
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.5, 0, 3);");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(originalStyle);
        });
    }

    private void setupHoverEffectForGradientButton(Button button, String color1, String color2) {
        if (button == null) return;
        
        String originalStyle = button.getStyle();
        
        button.setOnMouseEntered(e -> {
            button.setStyle(originalStyle + 
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.5, 0, 3);" +
                " -fx-translate-y: -2;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(originalStyle);
        });
    }

    private void setupTableStyles() {
        // Configuration des styles de tableaux
        if (usersTable != null) {
            setupAlternateTableRows(usersTable);
        }
        if (transactionsTable != null) {
            setupAlternateTableRows(transactionsTable);
        }
        if (walletsTable != null) {
            setupAlternateTableRows(walletsTable);
        }
        if (commissionsTable != null) {
            setupAlternateTableRows(commissionsTable);
        }
    }

    private <T> void setupAlternateTableRows(TableView<T> table) {
        table.setRowFactory(new javafx.util.Callback<TableView<T>, TableRow<T>>() {
            @Override
            public TableRow<T> call(TableView<T> tv) {
                return new TableRow<T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setStyle("");
                        } else {
                            if (getIndex() % 2 == 0) {
                                setStyle("-fx-background-color: white;");
                            } else {
                                setStyle("-fx-background-color: #f8fafc;");
                            }
                            
                            // Effet hover sur les lignes
                            setOnMouseEntered(e -> {
                                setStyle("-fx-background-color: #e0f2fe; -fx-cursor: hand;");
                            });
                            
                            setOnMouseExited(e -> {
                                if (getIndex() % 2 == 0) {
                                    setStyle("-fx-background-color: white;");
                                } else {
                                    setStyle("-fx-background-color: #f8fafc;");
                                }
                            });
                        }
                    }
                };
            }
        });
    }
    
    @FXML
    private void handleNotifications() {
        showNotificationsInterface();
    }

    private void showNotificationsInterface() {
        // Créer une nouvelle fenêtre/dialogue pour les notifications
        Stage notificationsStage = new Stage();
        notificationsStage.setTitle("Notifications Admin");
        notificationsStage.initModality(Modality.WINDOW_MODAL);
        notificationsStage.initOwner(MainApp.getPrimaryStage());
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f8f9fa;");
        
        Label title = new Label("🔔 Mes Notifications");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Boutons d'action
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button markAllReadButton = new Button("📌 Tout marquer comme lu");
        markAllReadButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        markAllReadButton.setOnAction(e -> markAllNotificationsAsRead());
        
        Button refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> loadNotificationsForDialog());
        
        actionButtons.getChildren().addAll(markAllReadButton, refreshButton);
        
        // Tableau des notifications
        TableView<Notification> notificationsTable = new TableView<>();
        notificationsTable.setPrefHeight(400);
        notificationsTable.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        // Colonne Statut (icône)
        TableColumn<Notification, String> statusCol = new TableColumn<>("");
        statusCol.setPrefWidth(40);
        statusCol.setCellValueFactory(cellData -> {
            boolean isRead = cellData.getValue().isEstLue();
            return new SimpleStringProperty(isRead ? "📌" : "🆕");
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
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px;");
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
                    }
                }
            }
        });
        
        // Colonne Titre
        TableColumn<Notification, String> titleCol = new TableColumn<>("Titre");
        titleCol.setPrefWidth(200);
        titleCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTitre())
        );
        
        // Colonne Message
        TableColumn<Notification, String> messageCol = new TableColumn<>("Message");
        messageCol.setPrefWidth(350);
        messageCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMessage())
        );
        
        // Colonne Date
        TableColumn<Notification, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateCreation();
            if (date != null) {
                return new SimpleStringProperty(
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new SimpleStringProperty("");
        });
        
        // Colonne Type
        TableColumn<Notification, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        typeCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType())
        );
        
        notificationsTable.getColumns().addAll(statusCol, titleCol, messageCol, dateCol, typeCol);
        
        // Double-clic pour marquer comme lu
        notificationsTable.setRowFactory(tv -> {
            TableRow<Notification> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Notification notif = row.getItem();
                    if (!notif.isEstLue()) {
                        markNotificationAsRead(notif);
                        loadNotificationsForDialog(); // Recharger
                    }
                }
            });
            return row;
        });
        
        // Compteur
        Label unreadCountLabel = new Label();
        unreadCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        
        // Bouton fermer
        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        closeButton.setOnAction(e -> notificationsStage.close());
        
        mainLayout.getChildren().addAll(title, actionButtons, notificationsTable, unreadCountLabel, closeButton);
        
        Scene scene = new Scene(mainLayout, 900, 600);
        notificationsStage.setScene(scene);
        
        // Charger les notifications
        loadNotificationsDataForTable(notificationsTable, unreadCountLabel);
        
        notificationsStage.showAndWait();
    }

    private void loadNotificationsDataForTable(TableView<Notification> table, Label countLabel) {
        try {
            if (notificationService == null || utilisateur == null) {
                countLabel.setText("Service de notifications non disponible");
                return;
            }
            
            List<Notification> notifications = notificationService.getUserNotifications(
                utilisateur.getUtilisateurId(), false
            );
            
            ObservableList<Notification> observableList = FXCollections.observableArrayList(notifications);
            table.setItems(observableList);
            
            // Compter les non lus
            long unreadCount = notifications.stream()
                .filter(n -> !n.isEstLue())
                .count();
            
            if (unreadCount > 0) {
                countLabel.setText("📊 " + unreadCount + " notification(s) non lue(s)");
                countLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                countLabel.setText("✅ Toutes vos notifications sont lues");
                countLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
            
        } catch (Exception e) {
            countLabel.setText("❌ Erreur chargement: " + e.getMessage());
            System.err.println("[ADMIN] Erreur chargement notifications: " + e.getMessage());
        }
    }

    private void loadNotificationsForDialog() {
        // Méthode utilitaire pour recharger dans le dialogue
    }

    private void markNotificationAsRead(Notification notification) {
        try {
            if (notificationService != null) {
                boolean success = notificationService.markAsRead(notification.getNotificationId());
                if (success) {
                    System.out.println("[ADMIN] Notification marquée comme lue: " + notification.getTitre());
                }
            }
        } catch (Exception e) {
            System.err.println("[ADMIN] Erreur marquage notification: " + e.getMessage());
        }
    }

    private void markAllNotificationsAsRead() {
        try {
            if (notificationService != null && utilisateur != null) {
                boolean success = notificationService.markAllAsRead(utilisateur.getUtilisateurId());
                if (success) {
                    System.out.println("[ADMIN] Toutes les notifications marquées comme lues");
                    updateNotificationBadge(); // Mettre à jour le badge
                    showAlert("Succès", "Toutes les notifications ont été marquées comme lues.");
                }
            }
        } catch (Exception e) {
            System.err.println("[ADMIN] Erreur marquage toutes notifications: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateNotificationBadge() {
        if (notificationService != null && utilisateur != null) {
            try {
                int unreadCount = notificationService.getUnreadCount(utilisateur.getUtilisateurId());
                if (unreadCount > 0) {
                    // Ajouter un badge visuel
                    System.out.println("[ADMIN] " + unreadCount + " notifications non lues");
                    // Mettre à jour le texte du bouton
                    if (notificationsButton != null) {
                        notificationsButton.setText("🔔 (" + unreadCount + ")");
                        notificationsButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14; -fx-min-width: 35; -fx-min-height: 35; -fx-background-radius: 17.5; -fx-cursor: hand;");
                    }
                } else {
                    if (notificationsButton != null) {
                        notificationsButton.setText("🔔");
                        notificationsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14; -fx-min-width: 35; -fx-min-height: 35; -fx-background-radius: 17.5; -fx-cursor: hand;");
                    }
                }
            } catch (Exception e) {
                System.err.println("[ADMIN] Erreur mise à jour badge: " + e.getMessage());
            }
        }
    }
    
    private void initCreerUtilisateurTab() {
        // Initialiser le ComboBox des rôles
        if (roleCombo != null) {
            roleCombo.getItems().addAll("ADMIN", "AGENT", "USER");
            roleCombo.setValue("USER");
            
            // Ajouter l'action listener pour le bouton créer
            if (createUserButton != null) {
                createUserButton.setOnAction(event -> handleCreateUser());
            }
        }
    }
    
    private void initGererUtilisateursTab() {
        if (usersTable != null) {
            // Configurer les colonnes
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
            nomColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().getPrenom() + " " + cellData.getValue().getNom()));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            roleColumn.setCellValueFactory(cellData -> {
                String roleName = cellData.getValue().getRoleName();
                if ("USER".equals(roleName)) {
                    return new SimpleStringProperty("CLIENT");
                }
                return new SimpleStringProperty(roleName);
            });
            statutColumn.setCellValueFactory(cellData -> {
                StatutUtilisateur statut = cellData.getValue().getStatut();
                return new SimpleStringProperty(statut != null ? statut.name() : "INACTIF");
            });
            
            // Colonne actions
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button detailsBtn = new Button("📋 Détails");
                private final Button editBtn = new Button("✏️ Modifier");
                private final Button toggleBtn = new Button("🔄 Statut");
                
                {
                    detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                    editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10;");
                    toggleBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10;");
                    
                    detailsBtn.setOnAction(e -> {
                        Utilisateur user = getTableView().getItems().get(getIndex());
                        showUserDetails(user);
                    });
                    
                    editBtn.setOnAction(e -> {
                        Utilisateur user = getTableView().getItems().get(getIndex());
                        editUser(user);
                    });
                    
                    toggleBtn.setOnAction(e -> {
                        Utilisateur user = getTableView().getItems().get(getIndex());
                        toggleUserStatus(user);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(5, detailsBtn, editBtn, toggleBtn);
                        hbox.setAlignment(Pos.CENTER);
                        setGraphic(hbox);
                    }
                }
            });
            
            // Ajouter les actions des boutons
            if (refreshUsersButton != null) {
                refreshUsersButton.setOnAction(event -> handleRefreshUsers());
            }
            
            if (exportExcelButton != null) {
                exportExcelButton.setOnAction(event -> handleExportUsersExcel());
            }
            
            if (exportPDFButton != null) {
                exportPDFButton.setOnAction(event -> handleExportUsersPDF());
            }
            
            // Initialiser la recherche avancée
            initRechercheAvancee();
        }
    }
    
    private void initRechercheAvancee() {
        // Initialiser les filtres
        if (filterRoleCombo != null) {
            filterRoleCombo.getItems().addAll("TOUS", "ADMIN", "AGENT", "USER");
            filterRoleCombo.setValue("TOUS");
        }
        
        if (filterStatusCombo != null) {
            filterStatusCombo.getItems().addAll("TOUS", "ACTIF", "INACTIF", "BLOQUE");
            filterStatusCombo.setValue("TOUS");
        }
        
        // Recherche en temps réel
        if (searchUserField != null) {
            searchUserField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() >= 2 || newValue.isEmpty()) {
                    performSearch();
                }
            });
        }
        
        if (btnSearch != null) {
            btnSearch.setOnAction(e -> performSearch());
        }
        
        if (btnClearSearch != null) {
            btnClearSearch.setOnAction(e -> {
                searchUserField.clear();
                filterRoleCombo.setValue("TOUS");
                filterStatusCombo.setValue("TOUS");
                chargerUtilisateurs();
            });
        }
    }
    
    private void initTransactionsTab() {
        if (transactionsTable != null) {
            transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("numeroTransaction"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            dateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().getDateTransaction() != null ?
                    cellData.getValue().getDateTransaction().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
            
            // Colonne agent (à récupérer depuis la transaction)
            agentColumn.setCellValueFactory(cellData -> {
                int agentId = cellData.getValue().getAgentId();
                if (agentId > 0) {
                    Utilisateur agent = utilisateurDAO.findById(agentId);
                    if (agent != null) {
                        return new SimpleStringProperty(agent.getPrenom() + " " + agent.getNom());
                    }
                }
                return new SimpleStringProperty("Système");
            });
            
            // Ajouter l'action du bouton rafraîchir
            if (refreshTransactionsButton != null) {
                refreshTransactionsButton.setOnAction(event -> handleRefreshTransactions());
            }
            
            // Initialiser les filtres
            initTransactionsFilters();
        }
    }
    
    private void initTransactionsFilters() {
        if (typeFilterCombo != null) {
            typeFilterCombo.getItems().addAll("TOUS", "DEPOT", "RETRAIT", "TRANSFERT");
            typeFilterCombo.setValue("TOUS");
        }
        
        if (filterTransactionStatusCombo != null) {
            filterTransactionStatusCombo.getItems().addAll("TOUS", "CONFIRME", "EN_ATTENTE", "ANNULE");
            filterTransactionStatusCombo.setValue("TOUS");
        }
        
        if (startDatePicker != null) {
            startDatePicker.setValue(java.time.LocalDate.now().minusDays(30));
        }
        
        if (endDatePicker != null) {
            endDatePicker.setValue(java.time.LocalDate.now());
        }
        
        if (btnSearchTransactions != null) {
            btnSearchTransactions.setOnAction(e -> filterTransactions());
        }
        
        if (btnClearTransactions != null) {
            btnClearTransactions.setOnAction(e -> clearTransactionFilters());
        }
        
        if (exportTransactionsButton != null) {
            exportTransactionsButton.setOnAction(e -> handleExportTransactions());
        }
    }
    
    private void filterTransactions() {
        String searchText = searchTransactionField.getText().trim();
        String typeFilter = typeFilterCombo.getValue();
        String statusFilter = filterTransactionStatusCombo.getValue();
        LocalDateTime startDate = startDatePicker.getValue() != null ? 
            startDatePicker.getValue().atStartOfDay() : null;
        LocalDateTime endDate = endDatePicker.getValue() != null ? 
            endDatePicker.getValue().atTime(23, 59, 59) : null;
        
        Map<String, Object> filters = new HashMap<>();
        
        if (searchText != null && !searchText.isEmpty()) {
            filters.put("search", searchText);
        }
        if (!"TOUS".equals(typeFilter)) {
            filters.put("type", typeFilter);
        }
        if (!"TOUS".equals(statusFilter)) {
            filters.put("statut", statusFilter);
        }
        if (startDate != null) {
            filters.put("start_date", startDate);
        }
        if (endDate != null) {
            filters.put("end_date", endDate);
        }
        
        try {
            List<Transaction> transactions = transactionDAO.findWithFilters(filters);
            ObservableList<Transaction> observableList = FXCollections.observableArrayList(transactions);
            transactionsTable.setItems(observableList);
            
            // Mettre à jour les statistiques
            updateTransactionStats(transactions);
            
            System.out.println("[INFO] " + transactions.size() + " transactions filtrées");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de filtrer les transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearTransactionFilters() {
        searchTransactionField.clear();
        typeFilterCombo.setValue("TOUS");
        filterTransactionStatusCombo.setValue("TOUS");
        startDatePicker.setValue(java.time.LocalDate.now().minusDays(30));
        endDatePicker.setValue(java.time.LocalDate.now());
        
        chargerTransactions();
    }
    
    private void updateTransactionStats(List<Transaction> transactions) {
        if (transactionStatsLabel != null) {
            double totalAmount = 0;
            int count = transactions.size();
            
            for (Transaction t : transactions) {
                totalAmount += t.getMontant();
            }
            
            String statsText = String.format("%d transaction(s) | Total: %,.0f GNF", count, totalAmount);
            transactionStatsLabel.setText(statsText);
        }
    }
    
    private void initPortefeuillesTab() {
        if (walletsTable != null) {
            // 1. Configuration des colonnes de base
            walletIdColumn.setCellValueFactory(new PropertyValueFactory<>("numeroPortefeuille"));
            
            // Colonne client
            clientColumn.setCellValueFactory(cellData -> {
                int userId = cellData.getValue().getUtilisateurId();
                Utilisateur user = utilisateurDAO.findById(userId);
                if (user != null) {
                    return new SimpleStringProperty(
                        user.getPrenom() + " " + user.getNom() + " (" + user.getEmail() + ")");
                }
                return new SimpleStringProperty("Inconnu");
            });
            
            soldeColumn.setCellValueFactory(cellData -> {
                double solde = cellData.getValue().getSolde();
                return new javafx.beans.property.SimpleDoubleProperty(solde).asObject();
            });
            
            // Format the display of solde column
            soldeColumn.setCellFactory(column -> new TableCell<Portefeuille, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f GNF", item));
                    }
                }
            });
            
            statutColumnWallet.setCellValueFactory(new PropertyValueFactory<>("statut"));
            
            // 2. Colonne limite - CORRECTION COMPLÈTE
            if (limiteColumn != null) {
                limiteColumn.setCellValueFactory(cellData -> {
                    Portefeuille wallet = cellData.getValue();
                    // Vérifier si les limites existent
                    double limiteRetrait = wallet.getLimiteRetraitQuotidien();
                    
                    // Debug pour voir les valeurs
                    System.out.println("[DEBUG LIMITES] Wallet " + wallet.getNumeroPortefeuille() + 
                                    " - Retrait: " + limiteRetrait + ", Transfert: " + wallet.getLimiteTransfert());
                    
                    // Retourner la limite de retrait comme valeur Double
                    return new javafx.beans.property.SimpleDoubleProperty(limiteRetrait).asObject();
                });
                
                // Ajouter un formateur pour l'affichage
                limiteColumn.setCellFactory(column -> new TableCell<Portefeuille, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            Portefeuille wallet = getTableRow().getItem();
                            if (wallet != null) {
                                String displayText = String.format("Retrait: %,.0f GNF\nTransfert: %,.0f GNF", 
                                    wallet.getLimiteRetraitQuotidien(), wallet.getLimiteTransfert());
                                setText(displayText);
                                setTooltip(new Tooltip(displayText.replace("\n", " / ")));
                            }
                            // Multi-ligne et alignement
                            setStyle("-fx-alignment: CENTER_LEFT; " +
                                    "-fx-padding: 8px 5px; " +
                                    "-fx-font-size: 12px;");
                        }
                    }
                });
            } else {
                System.err.println("[ERREUR] limiteColumn est null! Vérifiez le FXML");
            }
            
            // 3. Colonne actions
            actionsColumnWallet.setCellFactory(param -> new TableCell<Portefeuille, Void>() {
                private final Button detailsBtn = new Button("📋 Détails");
                private final Button editBtn = new Button("⚙️ Limites");
                private final Button toggleBtn = new Button("🔄 Statut");
                
                {
                    // Style des boutons
                    detailsBtn.setStyle("-fx-background-color: #3498db; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-font-size: 12px;");
                    editBtn.setStyle("-fx-background-color: #9b59b6; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-font-size: 12px;");
                    toggleBtn.setStyle("-fx-background-color: #27ae60; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-font-size: 12px;");
                    
                    // Actions des boutons
                    detailsBtn.setOnAction(e -> {
                        Portefeuille wallet = getTableView().getItems().get(getIndex());
                        showWalletDetails(wallet);
                    });
                    
                    editBtn.setOnAction(e -> {
                        Portefeuille wallet = getTableView().getItems().get(getIndex());
                        editWalletLimits(wallet);
                    });
                    
                    toggleBtn.setOnAction(e -> {
                        Portefeuille wallet = getTableView().getItems().get(getIndex());
                        toggleWalletStatus(wallet);
                    });
                    
                    // Effets hover
                    setupButtonHoverEffect(detailsBtn, "#3498db", "#2980b9");
                    setupButtonHoverEffect(editBtn, "#9b59b6", "#8e44ad");
                    setupButtonHoverEffect(toggleBtn, "#27ae60", "#229954");
                }
                
                private void setupButtonHoverEffect(Button button, String baseColor, String hoverColor) {
                    String originalStyle = button.getStyle();
                    button.setOnMouseEntered(event -> {
                        button.setStyle(originalStyle.replace(baseColor, hoverColor) + 
                                    " -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
                    });
                    button.setOnMouseExited(event -> {
                        button.setStyle(originalStyle);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(8, detailsBtn, editBtn, toggleBtn);
                        hbox.setAlignment(Pos.CENTER);
                        setGraphic(hbox);
                    }
                }
            });
            
            // 4. Configuration du bouton rafraîchir
            if (refreshWalletsButton != null) {
                refreshWalletsButton.setOnAction(event -> handleRefreshWallets());
                // Style du bouton
                refreshWalletsButton.setStyle("-fx-background-color: #3498db; " +
                                            "-fx-text-fill: white; " +
                                            "-fx-font-weight: bold; " +
                                            "-fx-padding: 8 16; " +
                                            "-fx-background-radius: 5;");
                refreshWalletsButton.setOnMouseEntered(e -> {
                    refreshWalletsButton.setStyle("-fx-background-color: #2980b9; " +
                                                "-fx-text-fill: white; " +
                                                "-fx-font-weight: bold; " +
                                                "-fx-padding: 8 16; " +
                                                "-fx-background-radius: 5; " +
                                                "-fx-cursor: hand;");
                });
                refreshWalletsButton.setOnMouseExited(e -> {
                    refreshWalletsButton.setStyle("-fx-background-color: #3498db; " +
                                                "-fx-text-fill: white; " +
                                                "-fx-font-weight: bold; " +
                                                "-fx-padding: 8 16; " +
                                                "-fx-background-radius: 5;");
                });
            }
            
            // 5. Initialiser les filtres
            initWalletsFilters();
            
            // 6. DEBUG - Vérifier que tout est initialisé
            System.out.println("[DEBUG] Initialisation portefeuilles terminée:");
            System.out.println("  - Table initialisée: " + (walletsTable != null));
            System.out.println("  - Colonne limite: " + (limiteColumn != null));
            System.out.println("  - Colonne solde: " + (soldeColumn != null));
            System.out.println("  - Bouton rafraîchir: " + (refreshWalletsButton != null));
        }
    }
    
    private void initWalletsFilters() {
        if (filterWalletStatusCombo != null) {
            filterWalletStatusCombo.getItems().addAll("TOUS", "ACTIF", "INACTIF", "BLOQUE");
            filterWalletStatusCombo.setValue("TOUS");
        }
        
        if (btnSearchWallets != null) {
            btnSearchWallets.setOnAction(e -> filterWallets());
        }
        
        if (btnClearWallets != null) {
            btnClearWallets.setOnAction(e -> clearWalletFilters());
        }
        
        if (exportWalletsButton != null) {
            exportWalletsButton.setOnAction(e -> handleExportWallets());
        }
    }
    
    private void filterWallets() {
        String searchText = searchWalletField.getText().trim();
        String clientText = searchWalletClientField.getText().trim();
        String statusFilter = filterWalletStatusCombo.getValue();
        Double minBalance = null;
        Double maxBalance = null;
        
        try {
            if (!filterMinBalanceField.getText().trim().isEmpty()) {
                minBalance = Double.parseDouble(filterMinBalanceField.getText().trim());
            }
            if (!filterMaxBalanceField.getText().trim().isEmpty()) {
                maxBalance = Double.parseDouble(filterMaxBalanceField.getText().trim());
            }
        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "Les montants doivent être des nombres valides");
            return;
        }
        
        Map<String, Object> filters = new HashMap<>();
        
        if (!searchText.isEmpty()) {
            filters.put("search_wallet", searchText);
        }
        if (!clientText.isEmpty()) {
            filters.put("search_client", clientText);
        }
        if (!"TOUS".equals(statusFilter)) {
            filters.put("statut", statusFilter);
        }
        if (minBalance != null) {
            filters.put("min_balance", minBalance);
        }
        if (maxBalance != null) {
            filters.put("max_balance", maxBalance);
        }
        
        try {
            List<Portefeuille> wallets = portefeuilleDAO.findWithFilters(filters);
            ObservableList<Portefeuille> observableList = FXCollections.observableArrayList(wallets);
            walletsTable.setItems(observableList);
            
            // Mettre à jour les statistiques
            updateWalletStats(wallets);
            
            System.out.println("[INFO] " + wallets.size() + " portefeuilles filtrés");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de filtrer les portefeuilles: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearWalletFilters() {
        searchWalletField.clear();
        searchWalletClientField.clear();
        filterWalletStatusCombo.setValue("TOUS");
        filterMinBalanceField.clear();
        filterMaxBalanceField.clear();
        
        chargerPortefeuilles();
    }
    
    private void updateWalletStats(List<Portefeuille> wallets) {
        if (walletStatsLabel != null) {
            double totalBalance = 0;
            int activeCount = 0;
            int blockedCount = 0;
            
            for (Portefeuille w : wallets) {
                totalBalance += w.getSolde();
                if ("ACTIF".equals(w.getStatut())) {
                    activeCount++;
                } else if ("BLOQUE".equals(w.getStatut())) {
                    blockedCount++;
                }
            }
            
            String statsText = String.format("%d portefeuille(s) | Actifs: %d | Bloqués: %d | Solde total: %,.0f GNF", 
                wallets.size(), activeCount, blockedCount, totalBalance);
            walletStatsLabel.setText(statsText);
        }
    }
    
    private void initCommissionsTab() {
        if (commissionsTable != null) {
            commissionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            
            // Colonne agent
            agentColumnCom.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getAgentComplet()));
            
            montantColumnCom.setCellValueFactory(new PropertyValueFactory<>("montantCommission"));
            pourcentageColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPourcentageFormatted()));
            dateColumnCom.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDateFormatted()));
            statutColumnCom.setCellValueFactory(new PropertyValueFactory<>("statut"));
            
            // Colonne actions
            actionsColumnCom.setCellFactory(param -> new TableCell<>() {
                private final Button detailsBtn = new Button("📋 Détails");
                private final Button payerBtn = new Button("💰 Payer");
                private final Button annulerBtn = new Button("❌ Annuler");
                
                {
                    detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                    payerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10;");
                    annulerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                    
                    detailsBtn.setOnAction(e -> {
                        Commission commission = getTableView().getItems().get(getIndex());
                        showCommissionDetails(commission);
                    });
                    
                    payerBtn.setOnAction(e -> {
                        Commission commission = getTableView().getItems().get(getIndex());
                        payerCommission(commission);
                    });
                    
                    annulerBtn.setOnAction(e -> {
                        Commission commission = getTableView().getItems().get(getIndex());
                        annulerCommission(commission);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Commission commission = getTableView().getItems().get(getIndex());
                        payerBtn.setDisable(!"PENDING".equals(commission.getStatut()));
                        annulerBtn.setDisable(!"PENDING".equals(commission.getStatut()));
                        
                        HBox hbox = new HBox(5, detailsBtn, payerBtn, annulerBtn);
                        hbox.setAlignment(Pos.CENTER);
                        setGraphic(hbox);
                    }
                }
            });
            
            // Ajouter les actions des boutons
            if (refreshCommissionsButton != null) {
                refreshCommissionsButton.setOnAction(event -> handleRefreshCommissions());
            }
            
            if (payerCommissionButton != null) {
                payerCommissionButton.setOnAction(event -> handlePayerCommission());
            }
            
            if (annulerCommissionButton != null) {
                annulerCommissionButton.setOnAction(event -> handleAnnulerCommission());
            }
            
            if (exporterCommissionsButton != null) {
                exporterCommissionsButton.setOnAction(event -> handleExporterCommissions());
            }
            
            // Initialiser les filtres
            initCommissionsFilters();

            // CHARGER LES AGENTS POUR LE FILTRE
            chargerAgentsPourFiltre();
        }
    }
    
    private void initCommissionsFilters() {
        if (statutFilterCombo != null) {
            statutFilterCombo.getItems().addAll("TOUS", "PENDING", "PAID", "CANCELLED");
            statutFilterCombo.setValue("TOUS");
        }
        
        if (commissionStartDatePicker != null) {
            commissionStartDatePicker.setValue(java.time.LocalDate.now().minusDays(30));
        }
        
        if (commissionEndDatePicker != null) {
            commissionEndDatePicker.setValue(java.time.LocalDate.now());
        }
        
        if (btnSearchCommissions != null) {
            btnSearchCommissions.setOnAction(e -> filterCommissions());
        }
        
        if (btnClearCommissions != null) {
            btnClearCommissions.setOnAction(e -> clearCommissionFilters());
        }
    }
    
    private void filterCommissions() {
        String statusFilter = statutFilterCombo.getValue();
        String agentFilter = agentFilterCombo.getValue();
        LocalDateTime startDate = commissionStartDatePicker.getValue() != null ? 
            commissionStartDatePicker.getValue().atStartOfDay() : null;
        LocalDateTime endDate = commissionEndDatePicker.getValue() != null ? 
            commissionEndDatePicker.getValue().atTime(23, 59, 59) : null;
        
        Map<String, Object> filters = new HashMap<>();
        
        if (!"TOUS".equals(statusFilter)) {
            filters.put("statut", statusFilter);
        }
        
        if (!"TOUS".equals(agentFilter)) {
            try {
                // Extraire l'ID de l'agent du texte formaté "ID - Nom Prénom (email)"
                String[] parts = agentFilter.split(" - ");
                if (parts.length > 0) {
                    int agentId = Integer.parseInt(parts[0].trim());
                    filters.put("agent_id", agentId);
                    System.out.println("[DEBUG] Filtre agent ID: " + agentId);
                }
            } catch (NumberFormatException e) {
                System.err.println("[ERREUR] Impossible d'extraire l'ID agent du filtre: " + agentFilter);
            }
        }
        
        if (startDate != null) {
            filters.put("start_date", startDate);
        }
        
        if (endDate != null) {
            filters.put("end_date", endDate);
        }
        
        try {
            System.out.println("[DEBUG] Filtres appliqués: " + filters);
            List<Commission> commissions = commissionDAO.findWithFilters(filters);
            ObservableList<Commission> observableList = FXCollections.observableArrayList(commissions);
            commissionsTable.setItems(observableList);
            
            // Mettre à jour les statistiques
            updateCommissionStats(commissions);
            
            System.out.println("[INFO] " + commissions.size() + " commissions filtrées");
            
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de filtrer les commissions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearCommissionFilters() {
        statutFilterCombo.setValue("TOUS");
        agentFilterCombo.setValue("TOUS");
        commissionStartDatePicker.setValue(java.time.LocalDate.now().minusDays(30));
        commissionEndDatePicker.setValue(java.time.LocalDate.now());
        
        chargerCommissions();
    }
    
    private void updateCommissionStats(List<Commission> commissions) {
        if (commissionStatsLabel != null) {
            double totalAmount = 0;
            int pendingCount = 0;
            int paidCount = 0;
            int cancelledCount = 0;
            
            for (Commission c : commissions) {
                totalAmount += c.getMontantCommission();
                String status = c.getStatut();
                if ("PENDING".equals(status)) {
                    pendingCount++;
                } else if ("PAID".equals(status)) {
                    paidCount++;
                } else if ("CANCELLED".equals(status)) {
                    cancelledCount++;
                }
            }
            
            String statsText = String.format("%d commission(s) | En attente: %d | Payées: %d | Annulées: %d | Total: %,.0f GNF", 
                commissions.size(), pendingCount, paidCount, cancelledCount, totalAmount);
            commissionStatsLabel.setText(statsText);
        }
    }
    
    private void performSearch() {
        String searchText = searchUserField.getText();
        String roleFilter = filterRoleCombo.getValue();
        String statusFilter = filterStatusCombo.getValue();
        
        Map<String, String> filters = new HashMap<>();
        if (searchText != null && !searchText.isEmpty()) {
            filters.put("search", searchText);
        }
        if (!"TOUS".equals(roleFilter)) {
            filters.put("role", roleFilter);
        }
        if (!"TOUS".equals(statusFilter)) {
            filters.put("statut", statusFilter);
        }
        
        List<Utilisateur> results = profileService.searchUsersAdvanced(filters);
        ObservableList<Utilisateur> observableList = FXCollections.observableArrayList(results);
        usersTable.setItems(observableList);
    }
    
    @FXML
    public void handleCreateUser() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String roleName = roleCombo.getValue();

        // Validation
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

        // Valider le téléphone s'il est fourni
        if (!telephone.isEmpty()) {
            if (!ValidationUtil.isValidGuineanPhone(telephone)) {
                afficherErreur("Erreur", ValidationUtil.getPhoneErrorMessage());
                return;
            }
        }

        // Vérifier que l'email n'existe pas
        if (utilisateurDAO.findByEmail(email) != null) {
            afficherErreur("Erreur", "Cet email est déjà utilisé");
            return;
        }

        // Vérifier que le téléphone n'existe pas (si fourni)
        if (!telephone.isEmpty() && utilisateurDAO.findByTelephone(telephone) != null) {
            afficherErreur("Erreur", "Ce téléphone est déjà utilisé");
            return;
        }

        try {
            // Hasher le mot de passe
            String passwordHash = SecurityUtil.hashPassword(password);

            // Récupérer l'ID du rôle
            int roleId = roleDAO.getRoleIdByName(roleName);
            if (roleId == -1) {
                afficherErreur("Erreur", "Rôle invalide");
                return;
            }

            // Créer l'utilisateur
            int userId = utilisateurDAO.create(nom, prenom, email, telephone, adresse, passwordHash, roleId);
            
            if (userId > 0) {
                afficherInfo("Succès", "Utilisateur créé avec succès !\n\nID: " + userId + 
                           "\nEmail: " + email + 
                           "\nRôle: " + roleName +
                           "\nMot de passe: " + password);
                
                // Réinitialiser les champs
                nomField.clear();
                prenomField.clear();
                emailField.clear();
                telephoneField.clear();
                adresseField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                roleCombo.setValue("USER");
                
                // Recharger les données
                chargerUtilisateurs();
                chargerStatistiques();
            } else {
                afficherErreur("Erreur", "Impossible de créer l'utilisateur");
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] Erreur lors de la création de l'utilisateur");
            e.printStackTrace();
            afficherErreur("Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefreshUsers() {
        chargerUtilisateurs();
    }
    
    @FXML
    private void handleExportUsersExcel() {
        exporterUtilisateursExcel();
    }
    
    @FXML
    private void handleExportUsersPDF() {
        exporterUtilisateursPDF();
    }
    
    @FXML
    private void handleRefreshTransactions() {
        chargerTransactions();
    }
    
    @FXML
    private void handleRefreshWallets() {
        chargerPortefeuilles();
    }
    
    @FXML
    private void handleRefreshCommissions() {
        chargerCommissions();
    }
    
    @FXML
    private void handlePayerCommission() {
        Commission selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            payerCommission(selected);
        } else {
            afficherErreur("Erreur", "Veuillez sélectionner une commission à payer");
        }
    }
    
    @FXML
    private void handleAnnulerCommission() {
        Commission selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            annulerCommission(selected);
        } else {
            afficherErreur("Erreur", "Veuillez sélectionner une commission à annuler");
        }
    }
    
    @FXML
    private void handleExporterCommissions() {
        exporterCommissions();
    }
    
    @FXML
    private void handleRefreshStats() {
        chargerStatistiques();
    }
    
    // Méthodes d'export PDF
    @FXML
    private void handleExportTransactions() {
        try {
            List<Transaction> transactions;
            
            // Vérifier si des filtres sont appliqués
            if (!searchTransactionField.getText().isEmpty() || 
                !"TOUS".equals(typeFilterCombo.getValue()) ||
                !"TOUS".equals(filterTransactionStatusCombo.getValue()) ||
                startDatePicker.getValue() != null ||
                endDatePicker.getValue() != null) {
                
                // Utiliser les transactions filtrées
                Map<String, Object> filters = new HashMap<>();
                if (!searchTransactionField.getText().isEmpty()) {
                    filters.put("search", searchTransactionField.getText());
                }
                if (!"TOUS".equals(typeFilterCombo.getValue())) {
                    filters.put("type", typeFilterCombo.getValue());
                }
                if (!"TOUS".equals(filterTransactionStatusCombo.getValue())) {
                    filters.put("statut", filterTransactionStatusCombo.getValue());
                }
                if (startDatePicker.getValue() != null) {
                    filters.put("start_date", startDatePicker.getValue().atStartOfDay());
                }
                if (endDatePicker.getValue() != null) {
                    filters.put("end_date", endDatePicker.getValue().atTime(23, 59, 59));
                }
                
                transactions = transactionDAO.findWithFilters(filters);
            } else {
                // Utiliser toutes les transactions
                transactions = transactionDAO.findAll();
            }
            
            if (transactions.isEmpty()) {
                afficherErreur("Erreur", "Aucune transaction à exporter");
                return;
            }
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir le dossier de sauvegarde");
            File selectedDirectory = directoryChooser.showDialog(null);
            
            if (selectedDirectory != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "transactions_" + timestamp + ".pdf";
                String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;
                
                // Préparer les données pour l'export
                List<Map<String, Object>> data = new ArrayList<>();
                double totalAmount = 0;
                
                for (Transaction t : transactions) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID", t.getNumeroTransaction());
                    row.put("Type", t.getType());
                    row.put("Montant", String.format("%,.0f GNF", t.getMontant()));
                    row.put("Description", t.getDescription() != null ? t.getDescription() : "");
                    row.put("Date", t.getDateTransaction() != null ? 
                        t.getDateTransaction().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                    
                    // Récupérer l'agent
                    String agentName = "Système";
                    if (t.getAgentId() > 0) {
                        Utilisateur agent = utilisateurDAO.findById(t.getAgentId());
                        if (agent != null) {
                            agentName = agent.getPrenom() + " " + agent.getNom();
                        }
                    }
                    row.put("Agent", agentName);
                    
                    data.add(row);
                    totalAmount += t.getMontant();
                }
                
                // Informations supplémentaires
                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("Nombre de transactions", transactions.size());
                extraInfo.put("Montant total", String.format("%,.0f GNF", totalAmount));
                extraInfo.put("Date d'export", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                extraInfo.put("Exporté par", utilisateur.getPrenom() + " " + utilisateur.getNom());
                
                // Exporter en PDF
                boolean success = ExportUtil.exportToPdf(
                    data, 
                    filePath, 
                    "Rapport des Transactions",
                    "Liste détaillée des transactions",
                    extraInfo
                );
                
                if (success) {
                    afficherInfo("Export réussi", 
                        "Rapport PDF créé avec succès !\n\n" +
                        "Chemin: " + filePath + "\n" +
                        "Nombre de transactions: " + transactions.size() + "\n" +
                        "Montant total: " + String.format("%,.0f GNF", totalAmount));
                } else {
                    afficherErreur("Erreur", "Échec de l'export PDF");
                }
            }
        } catch (Exception e) {
            afficherErreur("Erreur", "Export échoué: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExportWallets() {
        try {
            List<Portefeuille> wallets;
            
            // Vérifier si des filtres sont appliqués
            if (!searchWalletField.getText().isEmpty() || 
                !searchWalletClientField.getText().isEmpty() ||
                !"TOUS".equals(filterWalletStatusCombo.getValue()) ||
                !filterMinBalanceField.getText().isEmpty() ||
                !filterMaxBalanceField.getText().isEmpty()) {
                
                // Utiliser les portefeuilles filtrés
                Map<String, Object> filters = new HashMap<>();
                if (!searchWalletField.getText().isEmpty()) {
                    filters.put("search_wallet", searchWalletField.getText());
                }
                if (!searchWalletClientField.getText().isEmpty()) {
                    filters.put("search_client", searchWalletClientField.getText());
                }
                if (!"TOUS".equals(filterWalletStatusCombo.getValue())) {
                    filters.put("statut", filterWalletStatusCombo.getValue());
                }
                if (!filterMinBalanceField.getText().isEmpty()) {
                    try {
                        filters.put("min_balance", Double.parseDouble(filterMinBalanceField.getText()));
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                }
                if (!filterMaxBalanceField.getText().isEmpty()) {
                    try {
                        filters.put("max_balance", Double.parseDouble(filterMaxBalanceField.getText()));
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                }
                
                wallets = portefeuilleDAO.findWithFilters(filters);
            } else {
                // Utiliser tous les portefeuilles
                wallets = portefeuilleDAO.findAll();
            }
            
            if (wallets.isEmpty()) {
                afficherErreur("Erreur", "Aucun portefeuille à exporter");
                return;
            }
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir le dossier de sauvegarde");
            File selectedDirectory = directoryChooser.showDialog(null);
            
            if (selectedDirectory != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "portefeuilles_" + timestamp + ".pdf";
                String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;
                
                // Préparer les données pour l'export
                List<Map<String, Object>> data = new ArrayList<>();
                double totalBalance = 0;
                int activeCount = 0;
                int blockedCount = 0;
                
                for (Portefeuille w : wallets) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("Numéro", w.getNumeroPortefeuille());
                    row.put("Solde", String.format("%,.0f GNF", w.getSolde()));
                    row.put("Statut", w.getStatut());
                    row.put("Limite retrait", String.format("%,.0f GNF", w.getLimiteRetraitQuotidien()));
                    row.put("Limite transfert", String.format("%,.0f GNF", w.getLimiteTransfert()));
                    
                    // Récupérer le client
                    Utilisateur client = utilisateurDAO.findById(w.getUtilisateurId());
                    String clientInfo = "Inconnu";
                    if (client != null) {
                        clientInfo = client.getPrenom() + " " + client.getNom() + " (" + client.getEmail() + ")";
                    }
                    row.put("Client", clientInfo);
                    
                    row.put("Date création", w.getDateCreation() != null ? 
                        w.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                    
                    data.add(row);
                    totalBalance += w.getSolde();
                    
                    if ("ACTIF".equals(w.getStatut())) {
                        activeCount++;
                    } else if ("BLOQUE".equals(w.getStatut())) {
                        blockedCount++;
                    }
                }
                
                // Informations supplémentaires
                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("Nombre de portefeuilles", wallets.size());
                extraInfo.put("Portefeuilles actifs", activeCount);
                extraInfo.put("Portefeuilles bloqués", blockedCount);
                extraInfo.put("Solde total", String.format("%,.0f GNF", totalBalance));
                extraInfo.put("Date d'export", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                extraInfo.put("Exporté par", utilisateur.getPrenom() + " " + utilisateur.getNom());
                
                // Exporter en PDF
                boolean success = ExportUtil.exportToPdf(
                    data, 
                    filePath, 
                    "Rapport des Portefeuilles",
                    "Liste détaillée des portefeuilles",
                    extraInfo
                );
                
                if (success) {
                    afficherInfo("Export réussi", 
                        "Rapport PDF créé avec succès !\n\n" +
                        "Chemin: " + filePath + "\n" +
                        "Nombre de portefeuilles: " + wallets.size() + "\n" +
                        "Solde total: " + String.format("%,.0f GNF", totalBalance));
                } else {
                    afficherErreur("Erreur", "Échec de l'export PDF");
                }
            }
        } catch (Exception e) {
            afficherErreur("Erreur", "Export échoué: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Suppression des méthodes inutilisées pour l'export PDF des statistiques
    // Les méthodes handleExportStatsExcel() et handleExportStatsPDF() seront supprimées
    // car elles ne sont pas implémentées
    
    private void chargerUtilisateurs() {
        try {
            List<Utilisateur> utilisateurs = utilisateurDAO.findAll();
            ObservableList<Utilisateur> observableList = FXCollections.observableArrayList(utilisateurs);
            usersTable.setItems(observableList);
            System.out.println("[INFO] " + utilisateurs.size() + " utilisateurs chargés");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger les utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void chargerTransactions() {
        try {
            List<Transaction> transactions = transactionDAO.findAll();
            ObservableList<Transaction> observableList = FXCollections.observableArrayList(transactions);
            transactionsTable.setItems(observableList);
            
            // Mettre à jour les statistiques
            updateTransactionStats(transactions);
            
            System.out.println("[INFO] " + transactions.size() + " transactions chargées");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger les transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void chargerPortefeuilles() {
        try {
            List<Portefeuille> portefeuilles = portefeuilleDAO.findAll();
            ObservableList<Portefeuille> observableList = FXCollections.observableArrayList(portefeuilles);
            walletsTable.setItems(observableList);
            
            // Mettre à jour les statistiques
            updateWalletStats(portefeuilles);
            
            System.out.println("[INFO] " + portefeuilles.size() + " portefeuilles chargés");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger les portefeuilles: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void chargerCommissions() {
        try {
            List<Commission> commissions = commissionDAO.findAll();
            
            // Appliquer les filtres
            String statutFilter = statutFilterCombo.getValue();
            String agentFilter = agentFilterCombo.getValue();
            
            if (!"TOUS".equals(statutFilter) || !"TOUS".equals(agentFilter)) {
                List<Commission> filtered = new ArrayList<>();
                for (Commission c : commissions) {
                    boolean statutOk = "TOUS".equals(statutFilter) || statutFilter.equals(c.getStatut());
                    boolean agentOk = "TOUS".equals(agentFilter) || 
                                     agentFilter.equals(String.valueOf(c.getAgentId()));
                    
                    if (statutOk && agentOk) {
                        filtered.add(c);
                    }
                }
                commissions = filtered;
            }
            
            ObservableList<Commission> observableList = FXCollections.observableArrayList(commissions);
            commissionsTable.setItems(observableList);
            
            // Mettre à jour les statistiques
            updateCommissionStats(commissions);
            
            System.out.println("[INFO] " + commissions.size() + " commissions chargées");
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger les commissions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void chargerAgentsPourFiltre() {
        try {
            System.out.println("[DEBUG] Chargement des agents pour le filtre...");
            
            // Vider le ComboBox d'abord
            agentFilterCombo.getItems().clear();
            agentFilterCombo.getItems().add("TOUS");
            
            // Récupérer tous les agents
            List<Utilisateur> agents = utilisateurDAO.findByRoleId(roleDAO.getRoleIdByName("AGENT"));
            
            System.out.println("[DEBUG] Nombre d'agents trouvés: " + (agents != null ? agents.size() : 0));
            
            if (agents != null && !agents.isEmpty()) {
                for (Utilisateur agent : agents) {
                    // Format: "ID - Nom Prénom"
                    String displayText = agent.getUtilisateurId() + " - " + 
                                    agent.getPrenom() + " " + agent.getNom() + 
                                    " (" + agent.getEmail() + ")";
                    
                    // Stocker l'ID comme valeur, mais afficher le texte formaté
                    agentFilterCombo.getItems().add(displayText);
                    System.out.println("[DEBUG] Agent ajouté: " + displayText);
                }
            } else {
                System.out.println("[DEBUG] Aucun agent trouvé dans la base de données");
            }
            
            agentFilterCombo.setValue("TOUS");
            System.out.println("[DEBUG] Filtre agent initialisé avec " + agentFilterCombo.getItems().size() + " éléments");
            
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger les agents pour le filtre: " + e.getMessage());
            e.printStackTrace();
            
            // Toujours ajouter "TOUS" même en cas d'erreur
            agentFilterCombo.getItems().clear();
            agentFilterCombo.getItems().add("TOUS");
            agentFilterCombo.setValue("TOUS");
        }
    }
    
    private void chargerStatistiques() {
        try {
            System.out.println("[DEBUG] Début chargement des statistiques...");
            
            // 1. Statistiques utilisateurs
            Map<String, Object> userStats = utilisateurDAO.getUserStatistics();
            System.out.println("[DEBUG] User stats: " + userStats);
            
            if (totalUsersLabel != null) {
                Object totalUsers = userStats.get("total");
                if (totalUsers != null) {
                    totalUsersLabel.setText("Total: " + totalUsers);
                } else {
                    List<Utilisateur> users = utilisateurDAO.findAll();
                    totalUsersLabel.setText("Total: " + users.size());
                }
            }
            
            // Comptes par rôle
            int admins = ((Number) userStats.getOrDefault("admins", 0)).intValue();
            int agents = ((Number) userStats.getOrDefault("agents", 0)).intValue();
            int clients = ((Number) userStats.getOrDefault("clients", 0)).intValue();
            
            if (admins == 0 && agents == 0 && clients == 0) {
                List<Utilisateur> allUsers = utilisateurDAO.findAll();
                for (Utilisateur user : allUsers) {
                    String role = user.getRoleName();
                    if ("ADMIN".equals(role)) admins++;
                    else if ("AGENT".equals(role)) agents++;
                    else if ("USER".equals(role)) clients++;
                }
            }
            
            if (activeUsersLabel != null) {
                activeUsersLabel.setText(String.format("ADM: %d | AGT: %d | CLI: %d", admins, agents, clients));
            }
            
            // 2. Statistiques transactions
            Map<String, Object> transStats = reportService.getTransactionStatistics();
            System.out.println("[DEBUG] Transaction stats: " + transStats);
            
            if (totalTransactionsLabel != null) {
                Object monthCount = transStats.get("month_count");
                Object todayCount = transStats.get("today_count");
                
                String text = "Mois: " + (monthCount != null ? monthCount : "0");
                if (todayCount != null) {
                    text += " | Auj: " + todayCount;
                }
                totalTransactionsLabel.setText(text);
            }
            
            // Volume des transactions
            if (totalVolumeLabel != null) {
                Object monthVolume = transStats.get("month_volume");
                Object todayVolume = transStats.get("today_volume");
                
                String text = "Mois: ";
                if (monthVolume != null && monthVolume instanceof Number) {
                    double volume = ((Number) monthVolume).doubleValue();
                    text += String.format("%,.0f GNF", volume);
                } else {
                    text += "0 GNF";
                }
                
                if (todayVolume != null && todayVolume instanceof Number) {
                    text += " | Auj: " + String.format("%,.0f GNF", ((Number) todayVolume).doubleValue());
                }
                totalVolumeLabel.setText(text);
            }
            
            // Transactions par type
            Map<String, Integer> typeStats = new HashMap<>();
            if (transStats.containsKey("par_type")) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> tempStats = (Map<String, Integer>) transStats.get("par_type");
                    typeStats = tempStats;
                } catch (Exception e) {
                    System.err.println("[DEBUG] Erreur conversion par_type: " + e.getMessage());
                }
            }
            
            if (depositCountLabel != null) {
                int deposits = 0;
                if (typeStats.containsKey("DEPOT")) deposits = typeStats.get("DEPOT");
                else if (typeStats.containsKey("DÉPÔT")) deposits = typeStats.get("DÉPÔT");
                depositCountLabel.setText("Dépôts: " + deposits);
            }
            
            if (withdrawalCountLabel != null) {
                int withdrawals = 0;
                if (typeStats.containsKey("RETRAIT")) withdrawals = typeStats.get("RETRAIT");
                withdrawalCountLabel.setText("Retraits: " + withdrawals);
            }
            
            if (transferCountLabel != null) {
                int transfers = 0;
                if (typeStats.containsKey("TRANSFERT")) transfers = typeStats.get("TRANSFERT");
                transferCountLabel.setText("Transferts: " + transfers);
            }
            
            // 3. Statistiques financières
            Map<String, Object> financialStats = reportService.getFinancialStatistics();
            System.out.println("[DEBUG] Financial stats: " + financialStats);
            
            if (financialStats.containsKey("total_balance")) {
                Object totalBalance = financialStats.get("total_balance");
                if (totalBalance != null && totalBalance instanceof Number) {
                    double balance = ((Number) totalBalance).doubleValue();
                    if (balance > 0) {
                        System.out.println("[INFO] Solde total système: " + String.format("%,.0f GNF", balance));
                    }
                }
            }
            
            // 4. Statistiques commissions
            Map<String, Object> commissionStats = commissionDAO.getGlobalStats();
            System.out.println("[DEBUG] Commission stats: " + commissionStats);
            
            if (totalCommissionsLabel != null) {
                Object totalMontant = commissionStats.get("total_amount");
                if (totalMontant != null && totalMontant instanceof Number) {
                    double montant = ((Number) totalMontant).doubleValue();
                    totalCommissionsLabel.setText(String.format("%,.0f GNF", montant));
                } else {
                    totalCommissionsLabel.setText("0 GNF");
                }
            }
            
            // Détails des commissions par statut
            if (pendingCommissionsLabel != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> byStatus = 
                        (Map<String, Map<String, Object>>) commissionStats.get("by_status");
                    
                    double pending = 0, paid = 0, cancelled = 0;
                    
                    if (byStatus != null) {
                        if (byStatus.containsKey("PENDING")) {
                            pending = ((Number) byStatus.get("PENDING").get("amount")).doubleValue();
                        }
                        if (byStatus.containsKey("PAID")) {
                            paid = ((Number) byStatus.get("PAID").get("amount")).doubleValue();
                        }
                        if (byStatus.containsKey("CANCELLED")) {
                            cancelled = ((Number) byStatus.get("CANCELLED").get("amount")).doubleValue();
                        }
                    }
                    
                    pendingCommissionsLabel.setText(String.format("En attente: %,.0f GNF", pending));
                    paidCommissionsLabel.setText(String.format("Payées: %,.0f GNF", paid));
                    cancelledCommissionsLabel.setText(String.format("Annulées: %,.0f GNF", cancelled));
                    
                } catch (Exception e) {
                    System.err.println("[DEBUG] Erreur parsing by_status: " + e.getMessage());
                    pendingCommissionsLabel.setText("En attente: 0 GNF");
                    paidCommissionsLabel.setText("Payées: 0 GNF");
                    cancelledCommissionsLabel.setText("Annulées: 0 GNF");
                }
            }
            
            System.out.println("[INFO] Statistiques chargées avec succès");
            
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de charger les statistiques: " + e.getMessage());
            e.printStackTrace();
            
            // Valeurs par défaut
            if (totalUsersLabel != null) totalUsersLabel.setText("Erreur");
            if (totalTransactionsLabel != null) totalTransactionsLabel.setText("Erreur");
            if (totalVolumeLabel != null) totalVolumeLabel.setText("Erreur");
            if (totalCommissionsLabel != null) totalCommissionsLabel.setText("Erreur");
            if (activeUsersLabel != null) activeUsersLabel.setText("Erreur");
            if (depositCountLabel != null) depositCountLabel.setText("Erreur");
            if (withdrawalCountLabel != null) withdrawalCountLabel.setText("Erreur");
            if (transferCountLabel != null) transferCountLabel.setText("Erreur");
            if (pendingCommissionsLabel != null) pendingCommissionsLabel.setText("Erreur");
            if (paidCommissionsLabel != null) paidCommissionsLabel.setText("Erreur");
            if (cancelledCommissionsLabel != null) cancelledCommissionsLabel.setText("Erreur");
        }
    }
    
    // Méthodes pour afficher les détails
    private void showUserDetails(Utilisateur user) {
        String details = String.format(
            "📋 DÉTAILS UTILISATEUR\n\n" +
            "ID: %d\n" +
            "Nom: %s\n" +
            "Prénom: %s\n" +
            "Email: %s\n" +
            "Téléphone: %s\n" +
            "Adresse: %s\n" +
            "Rôle: %s\n" +
            "Statut: %s\n" +
            "Date d'inscription: %s\n" +
            "Dernière modification: %s\n",
            user.getUtilisateurId(),
            user.getNom(),
            user.getPrenom(),
            user.getEmail(),
            user.getTelephone() != null ? user.getTelephone() : "Non défini",
            user.getAdresse() != null ? user.getAdresse() : "Non définie",
            user.getRoleName(),
            user.getStatut() != null ? user.getStatut().name() : "INACTIF",
            user.getDateInscription() != null ? 
                user.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
            user.getDateModification() != null ? 
                user.getDateModification().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"
        );
        
        afficherInfo("Détails Utilisateur", details);
    }
    
    private void editUser(Utilisateur user) {
        // Créer un dialogue d'édition avec onglets
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifier l'utilisateur: " + user.getPrenom() + " " + user.getNom());
        
        TabPane tabPane = new TabPane();
        
        // Onglet 1: Informations
        Tab infoTab = new Tab("Informations");
        infoTab.setClosable(false);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nomField = new TextField(user.getNom());
        TextField prenomField = new TextField(user.getPrenom());
        TextField emailField = new TextField(user.getEmail());
        TextField telephoneField = new TextField(user.getTelephone() != null ? user.getTelephone() : "");
        TextField adresseField = new TextField(user.getAdresse() != null ? user.getAdresse() : "");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "AGENT", "USER");
        roleCombo.setValue(user.getRoleName());
        
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("ACTIF", "INACTIF", "BLOQUE");
        statutCombo.setValue(user.getStatut() != null ? user.getStatut().name() : "INACTIF");
        
        int row = 0;
        grid.add(new Label("Nom:"), 0, row);
        grid.add(nomField, 1, row);
        
        row++;
        grid.add(new Label("Prénom:"), 0, row);
        grid.add(prenomField, 1, row);
        
        row++;
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row);
        
        row++;
        grid.add(new Label("Téléphone:"), 0, row);
        grid.add(telephoneField, 1, row);
        
        row++;
        grid.add(new Label("Adresse:"), 0, row);
        grid.add(adresseField, 1, row);
        
        row++;
        grid.add(new Label("Rôle:"), 0, row);
        grid.add(roleCombo, 1, row);
        
        row++;
        grid.add(new Label("Statut:"), 0, row);
        grid.add(statutCombo, 1, row);
        
        infoTab.setContent(grid);
        
        // Onglet 2: Historique des modifications
        Tab historyTab = new Tab("Historique");
        historyTab.setClosable(false);
        
        TableView<Map<String, Object>> historyTable = new TableView<>();
        
        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            Object dateObj = cellData.getValue().get("date");
            if (dateObj instanceof LocalDateTime) {
                LocalDateTime date = (LocalDateTime) dateObj;
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty("");
        });
        
        TableColumn<Map<String, Object>, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty((String) cellData.getValue().get("action")));
        
        TableColumn<Map<String, Object>, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty((String) cellData.getValue().get("description")));
        
        historyTable.getColumns().addAll(dateCol, actionCol, descCol);
        historyTable.setPrefHeight(200);
        
        // Charger l'historique réel
        List<Map<String, Object>> history = profileService.getUserAuditHistory(user.getUtilisateurId());
        ObservableList<Map<String, Object>> historyData = FXCollections.observableArrayList(history);
        historyTable.setItems(historyData);
        
        VBox historyBox = new VBox(10, new Label("Journal des modifications:"), historyTable);
        historyBox.setPadding(new Insets(20));
        historyTab.setContent(historyBox);
        
        // Onglet 3: Sécurité
        Tab securityTab = new Tab("Sécurité");
        securityTab.setClosable(false);
        
        VBox securityBox = new VBox(10);
        securityBox.setPadding(new Insets(20));
        
        Button resetPasswordBtn = new Button("🔐 Réinitialiser le mot de passe");
        resetPasswordBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        securityBox.getChildren().addAll(resetPasswordBtn);
        securityTab.setContent(securityBox);
        
        tabPane.getTabs().addAll(infoTab, historyTab, securityTab);
        
        // Boutons d'action
        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        Button cancelBtn = new Button("Annuler");
        
        HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox mainLayout = new VBox(10, tabPane, buttonBox);
        mainLayout.setPadding(new Insets(10));
        
        // Gestion des événements
        saveBtn.setOnAction(e -> {
            // Collecter seulement les champs modifiés
            Map<String, String> updates = new HashMap<>();
            
            if (!nomField.getText().equals(user.getNom())) {
                updates.put("nom", nomField.getText());
            }
            if (!prenomField.getText().equals(user.getPrenom())) {
                updates.put("prenom", prenomField.getText());
            }
            if (!emailField.getText().equals(user.getEmail())) {
                updates.put("email", emailField.getText());
            }
            if (!telephoneField.getText().equals(user.getTelephone() != null ? user.getTelephone() : "")) {
                updates.put("telephone", telephoneField.getText());
            }
            if (!adresseField.getText().equals(user.getAdresse() != null ? user.getAdresse() : "")) {
                updates.put("adresse", adresseField.getText());
            }
            if (!roleCombo.getValue().equals(user.getRoleName())) {
                updates.put("role", roleCombo.getValue());
            }
            if (!statutCombo.getValue().equals(user.getStatut() != null ? user.getStatut().name() : "INACTIF")) {
                updates.put("statut", statutCombo.getValue());
            }
            
            if (!updates.isEmpty()) {
                // Utiliser ProfileService pour la mise à jour avec audit
                Map<String, Object> result = profileService.updateUserAsAdmin(
                    user.getUtilisateurId(), 
                    updates,
                    utilisateur.getUtilisateurId(),
                    utilisateur.getPrenom() + " " + utilisateur.getNom(),
                    "127.0.0.1" // À remplacer par la vraie IP
                );
                
                if ((Boolean) result.get("success")) {
                    afficherInfo("Succès", (String) result.get("message"));
                    
                    // Recharger l'historique
                    List<Map<String, Object>> updatedHistory = 
                        profileService.getUserAuditHistory(user.getUtilisateurId());
                    historyTable.setItems(FXCollections.observableArrayList(updatedHistory));
                    
                    // Recharger la liste des utilisateurs
                    chargerUtilisateurs();
                    
                    // Fermer après un délai
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            Platform.runLater(() -> dialog.close());
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else {
                    afficherErreur("Erreur", (String) result.get("message"));
                }
            } else {
                afficherInfo("Information", "Aucune modification détectée");
            }
        });
        
        // Gestion de la réinitialisation du mot de passe
        resetPasswordBtn.setOnAction(e -> {
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Nouveau mot de passe (min 6 caractères)");
            
            PasswordField confirmField = new PasswordField();
            confirmField.setPromptText("Confirmer le mot de passe");
            
            GridPane passwordGrid = new GridPane();
            passwordGrid.setHgap(10);
            passwordGrid.setVgap(10);
            passwordGrid.setPadding(new Insets(20));
            
            passwordGrid.add(new Label("Nouveau mot de passe:"), 0, 0);
            passwordGrid.add(passwordField, 1, 0);
            passwordGrid.add(new Label("Confirmation:"), 0, 1);
            passwordGrid.add(confirmField, 1, 1);
            
            Alert passwordDialog = new Alert(Alert.AlertType.CONFIRMATION);
            passwordDialog.setTitle("Réinitialisation du mot de passe");
            passwordDialog.setHeaderText("Réinitialiser pour " + user.getPrenom() + " " + user.getNom());
            passwordDialog.getDialogPane().setContent(passwordGrid);
            
            Optional<ButtonType> result = passwordDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String newPassword = passwordField.getText();
                String confirmPassword = confirmField.getText();
                
                if (!newPassword.equals(confirmPassword)) {
                    afficherErreur("Erreur", "Les mots de passe ne correspondent pas");
                    return;
                }
                
                if (newPassword.length() < 6) {
                    afficherErreur("Erreur", "Le mot de passe doit contenir au moins 6 caractères");
                    return;
                }
                
                // Utiliser ProfileService pour réinitialiser avec audit
                Map<String, Object> resetResult = profileService.resetPasswordAsAdmin(
                    user.getUtilisateurId(),
                    newPassword,
                    utilisateur.getUtilisateurId(),
                    utilisateur.getPrenom() + " " + utilisateur.getNom(),
                    "127.0.0.1"
                );
                
                if ((Boolean) resetResult.get("success")) {
                    afficherInfo("Succès", (String) resetResult.get("message"));
                    // Recharger l'historique
                    List<Map<String, Object>> updatedHistory = 
                        profileService.getUserAuditHistory(user.getUtilisateurId());
                    historyTable.setItems(FXCollections.observableArrayList(updatedHistory));
                } else {
                    afficherErreur("Erreur", (String) resetResult.get("message"));
                }
            }
        });
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        Scene scene = new Scene(mainLayout, 600, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void toggleUserStatus(Utilisateur user) {
        StatutUtilisateur nouveauStatut = (user.getStatut() == StatutUtilisateur.ACTIF) ? 
            StatutUtilisateur.INACTIF : StatutUtilisateur.ACTIF;
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Changement de statut");
        dialog.setHeaderText("Changer le statut de " + user.getPrenom() + " " + user.getNom());
        dialog.setContentText("Nouveau statut (" + nouveauStatut.name() + "):");
        
        dialog.showAndWait().ifPresent(confirmation -> {
            if (confirmation.equalsIgnoreCase("oui") || confirmation.equals("1")) {
                boolean success = utilisateurDAO.updateStatus(user.getUtilisateurId(), nouveauStatut.name());
                if (success) {
                    afficherInfo("Succès", "Statut de l'utilisateur changé avec succès");
                    chargerUtilisateurs();
                    chargerStatistiques();
                } else {
                    afficherErreur("Erreur", "Impossible de changer le statut");
                }
            }
        });
    }
    
    private void showWalletDetails(Portefeuille wallet) {
        Utilisateur user = utilisateurDAO.findById(wallet.getUtilisateurId());
        String userInfo = (user != null) ? user.getPrenom() + " " + user.getNom() + " (" + user.getEmail() + ")" : "Inconnu";
        
        String details = String.format(
            "💰 DÉTAILS PORTEFEUILLE\n\n" +
            "Numéro: %s\n" +
            "Client: %s\n" +
            "Solde: %,.0f GNF\n" +
            "Devise: %s\n" +
            "Statut: %s\n" +
            "Limite retrait quotidien: %,.0f GNF\n" +
            "Limite transfert: %,.0f GNF\n" +
            "Date création: %s\n" +
            "Dernière modification: %s\n",
            wallet.getNumeroPortefeuille(),
            userInfo,
            wallet.getSolde(),
            wallet.getDevise(),
            wallet.getStatut(),
            wallet.getLimiteRetraitQuotidien(),
            wallet.getLimiteTransfert(),
            wallet.getDateCreation() != null ? 
                wallet.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
            wallet.getDateModification() != null ? 
                wallet.getDateModification().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"
        );
        
        afficherInfo("Détails Portefeuille", details);
    }
    
    private void editWalletLimits(Portefeuille wallet) {
        TextInputDialog retraitDialog = new TextInputDialog(String.valueOf(wallet.getLimiteRetraitQuotidien()));
        retraitDialog.setTitle("Modifier limites");
        retraitDialog.setHeaderText("Modifier les limites du portefeuille " + wallet.getNumeroPortefeuille());
        retraitDialog.setContentText("Limite retrait quotidien (GNF):");
        
        retraitDialog.showAndWait().ifPresent(nouvelleLimiteRetrait -> {
            try {
                double limiteRetrait = Double.parseDouble(nouvelleLimiteRetrait);
                
                TextInputDialog transfertDialog = new TextInputDialog(String.valueOf(wallet.getLimiteTransfert()));
                transfertDialog.setTitle("Modifier limites");
                transfertDialog.setHeaderText("Modifier les limites du portefeuille " + wallet.getNumeroPortefeuille());
                transfertDialog.setContentText("Limite transfert (GNF):");
                
                transfertDialog.showAndWait().ifPresent(nouvelleLimiteTransfert -> {
                    try {
                        double limiteTransfert = Double.parseDouble(nouvelleLimiteTransfert);
                        
                        boolean success = portefeuilleDAO.updateLimites(wallet.getId(), limiteRetrait, limiteTransfert);
                        if (success) {
                            afficherInfo("Succès", "Limites mises à jour avec succès");
                            chargerPortefeuilles();
                        } else {
                            afficherErreur("Erreur", "Impossible de mettre à jour les limites");
                        }
                    } catch (NumberFormatException ex) {
                        afficherErreur("Erreur", "Limite transfert invalide");
                    }
                });
            } catch (NumberFormatException ex) {
                afficherErreur("Erreur", "Limite retrait invalide");
            }
        });
    }
    
    private void toggleWalletStatus(Portefeuille wallet) {
        String nouveauStatut = "ACTIF".equals(wallet.getStatut()) ? "INACTIF" : "ACTIF";
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Changement de statut");
        dialog.setHeaderText("Changer le statut du portefeuille " + wallet.getNumeroPortefeuille());
        dialog.setContentText("Nouveau statut (" + nouveauStatut + "):");
        
        dialog.showAndWait().ifPresent(confirmation -> {
            if (confirmation.equalsIgnoreCase("oui") || confirmation.equals("1")) {
                boolean success = portefeuilleDAO.updateStatus(wallet.getId(), nouveauStatut);
                if (success) {
                    afficherInfo("Succès", "Statut du portefeuille changé avec succès");
                    chargerPortefeuilles();
                } else {
                    afficherErreur("Erreur", "Impossible de changer le statut");
                }
            }
        });
    }
    
    private void showCommissionDetails(Commission commission) {
        String details = String.format(
            "💵 DÉTAILS COMMISSION\n\n" +
            "ID: %d\n" +
            "Agent: %s\n" +
            "Email agent: %s\n" +
            "ID Transaction: %d\n" +
            "Montant commission: %,.0f GNF\n" +
            "Pourcentage: %.1f%%\n" +
            "Statut: %s\n" +
            "Date création: %s\n",
            commission.getId(),
            commission.getAgentComplet(),
            commission.getAgentEmail() != null ? commission.getAgentEmail() : "N/A",
            commission.getTransactionId(),
            commission.getMontantCommission(),
            commission.getPourcentage(),
            commission.getStatut(),
            commission.getDateFormatted()
        );
        
        afficherInfo("Détails Commission", details);
    }
    
    private void payerCommission(Commission commission) {
        if (!"PENDING".equals(commission.getStatut())) {
            afficherErreur("Erreur", "Cette commission n'est pas en attente de paiement");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Paiement Commission");
        dialog.setHeaderText("Payer la commission #" + commission.getId());
        dialog.setContentText("Référence de paiement:");
        
        dialog.showAndWait().ifPresent(reference -> {
            if (reference.trim().isEmpty()) {
                afficherErreur("Erreur", "La référence de paiement est obligatoire");
                return;
            }
            
            boolean success = commissionService.payerCommission(commission.getId(), 
                utilisateur.getUtilisateurId(), reference);
            
            if (success) {
                afficherInfo("Succès", String.format(
                    "✅ Commission payée !\n\n" +
                    "ID: %d\n" +
                    "Agent: %s\n" +
                    "Montant: %,.0f GNF\n" +
                    "Référence: %s\n",
                    commission.getId(),
                    commission.getAgentComplet(),
                    commission.getMontantCommission(),
                    reference
                ));
                chargerCommissions();
                chargerStatistiques();
            } else {
                afficherErreur("Erreur", "Impossible de payer la commission");
            }
        });
    }
    
    private void annulerCommission(Commission commission) {
        if (!"PENDING".equals(commission.getStatut())) {
            afficherErreur("Erreur", "Seules les commissions en attente peuvent être annulées");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Annulation Commission");
        dialog.setHeaderText("Annuler la commission #" + commission.getId());
        dialog.setContentText("Raison de l'annulation:");
        
        dialog.showAndWait().ifPresent(raison -> {
            if (raison.trim().isEmpty()) {
                afficherErreur("Erreur", "La raison d'annulation est obligatoire");
                return;
            }
            
            boolean success = commissionService.annulerCommission(commission.getId(), 
                utilisateur.getUtilisateurId(), raison);
            
            if (success) {
                afficherInfo("Succès", String.format(
                    "✅ Commission annulée !\n\n" +
                    "ID: %d\n" +
                    "Agent: %s\n" +
                    "Montant: %,.0f GNF\n" +
                    "Raison: %s\n",
                    commission.getId(),
                    commission.getAgentComplet(),
                    commission.getMontantCommission(),
                    raison
                ));
                chargerCommissions();
                chargerStatistiques();
            } else {
                afficherErreur("Erreur", "Impossible d'annuler la commission");
            }
        });
    }
    
    // Méthodes d'export
    private void exporterUtilisateursExcel() {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir le dossier de sauvegarde");
            File selectedDirectory = directoryChooser.showDialog(null);
            
            if (selectedDirectory != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filePath = selectedDirectory.getAbsolutePath() + File.separator + "utilisateurs_" + timestamp + ".csv";
                
                List<Map<String, Object>> data = new ArrayList<>();
                List<Utilisateur> utilisateurs = utilisateurDAO.findAll();
                
                for (Utilisateur user : utilisateurs) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID", user.getUtilisateurId());
                    row.put("Nom", user.getNom());
                    row.put("Prénom", user.getPrenom());
                    row.put("Email", user.getEmail());
                    row.put("Téléphone", user.getTelephone() != null ? user.getTelephone() : "");
                    row.put("Rôle", user.getRoleName());
                    row.put("Statut", user.getStatut() != null ? user.getStatut().name() : "INACTIF");
                    data.add(row);
                }
                
                if (!data.isEmpty()) {
                    boolean success = ExportUtil.exportToCsv(data, filePath, "Utilisateurs");
                    if (success) {
                        afficherInfo("Export réussi", 
                            "Fichier créé avec succès !\n\n" +
                            "Chemin: " + filePath + "\n\n" +
                            "Le fichier CSV peut être ouvert avec :\n" +
                            "• Microsoft Excel\n" +
                            "• LibreOffice Calc\n" +
                            "• Google Sheets\n" +
                            "• Bloc-notes");
                    }
                }
            }
        } catch (Exception e) {
            afficherErreur("Erreur", "Export échoué: " + e.getMessage());
        }
    }

    private void exporterUtilisateursPDF() {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir le dossier de sauvegarde");
            File selectedDirectory = directoryChooser.showDialog(null);
            
            if (selectedDirectory != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "utilisateurs_" + timestamp + ".pdf";
                String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;
                
                List<Map<String, Object>> data = new ArrayList<>();
                List<Utilisateur> utilisateurs = utilisateurDAO.findAll();
                
                for (Utilisateur user : utilisateurs) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID", user.getUtilisateurId());
                    row.put("Nom", user.getNom());
                    row.put("Prénom", user.getPrenom());
                    row.put("Email", user.getEmail());
                    row.put("Téléphone", user.getTelephone() != null ? user.getTelephone() : "");
                    row.put("Rôle", user.getRoleName());
                    row.put("Statut", user.getStatut() != null ? user.getStatut().name() : "INACTIF");
                    data.add(row);
                }
                
                if (!data.isEmpty()) {
                    // Informations supplémentaires
                    Map<String, Object> extraInfo = new HashMap<>();
                    extraInfo.put("Nombre d'utilisateurs", utilisateurs.size());
                    
                    // Compter par rôle
                    int admins = 0, agents = 0, clients = 0;
                    for (Utilisateur user : utilisateurs) {
                        String role = user.getRoleName();
                        if ("ADMIN".equals(role)) admins++;
                        else if ("AGENT".equals(role)) agents++;
                        else if ("USER".equals(role)) clients++;
                    }
                    extraInfo.put("Administrateurs", admins);
                    extraInfo.put("Agents", agents);
                    extraInfo.put("Clients", clients);
                    
                    extraInfo.put("Date d'export", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    extraInfo.put("Exporté par", utilisateur.getPrenom() + " " + utilisateur.getNom());
                    
                    // Exporter en PDF
                    boolean success = ExportUtil.exportToPdf(
                        data, 
                        filePath, 
                        "Rapport des Utilisateurs",
                        "Liste détaillée des utilisateurs",
                        extraInfo
                    );
                    
                    if (success) {
                        afficherInfo("Export réussi", 
                            "Rapport PDF créé avec succès !\n\n" +
                            "Chemin: " + filePath + "\n" +
                            "Nombre d'utilisateurs: " + utilisateurs.size());
                    } else {
                        afficherErreur("Erreur", "Échec de l'export PDF");
                    }
                }
            }
        } catch (Exception e) {
            afficherErreur("Erreur", "Export échoué: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exporterCommissions() {
        try {
            List<Commission> commissions;
            
            // Vérifier si des filtres sont appliqués
            if (!"TOUS".equals(statutFilterCombo.getValue()) || 
                !"TOUS".equals(agentFilterCombo.getValue()) ||
                commissionStartDatePicker.getValue() != null ||
                commissionEndDatePicker.getValue() != null) {
                
                // Utiliser les commissions filtrées
                Map<String, Object> filters = new HashMap<>();
                if (!"TOUS".equals(statutFilterCombo.getValue())) {
                    filters.put("statut", statutFilterCombo.getValue());
                }
                if (!"TOUS".equals(agentFilterCombo.getValue())) {
                    try {
                        filters.put("agent_id", Integer.parseInt(agentFilterCombo.getValue()));
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                }
                if (commissionStartDatePicker.getValue() != null) {
                    filters.put("start_date", commissionStartDatePicker.getValue().atStartOfDay());
                }
                if (commissionEndDatePicker.getValue() != null) {
                    filters.put("end_date", commissionEndDatePicker.getValue().atTime(23, 59, 59));
                }
                
                commissions = commissionDAO.findWithFilters(filters);
            } else {
                // Utiliser toutes les commissions
                commissions = commissionDAO.findAll();
            }
            
            if (commissions.isEmpty()) {
                afficherErreur("Erreur", "Aucune commission à exporter");
                return;
            }
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir le dossier de sauvegarde");
            File selectedDirectory = directoryChooser.showDialog(null);
            
            if (selectedDirectory != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "commissions_" + timestamp + ".pdf";
                String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;
                
                // Préparer les données pour l'export
                List<Map<String, Object>> data = new ArrayList<>();
                double totalAmount = 0;
                int pendingCount = 0, paidCount = 0, cancelledCount = 0;
                
                for (Commission c : commissions) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID", c.getId());
                    row.put("Agent", c.getAgentComplet());
                    row.put("Montant", String.format("%,.0f GNF", c.getMontantCommission()));
                    row.put("Pourcentage", c.getPourcentageFormatted());
                    row.put("Statut", c.getStatut());
                    row.put("Date", c.getDateFormatted());
                    
                    data.add(row);
                    totalAmount += c.getMontantCommission();
                    
                    String status = c.getStatut();
                    if ("PENDING".equals(status)) pendingCount++;
                    else if ("PAID".equals(status)) paidCount++;
                    else if ("CANCELLED".equals(status)) cancelledCount++;
                }
                
                // Informations supplémentaires
                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("Nombre de commissions", commissions.size());
                extraInfo.put("En attente", pendingCount);
                extraInfo.put("Payées", paidCount);
                extraInfo.put("Annulées", cancelledCount);
                extraInfo.put("Montant total", String.format("%,.0f GNF", totalAmount));
                extraInfo.put("Date d'export", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                extraInfo.put("Exporté par", utilisateur.getPrenom() + " " + utilisateur.getNom());
                
                // Exporter en PDF
                boolean success = ExportUtil.exportToPdf(
                    data, 
                    filePath, 
                    "Rapport des Commissions",
                    "Liste détaillée des commissions",
                    extraInfo
                );
                
                if (success) {
                    afficherInfo("Export réussi", 
                        "Rapport PDF créé avec succès !\n\n" +
                        "Chemin: " + filePath + "\n" +
                        "Nombre de commissions: " + commissions.size() + "\n" +
                        "Montant total: " + String.format("%,.0f GNF", totalAmount));
                } else {
                    afficherErreur("Erreur", "Échec de l'export PDF");
                }
            }
        } catch (Exception e) {
            afficherErreur("Erreur", "Export échoué: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateReport() {
        // Créer un dialogue pour choisir le type de rapport
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            "Transactions par période",
            "Transactions par période",
            "Utilisateurs par statut",
            "Portefeuilles par solde",
            "Commissions agents",
            "Activité quotidienne"
        );
        
        dialog.setTitle("Générer un rapport");
        dialog.setHeaderText("Sélectionnez le type de rapport");
        dialog.setContentText("Type de rapport:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reportType -> {
            // Créer un dialogue pour sélectionner la période
            Dialog<LocalDate[]> dateDialog = new Dialog<>();
            dateDialog.setTitle("Période du rapport");
            dateDialog.setHeaderText("Sélectionnez la période pour le rapport");
            
            DatePicker startDatePicker = new DatePicker(LocalDate.now().minusDays(30));
            DatePicker endDatePicker = new DatePicker(LocalDate.now());
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            grid.add(new Label("Date de début:"), 0, 0);
            grid.add(startDatePicker, 1, 0);
            grid.add(new Label("Date de fin:"), 0, 1);
            grid.add(endDatePicker, 1, 1);
            
            dateDialog.getDialogPane().setContent(grid);
            
            ButtonType generateButton = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
            dateDialog.getDialogPane().getButtonTypes().addAll(generateButton, ButtonType.CANCEL);
            
            dateDialog.setResultConverter(dialogButton -> {
                if (dialogButton == generateButton) {
                    return new LocalDate[]{startDatePicker.getValue(), endDatePicker.getValue()};
                }
                return null;
            });
            
            Optional<LocalDate[]> dates = dateDialog.showAndWait();
            dates.ifPresent(selectedDates -> {
                // Générer le rapport
                String report = reportService.generateReport(reportType, selectedDates[0], selectedDates[1]);
                
                // Afficher le rapport dans une nouvelle fenêtre
                Stage reportStage = new Stage();
                reportStage.setTitle("Rapport: " + reportType);
                
                TextArea reportArea = new TextArea(report);
                reportArea.setEditable(false);
                reportArea.setWrapText(true);
                reportArea.setPrefSize(800, 600);
                
                ScrollPane scrollPane = new ScrollPane(reportArea);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                
                Button saveButton = new Button("💾 Enregistrer");
                saveButton.setOnAction(e -> {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Enregistrer le rapport");
                    File selectedDirectory = directoryChooser.showDialog(reportStage);
                    
                    if (selectedDirectory != null) {
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String fileName = "rapport_" + reportType.toLowerCase().replace(" ", "_") + "_" + timestamp + ".txt";
                        String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;
                        
                        try {
                            Files.write(Paths.get(filePath), report.getBytes());
                            afficherInfo("Succès", "Rapport enregistré avec succès !\n\nChemin: " + filePath);
                        } catch (IOException ex) {
                            afficherErreur("Erreur", "Impossible d'enregistrer le rapport: " + ex.getMessage());
                        }
                    }
                });
                
                VBox layout = new VBox(10, scrollPane, saveButton);
                layout.setPadding(new Insets(10));
                
                Scene scene = new Scene(layout, 850, 650);
                reportStage.setScene(scene);
                reportStage.show();
            });
        });
    }

    @FXML
    public void handleLogout() {
        System.out.println("[OK] Déconnexion de: " + utilisateur.getEmail());
        com.ewallet.core.utils.SessionManager.clearSession();
        MainApp.showLogin();
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