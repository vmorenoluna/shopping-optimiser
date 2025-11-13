# Shopping Optimiser

A Java demonstration project showcasing **Timefold Solver** capabilities for solving optimization problems. This project tackles the real-world problem of optimizing clothing purchases to maximize wardrobe versatility while staying within budget.

## The Problem

Imagine you want to optimize your clothing purchases. The challenge is to maximize outfit combinations while staying within budget.

**Your problem variables include:**
- Available items with their prices and categories
- Style preferences
- Stock availability
- Budget constraints

**Your goals:**
1. **Maximize versatility** - Get the most outfit combinations possible
2. **Stay within budget** - That's non-negotiable
3. **Ensure essentials covered** - You need at least 1 top, 1 bottom, and 1 pair of shoes

## Constraints

### Hard Constraints (Must-Haves)
These constraints must be satisfied for any valid solution:

- **Budget Constraint**: Total cost must be ≤ your budget (configurable as a problem fact)
- **Essential Categories**: Must have at least 1 TOP, 1 BOTTOM, and 1 SHOES to create a complete outfit
- **Stock Availability**: Items must actually be in stock in your size

### Soft Constraints (Optimization Goals)
These are what we optimize to find the best solution:

- **Maximize Outfit Combinations**: Maximize the number of different outfits you can create
- **Coordinated Colors**: Prefer colors that work well together (neutral colors like black, white, navy, gray get higher scores)
- **Style Preferences**: Match your personal style preferences where possible

## Project Structure

```
shopping-optimised/
├── build.sbt                              # SBT build configuration (Java + JavaFX with Timefold 1.14.0)
├── run-ui.bat                             # Windows batch script to run UI version
├── project/
│   ├── build.properties                   # SBT version
│   └── plugins.sbt                        # JUnit 5 integration plugin
└── src/
    ├── main/
    │   ├── java/com/shoppingoptimiser/
    │   │   ├── domain/
    │   │   │   ├── Budget.java            # Problem fact for budget constraint
    │   │   │   ├── ClothingCategory.java  # Enum for clothing categories
    │   │   │   ├── ClothingItem.java      # Planning entity (items to select)
    │   │   │   └── WardrobeSolution.java  # Planning solution
    │   │   ├── solver/
    │   │   │   └── WardrobeConstraintProvider.java  # Constraint definitions
    │   │   ├── WardrobeOptimizerApp.java  # Main console application
    │   │   └── WardrobeOptimizerUI.java   # JavaFX GUI application
    │   └── resources/
    │       └── logback.xml                 # Logging configuration
    └── test/
        └── java/com/shoppingoptimiser/solver/
            └── WardrobeConstraintProviderTest.java  # JUnit 5 tests (24 tests)
```

## Key Timefold Concepts Demonstrated

### 1. Planning Entity (`ClothingItem`)
The `@PlanningEntity` annotation marks the class whose instances will be modified by the solver. Each `ClothingItem` has:
- A `@PlanningVariable` (`selected: Boolean`) that the solver changes to find the optimal solution
- Problem properties (price, category, style, color, stock status)

### 2. Problem Facts (`Budget`)
Problem facts are immutable input data used in constraints:
- The `Budget` class is annotated with `@ProblemFactProperty` in the solution
- Can be referenced in constraint streams using `.join(Budget.class)`
- Allows dynamic constraint values without code changes

### 3. Planning Solution (`WardrobeSolution`)
The `@PlanningSolution` annotation marks the class representing both the problem and solution:
- Contains the collection of planning entities (`items`)
- Holds problem facts (budget, style preferences)
- Provides value range for planning variables (`true/false` for selection)
- Has a `@PlanningScore` field for the solution quality

### 4. Constraint Provider (`WardrobeConstraintProvider`)
Implements `ConstraintProvider` to define the rules using Constraint Streams API:
- Hard constraints (penalize invalid solutions) - must have score 0hard for valid solution
- Soft constraints (reward/penalize to find optimal solutions)
- Uses `forEach`, `filter`, `groupBy`, `join`, `ifNotExists` patterns

## Prerequisites

- Java 11 or higher
- SBT 1.9.x

## Running the Application

The project includes two runnable examples:

### 1. Console Application (Text-based)

Navigate to the project directory and run:
```bash
cd shopping-optimised
sbt run
```

The console application will:
- Generate 500 clothing items with variety in styles, colors, and categories
- Set a budget of $120 (configurable)
- Display inventory statistics with histograms
- Run the Timefold solver for up to 60 seconds or until optimal solution found
- Display the optimal selection of items
- Show the number of outfit combinations possible

### 2. Graphical UI Application (JavaFX)

To run the graphical UI version with visual clothing representations:

**Windows:**
```bash
run-ui.bat
```

**Or manually:**
```bash
sbt "set fork := false" "runMain com.shoppingoptimiser.WardrobeOptimizerUI"
```

**Note:** The UI version requires `fork := false` to properly load JavaFX modules.

The UI application features:
- Visual representation of clothing items (tops, bottoms, shoes, outerwear, accessories)
- Color-coded items based on actual colors
- Real-time solver progress tracking
- Interactive "Solve" button to start optimization
- Split view showing inventory and selected solution
- Detailed solver log
- Visual indicators for selected items and out-of-stock items

