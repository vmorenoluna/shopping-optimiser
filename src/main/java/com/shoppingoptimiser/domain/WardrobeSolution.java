package com.shoppingoptimiser.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the complete wardrobe optimization problem and solution.
 */
@PlanningSolution
public class WardrobeSolution {

    @PlanningEntityCollectionProperty
    private List<ClothingItem> items;

    @ProblemFactProperty
    private Budget budget;
    private List<String> preferredStyles; // User's style preferences

    @PlanningScore
    private HardSoftScore score;

    // No-arg constructor required by Timefold
    public WardrobeSolution() {
    }

    public WardrobeSolution(List<ClothingItem> items, double budgetAmount, List<String> preferredStyles) {
        this.items = items;
        this.budget = new Budget(budgetAmount);
        this.preferredStyles = preferredStyles != null ? preferredStyles : new ArrayList<>();
    }

    @ValueRangeProvider(id = "selectedRange")
    public List<Boolean> getSelectedRange() {
        return List.of(Boolean.TRUE, Boolean.FALSE);
    }

    public List<ClothingItem> getSelectedItems() {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                .filter(item -> item.getSelected() != null && item.getSelected())
                .collect(Collectors.toList());
    }

    public double getTotalCost() {
        return getSelectedItems().stream()
                .mapToDouble(ClothingItem::getPrice)
                .sum();
    }

    // Getters and setters
    public List<ClothingItem> getItems() {
        return items;
    }

    public void setItems(List<ClothingItem> items) {
        this.items = items;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public List<String> getPreferredStyles() {
        return preferredStyles;
    }

    public void setPreferredStyles(List<String> preferredStyles) {
        this.preferredStyles = preferredStyles;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    @Override
    public String toString() {
        List<ClothingItem> selected = getSelectedItems();
        StringBuilder sb = new StringBuilder();
        sb.append("\nWardrobeSolution:\n");
        sb.append("  Budget: $").append(budget != null ? String.format("%.2f", budget.getAmount()) : "0.00").append("\n");
        sb.append("  Total Cost: $").append(String.format("%.2f", getTotalCost())).append("\n");
        sb.append("  Items Selected: ").append(selected.size()).append("\n");
        sb.append("  Score: ").append(score).append("\n");
        sb.append("  Selected Items:\n");
        for (ClothingItem item : selected) {
            sb.append("    - ID: ").append(item.getId())
              .append(", Name: ").append(item.getName())
              .append(", Category: ").append(item.getCategory())
              .append(", Price: $").append(String.format("%.2f", item.getPrice()))
              .append(", Style: ").append(item.getStyle())
              .append(", Color: ").append(item.getColor())
              .append(", InStock: ").append(item.isInStock())
              .append("\n");
        }
        return sb.toString();
    }
}
