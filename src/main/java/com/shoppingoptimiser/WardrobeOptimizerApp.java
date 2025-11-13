package com.shoppingoptimiser;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.shoppingoptimiser.domain.ClothingCategory;
import com.shoppingoptimiser.domain.ClothingItem;
import com.shoppingoptimiser.domain.WardrobeSolution;
import com.shoppingoptimiser.solver.WardrobeConstraintProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application to demonstrate the wardrobe optimizer.
 */
public class WardrobeOptimizerApp {

    public static void main(String[] args) {
        System.out.println("=== Wardrobe Optimizer ===");
        System.out.println("Optimizing clothing purchases to maximize outfit combinations while staying within budget");
        System.out.println();

        // Create sample clothing items
        List<ClothingItem> items = createSampleWardrobe();

        // Define budget
        double budget = 120.0;

        // Define style preferences
        List<String> preferredStyles = List.of("casual", "smart-casual");

        // Create the problem
        WardrobeSolution problem = new WardrobeSolution(items, budget, preferredStyles);

        System.out.println("Problem setup:");
        System.out.println("  Budget: $" + budget);
        System.out.println("  Available items: " + items.size());
        System.out.println("  Preferred styles: " + String.join(", ", preferredStyles));
        System.out.println();

        // Print inventory statistics
        printInventoryStatistics(items);

        // Create solver with termination config: stop when optimal solution found OR 1 minute passed
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(WardrobeSolution.class)
                .withEntityClasses(ClothingItem.class)
                .withConstraintProviderClass(WardrobeConstraintProvider.class);

        // Configure termination: stop at optimal score (0hard) OR after 60 seconds
        solverConfig.withTerminationConfig(new ai.timefold.solver.core.config.solver.termination.TerminationConfig()
                .withBestScoreLimit("0hard/*soft")  // Stop when all hard constraints satisfied
                .withSpentLimit(Duration.ofSeconds(60)));  // OR stop after 60 seconds

        SolverFactory<WardrobeSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<WardrobeSolution> solver = solverFactory.buildSolver();

        // Solve the problem
        System.out.println("Solving (max 60 seconds or until optimal solution found)...");
        long startTime = System.currentTimeMillis();
        WardrobeSolution solution = solver.solve(problem);
        long endTime = System.currentTimeMillis();
        double solvingTime = (endTime - startTime) / 1000.0;

        System.out.println("Solving completed in " + String.format("%.2f", solvingTime) + " seconds");

        // Display results
        System.out.println();
        System.out.println("=== Solution Found ===");
        System.out.println(solution);

        List<ClothingItem> selected = solution.getSelectedItems();
        System.out.println("\nOutfit Combinations:");
        long tops = selected.stream().filter(item -> item.getCategory() == ClothingCategory.TOP).count();
        long bottoms = selected.stream().filter(item -> item.getCategory() == ClothingCategory.BOTTOM).count();
        long shoes = selected.stream().filter(item -> item.getCategory() == ClothingCategory.SHOES).count();
        System.out.println("  Tops: " + tops);
        System.out.println("  Bottoms: " + bottoms);
        System.out.println("  Shoes: " + shoes);
        System.out.println("  Basic combinations: " + (tops * bottoms));
        System.out.println("  With shoe variations: " + (tops * bottoms * Math.max(1, shoes)));
    }

