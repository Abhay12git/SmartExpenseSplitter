package strategy;

import model.Split;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PercentageSplitStrategy implements SplitStrategy {

    private static final double TOLERANCE = 0.01;

    @Override
    public List<Split> calculateSplits(double totalAmount,
                                       List<User> participants,
                                       Map<User, Double> inputs) {
        validate(totalAmount, participants, inputs);

        List<Split> splits   = new ArrayList<>();
        double      assigned = 0.0;

        // Process all but the last user normally
        for (int i = 0; i < participants.size() - 1; i++) {
            User   user       = participants.get(i);
            double percentage = inputs.get(user);
            double amount     = Math.round((totalAmount * percentage / 100.0) * 100) / 100.0;
            splits.add(new Split(user, amount, percentage));
            assigned += amount;
        }

        // Last user absorbs any rounding residual
        User   lastUser       = participants.get(participants.size() - 1);
        double lastPercentage = inputs.get(lastUser);
        double lastAmount     = Math.round((totalAmount - assigned) * 100) / 100.0;
        splits.add(new Split(lastUser, lastAmount, lastPercentage));

        return splits;
    }

    private void validate(double totalAmount,
                          List<User> participants,
                          Map<User, Double> inputs) {
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException(
                "Participant list cannot be empty.");
        }
        if (totalAmount <= 0) {
            throw new IllegalArgumentException(
                "Total amount must be greater than zero.");
        }
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException(
                "Percentage inputs cannot be empty.");
        }

        // Every participant must have a percentage entry
        for (User u : participants) {
            if (!inputs.containsKey(u)) {
                throw new IllegalArgumentException(
                    "Missing percentage for user: " + u.getName());
            }
            double pct = inputs.get(u);
            if (pct <= 0 || pct > 100) {
                throw new IllegalArgumentException(
                    "Percentage must be between 0 and 100 for: " + u.getName());
            }
        }

        // Percentages must sum to 100 (within tolerance)
        double sum = inputs.values().stream()
                           .mapToDouble(Double::doubleValue)
                           .sum();
        if (Math.abs(sum - 100.0) > TOLERANCE) {
            throw new IllegalArgumentException(
                String.format("Percentages must sum to 100. Current sum: %.2f", sum));
        }
    }
}