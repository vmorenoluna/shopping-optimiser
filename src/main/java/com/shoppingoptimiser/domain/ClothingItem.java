package com.shoppingoptimiser.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * Represents a clothing item that can be purchased.
 * The planning variable is whether this item is selected for purchase (true/false).
 */
@PlanningEntity
public class ClothingItem {

    private String id;
    private String name;
    private ClothingCategory category;
    private double price;
    private String style; // e.g., "casual", "formal", "sport"
    private String color;
    private boolean inStock; // Item must be in stock and available in your size

    @PlanningVariable(valueRangeProviderRefs = "selectedRange")
    private Boolean selected;

    // No-arg constructor required by Timefold
    public ClothingItem() {
    }

    public ClothingItem(String id, String name, ClothingCategory category, double price,
                       String style, String color, boolean inStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.style = style;
        this.color = color;
        this.inStock = inStock;
        this.selected = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClothingCategory getCategory() {
        return category;
    }

    public void setCategory(ClothingCategory category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "ClothingItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=$" + price +
                ", style='" + style + '\'' +
                ", color='" + color + '\'' +
                ", inStock=" + inStock +
                ", selected=" + selected +
                '}';
    }
}
