package com.shoppingoptimiser.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.shoppingoptimiser.domain.Budget;
import com.shoppingoptimiser.domain.ClothingCategory;
import com.shoppingoptimiser.domain.ClothingItem;
import com.shoppingoptimiser.domain.WardrobeSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for WardrobeConstraintProvider - testing each constraint in isolation.
 */
public class WardrobeConstraintProviderTest {

    private ConstraintVerifier<WardrobeConstraintProvider, WardrobeSolution> constraintVerifier;

    @BeforeEach
    public void setup() {
        constraintVerifier = ConstraintVerifier.build(
                new WardrobeConstraintProvider(),
                WardrobeSolution.class,
                ClothingItem.class);
    }

    // ==================== HARD CONSTRAINT TESTS ====================

    @Test
    public void budgetConstraint_withinBudget_noPenalty() {
        // Items totaling $100 (well within $500 budget)
        Budget budget = new Budget(500.0);
        ClothingItem item1 = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        item1.setSelected(true);

        ClothingItem item2 = createItem("2", "Pants", ClothingCategory.BOTTOM, 50.0, true, false);
        item2.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::budgetConstraint)
                .given(item1, item2, budget)
                .penalizesBy(0);
    }

    @Test
    public void budgetConstraint_overBudget_penalized() {
        // Items totaling $600 (over $500 budget by $100)
        Budget budget = new Budget(500.0);
        ClothingItem item1 = createItem("1", "Expensive Shirt", ClothingCategory.TOP, 300.0, true, false);
        item1.setSelected(true);

        ClothingItem item2 = createItem("2", "Expensive Pants", ClothingCategory.BOTTOM, 300.0, true, false);
        item2.setSelected(true);

        // Penalty should be (600 - 500) * 100 = 10000
        constraintVerifier.verifyThat(WardrobeConstraintProvider::budgetConstraint)
                .given(item1, item2, budget)
                .penalizesBy(10000);
    }

    @Test
    public void budgetConstraint_exactlyAtBudget_noPenalty() {
        // Items totaling exactly $500
        Budget budget = new Budget(500.0);
        ClothingItem item1 = createItem("1", "Shirt", ClothingCategory.TOP, 250.0, true, false);
        item1.setSelected(true);

        ClothingItem item2 = createItem("2", "Pants", ClothingCategory.BOTTOM, 250.0, true, false);
        item2.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::budgetConstraint)
                .given(item1, item2, budget)
                .penalizesBy(0);
    }

    @Test
    public void budgetConstraint_unselectedItemsNotCounted() {
        // Selected items total $100, unselected would push over budget
        Budget budget = new Budget(500.0);
        ClothingItem selectedItem = createItem("1", "Shirt", ClothingCategory.TOP, 100.0, true, false);
        selectedItem.setSelected(true);

        ClothingItem unselectedItem = createItem("2", "Expensive Pants", ClothingCategory.BOTTOM, 500.0, true, false);
        unselectedItem.setSelected(false);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::budgetConstraint)
                .given(selectedItem, unselectedItem, budget)
                .penalizesBy(0);
    }

    @Test
    public void mustHaveAtLeastOneTop_topSelected_noPenalty() {
        ClothingItem top = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        top.setSelected(true);

        ClothingItem bottom = createItem("2", "Pants", ClothingCategory.BOTTOM, 50.0, true, false);
        bottom.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustHaveAtLeastOneTop)
                .given(top, bottom)
                .penalizesBy(0);
    }

    @Test
    public void mustHaveAtLeastOneTop_noTopSelected_penalized() {
        ClothingItem top = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        top.setSelected(false);

        ClothingItem bottom = createItem("2", "Pants", ClothingCategory.BOTTOM, 50.0, true, false);
        bottom.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustHaveAtLeastOneTop)
                .given(top, bottom)
                .penalizesBy(1000);
    }

    @Test
    public void mustHaveAtLeastOneBottom_bottomSelected_noPenalty() {
        ClothingItem top = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        top.setSelected(true);

        ClothingItem bottom = createItem("2", "Pants", ClothingCategory.BOTTOM, 50.0, true, false);
        bottom.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustHaveAtLeastOneBottom)
                .given(top, bottom)
                .penalizesBy(0);
    }

    @Test
    public void mustHaveAtLeastOneBottom_noBottomSelected_penalized() {
        ClothingItem top = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        top.setSelected(true);

        ClothingItem bottom = createItem("2", "Pants", ClothingCategory.BOTTOM, 50.0, true, false);
        bottom.setSelected(false);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustHaveAtLeastOneBottom)
                .given(top, bottom)
                .penalizesBy(1000);
    }

    @Test
    public void mustHaveAtLeastOneShoes_shoesSelected_noPenalty() {
        ClothingItem top = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        top.setSelected(true);

        ClothingItem shoes = createItem("2", "Sneakers", ClothingCategory.SHOES, 80.0, true, false);
        shoes.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustHaveAtLeastOneShoes)
                .given(top, shoes)
                .penalizesBy(0);
    }

    @Test
    public void mustHaveAtLeastOneShoes_noShoesSelected_penalized() {
        ClothingItem top = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        top.setSelected(true);

        ClothingItem shoes = createItem("2", "Sneakers", ClothingCategory.SHOES, 80.0, true, false);
        shoes.setSelected(false);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustHaveAtLeastOneShoes)
                .given(top, shoes)
                .penalizesBy(1000);
    }

    @Test
    public void mustHaveAtLeastOneShoes_multipleAccessoriesButNoShoesSelected_penalized() {
        ClothingItem accessory1 = createItem("1", "Belt", ClothingCategory.ACCESSORY, 30.0, true, false);
        accessory1.setSelected(true);

        ClothingItem accessory2 = createItem("2", "Hat", ClothingCategory.ACCESSORY, 25.0, true, false);
        accessory2.setSelected(true);

        ClothingItem shoes = createItem("3", "Sneakers", ClothingCategory.SHOES, 80.0, true, false);
        shoes.setSelected(false);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustHaveAtLeastOneShoes)
                .given(accessory1, accessory2, shoes)
                .penalizesBy(1000);
    }

    @Test
    public void mustBeInStockConstraint_inStockItemSelected_noPenalty() {
        ClothingItem inStockItem = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        inStockItem.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustBeInStockConstraint)
                .given(inStockItem)
                .penalizesBy(0);
    }

    @Test
    public void mustBeInStockConstraint_outOfStockItemSelected_penalized() {
        ClothingItem outOfStockItem = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, false, false);
        outOfStockItem.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustBeInStockConstraint)
                .given(outOfStockItem)
                .penalizesBy(100);
    }

    @Test
    public void mustBeInStockConstraint_outOfStockItemNotSelected_noPenalty() {
        ClothingItem outOfStockItem = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, false, false);
        outOfStockItem.setSelected(false);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::mustBeInStockConstraint)
                .given(outOfStockItem)
                .penalizesBy(0);
    }

    @Test
    public void topAndBottomDifferentColors_differentColors_noPenalty() {
        ClothingItem blackTop = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackTop.setSelected(true);

        ClothingItem whiteBottom = createItem("2", "White Pants", ClothingCategory.BOTTOM, 50.0, "white", true, false);
        whiteBottom.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::topAndBottomDifferentColors)
                .given(blackTop, whiteBottom)
                .penalizesBy(0);
    }

    @Test
    public void topAndBottomDifferentColors_sameColor_penalized() {
        ClothingItem blackTop = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackTop.setSelected(true);

        ClothingItem blackBottom = createItem("2", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::topAndBottomDifferentColors)
                .given(blackTop, blackBottom)
                .penalizesBy(1000);
    }

    @Test
    public void topAndBottomDifferentColors_sameColorCaseInsensitive_penalized() {
        ClothingItem blackTop = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "BLACK", true, false);
        blackTop.setSelected(true);

        ClothingItem blackBottom = createItem("2", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::topAndBottomDifferentColors)
                .given(blackTop, blackBottom)
                .penalizesBy(1000);
    }

    @Test
    public void topAndBottomDifferentColors_multipleTopsAndBottomsSameColor_penalizedMultipleTimes() {
        ClothingItem blackTop1 = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackTop1.setSelected(true);

        ClothingItem blackTop2 = createItem("2", "Black Polo", ClothingCategory.TOP, 50.0, "black", true, false);
        blackTop2.setSelected(true);

        ClothingItem blackBottom1 = createItem("3", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom1.setSelected(true);

        ClothingItem blackBottom2 = createItem("4", "Black Jeans", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom2.setSelected(true);

        // Each top paired with each bottom: 2 tops * 2 bottoms * 1000 = 4000
        constraintVerifier.verifyThat(WardrobeConstraintProvider::topAndBottomDifferentColors)
                .given(blackTop1, blackTop2, blackBottom1, blackBottom2)
                .penalizesBy(4000);
    }

    @Test
    public void topAndBottomDifferentColors_onlyTopSelected_noPenalty() {
        ClothingItem blackTop = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackTop.setSelected(true);

        ClothingItem blackBottom = createItem("2", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(false);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::topAndBottomDifferentColors)
                .given(blackTop, blackBottom)
                .penalizesBy(0);
    }

    @Test
    public void topAndBottomDifferentColors_onlyBottomSelected_noPenalty() {
        ClothingItem blackTop = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackTop.setSelected(false);

        ClothingItem blackBottom = createItem("2", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::topAndBottomDifferentColors)
                .given(blackTop, blackBottom)
                .penalizesBy(0);
    }

    @Test
    public void topAndBottomDifferentColors_mixedColorCombinations_partiallyPenalized() {
        ClothingItem blackTop = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackTop.setSelected(true);

        ClothingItem whiteTop = createItem("2", "White Shirt", ClothingCategory.TOP, 50.0, "white", true, false);
        whiteTop.setSelected(true);

        ClothingItem blackBottom = createItem("3", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        ClothingItem navyBottom = createItem("4", "Navy Pants", ClothingCategory.BOTTOM, 50.0, "navy", true, false);
        navyBottom.setSelected(true);

        // Only black top + black bottom = 1 * 1000 = 1000
        constraintVerifier.verifyThat(WardrobeConstraintProvider::topAndBottomDifferentColors)
                .given(blackTop, whiteTop, blackBottom, navyBottom)
                .penalizesBy(1000);
    }

    @Test
    public void bottomAndShoesDifferentColors_differentColors_noPenalty() {
        ClothingItem blackBottom = createItem("1", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        ClothingItem brownShoes = createItem("2", "Brown Sneakers", ClothingCategory.SHOES, 80.0, "brown", true, false);
        brownShoes.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::bottomAndShoesDifferentColors)
                .given(blackBottom, brownShoes)
                .penalizesBy(0);
    }

    @Test
    public void bottomAndShoesDifferentColors_sameColor_penalized() {
        ClothingItem blackBottom = createItem("1", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        ClothingItem blackShoes = createItem("2", "Black Sneakers", ClothingCategory.SHOES, 80.0, "black", true, false);
        blackShoes.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::bottomAndShoesDifferentColors)
                .given(blackBottom, blackShoes)
                .penalizesBy(1000);
    }

    @Test
    public void bottomAndShoesDifferentColors_sameColorCaseInsensitive_penalized() {
        ClothingItem blackBottom = createItem("1", "Black Pants", ClothingCategory.BOTTOM, 50.0, "BLACK", true, false);
        blackBottom.setSelected(true);

        ClothingItem blackShoes = createItem("2", "Black Sneakers", ClothingCategory.SHOES, 80.0, "black", true, false);
        blackShoes.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::bottomAndShoesDifferentColors)
                .given(blackBottom, blackShoes)
                .penalizesBy(1000);
    }

    @Test
    public void bottomAndShoesDifferentColors_multipleBottomsAndShoesSameColor_penalizedMultipleTimes() {
        ClothingItem blackBottom1 = createItem("1", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom1.setSelected(true);

        ClothingItem blackBottom2 = createItem("2", "Black Jeans", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom2.setSelected(true);

        ClothingItem blackShoes1 = createItem("3", "Black Sneakers", ClothingCategory.SHOES, 80.0, "black", true, false);
        blackShoes1.setSelected(true);

        ClothingItem blackShoes2 = createItem("4", "Black Boots", ClothingCategory.SHOES, 90.0, "black", true, false);
        blackShoes2.setSelected(true);

        // Each bottom paired with each shoe: 2 bottoms * 2 shoes * 1000 = 4000
        constraintVerifier.verifyThat(WardrobeConstraintProvider::bottomAndShoesDifferentColors)
                .given(blackBottom1, blackBottom2, blackShoes1, blackShoes2)
                .penalizesBy(4000);
    }

    @Test
    public void bottomAndShoesDifferentColors_onlyBottomSelected_noPenalty() {
        ClothingItem blackBottom = createItem("1", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        ClothingItem blackShoes = createItem("2", "Black Sneakers", ClothingCategory.SHOES, 80.0, "black", true, false);
        blackShoes.setSelected(false);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::bottomAndShoesDifferentColors)
                .given(blackBottom, blackShoes)
                .penalizesBy(0);
    }

    @Test
    public void bottomAndShoesDifferentColors_onlyShoesSelected_noPenalty() {
        ClothingItem blackBottom = createItem("1", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(false);

        ClothingItem blackShoes = createItem("2", "Black Sneakers", ClothingCategory.SHOES, 80.0, "black", true, false);
        blackShoes.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::bottomAndShoesDifferentColors)
                .given(blackBottom, blackShoes)
                .penalizesBy(0);
    }

    @Test
    public void bottomAndShoesDifferentColors_mixedColorCombinations_partiallyPenalized() {
        ClothingItem blackBottom = createItem("1", "Black Pants", ClothingCategory.BOTTOM, 50.0, "black", true, false);
        blackBottom.setSelected(true);

        ClothingItem navyBottom = createItem("2", "Navy Pants", ClothingCategory.BOTTOM, 50.0, "navy", true, false);
        navyBottom.setSelected(true);

        ClothingItem blackShoes = createItem("3", "Black Sneakers", ClothingCategory.SHOES, 80.0, "black", true, false);
        blackShoes.setSelected(true);

        ClothingItem brownShoes = createItem("4", "Brown Boots", ClothingCategory.SHOES, 90.0, "brown", true, false);
        brownShoes.setSelected(true);

        // Only black bottom + black shoes = 1 * 1000 = 1000
        constraintVerifier.verifyThat(WardrobeConstraintProvider::bottomAndShoesDifferentColors)
                .given(blackBottom, navyBottom, blackShoes, brownShoes)
                .penalizesBy(1000);
    }

    // ==================== SOFT CONSTRAINT TESTS ====================

    @Test
    public void maximizeOutfitCombinations_multipleTopAndBottoms_rewarded() {
        // 2 tops and 2 bottoms selected
        ClothingItem top1 = createItem("1", "Shirt 1", ClothingCategory.TOP, 50.0, true, false);
        top1.setSelected(true);

        ClothingItem top2 = createItem("2", "Shirt 2", ClothingCategory.TOP, 50.0, true, false);
        top2.setSelected(true);

        ClothingItem bottom1 = createItem("3", "Pants 1", ClothingCategory.BOTTOM, 50.0, true, false);
        bottom1.setSelected(true);

        ClothingItem bottom2 = createItem("4", "Pants 2", ClothingCategory.BOTTOM, 50.0, true, false);
        bottom2.setSelected(true);

        // Each category gets count * 10 reward: 2*10 + 2*10 = 40
        constraintVerifier.verifyThat(WardrobeConstraintProvider::maximizeOutfitCombinations)
                .given(top1, top2, bottom1, bottom2)
                .rewardsWith(40);
    }

    @Test
    public void maximizeOutfitCombinations_onlyTops_lessReward() {
        ClothingItem top1 = createItem("1", "Shirt 1", ClothingCategory.TOP, 50.0, true, false);
        top1.setSelected(true);

        ClothingItem top2 = createItem("2", "Shirt 2", ClothingCategory.TOP, 50.0, true, false);
        top2.setSelected(true);

        // Only tops: 2*10 = 20
        constraintVerifier.verifyThat(WardrobeConstraintProvider::maximizeOutfitCombinations)
                .given(top1, top2)
                .rewardsWith(20);
    }

    @Test
    public void maximizeOutfitCombinations_noTopOrBottoms_noReward() {
        ClothingItem shoes = createItem("1", "Sneakers", ClothingCategory.SHOES, 80.0, true, false);
        shoes.setSelected(true);

        ClothingItem accessory = createItem("2", "Belt", ClothingCategory.ACCESSORY, 30.0, true, false);
        accessory.setSelected(true);

        constraintVerifier.verifyThat(WardrobeConstraintProvider::maximizeOutfitCombinations)
                .given(shoes, accessory)
                .rewardsWith(0);
    }

    @Test
    public void maximizeOutfitCombinations_unselectedItemsNotCounted() {
        ClothingItem selectedTop = createItem("1", "Shirt", ClothingCategory.TOP, 50.0, true, false);
        selectedTop.setSelected(true);

        ClothingItem unselectedBottom = createItem("2", "Pants", ClothingCategory.BOTTOM, 50.0, true, false);
        unselectedBottom.setSelected(false);

        // Only 1 top selected: 1*10 = 10
        constraintVerifier.verifyThat(WardrobeConstraintProvider::maximizeOutfitCombinations)
                .given(selectedTop, unselectedBottom)
                .rewardsWith(10);
    }

    @Test
    public void coordinatedColors_neutralColors_highReward() {
        ClothingItem blackShirt = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackShirt.setSelected(true);

        ClothingItem whiteShirt = createItem("2", "White Shirt", ClothingCategory.TOP, 50.0, "white", true, false);
        whiteShirt.setSelected(true);

        ClothingItem navyPants = createItem("3", "Navy Pants", ClothingCategory.BOTTOM, 50.0, "navy", true, false);
        navyPants.setSelected(true);

        // 3 neutral colors: 3 * 5 = 15
        constraintVerifier.verifyThat(WardrobeConstraintProvider::coordinatedColors)
                .given(blackShirt, whiteShirt, navyPants)
                .rewardsWith(15);
    }

    @Test
    public void coordinatedColors_nonNeutralColors_lowerReward() {
        ClothingItem redShirt = createItem("1", "Red Shirt", ClothingCategory.TOP, 50.0, "red", true, false);
        redShirt.setSelected(true);

        ClothingItem greenPants = createItem("2", "Green Pants", ClothingCategory.BOTTOM, 50.0, "green", true, false);
        greenPants.setSelected(true);

        // 2 non-neutral colors: 2 * 1 = 2
        constraintVerifier.verifyThat(WardrobeConstraintProvider::coordinatedColors)
                .given(redShirt, greenPants)
                .rewardsWith(2);
    }

    @Test
    public void coordinatedColors_mixedColors_mixedReward() {
        ClothingItem blackShirt = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        blackShirt.setSelected(true);

        ClothingItem redPants = createItem("2", "Red Pants", ClothingCategory.BOTTOM, 50.0, "red", true, false);
        redPants.setSelected(true);

        // 1 neutral (5) + 1 non-neutral (1) = 6
        constraintVerifier.verifyThat(WardrobeConstraintProvider::coordinatedColors)
                .given(blackShirt, redPants)
                .rewardsWith(6);
    }

    @Test
    public void coordinatedColors_unselectedItemsNotCounted() {
        ClothingItem selectedBlack = createItem("1", "Black Shirt", ClothingCategory.TOP, 50.0, "black", true, false);
        selectedBlack.setSelected(true);

        ClothingItem unselectedWhite = createItem("2", "White Shirt", ClothingCategory.TOP, 50.0, "white", true, false);
        unselectedWhite.setSelected(false);

        // Only 1 neutral color selected: 1 * 5 = 5
        constraintVerifier.verifyThat(WardrobeConstraintProvider::coordinatedColors)
                .given(selectedBlack, unselectedWhite)
                .rewardsWith(5);
    }

    // ==================== HELPER METHODS ====================

    private ClothingItem createItem(String id, String name, ClothingCategory category,
                                   double price, boolean inStock, boolean essential) {
        return createItem(id, name, category, price, "casual", "blue", inStock);
    }

    private ClothingItem createItem(String id, String name, ClothingCategory category,
                                   double price, String color, boolean inStock, boolean essential) {
        return createItem(id, name, category, price, "casual", color, inStock);
    }

    private ClothingItem createItem(String id, String name, ClothingCategory category,
                                   double price, String style, String color,
                                   boolean inStock) {
        ClothingItem item = new ClothingItem();
        item.setId(id);
        item.setName(name);
        item.setCategory(category);
        item.setPrice(price);
        item.setStyle(style);
        item.setColor(color);
        item.setInStock(inStock);
        return item;
    }
}
