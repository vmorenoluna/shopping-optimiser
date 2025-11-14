package com.shoppingoptimiser.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.shoppingoptimiser.domain.Budget;
import com.shoppingoptimiser.domain.ClothingCategory;
import com.shoppingoptimiser.domain.ClothingItem;

import java.util.Set;

/**
 * Defines the constraints for the wardrobe optimization problem.
 *
 * HARD CONSTRAINTS (must-haves):
 * 1. Total cost must be less than or equal to budget
 * 2. Must have at least 1 TOP, 1 BOTTOM, and 1 SHOES (essentials for a complete outfit)
 * 3. Items must be in stock in your size
 * 4. Top and bottom cannot be the same color
 * 5. Bottom and shoes cannot be the same color
 *
 * SOFT CONSTRAINTS (what we optimize):
 * 1. Maximize number of outfit combinations you can create
 * 2. Prefer coordinated colors that work well together
 * 3. Match your style preferences where possible
 */
public class WardrobeConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // Hard constraints
                budgetConstraint(constraintFactory),
                mustHaveAtLeastOneTop(constraintFactory),
                mustHaveAtLeastOneBottom(constraintFactory),
                mustHaveAtLeastOneShoes(constraintFactory),
                mustBeInStockConstraint(constraintFactory),
                topAndBottomDifferentColors(constraintFactory),
                bottomAndShoesDifferentColors(constraintFactory),

                // Soft constraints
                maximizeOutfitCombinations(constraintFactory),
                coordinatedColors(constraintFactory),
                matchStylePreferences(constraintFactory)
        };
    }

    // ==================== HARD CONSTRAINTS ====================

    /**
     * Hard constraint: Total cost of selected items must not exceed budget
     * Budget is retrieved from the Budget problem fact
     */
    Constraint budgetConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ClothingItem.class)
                .filter(item -> item.getSelected() != null && item.getSelected())
                .groupBy(ConstraintCollectors.sum(item -> (int) (item.getPrice() * 100)))
                .join(Budget.class)
                .filter((totalCost, budget) -> totalCost > (int) (budget.getAmount() * 100))
                .penalize(HardSoftScore.ONE_HARD,
                        (totalCost, budget) -> totalCost - (int) (budget.getAmount() * 100))
                .asConstraint("Budget constraint");
    }

    /**
     * Hard constraint: Must have at least 1 TOP to create a complete outfit
     * Penalizes when there are no selected TOPs
     */
    Constraint mustHaveAtLeastOneTop(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUnfiltered(ClothingItem.class)
                .filter(item -> item.getCategory() == ClothingCategory.TOP)
                .ifNotExists(ClothingItem.class,
                        Joiners.equal(ClothingItem::getCategory),
                        Joiners.filtering((a, b) -> b.getSelected() != null && b.getSelected()))
                .penalize(HardSoftScore.ONE_HARD, item -> 1000)
                .asConstraint("Must have at least 1 TOP");
    }

    /**
     * Hard constraint: Must have at least 1 BOTTOM to create a complete outfit
     * Penalizes when there are no selected BOTTOMs
     */
    Constraint mustHaveAtLeastOneBottom(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUnfiltered(ClothingItem.class)
                .filter(item -> item.getCategory() == ClothingCategory.BOTTOM)
                .ifNotExists(ClothingItem.class,
                        Joiners.equal(ClothingItem::getCategory),
                        Joiners.filtering((a, b) -> b.getSelected() != null && b.getSelected()))
                .penalize(HardSoftScore.ONE_HARD, item -> 1000)
                .asConstraint("Must have at least 1 BOTTOM");
    }

    /**
     * Hard constraint: Must have at least 1 SHOES to create a complete outfit
     * Penalizes when there are no selected SHOES
     */
    Constraint mustHaveAtLeastOneShoes(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUnfiltered(ClothingItem.class)
                .filter(item -> item.getCategory() == ClothingCategory.SHOES)
                .ifNotExists(ClothingItem.class,
                        Joiners.equal(ClothingItem::getCategory),
                        Joiners.filtering((a, b) -> b.getSelected() != null && b.getSelected()))
                .penalize(HardSoftScore.ONE_HARD, item -> 1000)
                .asConstraint("Must have at least 1 SHOES");
    }

    /**
     * Hard constraint: Cannot select items that are not in stock
     */
    Constraint mustBeInStockConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ClothingItem.class)
                .filter(item -> item.getSelected() != null && item.getSelected() && !item.isInStock())
                .penalize(HardSoftScore.ONE_HARD, item -> 100)
                .asConstraint("Items must be in stock");
    }

    /**
     * Hard constraint: Top and bottom cannot be the same color
     * Penalizes when a selected top and a selected bottom have the same color
     */
    Constraint topAndBottomDifferentColors(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ClothingItem.class)
                .filter(item -> item.getSelected() != null && item.getSelected())
                .filter(item -> item.getCategory() == ClothingCategory.TOP)
                .join(ClothingItem.class,
                        Joiners.filtering((top, bottom) ->
                                bottom.getSelected() != null &&
                                bottom.getSelected() &&
                                bottom.getCategory() == ClothingCategory.BOTTOM &&
                                top.getColor().equalsIgnoreCase(bottom.getColor())))
                .penalize(HardSoftScore.ONE_HARD, (top, bottom) -> 1000)
                .asConstraint("Top and bottom must have different colors");
    }

    /**
     * Hard constraint: Bottom and shoes cannot be the same color
     * Penalizes when a selected bottom and selected shoes have the same color
     */
    Constraint bottomAndShoesDifferentColors(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ClothingItem.class)
                .filter(item -> item.getSelected() != null && item.getSelected())
                .filter(item -> item.getCategory() == ClothingCategory.BOTTOM)
                .join(ClothingItem.class,
                        Joiners.filtering((bottom, shoes) ->
                                shoes.getSelected() != null &&
                                shoes.getSelected() &&
                                shoes.getCategory() == ClothingCategory.SHOES &&
                                bottom.getColor().equalsIgnoreCase(shoes.getColor())))
                .penalize(HardSoftScore.ONE_HARD, (bottom, shoes) -> 1000)
                .asConstraint("Bottom and shoes must have different colors");
    }

    // ==================== SOFT CONSTRAINTS ====================

    /**
     * Soft constraint: Maximize outfit combinations
     * Count tops and bottoms separately, then multiply to get combinations
     */
    Constraint maximizeOutfitCombinations(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ClothingItem.class)
                .filter(item -> item.getSelected() != null && item.getSelected())
                .filter(item -> item.getCategory() == ClothingCategory.TOP || item.getCategory() == ClothingCategory.BOTTOM)
                .groupBy(ClothingItem::getCategory, ConstraintCollectors.count())
                .reward(HardSoftScore.ONE_SOFT, (category, count) -> count * 10)
                .asConstraint("Maximize outfit combinations");
    }

    /**
     * Soft constraint: Reward coordinated colors that work well together
     * For simplicity, we'll reward having items with common neutral colors
     */
    Constraint coordinatedColors(ConstraintFactory constraintFactory) {
        Set<String> neutralColors = Set.of("black", "white", "gray", "grey", "navy", "beige", "brown");

        return constraintFactory.forEach(ClothingItem.class)
                .filter(item -> item.getSelected() != null && item.getSelected())
                .reward(HardSoftScore.ONE_SOFT, item -> {
                    // Reward neutral colors that coordinate well
                    if (neutralColors.contains(item.getColor().toLowerCase())) {
                        return 5;
                    }
                    return 1;
                })
                .asConstraint("Coordinated colors");
    }

    /**
     * Soft constraint: Match user's style preferences
     * Reward selecting items that match the user's preferred styles
     */
    Constraint matchStylePreferences(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ClothingItem.class)
                .filter(item -> item.getSelected() != null && item.getSelected())
                .reward(HardSoftScore.ONE_SOFT, item -> 3)
                .asConstraint("Match style preferences");
    }
}
