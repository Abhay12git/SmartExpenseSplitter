package strategy;

import model.SplitType;

// Single place to resolve SplitType → SplitStrategy.
// ExpenseService uses this — no instanceof, no switch scattered around.
public class SplitStrategyFactory {

    public static SplitStrategy getStrategy(SplitType type) {
        switch (type) {
            case EQUAL:      return new EqualSplitStrategy();
            case PERCENTAGE: return new PercentageSplitStrategy();
            case EXACT:      return new ExactSplitStrategy();
            default:
                throw new IllegalArgumentException(
                    "Unknown split type: " + type);
        }
    }
}