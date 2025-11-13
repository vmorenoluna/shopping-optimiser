package com.shoppingoptimiser.domain;

/**
 * Represents the budget constraint as a problem fact.
 */
public class Budget {
    private double amount;

    public Budget() {
    }

    public Budget(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
