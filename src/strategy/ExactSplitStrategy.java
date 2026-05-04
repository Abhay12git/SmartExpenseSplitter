package strategy;

import model.Split;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExactSplitStrategy implements SplitStrategy {

    private static final double TOLERANCE = 0.01;

    @Override
    public List<Split> calculateSplits(double totalAmount,
                                       List<User> participants,
                                       Map<User, Double> inputs) {
        validate(totalAmount, participants, inputs);

        List<Split> splits = new ArrayList<>();
        for (User user : participants) {
            double amount = inputs.get(user);
            splits.add(new Split(user, amount));
        }
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
                "Exact amount inputs cannot be empty.");
        }

        for (User u : participants) {
            if (!inputs.containsKey(u)) {
                throw new IllegalArgumentException(
                    "Missing exact amount for user: " + u.getName());
            }
            if (inputs.get(u) < 0) {
                throw new IllegalArgumentException(
                    "Exact amount cannot be negative for: " + u.getName());
            }
        }

        // Exact amounts must sum to totalAmount (within tolerance)
        double sum = inputs.values().stream()
                           .mapToDouble(Double::doubleValue)
                           .sum();
        if (Math.abs(sum - totalAmount) > TOLERANCE) {
            throw new IllegalArgumentException(
                String.format(
                    "Exact amounts (%.2f) must sum to total amount (%.2f).",
                    sum, totalAmount));
        }
    }
}