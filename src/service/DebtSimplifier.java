package service;

import model.Transaction;
import model.User;

import java.util.*;

public class DebtSimplifier {

    /**
     * Takes a net balance map (from BalanceService.getGroupBalances)
     * and returns the minimum set of transactions to settle all debts.
     *
     * @param balances Map<User, Double>
     *                 positive → user is owed money
     *                 negative → user owes money
     * @return ordered list of Transaction objects
     */
    public List<Transaction> simplify(Map<User, Double> balances) {
        if (balances == null || balances.isEmpty()) {
            return Collections.emptyList();
        }

        // Copy to avoid mutating the original map
        Map<User, Double> mutable = new HashMap<>(balances);

        // Max-heap for creditors (highest credit first)
        PriorityQueue<Map.Entry<User, Double>> creditors =
            new PriorityQueue<>(
                (a, b) -> Double.compare(b.getValue(), a.getValue())
            );

        // Max-heap for debtors (highest debt magnitude first)
        PriorityQueue<Map.Entry<User, Double>> debtors =
            new PriorityQueue<>(
                (a, b) -> Double.compare(a.getValue(), b.getValue())
            );

        // Partition users into creditors and debtors
        for (Map.Entry<User, Double> entry : mutable.entrySet()) {
            double val = Math.round(entry.getValue() * 100) / 100.0;
            if (val > 0.0) {
                creditors.offer(new AbstractMap.SimpleEntry<>(
                    entry.getKey(), val));
            } else if (val < 0.0) {
                debtors.offer(new AbstractMap.SimpleEntry<>(
                    entry.getKey(), val));
            }
            // val == 0 → already settled, skip
        }

        List<Transaction> transactions = new ArrayList<>();

        // Greedy matching loop
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<User, Double> creditor = creditors.poll();
            Map.Entry<User, Double> debtor   = debtors.poll();

            User   creditorUser = creditor.getKey();
            User   debtorUser   = debtor.getKey();
            double credit       = creditor.getValue();        // positive
            double debt         = Math.abs(debtor.getValue()); // positive magnitude

            double settled = Math.min(credit, debt);
            settled = Math.round(settled * 100) / 100.0;

            // Record this transaction
            transactions.add(new Transaction(debtorUser, creditorUser, settled));

            double remainingCredit = Math.round((credit - settled) * 100) / 100.0;
            double remainingDebt   = Math.round((debt   - settled) * 100) / 100.0;

            // If creditor still has remaining credit, push back
            if (remainingCredit > 0.0) {
                creditors.offer(new AbstractMap.SimpleEntry<>(
                    creditorUser, remainingCredit));
            }

            // If debtor still has remaining debt, push back
            if (remainingDebt > 0.0) {
                debtors.offer(new AbstractMap.SimpleEntry<>(
                    debtorUser, -remainingDebt));
            }
        }

        return transactions;
    }
}