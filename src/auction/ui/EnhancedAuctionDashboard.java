// ============================================================================
// ENHANCED DASHBOARD - Version améliorée avec intégration JADE
// ============================================================================
package auction.ui;

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
import jade.wrapper.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.*;

public class EnhancedAuctionDashboard extends Application {
    
    private AgentContainer container;
    private ObservableList<AuctionData> auctions;
    private ObservableList<AgentData> agents;
    private TableView<AuctionData> auctionTable;
    private TableView<AgentData> agentTable;
    private TextArea logArea;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🏛️ Système Multi-Agents d'Enchères - Dashboard Moderne");
        
        auctions = FXCollections.observableArrayList();
        agents = FXCollections.observableArrayList();
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");
        
        // Header avec animation
        root.setTop(createAnimatedHeader());
        
        // Contenu principal avec tabs
        root.setCenter(createMainTabs());
        
        // Panel de contrôle
        root.setBottom(createControlPanel());
        
        Scene scene = new Scene(root, 1600, 1000);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        // Démarrer le monitoring
        startRealtimeMonitoring();
    }
    
    private VBox createAnimatedHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(30, 40, 30, 40));
        header.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1);" +
                       "-fx-background-radius: 0 0 30 30;");
        
        HBox titleBox = new HBox(20);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        // Titre principal avec gradient
        Label title = new Label("🏛️ AUCTION SYSTEM");
        title.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; " +
                      "-fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
        
        // Animation du titre
        ScaleTransition st = new ScaleTransition(Duration.millis(2000), title);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Statistiques en temps réel
        HBox statsBox = new HBox(20);
        statsBox.getChildren().addAll(
            createGlassStatCard("🎯", "Enchères", "5", "#4CAF50"),
            createGlassStatCard("👥", "Agents", "15", "#2196F3"),
            createGlassStatCard("💰", "Volume", "45K€", "#FF9800")
        );
        
        titleBox.getChildren().addAll(title, spacer, statsBox);
        
        Label subtitle = new Label("Plateforme Multi-Agents en Temps Réel • JADE Framework");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.9);");
        
        header.getChildren().addAll(titleBox, subtitle);
        
        return header;
    }
    
    private VBox createGlassStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 25, 15, 25));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);" +
                     "-fx-background-radius: 15;" +
                     "-fx-border-color: rgba(255, 255, 255, 0.3);" +
                     "-fx-border-radius: 15;" +
                     "-fx-border-width: 2;" +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        
        // Effet de verre (glassmorphism)
        card.setEffect(new GaussianBlur(2));
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);");
        
        card.getChildren().addAll(iconLabel, valueLabel, labelText);
        
        // Animation de pulsation
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(card.scaleXProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(card.scaleXProperty(), 1.05)),
            new KeyFrame(Duration.seconds(2), new KeyValue(card.scaleXProperty(), 1.0))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
        return card;
    }
    
    private TabPane createMainTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        
        Tab dashboardTab = new Tab("📊 Dashboard");
        dashboardTab.setContent(createDashboardView());
        
        Tab auctionsTab = new Tab("🎯 Enchères Live");
        auctionsTab.setContent(createAuctionsTable());
        
        Tab agentsTab = new Tab("👥 Agents");
        agentsTab.setContent(createAgentsTable());
        
        Tab analyticsTab = new Tab("📈 Analytics");
        analyticsTab.setContent(createAnalyticsView());
        
        Tab logsTab = new Tab("📝 Logs");
        logsTab.setContent(createLogsView());
        
        tabPane.getTabs().addAll(dashboardTab, auctionsTab, agentsTab, analyticsTab, logsTab);
        
        return tabPane;
    }
    
    private GridPane createDashboardView() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: rgba(0, 0, 0, 0.05);");
        
        // Créer des cartes de visualisation
        VBox card1 = createDashboardCard("🔥 Top Enchères", 
            "Les enchères les plus actives du moment");
        VBox card2 = createDashboardCard("👑 Meilleurs Agents", 
            "Classement des agents les plus performants");
        VBox card3 = createDashboardCard("💹 Tendances", 
            "Analyse des tendances du marché");
        VBox card4 = createDashboardCard("⚡ Activité en direct", 
            "Flux d'activité en temps réel");
        
        grid.add(card1, 0, 0);
        grid.add(card2, 1, 0);
        grid.add(card3, 0, 1);
        grid.add(card4, 1, 1);
        
        // Responsive
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);
        
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        grid.getRowConstraints().addAll(row1, row2);
        
        return grid;
    }
    
    private VBox createDashboardCard(String title, String description) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white;" +
                     "-fx-background-radius: 20;" +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);
        
        // Contenu de la carte (à personnaliser)
        Label content = new Label("Chargement des données...");
        content.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");
        
        card.getChildren().addAll(titleLabel, descLabel, new Separator(), content);
        
        VBox.setVgrow(card, Priority.ALWAYS);
        
        return card;
    }
    
    private VBox createAuctionsTable() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white;");
        
        Label title = new Label("🎯 Enchères en Direct");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        auctionTable = new TableView<>();
        auctionTable.setItems(auctions);
        
        TableColumn<AuctionData, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        
        TableColumn<AuctionData, String> nameCol = new TableColumn<>("Article");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        TableColumn<AuctionData, String> priceCol = new TableColumn<>("Prix Actuel");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        
        TableColumn<AuctionData, String> winnerCol = new TableColumn<>("Gagnant");
        winnerCol.setCellValueFactory(cellData -> cellData.getValue().winnerProperty());
        
        TableColumn<AuctionData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        auctionTable.getColumns().addAll(idCol, nameCol, priceCol, winnerCol, statusCol);
        
        container.getChildren().addAll(title, auctionTable);
        VBox.setVgrow(auctionTable, Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createAgentsTable() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white;");
        
        Label title = new Label("👥 Agents Actifs");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        agentTable = new TableView<>();
        agentTable.setItems(agents);
        
        TableColumn<AgentData, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        TableColumn<AgentData, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        
        TableColumn<AgentData, String> budgetCol = new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(cellData -> cellData.getValue().budgetProperty());
        
        TableColumn<AgentData, String> bidsCol = new TableColumn<>("Offres");
        bidsCol.setCellValueFactory(cellData -> cellData.getValue().bidsProperty());
        
        TableColumn<AgentData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        agentTable.getColumns().addAll(nameCol, typeCol, budgetCol, bidsCol, statusCol);
        
        container.getChildren().addAll(title, agentTable);
        VBox.setVgrow(agentTable, Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createAnalyticsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: white;");
        
        // Graphiques d'analyse
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Évolution des Prix en Temps Réel");
        chart.setPrefHeight(400);
        
        container.getChildren().add(chart);
        
        return container;
    }
    
    private VBox createLogsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #1e1e1e;");
        
        Label title = new Label("📝 Journal d'Activité Système");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #2d2d30;" +
                        "-fx-text-fill: #d4d4d4;" +
                        "-fx-font-family: 'Consolas', 'Monaco', monospace;" +
                        "-fx-font-size: 12px;");
        
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        container.getChildren().addAll(title, logArea);
        
        return container;
    }
    
    private HBox createControlPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(20, 30, 20, 30));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95);" +
                      "-fx-border-color: rgba(0, 0, 0, 0.1);" +
                      "-fx-border-width: 2 0 0 0;");
        
        Button pauseBtn = createControlButton("⏸️ Pause", "#f39c12");
        Button resumeBtn = createControlButton("▶️ Reprendre", "#27ae60");
        Button resetBtn = createControlButton("🔄 Réinitialiser", "#e74c3c");
        Button exportBtn = createControlButton("📥 Exporter", "#3498db");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label("🟢 Système Opérationnel");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        panel.getChildren().addAll(pauseBtn, resumeBtn, resetBtn, exportBtn, spacer, statusLabel);
        
        return panel;
    }
    
    private Button createControlButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 20;" +
                    "-fx-cursor: hand;");
        
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        
        return btn;
    }
    
    private void startRealtimeMonitoring() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Platform.runLater(() -> {
                // Mettre à jour les données en temps réel
                updateDashboardData();
            });
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
    private void updateDashboardData() {
        // Simuler des mises à jour de données
        // À connecter avec JADE
    }
    
    // Classes de données
    public static class AuctionData {
        private final javafx.beans.property.SimpleStringProperty id;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty price;
        private final javafx.beans.property.SimpleStringProperty winner;
        private final javafx.beans.property.SimpleStringProperty status;
        
        public AuctionData(String id, String name, String price, String winner, String status) {
            this.id = new javafx.beans.property.SimpleStringProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.price = new javafx.beans.property.SimpleStringProperty(price);
            this.winner = new javafx.beans.property.SimpleStringProperty(winner);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }
        
        public javafx.beans.property.StringProperty idProperty() { return id; }
        public javafx.beans.property.StringProperty nameProperty() { return name; }
        public javafx.beans.property.StringProperty priceProperty() { return price; }
        public javafx.beans.property.StringProperty winnerProperty() { return winner; }
        public javafx.beans.property.StringProperty statusProperty() { return status; }
    }
    
    public static class AgentData {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty budget;
        private final javafx.beans.property.SimpleStringProperty bids;
        private final javafx.beans.property.SimpleStringProperty status;
        
        public AgentData(String name, String type, String budget, String bids, String status) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.budget = new javafx.beans.property.SimpleStringProperty(budget);
            this.bids = new javafx.beans.property.SimpleStringProperty(bids);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }
        
        public javafx.beans.property.StringProperty nameProperty() { return name; }
        public javafx.beans.property.StringProperty typeProperty() { return type; }
        public javafx.beans.property.StringProperty budgetProperty() { return budget; }
        public javafx.beans.property.StringProperty bidsProperty() { return bids; }
        public javafx.beans.property.StringProperty statusProperty() { return status; }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}