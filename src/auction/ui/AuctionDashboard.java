// ============================================================================
// INTERFACE MODERNE JAVAFX - AuctionDashboard.java
// ============================================================================
package auction.ui;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.collections.*;
import javafx.scene.chart.*;
import javafx.scene.effect.*;
import javafx.animation.*;
import javafx.util.Duration;
import java.util.*;

public class AuctionDashboard extends Application {
    
    private ObservableList<AuctionItemUI> auctionItems;
    private ObservableList<AgentUI> agents;
    private ObservableList<ActivityLog> activityLogs;
    private LineChart<String, Number> priceChart;
    private BarChart<String, Number> agentChart;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🏛️ Système Multi-Agents d'Enchères - Dashboard");
        
        // Initialiser les données
        auctionItems = FXCollections.observableArrayList();
        agents = FXCollections.observableArrayList();
        activityLogs = FXCollections.observableArrayList();
        
        // Layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fa;");
        
        // Header
        root.setTop(createHeader());
        
        // Corps principal
        root.setCenter(createMainContent());
        
        // Barre latérale droite
        root.setRight(createRightSidebar());
        
        // Footer
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Démarrer les animations et simulations
        startSimulation();
    }
    
    // ============================================================================
    // HEADER - Barre supérieure
    // ============================================================================
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);" +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Logo et titre
        VBox titleBox = new VBox(5);
        Label title = new Label("🏛️ Auction System");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitle = new Label("Système Multi-Agents en Temps Réel");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.8);");
        
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Statistiques en temps réel
        HBox stats = new HBox(30);
        stats.setAlignment(Pos.CENTER_RIGHT);
        
        stats.getChildren().addAll(
            createStatCard("Enchères Actives", "5", "#4CAF50"),
            createStatCard("Agents Connectés", "15", "#2196F3"),
            createStatCard("Total Transactions", "1,247€", "#FF9800")
        );
        
        header.getChildren().addAll(titleBox, spacer, stats);
        
        return header;
    }
    
    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.15);" +
                     "-fx-background-radius: 10;" +
                     "-fx-border-color: rgba(255,255,255,0.3);" +
                     "-fx-border-radius: 10;" +
                     "-fx-border-width: 1;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        
        card.getChildren().addAll(valueLabel, labelText);
        
        // Animation d'entrée
        ScaleTransition st = new ScaleTransition(Duration.millis(300), card);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
        
        return card;
    }
    
    // ============================================================================
    // CONTENU PRINCIPAL - Onglets
    // ============================================================================
    private TabPane createMainContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        
        // Onglet 1: Enchères en direct
        Tab auctionsTab = new Tab("🎯 Enchères en Direct");
        auctionsTab.setContent(createAuctionsView());
        
        // Onglet 2: Agents
        Tab agentsTab = new Tab("👥 Agents");
        agentsTab.setContent(createAgentsView());
        
        // Onglet 3: Analytics
        Tab analyticsTab = new Tab("📊 Analytics");
        analyticsTab.setContent(createAnalyticsView());
        
        // Onglet 4: Journal d'activité
        Tab activityTab = new Tab("📝 Journal");
        activityTab.setContent(createActivityView());
        
        tabPane.getTabs().addAll(auctionsTab, agentsTab, analyticsTab, activityTab);
        
        return tabPane;
    }
    
    // ============================================================================
    // VUE ENCHÈRES - Grille moderne avec cartes
    // ============================================================================
    private ScrollPane createAuctionsView() {
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(20);
        flowPane.setVgap(20);
        flowPane.setPadding(new Insets(30));
        flowPane.setStyle("-fx-background-color: #f5f7fa;");
        
        // Ajouter des enchères de démo
        for (int i = 1; i <= 6; i++) {
            flowPane.getChildren().add(createAuctionCard(
                "ITEM-" + i,
                "Article Premium " + i,
                100 + i * 50,
                200 + i * 80,
                "aggressive" + (i % 2 + 1)
            ));
        }
        
        ScrollPane scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        return scrollPane;
    }
    
    private VBox createAuctionCard(String id, String name, double currentPrice, 
                                   double maxPrice, String currentLeader) {
        VBox card = new VBox(15);
        card.setPrefWidth(350);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white;" +
                     "-fx-background-radius: 15;" +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        
        // En-tête de la carte
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label itemName = new Label(name);
        itemName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label status = new Label("🔴 LIVE");
        status.setStyle("-fx-background-color: #fee; -fx-text-fill: #e74c3c;" +
                       "-fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 11px;");
        
        // Animation pulsation
        FadeTransition ft = new FadeTransition(Duration.millis(1000), status);
        ft.setFromValue(1.0);
        ft.setToValue(0.3);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();
        
        header.getChildren().addAll(itemName, spacer, status);
        
        // Prix actuel
        VBox priceBox = new VBox(5);
        Label priceLabel = new Label("Prix Actuel");
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        Label price = new Label(String.format("%.2f €", currentPrice));
        price.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        priceBox.getChildren().addAll(priceLabel, price);
        
        // Barre de progression
        ProgressBar progress = new ProgressBar(currentPrice / maxPrice);
        progress.setPrefWidth(310);
        progress.setStyle("-fx-accent: #667eea;");
        
        Label progressLabel = new Label(String.format("%.0f%% du prix max", 
                                       (currentPrice / maxPrice) * 100));
        progressLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        
        // Leader actuel
        HBox leaderBox = new HBox(10);
        leaderBox.setAlignment(Pos.CENTER_LEFT);
        leaderBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label leaderIcon = new Label("👑");
        leaderIcon.setStyle("-fx-font-size: 20px;");
        
        VBox leaderInfo = new VBox(2);
        Label leaderText = new Label("Leader Actuel");
        leaderText.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        Label leaderName = new Label(currentLeader);
        leaderName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        leaderInfo.getChildren().addAll(leaderText, leaderName);
        leaderBox.getChildren().addAll(leaderIcon, leaderInfo);
        
        // Temps restant
        Label timeLeft = new Label("⏰ Temps restant: 4:32");
        timeLeft.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        // Bouton d'action
        Button detailsBtn = new Button("Voir les détails →");
        detailsBtn.setStyle("-fx-background-color: #667eea;" +
                          "-fx-text-fill: white;" +
                          "-fx-font-weight: bold;" +
                          "-fx-background-radius: 8;" +
                          "-fx-padding: 10 20;" +
                          "-fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        
        detailsBtn.setOnMouseEntered(e -> 
            detailsBtn.setStyle("-fx-background-color: #5568d3;" +
                              "-fx-text-fill: white;" +
                              "-fx-font-weight: bold;" +
                              "-fx-background-radius: 8;" +
                              "-fx-padding: 10 20;" +
                              "-fx-cursor: hand;"));
        
        detailsBtn.setOnMouseExited(e -> 
            detailsBtn.setStyle("-fx-background-color: #667eea;" +
                              "-fx-text-fill: white;" +
                              "-fx-font-weight: bold;" +
                              "-fx-background-radius: 8;" +
                              "-fx-padding: 10 20;" +
                              "-fx-cursor: hand;"));
        
        card.getChildren().addAll(header, priceBox, progress, progressLabel, 
                                 leaderBox, timeLeft, detailsBtn);
        
        // Animation d'entrée
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), card);
        tt.setFromY(50);
        tt.setToY(0);
        
        FadeTransition fade = new FadeTransition(Duration.millis(500), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ParallelTransition pt = new ParallelTransition(tt, fade);
        pt.play();
        
        return card;
    }
    
    // ============================================================================
    // VUE AGENTS - Liste avec avatars
    // ============================================================================
    private ScrollPane createAgentsView() {
        VBox agentsBox = new VBox(15);
        agentsBox.setPadding(new Insets(30));
        agentsBox.setStyle("-fx-background-color: #f5f7fa;");
        
        // Titre de section
        Label sectionTitle = new Label("Agents Actifs");
        sectionTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        agentsBox.getChildren().add(sectionTitle);
        
        // Agents
        String[][] agentsData = {
            {"🔥", "AggressiveBidder 1", "5,247€", "23 offres", "#e74c3c"},
            {"🔥", "AggressiveBidder 2", "4,892€", "19 offres", "#e74c3c"},
            {"🐢", "ConservativeBidder 1", "2,134€", "7 offres", "#3498db"},
            {"🐢", "ConservativeBidder 2", "1,987€", "5 offres", "#3498db"},
            {"🧠", "IntelligentBidder 1", "7,654€", "31 offres", "#9b59b6"},
            {"🧠", "IntelligentBidder 2", "8,123€", "28 offres", "#9b59b6"},
            {"🏛️", "Auctioneer", "-", "5 enchères", "#f39c12"},
            {"📊", "Monitor", "-", "Actif", "#27ae60"},
            {"🏦", "Bank", "45,231€", "15 comptes", "#16a085"},
            {"📈", "MarketAnalyst", "-", "Analyse", "#2980b9"}
        };
        
        for (String[] agent : agentsData) {
            agentsBox.getChildren().add(createAgentRow(agent[0], agent[1], agent[2], 
                                                       agent[3], agent[4]));
        }
        
        ScrollPane scrollPane = new ScrollPane(agentsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        return scrollPane;
    }
    
    private HBox createAgentRow(String icon, String name, String budget, 
                               String activity, String color) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");
        
        // Avatar
        Label avatar = new Label(icon);
        avatar.setStyle("-fx-font-size: 32px;" +
                       "-fx-min-width: 50; -fx-min-height: 50;" +
                       "-fx-alignment: center;" +
                       "-fx-background-color: " + color + "20;" +
                       "-fx-background-radius: 25;");
        
        // Info agent
        VBox info = new VBox(5);
        Label agentName = new Label(name);
        agentName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label activityLabel = new Label(activity);
        activityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        info.getChildren().addAll(agentName, activityLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Budget
        VBox budgetBox = new VBox(2);
        budgetBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label budgetLabel = new Label("Budget");
        budgetLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        
        Label budgetValue = new Label(budget);
        budgetValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        budgetBox.getChildren().addAll(budgetLabel, budgetValue);
        
        // Indicateur de statut
        Circle statusIndicator = new Circle(5, Color.web("#27ae60"));
        statusIndicator.setEffect(new DropShadow(5, Color.web("#27ae60")));
        
        // Animation du statut
        ScaleTransition st = new ScaleTransition(Duration.millis(1000), statusIndicator);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.3);
        st.setToY(1.3);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();
        
        row.getChildren().addAll(avatar, info, spacer, budgetBox, statusIndicator);
        
        return row;
    }
    
    // ============================================================================
    // VUE ANALYTICS - Graphiques
    // ============================================================================
    private VBox createAnalyticsView() {
        VBox analyticsBox = new VBox(20);
        analyticsBox.setPadding(new Insets(30));
        analyticsBox.setStyle("-fx-background-color: #f5f7fa;");
        
        // Titre
        Label title = new Label("📊 Analyse en Temps Réel");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Graphique des prix
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        priceChart = new LineChart<>(xAxis, yAxis);
        priceChart.setTitle("Évolution des Prix");
        priceChart.setPrefHeight(300);
        priceChart.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("ITEM-1");
        series1.getData().add(new XYChart.Data<>("10:00", 100));
        series1.getData().add(new XYChart.Data<>("10:05", 120));
        series1.getData().add(new XYChart.Data<>("10:10", 145));
        series1.getData().add(new XYChart.Data<>("10:15", 180));
        
        priceChart.getData().add(series1);
        
        // Graphique des agents
        CategoryAxis xAxisBar = new CategoryAxis();
        NumberAxis yAxisBar = new NumberAxis();
        agentChart = new BarChart<>(xAxisBar, yAxisBar);
        agentChart.setTitle("Activité des Agents");
        agentChart.setPrefHeight(300);
        agentChart.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Nombre d'offres");
        series2.getData().add(new XYChart.Data<>("Aggressive", 23));
        series2.getData().add(new XYChart.Data<>("Conservative", 12));
        series2.getData().add(new XYChart.Data<>("Intelligent", 31));
        
        agentChart.getData().add(series2);
        
        analyticsBox.getChildren().addAll(title, priceChart, agentChart);
        
        return analyticsBox;
    }
    
    // ============================================================================
    // VUE ACTIVITÉ - Journal en temps réel
    // ============================================================================
    private ScrollPane createActivityView() {
        VBox activityBox = new VBox(10);
        activityBox.setPadding(new Insets(30));
        activityBox.setStyle("-fx-background-color: #f5f7fa;");
        
        Label title = new Label("📝 Journal d'Activité");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        activityBox.getChildren().add(title);
        
        // Logs simulés
        String[] logs = {
            "✅ aggressive1 a placé une offre de 245€ sur ITEM-3",
            "🆕 Nouvelle enchère créée: ITEM-7",
            "💸 Paiement traité: 189€ de conservative2",
            "📊 MarketAnalyst: Tendance haussière détectée",
            "🏦 Compte créé pour intelligent2",
            "⚠️ Anomalie détectée: Volume suspect sur ITEM-2"
        };
        
        for (String log : logs) {
            activityBox.getChildren().add(createLogEntry(log));
        }
        
        ScrollPane scrollPane = new ScrollPane(activityBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        return scrollPane;
    }
    
    private HBox createLogEntry(String text) {
        HBox entry = new HBox(15);
        entry.setPadding(new Insets(12, 15, 12, 15));
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.setStyle("-fx-background-color: white;" +
                      "-fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        
        Label time = new Label(String.format("%tH:%tM:%tS", new Date(), new Date(), new Date()));
        time.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-family: monospace;");
        
        Label message = new Label(text);
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        message.setWrapText(true);
        
        entry.getChildren().addAll(time, message);
        
        return entry;
    }
    
    // ============================================================================
    // SIDEBAR DROITE - Statistiques rapides
    // ============================================================================
    private VBox createRightSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setStyle("-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 0 1;");
        
        Label title = new Label("⚡ Quick Stats");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        sidebar.getChildren().addAll(
            title,
            createQuickStat("🎯", "Enchères totales", "47"),
            createQuickStat("💰", "Volume total", "45,231€"),
            createQuickStat("👥", "Agents actifs", "15/15"),
            createQuickStat("⚡", "Offres/minute", "12.4"),
            createQuickStat("🏆", "Top Agent", "intelligent1")
        );
        
        return sidebar;
    }
    
    private VBox createQuickStat(String icon, String label, String value) {
        VBox stat = new VBox(5);
        stat.setPadding(new Insets(12));
        stat.setStyle("-fx-background-color: #f8f9fa;" +
                     "-fx-background-radius: 10;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        header.getChildren().addAll(iconLabel, labelText);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        stat.getChildren().addAll(header, valueLabel);
        
        return stat;
    }
    
    // ============================================================================
    // FOOTER
    // ============================================================================
    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(15, 30, 15, 30));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: white;" +
                       "-fx-border-color: #e0e0e0;" +
                       "-fx-border-width: 1 0 0 0;");
        
        Label copyright = new Label("© 2024 Auction System - Multi-Agent Platform");
        copyright.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label status = new Label("🟢 Système Opérationnel");
        status.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        footer.getChildren().addAll(copyright, spacer, status);
        
        return footer;
    }
    
    // ============================================================================
    // SIMULATION - Mise à jour en temps réel
    // ============================================================================
    private void startSimulation() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            // Simuler des mises à jour
            Platform.runLater(() -> {
                // Ajouter un log
                // Mettre à jour les graphiques
                // Etc.
            });
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
    // ============================================================================
    // CLASSES UTILITAIRES
    // ============================================================================
    class AuctionItemUI {
        String id;
        String name;
        double currentPrice;
        String winner;
    }
    
    class AgentUI {
        String name;
        String type;
        double budget;
        int bids;
    }
    
    class ActivityLog {
        Date timestamp;
        String message;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}