    /**
     * Print inventory statistics with histograms
     */
    private static void printInventoryStatistics(List<ClothingItem> items) {
        System.out.println("=== Inventory Statistics ===");
        System.out.println();

        // Category breakdown
        System.out.println("Items by Category:");
        java.util.Map<ClothingCategory, Long> categoryCount = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ClothingItem::getCategory,
                        java.util.stream.Collectors.counting()));
        printHistogram(categoryCount);

        // Style breakdown
        System.out.println("\nItems by Style:");
        java.util.Map<String, Long> styleCount = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ClothingItem::getStyle,
                        java.util.stream.Collectors.counting()));
        printHistogram(styleCount);

        // Color breakdown
        System.out.println("\nItems by Color:");
        java.util.Map<String, Long> colorCount = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ClothingItem::getColor,
                        java.util.stream.Collectors.counting()));
        printHistogram(colorCount);

        // Price range breakdown
        System.out.println("\nItems by Price Range:");
        java.util.Map<String, Long> priceRanges = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> getPriceRange(item.getPrice()),
                        java.util.stream.Collectors.counting()));
        // Sort by price range
        java.util.Map<String, Long> sortedPriceRanges = new java.util.LinkedHashMap<>();
        sortedPriceRanges.put("$0-$25", priceRanges.getOrDefault("$0-$25", 0L));
        sortedPriceRanges.put("$26-$50", priceRanges.getOrDefault("$26-$50", 0L));
        sortedPriceRanges.put("$51-$75", priceRanges.getOrDefault("$51-$75", 0L));
        sortedPriceRanges.put("$76-$100", priceRanges.getOrDefault("$76-$100", 0L));
        sortedPriceRanges.put("$101+", priceRanges.getOrDefault("$101+", 0L));
        printHistogram(sortedPriceRanges);

        // Stock status
        System.out.println("\nStock Status:");
        long inStock = items.stream().filter(ClothingItem::isInStock).count();
        long outOfStock = items.size() - inStock;
        java.util.Map<String, Long> stockStatus = new java.util.LinkedHashMap<>();
        stockStatus.put("In Stock", inStock);
        stockStatus.put("Out of Stock", outOfStock);
        printHistogram(stockStatus);

        // Price statistics
        System.out.println("\nPrice Statistics:");
        double avgPrice = items.stream().mapToDouble(ClothingItem::getPrice).average().orElse(0.0);
        double minPrice = items.stream().mapToDouble(ClothingItem::getPrice).min().orElse(0.0);
        double maxPrice = items.stream().mapToDouble(ClothingItem::getPrice).max().orElse(0.0);
        double totalInventoryValue = items.stream().mapToDouble(ClothingItem::getPrice).sum();
        System.out.println("  Average: $" + String.format("%.2f", avgPrice));
        System.out.println("  Min: $" + String.format("%.2f", minPrice));
        System.out.println("  Max: $" + String.format("%.2f", maxPrice));
        System.out.println("  Total Inventory Value: $" + String.format("%.2f", totalInventoryValue));

        System.out.println();
    }

    /**
     * Get price range label for an item
     */
    private static String getPriceRange(double price) {
        if (price <= 25) return "$0-$25";
        if (price <= 50) return "$26-$50";
        if (price <= 75) return "$51-$75";
        if (price <= 100) return "$76-$100";
        return "$101+";
    }

    /**
     * Print a horizontal histogram
     */
    private static <T> void printHistogram(java.util.Map<T, Long> data) {
        if (data.isEmpty()) return;

        // Find max value for scaling
        long maxValue = data.values().stream().mapToLong(Long::longValue).max().orElse(1);
        int maxBarLength = 50;

        // Print each bar
        data.forEach((key, value) -> {
            int barLength = (int) ((value * maxBarLength) / maxValue);
            String bar = "█".repeat(Math.max(0, barLength));
            System.out.println(String.format("  %-20s %3d %s", key.toString(), value, bar));
        });
    }

    /**
     * Create a sample wardrobe with various clothing items (500 items total)
     */
    private static List<ClothingItem> createSampleWardrobe() {
        List<ClothingItem> items = new ArrayList<>();

        // Generate 500 items with variety
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

        java.util.Random rand = new java.util.Random(42); // Fixed seed for reproducibility

        // Generate Tops (200 items)
        for (int i = 1; i <= 200; i++) {
            String id = "T" + i;
            String type = topTypes[rand.nextInt(topTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 15 + rand.nextInt(85); // $15-$99
            boolean inStock = rand.nextDouble() > 0.05; // 95% in stock
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.TOP, price, style, color, inStock));
        }

        // Generate Bottoms (150 items)
        for (int i = 1; i <= 150; i++) {
            String id = "B" + i;
            String type = bottomTypes[rand.nextInt(bottomTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 25 + rand.nextInt(100); // $25-$124
            boolean inStock = rand.nextDouble() > 0.05; // 95% in stock
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.BOTTOM, price, style, color, inStock));
        }

        // Generate Shoes (80 items)
        for (int i = 1; i <= 80; i++) {
            String id = "S" + i;
            String type = shoeTypes[rand.nextInt(shoeTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 40 + rand.nextInt(140); // $40-$179
            boolean inStock = rand.nextDouble() > 0.08; // 92% in stock
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.SHOES, price, style, color, inStock));
        }

        // Generate Outerwear (40 items)
        for (int i = 1; i <= 40; i++) {
            String id = "O" + i;
            String type = outerwearTypes[rand.nextInt(outerwearTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 60 + rand.nextInt(140); // $60-$199
            boolean inStock = rand.nextDouble() > 0.10; // 90% in stock
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.OUTERWEAR, price, style, color, inStock));
        }

        // Generate Accessories (30 items)
        for (int i = 1; i <= 30; i++) {
            String id = "A" + i;
            String type = accessoryTypes[rand.nextInt(accessoryTypes.length)];
            String color = colors[rand.nextInt(colors.length)];
            String style = styles[rand.nextInt(styles.length)];
            double price = 10 + rand.nextInt(140); // $10-$149
            boolean inStock = rand.nextDouble() > 0.05; // 95% in stock
            String name = color.substring(0, 1).toUpperCase() + color.substring(1) + " " + type;
            items.add(new ClothingItem(id, name, ClothingCategory.ACCESSORY, price, style, color, inStock));
        }

        return items;
    }
}
