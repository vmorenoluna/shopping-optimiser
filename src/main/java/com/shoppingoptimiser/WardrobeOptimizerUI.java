package com.shoppingoptimiser;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.shoppingoptimiser.domain.ClothingCategory;
import com.shoppingoptimiser.domain.ClothingItem;
import com.shoppingoptimiser.domain.WardrobeSolution;
import com.shoppingoptimiser.solver.WardrobeConstraintProvider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * JavaFX UI for the shopping optimizer.
 * Shows visual representations of clothing items and solver progress.
 */
public class WardrobeOptimizerUI extends Application {

    private TextArea logArea;
    private Canvas inventoryCanvas;
    private Canvas solutionCanvas;
    private Label statusLabel;
    private Label scoreLabel;
    private Label budgetDisplayLabel;
    private ProgressBar progressBar;
    private Button solveButton;
    private Button regenerateButton;
    private Spinner<Double> budgetSpinner;
    private Spinner<Integer> inventorySizeSpinner;
    private Label inventoryLabel;

    private List<ClothingItem> items;
    private WardrobeSolution solution;
    private double budget = 120.0;
    private int inventorySize = 500;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Shopping Optimizer - Timefold Solver");

        // Main layout - use SplitPane for resizable bottom
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Title and controls
        VBox topBox = createTopSection();
        root.setTop(topBox);

        // Center and Bottom: Vertically split with center (inventory/solution) and bottom (log)
        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        mainSplit.setDividerPositions(0.75);  // 75% for center, 25% for log

        // Center: Split view with inventory and solution
        SplitPane centerSplit = createCenterSection();

        // Bottom: Log area
        VBox bottomBox = createBottomSection();

