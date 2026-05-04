package strategy;

import model.Split;
import model.User;

import java.util.List;
import java.util.Map;

public interface SplitStrategy {

    /**
     * Calculate the split amounts for each participant.
     *
     * @param totalAmount  the full expense amount to divide
     * @param participants the users sharing this expense
     * @param inputs       strategy-specific inputs:
     *                       EQUAL      → ignored (pass empty map)
     *                       PERCENTAGE → Map<User, Double> percentage per user
     *                       EXACT      → Map<User, Double> exact amount per user
     * @return list of Split objects that MUST sum to totalAmount
     * @throws IllegalArgumentException on any validation failure
     */
    List<Split> calculateSplits(double totalAmount,
                                List<User> participants,
                                Map<User, Double> inputs);
}