### 3. Run Tests
```bash
sbt test
```

**Problem Scale**: 500 entities, 500 variables, ~3.27 × 10^150 possible combinations

## Expected Output

The solver will find a solution that:
- Stays within the $120 budget
- Includes at least 1 TOP, 1 BOTTOM, and 1 SHOES
- Only selects items in stock
- Maximizes the number of outfit combinations
- Prefers coordinated colors
- Considers style preferences

Example output:
```
=== Wardrobe Optimizer ===
Optimizing clothing purchases to maximize outfit combinations while staying within budget

Problem setup:
  Budget: $120.0
  Available items: 500
  Preferred styles: casual, smart-casual

=== Inventory Statistics ===
Items by Category:
  BOTTOM               150 █████████████████████████████████
  OUTERWEAR             40 ████████
  SHOES                 80 ████████████████
  TOP                  200 ██████████████████████████████████████████████
  ACCESSORY             30 ██████

Items by Style:
  casual                37 ███████
  formal                67 █████████████
  smart-casual          74 ██████████████
  sport                 69 █████████████
  streetwear            79 ███████████████
  vintage               62 ████████████
  beach                 56 ███████████
  outdoor               56 ███████████

[Additional statistics omitted for brevity]

Solving (max 60 seconds or until optimal solution found)...
17:36:12.695 [main] INFO  a.t.s.core.impl.solver.DefaultSolver - Problem scale: entity count (500), variable count (500), approximate value count (2), approximate problem scale (3.273391 × 10^150).
Solving completed in 3.86 seconds

=== Solution Found ===
WardrobeSolution:
  Budget: $120.00
  Total Cost: $108.00
  Items Selected: 4
  Score: 0hard/54soft
  Selected Items:
    - ID: T14, Name: Gray Hoodie, Category: TOP, Price: $14.00, Style: casual, Color: gray, InStock: true
    - ID: B65, Name: Beige Jeans, Category: BOTTOM, Price: $35.00, Style: casual, Color: beige, InStock: true
    - ID: S19, Name: Yellow Boots, Category: SHOES, Price: $59.00, Style: casual, Color: yellow, InStock: true

Outfit Combinations:
  Tops: 1
  Bottoms: 1
  Shoes: 1
  Basic combinations: 1
  With outerwear: 1
  With accessories: 1
```

## Customization

### Modify the Budget
Edit `WardrobeOptimizerApp.java`:
```java
double budget = 200.0; // Change to your desired budget
```
The budget is modeled as a `Budget` problem fact, making it easy to change without modifying constraints.

### Add More Items
Modify the `createSampleWardrobe()` method in `WardrobeOptimizerApp.java`:
- Currently generates 500 items programmatically (200 tops, 150 bottoms, 80 shoes, 40 outerwear, 30 accessories)
- Adjust the loop counts to change inventory size
- Add new styles, colors, or categories as needed

### Adjust Solver Time
Edit the termination config in `WardrobeOptimizerApp.java`:
```java
solverConfig.withTerminationConfig(new TerminationConfig()
    .withBestScoreLimit("0hard/*soft")  // Stop when optimal
    .withSpentLimit(Duration.ofSeconds(30)));  // OR 30 seconds
```

### Tune Constraints
Modify constraint weights in `WardrobeConstraintProvider.java`:
- Hard constraints use penalties (must be 0 for valid solution)
- Soft constraints use rewards/penalties to guide optimization
- Adjust multipliers to change relative importance

### Suppress Debug Logging
The `logback.xml` configuration sets Timefold to INFO level. To see more details, change to DEBUG:
```xml
<logger name="ai.timefold.solver" level="DEBUG"/>
```

## Testing

The project includes comprehensive JUnit 5 tests for all constraints:
- 24 tests covering all 6 constraints in isolation
- Uses `ConstraintVerifier` for fast, focused testing
- Tests both positive and negative cases
- Run with: `sbt test`

Example test:
```java
@Test
public void budgetConstraint_overBudget_penalized() {
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
```

## Learning Resources

- [Timefold Solver Documentation](https://docs.timefold.ai/)
- [Timefold Quickstarts](https://github.com/TimefoldAI/timefold-quickstarts)
- [Constraint Streams API](https://docs.timefold.ai/timefold-solver/latest/constraint-streams/constraint-streams)
- [Timefold Testing](https://docs.timefold.ai/timefold-solver/latest/testing/testing)

## Key Learnings from This Project

1. **Problem Facts vs Planning Variables**: Budget is a problem fact (immutable input), while item selection is a planning variable (what the solver changes)

2. **Essential Categories Pattern**: Instead of marking specific items as essential, we enforce having at least one item from essential categories (TOP, BOTTOM, SHOES) using `ifNotExists` pattern

3. **Constraint Streams Patterns**:
   - Use `groupBy` + `join` to access problem facts in constraints
   - Use `fromUnfiltered` + `ifNotExists` to penalize missing categories
   - Convert prices to cents (multiply by 100) to work with integers in constraints

4. **Testing**: Use `ConstraintVerifier` to test constraints in isolation, making debugging much easier

5. **Solver Termination**: Combine `bestScoreLimit` (stop when optimal) with `spentLimit` (timeout) for practical solving

## License

This is a demonstration project for educational purposes.