        mainSplit.getItems().addAll(centerSplit, bottomBox);
        root.setCenter(mainSplit);

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize with sample data
        initializeInventory();
    }

    private VBox createTopSection() {
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: #2c3e50;");

        Label titleLabel = new Label("Shopping Optimizer");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Maximize outfit combinations while staying within budget");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);

        // Configuration panel
        HBox configPanel = new HBox(15);
        configPanel.setAlignment(Pos.CENTER_LEFT);
        configPanel.setPadding(new Insets(10, 0, 0, 0));

        // Budget spinner
        Label budgetConfigLabel = new Label("Budget: $");
        budgetConfigLabel.setFont(Font.font("Arial", 14));
        budgetConfigLabel.setTextFill(Color.WHITE);

        budgetSpinner = new Spinner<>(50.0, 500.0, budget, 10.0);
        budgetSpinner.setEditable(true);
        budgetSpinner.setPrefWidth(100);
        budgetSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            budget = newVal;
            budgetDisplayLabel.setText("Budget: $" + String.format("%.0f", budget));
        });

        // Inventory size spinner
        Label inventorySizeLabel = new Label("Items:");
        inventorySizeLabel.setFont(Font.font("Arial", 14));
        inventorySizeLabel.setTextFill(Color.WHITE);

        inventorySizeSpinner = new Spinner<>(100, 1000, inventorySize, 100);
        inventorySizeSpinner.setEditable(true);
        inventorySizeSpinner.setPrefWidth(100);
        inventorySizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            inventorySize = newVal;
            inventoryLabel.setText("Inventory (" + inventorySize + " items)");
        });

        // Regenerate button
        regenerateButton = new Button("Regenerate Inventory");
        regenerateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        regenerateButton.setOnAction(e -> initializeInventory());

        configPanel.getChildren().addAll(budgetConfigLabel, budgetSpinner,
                                         new Separator(javafx.geometry.Orientation.VERTICAL),
                                         inventorySizeLabel, inventorySizeSpinner,
                                         regenerateButton);

        // Control panel
        HBox controlPanel = new HBox(15);
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.setPadding(new Insets(10, 0, 0, 0));

        budgetDisplayLabel = new Label("Budget: $" + String.format("%.0f", budget));
        budgetDisplayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        budgetDisplayLabel.setTextFill(Color.WHITE);

        statusLabel = new Label("Ready to solve");
        statusLabel.setFont(Font.font("Arial", 14));
        statusLabel.setTextFill(Color.LIGHTGREEN);

        scoreLabel = new Label("Score: --");
        scoreLabel.setFont(Font.font("Arial", 14));
        scoreLabel.setTextFill(Color.WHITE);

        solveButton = new Button("Solve");
        solveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        solveButton.setOnAction(e -> solveProblem());

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);

        controlPanel.getChildren().addAll(budgetDisplayLabel, new Separator(javafx.geometry.Orientation.VERTICAL),
                                         statusLabel, scoreLabel,
                                         new Separator(javafx.geometry.Orientation.VERTICAL),
                                         solveButton, progressBar);

        topBox.getChildren().addAll(titleLabel, subtitleLabel, configPanel, controlPanel);
        return topBox;
    }

    private SplitPane createCenterSection() {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        // Left: Inventory view
        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(10));

        inventoryLabel = new Label("Inventory (" + inventorySize + " items)");
        inventoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        inventoryCanvas = new Canvas(650, 2000);  // Start with larger height
        ScrollPane inventoryScroll = new ScrollPane(inventoryCanvas);
        inventoryScroll.setFitToWidth(false);
        inventoryScroll.setPrefViewportHeight(500);

        // Bind canvas width to scroll pane viewport width and redraw on resize
        inventoryScroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && items != null && !items.isEmpty()) {
                inventoryCanvas.setWidth(Math.max(650, newVal.getWidth()));
                drawInventory();
            }
        });

        leftBox.getChildren().addAll(inventoryLabel, inventoryScroll);
        VBox.setVgrow(inventoryScroll, Priority.ALWAYS);

        // Right: Solution view
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(10));

        Label solutionLabel = new Label("Selected Items & Outfit Combinations");
        solutionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        solutionCanvas = new Canvas(650, 2000);  // Start with larger height
        ScrollPane solutionScroll = new ScrollPane(solutionCanvas);
        solutionScroll.setFitToWidth(false);
        solutionScroll.setPrefViewportHeight(500);

        // Bind canvas width to scroll pane viewport width and redraw on resize
        solutionScroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && solution != null) {
                solutionCanvas.setWidth(Math.max(650, newVal.getWidth()));
                drawSolution();
            }
        });

        rightBox.getChildren().addAll(solutionLabel, solutionScroll);
        VBox.setVgrow(solutionScroll, Priority.ALWAYS);

        splitPane.getItems().addAll(leftBox, rightBox);
        return splitPane;
    }

    private VBox createBottomSection() {
        VBox bottomBox = new VBox(5);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        Label logLabel = new Label("Solver Log:");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        // Make the TextArea grow to fill available space
        VBox.setVgrow(logArea, Priority.ALWAYS);

        bottomBox.getChildren().addAll(logLabel, logArea);
        return bottomBox;
    }

    private void initializeInventory() {
        log("Initializing inventory with " + inventorySize + " items...");
        items = createSampleWardrobe();
        log("Inventory created: " + items.size() + " items");
        inventoryLabel.setText("Inventory (" + items.size() + " items)");

        // Clear solution
        solution = null;
        GraphicsContext gc = solutionCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, solutionCanvas.getWidth(), solutionCanvas.getHeight());

        // Reset status
        statusLabel.setText("Ready to solve");
        statusLabel.setTextFill(Color.LIGHTGREEN);
        scoreLabel.setText("Score: --");
        progressBar.setProgress(0);

        drawInventory();
        logInventoryStatistics();
    }

    private void drawInventory() {
        GraphicsContext gc = inventoryCanvas.getGraphicsContext2D();

        // Group by category
        Map<ClothingCategory, List<ClothingItem>> byCategory = items.stream()
                .collect(Collectors.groupingBy(ClothingItem::getCategory));

        double y = 20;
        double canvasWidth = inventoryCanvas.getWidth() - 20;  // Use actual canvas width minus padding
        int itemsPerRow = Math.max(1, (int) (canvasWidth / 60));

        for (ClothingCategory category : ClothingCategory.values()) {
            List<ClothingItem> categoryItems = byCategory.getOrDefault(category, new ArrayList<>());
            if (categoryItems.isEmpty()) continue;

            // Draw category header
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setFill(Color.BLACK);
            gc.fillText(category + " (" + categoryItems.size() + " items)", 10, y);
            y += 25;

            // Draw all items for this category
            double x = 10;
            int itemsInRow = 0;
            for (int i = 0; i < categoryItems.size(); i++) {
                ClothingItem item = categoryItems.get(i);
                drawClothingItem(gc, item, x, y, false);
                x += 60;
                itemsInRow++;

                if (itemsInRow >= itemsPerRow) {
                    x = 10;
                    y += 70;
                    itemsInRow = 0;
                }
            }

            // Move to next category (add space if items didn't fill last row)
            if (itemsInRow > 0) {
                y += 70;
            }
            y += 20;  // Extra spacing between categories
        }

        // Update canvas height to fit all content
        double finalHeight = Math.max(2000, y + 50);
        inventoryCanvas.setHeight(finalHeight);

        // Clear the entire canvas first with the new height
        gc.clearRect(0, 0, inventoryCanvas.getWidth(), inventoryCanvas.getHeight());

        // Redraw everything now that we know the size
        y = 20;
        for (ClothingCategory category : ClothingCategory.values()) {
            List<ClothingItem> categoryItems = byCategory.getOrDefault(category, new ArrayList<>());
            if (categoryItems.isEmpty()) continue;

            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setFill(Color.BLACK);
            gc.fillText(category + " (" + categoryItems.size() + " items)", 10, y);
            y += 25;

            double x = 10;
            int itemsInRow = 0;
            for (int i = 0; i < categoryItems.size(); i++) {
                ClothingItem item = categoryItems.get(i);
                drawClothingItem(gc, item, x, y, false);
                x += 60;
                itemsInRow++;

                if (itemsInRow >= itemsPerRow) {
                    x = 10;
                    y += 70;
                    itemsInRow = 0;
                }
            }

            if (itemsInRow > 0) {
                y += 70;
            }
            y += 20;
        }
    }

    private void drawClothingItem(GraphicsContext gc, ClothingItem item, double x, double y, boolean selected) {
        // Get color from item
        Color itemColor = getColorFromString(item.getColor());

        // Draw based on category
        switch (item.getCategory()) {
            case TOP:
                drawTop(gc, x, y, itemColor, selected, item.isInStock());
                break;
            case BOTTOM:
                drawBottom(gc, x, y, itemColor, selected, item.isInStock());
                break;
            case SHOES:
                drawShoes(gc, x, y, itemColor, selected, item.isInStock());
                break;
            case OUTERWEAR:
                drawOuterwear(gc, x, y, itemColor, selected, item.isInStock());
                break;
            case ACCESSORY:
                drawAccessory(gc, x, y, itemColor, selected, item.isInStock());
                break;
        }

        // Draw price
        gc.setFont(Font.font("Arial", 10));
        gc.setFill(Color.BLACK);
        gc.fillText("$" + (int)item.getPrice(), x + 5, y + 60);

        // Draw selection indicator
        if (selected) {
            gc.setStroke(Color.GREEN);
            gc.setLineWidth(3);
            gc.strokeRect(x - 2, y - 2, 54, 64);
        }

        // Draw out of stock indicator
        if (!item.isInStock()) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, x + 50, y + 60);
            gc.strokeLine(x + 50, y, x, y + 60);
        }
    }

    private void drawTop(GraphicsContext gc, double x, double y, Color color, boolean selected, boolean inStock) {
        // Draw a T-shirt shape
        double alpha = inStock ? 1.0 : 0.3;
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

        // Body
        gc.fillRect(x + 10, y + 15, 30, 35);
        // Sleeves
        gc.fillRect(x + 5, y + 15, 10, 15);
        gc.fillRect(x + 35, y + 15, 10, 15);
        // Collar
        gc.fillRect(x + 20, y + 10, 10, 5);

        // Outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x + 10, y + 15, 30, 35);
        gc.strokeRect(x + 5, y + 15, 10, 15);
        gc.strokeRect(x + 35, y + 15, 10, 15);
    }

    private void drawBottom(GraphicsContext gc, double x, double y, Color color, boolean selected, boolean inStock) {
        // Draw pants shape
        double alpha = inStock ? 1.0 : 0.3;
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

        // Waist
        gc.fillRect(x + 10, y + 10, 30, 5);
        // Left leg
        gc.fillRect(x + 12, y + 15, 12, 35);
        // Right leg
        gc.fillRect(x + 26, y + 15, 12, 35);

        // Outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x + 12, y + 15, 12, 35);
        gc.strokeRect(x + 26, y + 15, 12, 35);
    }

    private void drawShoes(GraphicsContext gc, double x, double y, Color color, boolean selected, boolean inStock) {
        // Draw shoe shape
        double alpha = inStock ? 1.0 : 0.3;
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

        // Left shoe
        gc.fillOval(x + 8, y + 30, 15, 20);
        gc.fillRect(x + 8, y + 35, 15, 10);

        // Right shoe
        gc.fillOval(x + 27, y + 30, 15, 20);
        gc.fillRect(x + 27, y + 35, 15, 10);

        // Outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(x + 8, y + 30, 15, 20);
        gc.strokeOval(x + 27, y + 30, 15, 20);
    }

    private void drawOuterwear(GraphicsContext gc, double x, double y, Color color, boolean selected, boolean inStock) {
        // Draw jacket shape
        double alpha = inStock ? 1.0 : 0.3;
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

        // Body
        gc.fillRect(x + 8, y + 15, 34, 40);
        // Collar
        gc.fillPolygon(new double[]{x + 18, x + 25, x + 20},
                      new double[]{y + 15, y + 5, y + 15}, 3);
        gc.fillPolygon(new double[]{x + 32, x + 25, x + 30},
                      new double[]{y + 15, y + 5, y + 15}, 3);

        // Outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x + 8, y + 15, 34, 40);
        gc.strokeLine(x + 25, y + 15, x + 25, y + 55);
    }

    private void drawAccessory(GraphicsContext gc, double x, double y, Color color, boolean selected, boolean inStock) {
        // Draw accessory as a simple circle/badge
        double alpha = inStock ? 1.0 : 0.3;
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

        gc.fillOval(x + 15, y + 20, 20, 20);

        // Outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(x + 15, y + 20, 20, 20);

        // Inner circle
        gc.strokeOval(x + 20, y + 25, 10, 10);
    }

    private Color getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "black": return Color.BLACK;
            case "white": return Color.WHITE;
            case "gray": case "grey": return Color.GRAY;
            case "navy": return Color.DARKBLUE;
            case "blue": return Color.BLUE;
            case "red": return Color.RED;
            case "green": return Color.GREEN;
            case "beige": return Color.BEIGE;
            case "brown": return Color.BROWN;
            case "purple": return Color.PURPLE;
            case "yellow": return Color.YELLOW;
            case "orange": return Color.ORANGE;
            default: return Color.LIGHTGRAY;
        }
    }

    private void solveProblem() {
        solveButton.setDisable(true);
        statusLabel.setText("Solving...");
        statusLabel.setTextFill(Color.YELLOW);
        progressBar.setProgress(-1); // Indeterminate

        // Solve in background thread
        new Thread(() -> {
            try {
                log("\n=== Starting Solver ===");
                log("Budget: $" + budget);
                log("Available items: " + items.size());
                log("Preferred styles: casual, smart-casual");

                // Create problem
                List<String> preferredStyles = List.of("casual", "smart-casual");
                WardrobeSolution problem = new WardrobeSolution(items, budget, preferredStyles);

                // Configure solver with random seed for non-deterministic results
                SolverConfig solverConfig = new SolverConfig()
                        .withSolutionClass(WardrobeSolution.class)
                        .withEntityClasses(ClothingItem.class)
                        .withConstraintProviderClass(WardrobeConstraintProvider.class)
                        .withRandomSeed(System.currentTimeMillis());  // Different seed each run

                solverConfig.withTerminationConfig(
                        new ai.timefold.solver.core.config.solver.termination.TerminationConfig()
                                .withBestScoreLimit("0hard/*soft")
                                .withSpentLimit(Duration.ofSeconds(60)));

                SolverFactory<WardrobeSolution> solverFactory = SolverFactory.create(solverConfig);
                Solver<WardrobeSolution> solver = solverFactory.buildSolver();

                log("Solving (max 60 seconds or until optimal solution found)...");
                long startTime = System.currentTimeMillis();
                solution = solver.solve(problem);
                long endTime = System.currentTimeMillis();
                double solvingTime = (endTime - startTime) / 1000.0;

                log("Solving completed in " + String.format("%.2f", solvingTime) + " seconds");
                log("\n=== Solution Found ===");
                log("Score: " + solution.getScore());
                log("Total Cost: $" + String.format("%.2f", solution.getTotalCost()));
                log("Items Selected: " + solution.getSelectedItems().size());

                List<ClothingItem> selected = solution.getSelectedItems();
                log("\nSelected Items:");
                for (ClothingItem item : selected) {
                    log("  - " + item.getName() + " (" + item.getCategory() + ") - $" +
                        String.format("%.2f", item.getPrice()) + " - " + item.getStyle() + " - " + item.getColor());
                }

                // Calculate outfit combinations
                long tops = selected.stream().filter(item -> item.getCategory() == ClothingCategory.TOP).count();
                long bottoms = selected.stream().filter(item -> item.getCategory() == ClothingCategory.BOTTOM).count();
                long shoes = selected.stream().filter(item -> item.getCategory() == ClothingCategory.SHOES).count();

                log("\nOutfit Combinations:");
                log("  Tops: " + tops);
                log("  Bottoms: " + bottoms);
                log("  Shoes: " + shoes);
                log("  Basic combinations: " + (tops * bottoms));
                log("  With shoe variations: " + (tops * bottoms * Math.max(1, shoes)));

                // Update UI
                Platform.runLater(() -> {
                    statusLabel.setText("Solution Found!");
                    statusLabel.setTextFill(Color.LIGHTGREEN);
                    scoreLabel.setText("Score: " + solution.getScore());
                    progressBar.setProgress(1.0);
                    drawSolution();
                    solveButton.setDisable(false);
                });

            } catch (Exception e) {
                log("ERROR: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error!");
                    statusLabel.setTextFill(Color.RED);
                    progressBar.setProgress(0);
                    solveButton.setDisable(false);
                });
            }
        }).start();
    }

    private void drawSolution() {
        if (solution == null) return;

        GraphicsContext gc = solutionCanvas.getGraphicsContext2D();

        List<ClothingItem> selected = solution.getSelectedItems();

        // First pass: calculate required height
        double y = 120;
        Map<ClothingCategory, List<ClothingItem>> byCategory = selected.stream()
                .collect(Collectors.groupingBy(ClothingItem::getCategory));

        double canvasWidth = solutionCanvas.getWidth() - 20;  // Use actual canvas width minus padding
        int itemsPerRow = Math.max(1, (int) (canvasWidth / 80));

        for (ClothingCategory category : ClothingCategory.values()) {
            List<ClothingItem> categoryItems = byCategory.getOrDefault(category, new ArrayList<>());
            if (categoryItems.isEmpty()) continue;

            y += 25;  // Header
            int rows = (int) Math.ceil((double) categoryItems.size() / itemsPerRow);
            y += rows * 100;  // Items with labels
            y += 20;  // Spacing between categories
        }
        y += 50;  // Outfit combinations line

        // Set canvas height
        double finalHeight = Math.max(2000, y + 50);
        solutionCanvas.setHeight(finalHeight);
        gc.clearRect(0, 0, solutionCanvas.getWidth(), solutionCanvas.getHeight());

        // Second pass: draw everything
        // Draw summary
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setFill(Color.BLACK);
        gc.fillText("Solution Summary", 10, 20);

        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Score: " + solution.getScore(), 10, 45);
        gc.fillText("Total Cost: $" + String.format("%.2f", solution.getTotalCost()) + " / $" + budget, 10, 65);
        gc.fillText("Items Selected: " + selected.size(), 10, 85);

        y = 120;
        for (ClothingCategory category : ClothingCategory.values()) {
            List<ClothingItem> categoryItems = byCategory.getOrDefault(category, new ArrayList<>());
            if (categoryItems.isEmpty()) continue;

            // Draw category header
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setFill(Color.BLACK);
            gc.fillText(category + " (" + categoryItems.size() + " items)", 10, y);
            y += 25;

            // Draw items
            double x = 10;
            int itemsInRow = 0;
            for (ClothingItem item : categoryItems) {
                drawClothingItem(gc, item, x, y, true);

                // Draw item details
                gc.setFont(Font.font("Arial", 10));
                gc.setFill(Color.BLACK);
                gc.fillText(item.getName(), x, y + 75);

                x += 80;
                itemsInRow++;

                if (itemsInRow >= itemsPerRow) {
                    x = 10;
                    y += 100;
                    itemsInRow = 0;
                }
            }

            if (itemsInRow > 0) {
                y += 100;
            }
            y += 20;  // Spacing between categories
        }

        // Draw outfit combinations visualization
        List<ClothingItem> tops = selected.stream()
                .filter(item -> item.getCategory() == ClothingCategory.TOP)
                .collect(Collectors.toList());
        List<ClothingItem> bottoms = selected.stream()
                .filter(item -> item.getCategory() == ClothingCategory.BOTTOM)
                .collect(Collectors.toList());
        List<ClothingItem> shoesList = selected.stream()
                .filter(item -> item.getCategory() == ClothingCategory.SHOES)
                .collect(Collectors.toList());
        List<ClothingItem> outerwearList = selected.stream()
                .filter(item -> item.getCategory() == ClothingCategory.OUTERWEAR)
                .collect(Collectors.toList());
        List<ClothingItem> accessoryList = selected.stream()
                .filter(item -> item.getCategory() == ClothingCategory.ACCESSORY)
                .collect(Collectors.toList());

        long totalCombinations = tops.size() * bottoms.size() * Math.max(1, shoesList.size());

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setFill(Color.BLACK);
        gc.fillText("Outfit Combinations: " + totalCombinations, 10, y);
        y += 30;

        // Draw a few example combinations (up to 6)
        if (!tops.isEmpty() && !bottoms.isEmpty()) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("Example Outfits:", 10, y);
            y += 25;

            int combinationsToShow = Math.min(6, (int)totalCombinations);
            int comboCount = 0;

            outerLoop:
            for (ClothingItem top : tops) {
                for (ClothingItem bottom : bottoms) {
                    if (comboCount >= combinationsToShow) break outerLoop;

                    ClothingItem shoe = shoesList.isEmpty() ? null : shoesList.get(comboCount % shoesList.size());
                    // Optionally add outerwear (show on some outfits)
                    ClothingItem outerwear = !outerwearList.isEmpty() && comboCount % 2 == 0 ?
                                            outerwearList.get(comboCount % outerwearList.size()) : null;
                    // Optionally add accessory (show on some outfits)
                    ClothingItem accessory = !accessoryList.isEmpty() && comboCount % 3 == 0 ?
                                            accessoryList.get(comboCount % accessoryList.size()) : null;

                    // Draw outfit number
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    gc.setFill(Color.DARKGRAY);
                    gc.fillText("Outfit " + (comboCount + 1), 10, y + 10);

                    double xPos = 10;

                    // Draw the top
                    drawClothingItem(gc, top, xPos, y + 15, false);
                    gc.setFont(Font.font("Arial", 9));
                    gc.setFill(Color.BLACK);
                    gc.fillText(top.getName().length() > 12 ? top.getName().substring(0, 12) + "..." : top.getName(),
                               xPos, y + 95);
                    xPos += 65;

                    // Draw the bottom
                    drawClothingItem(gc, bottom, xPos, y + 15, false);
                    gc.fillText(bottom.getName().length() > 12 ? bottom.getName().substring(0, 12) + "..." : bottom.getName(),
                               xPos, y + 95);
                    xPos += 65;

                    // Draw the shoes if available
                    if (shoe != null) {
                        drawClothingItem(gc, shoe, xPos, y + 15, false);
                        gc.fillText(shoe.getName().length() > 12 ? shoe.getName().substring(0, 12) + "..." : shoe.getName(),
                                   xPos, y + 95);
                        xPos += 65;
                    }

                    // Draw outerwear if selected for this outfit
                    if (outerwear != null) {
                        drawClothingItem(gc, outerwear, xPos, y + 15, false);
                        gc.fillText(outerwear.getName().length() > 11 ? outerwear.getName().substring(0, 11) + "..." : outerwear.getName(),
                                   xPos, y + 95);
                        xPos += 65;
                    }

                    // Draw accessory if selected for this outfit
                    if (accessory != null) {
                        drawClothingItem(gc, accessory, xPos, y + 15, false);
                        gc.fillText(accessory.getName().length() > 11 ? accessory.getName().substring(0, 11) + "..." : accessory.getName(),
                                   xPos, y + 95);
                        xPos += 65;
                    }

                    comboCount++;
                    y += 110;
                }
            }

            if (totalCombinations > combinationsToShow) {
                gc.setFont(Font.font("Arial", 12));
                gc.setFill(Color.GRAY);
                gc.fillText("... and " + (totalCombinations - combinationsToShow) + " more combinations", 10, y);
                y += 25;
            }
        }
    }

    private void logInventoryStatistics() {
        log("\n=== Inventory Statistics ===");

        // Category breakdown
        Map<ClothingCategory, Long> categoryCount = items.stream()
                .collect(Collectors.groupingBy(ClothingItem::getCategory, Collectors.counting()));
        log("\nItems by Category:");
        categoryCount.forEach((category, count) -> log("  " + category + ": " + count));

        // Style breakdown
        Map<String, Long> styleCount = items.stream()
                .collect(Collectors.groupingBy(ClothingItem::getStyle, Collectors.counting()));
        log("\nItems by Style:");
        styleCount.forEach((style, count) -> log("  " + style + ": " + count));

        // Stock status
        long inStock = items.stream().filter(ClothingItem::isInStock).count();
        long outOfStock = items.size() - inStock;
        log("\nStock Status:");
        log("  In Stock: " + inStock);
        log("  Out of Stock: " + outOfStock);

        // Price statistics
        double avgPrice = items.stream().mapToDouble(ClothingItem::getPrice).average().orElse(0.0);
        double minPrice = items.stream().mapToDouble(ClothingItem::getPrice).min().orElse(0.0);
        double maxPrice = items.stream().mapToDouble(ClothingItem::getPrice).max().orElse(0.0);
        log("\nPrice Statistics:");
        log("  Average: $" + String.format("%.2f", avgPrice));
        log("  Min: $" + String.format("%.2f", minPrice));
        log("  Max: $" + String.format("%.2f", maxPrice));
    }

    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    /**
     * Create sample inventory based on configured size
     */
    private List<ClothingItem> createSampleWardrobe() {
        List<ClothingItem> items = new ArrayList<>();

        String[] styles = {"casual", "formal", "smart-casual", "sport", "streetwear", "vintage", "beach", "outdoor"};
        String[] colors = {"black", "white", "gray", "navy", "blue", "red", "green", "beige", "brown", "purple", "yellow", "orange"};
        String[] topTypes = {"T-shirt", "Dress Shirt", "Sweater", "Polo", "Henley", "Oxford Shirt", "Flannel",
                            "Turtleneck", "Cardigan", "Hoodie", "Tank Top", "Rugby Shirt", "Blouse", "Vest"};
        String[] bottomTypes = {"Jeans", "Chinos", "Dress Pants", "Shorts", "Cargo Pants", "Slacks", "Joggers",
                               "Corduroy Pants", "Leggings", "Track Pants"};
        String[] shoeTypes = {"Sneakers", "Dress Shoes", "Loafers", "Running Shoes", "Boots", "Sandals",
                             "Boat Shoes", "Canvas Shoes", "Oxfords", "Brogues"};
        String[] outerwearTypes = {"Jacket", "Blazer", "Coat", "Parka", "Vest", "Windbreaker", "Raincoat"};
        String[] accessoryTypes = {"Belt", "Watch", "Sunglasses", "Hat", "Scarf", "Tie", "Bracelet"};

        Random rand = new Random(); // Random seed for variety on regenerate

        // Calculate distribution: 40% tops, 30% bottoms, 16% shoes, 8% outerwear, 6% accessories
        int numTops = (int) (inventorySize * 0.40);
        int numBottoms = (int) (inventorySize * 0.30);
        int numShoes = (int) (inventorySize * 0.16);
        int numOuterwear = (int) (inventorySize * 0.08);
        int numAccessories = inventorySize - numTops - numBottoms - numShoes - numOuterwear;

        // Generate Tops
        for (int i = 1; i <= numTops; i++) {
            String id = "T" + i;
            String type = topTypes[rand.nextInt(topTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 15 + rand.nextInt(85);
            boolean inStock = rand.nextDouble() > 0.05;
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.TOP, price, style, color, inStock));
        }

        // Generate Bottoms
        for (int i = 1; i <= numBottoms; i++) {
            String id = "B" + i;
            String type = bottomTypes[rand.nextInt(bottomTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 25 + rand.nextInt(100);
            boolean inStock = rand.nextDouble() > 0.05;
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.BOTTOM, price, style, color, inStock));
        }

        // Generate Shoes
        for (int i = 1; i <= numShoes; i++) {
            String id = "S" + i;
            String type = shoeTypes[rand.nextInt(shoeTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 40 + rand.nextInt(140);
            boolean inStock = rand.nextDouble() > 0.08;
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.SHOES, price, style, color, inStock));
        }

        // Generate Outerwear
        for (int i = 1; i <= numOuterwear; i++) {
            String id = "O" + i;
            String type = outerwearTypes[rand.nextInt(outerwearTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 60 + rand.nextInt(140);
            boolean inStock = rand.nextDouble() > 0.10;
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.OUTERWEAR, price, style, color, inStock));
        }

        // Generate Accessories
        for (int i = 1; i <= numAccessories; i++) {
            String id = "A" + i;
            String type = accessoryTypes[rand.nextInt(accessoryTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 10 + rand.nextInt(140);
            boolean inStock = rand.nextDouble() > 0.05;
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.ACCESSORY, price, style, color, inStock));
        }

        return items;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
