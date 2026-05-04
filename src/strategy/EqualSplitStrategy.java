package strategy;

import model.Split;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public List<Split> calculateSplits(double totalAmount,
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

        int    count      = participants.size();
        // Floor to 2 decimal places for each share
        double baseShare  = Math.floor((totalAmount / count) * 100) / 100.0;
        // Remainder goes to the first participant (avoids floating-point drift)
        double remainder  = Math.round(
                                (totalAmount - baseShare * count) * 100) / 100.0;

        List<Split> splits = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double share = (i == 0) ? baseShare + remainder : baseShare;
            splits.add(new Split(participants.get(i), share));
        }

        return splits;
    }
}