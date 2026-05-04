package service;

import model.Expense;
import model.Split;
import model.User;
import repository.ExpenseRepository;

import java.util.*;

public class BalanceService {
    private final ExpenseRepository expenseRepo;

    public BalanceService(ExpenseRepository expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    /**
     * Returns net balance for every user in a group.
     * Positive  → user is owed this amount.
     * Negative  → user owes this amount.
     */
    public Map<User, Double> getGroupBalances(String groupId) {
        List<Expense>    expenses = expenseRepo.findByGroupId(groupId);
        Map<User, Double> balance = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            User   paidBy = expense.getPaidBy();
            double total  = expense.getTotalAmount();

            // Payer gets full credit
            balance.merge(paidBy, total, Double::sum);

            // Each participant is debited their share
            for (Split split : expense.getSplits()) {
                balance.merge(split.getUser(), -split.getAmount(), Double::sum);
            }
        }

        // Round all values to 2 decimal places to kill floating-point noise
        balance.replaceAll((user, val) ->
            Math.round(val * 100) / 100.0);

        // Remove settled users (balance == 0)
        balance.entrySet().removeIf(e -> e.getValue() == 0.0);

        return balance;
    }

    /**
     * Net balance between exactly two users across ALL groups.
     * Positive → userA is owed money by userB.
     * Negative → userA owes money to userB.
     */
    public double getBalanceBetween(User userA, User userB) {
        List<Expense> all     = expenseRepo.findAll();
        double        balance = 0.0;

        for (Expense expense : all) {
            boolean aIsPayer = expense.getPaidBy().getId().equals(userA.getId());
            boolean bIsPayer = expense.getPaidBy().getId().equals(userB.getId());

            for (Split split : expense.getSplits()) {
                boolean splitIsA = split.getUser().getId().equals(userA.getId());
                boolean splitIsB = split.getUser().getId().equals(userB.getId());

                if (aIsPayer && splitIsB) {
                    // A paid, B owes → A is owed
                    balance += split.getAmount();
                }
                if (bIsPayer && splitIsA) {
                    // B paid, A owes → A owes
                    balance -= split.getAmount();
                }
            }
        }

        return Math.round(balance * 100) / 100.0;
    }
}