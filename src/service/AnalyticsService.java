package service;

import model.Expense;
import model.Split;
import model.User;
import repository.ExpenseRepository;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {

    private final ExpenseRepository expenseRepo;
    private final BalanceService    balanceService;
    private final GroupService      groupService;

    public AnalyticsService(ExpenseRepository expenseRepo,
                            BalanceService    balanceService,
                            GroupService      groupService) {
        this.expenseRepo    = expenseRepo;
        this.balanceService = balanceService;
        this.groupService   = groupService;
    }

    // ─────────────────────────────────────────────
    // 1. Total amount paid by each user (globally)
    // ─────────────────────────────────────────────

    /**
     * Returns how much each user has paid out-of-pocket across all expenses.
     * Sorted descending by amount paid.
     */
    public Map<User, Double> totalPaidPerUser() {
        Map<User, Double> result = new LinkedHashMap<>();

        for (Expense expense : expenseRepo.findAll()) {
            result.merge(expense.getPaidBy(),
                         expense.getTotalAmount(),
                         Double::sum);
        }

        return sortedDescending(result);
    }

    // ─────────────────────────────────────────────
    // 2. Total amount owed by each user (globally)
    //    i.e. sum of all their splits across all expenses
    // ─────────────────────────────────────────────

    /**
     * Returns how much each user owes in total across all expenses.
     * (This is gross debt — before netting against what they paid.)
     */
    public Map<User, Double> totalOwedPerUser() {
        Map<User, Double> result = new LinkedHashMap<>();

        for (Expense expense : expenseRepo.findAll()) {
            for (Split split : expense.getSplits()) {
                result.merge(split.getUser(),
                             split.getAmount(),
                             Double::sum);
            }
        }

        return sortedDescending(result);
    }

    // ─────────────────────────────────────────────
    // 3. Who owes the most (net, across all groups)
    // ─────────────────────────────────────────────

    /**
     * Finds the user with the highest net negative balance globally.
     * Returns empty if everyone is settled.
     */
    public Optional<Map.Entry<User, Double>> largestDebtor() {
        // Aggregate net balances across all groups
        Map<User, Double> globalNet = new HashMap<>();

        for (var group : groupService.getAllGroups()) {
            Map<User, Double> groupBalances =
                balanceService.getGroupBalances(group.getId());

            groupBalances.forEach((user, balance) ->
                globalNet.merge(user, balance, Double::sum));
        }

        return globalNet.entrySet().stream()
            .filter(e -> e.getValue() < 0)           // only debtors
            .min(Map.Entry.comparingByValue());       // most negative = most owed
    }

    /**
     * Finds the user with the highest net positive balance globally.
     * Returns empty if no one is owed money.
     */
    public Optional<Map.Entry<User, Double>> largestCreditor() {
        Map<User, Double> globalNet = new HashMap<>();

        for (var group : groupService.getAllGroups()) {
            Map<User, Double> groupBalances =
                balanceService.getGroupBalances(group.getId());

            groupBalances.forEach((user, balance) ->
                globalNet.merge(user, balance, Double::sum));
        }

        return globalNet.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .max(Map.Entry.comparingByValue());
    }

    // ─────────────────────────────────────────────
    // 4. Monthly summary for a group
    // ─────────────────────────────────────────────

    /**
     * Returns total spending per user in a group for a given month/year.
     * "Spending" here = share of each expense (split amount), not what they paid.
     */
    public Map<User, Double> monthlySummaryByShare(String groupId,
                                                    int year,
                                                    int month) {
        List<Expense> expenses =
            expenseRepo.findByGroupAndMonth(groupId, year, month);

        Map<User, Double> result = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            for (Split split : expense.getSplits()) {
                result.merge(split.getUser(),
                             split.getAmount(),
                             Double::sum);
            }
        }

        return sortedDescending(result);
    }

    /**
     * Returns total amount paid out by each user in a group for a given month.
     */
    public Map<User, Double> monthlySummaryByPaid(String groupId,
                                                   int year,
                                                   int month) {
        List<Expense> expenses =
            expenseRepo.findByGroupAndMonth(groupId, year, month);

        Map<User, Double> result = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            result.merge(expense.getPaidBy(),
                         expense.getTotalAmount(),
                         Double::sum);
        }

        return sortedDescending(result);
    }

    // ─────────────────────────────────────────────
    // 5. Group spending share (% of total per user)
    // ─────────────────────────────────────────────

    /**
     * Returns each user's percentage share of total group spending.
     * Useful for understanding who drives group costs.
     */
    public Map<User, Double> spendingSharePercent(String groupId) {
        List<Expense> expenses = expenseRepo.findByGroupId(groupId);

        Map<User, Double> shareAmounts = new LinkedHashMap<>();
        double            groupTotal   = 0.0;

        for (Expense expense : expenses) {
            groupTotal += expense.getTotalAmount();
            for (Split split : expense.getSplits()) {
                shareAmounts.merge(split.getUser(),
                                   split.getAmount(),
                                   Double::sum);
            }
        }

        if (groupTotal == 0.0) return Collections.emptyMap();

        final double total = groupTotal;
        Map<User, Double> percentages = new LinkedHashMap<>();
        shareAmounts.forEach((user, amount) ->
            percentages.put(user,
                Math.round((amount / total * 100.0) * 100) / 100.0));

        return sortedDescending(percentages);
    }

    // ─────────────────────────────────────────────
    // 6. Full monthly report (formatted summary)
    // ─────────────────────────────────────────────

    public MonthlyReport generateMonthlyReport(String groupId,
                                               int year,
                                               int month) {
        List<Expense> expenses =
            expenseRepo.findByGroupAndMonth(groupId, year, month);

        double totalSpending = expenses.stream()
            .mapToDouble(Expense::getTotalAmount)
            .sum();

        Map<User, Double> paidMap  = monthlySummaryByPaid(groupId, year, month);
        Map<User, Double> shareMap = monthlySummaryByShare(groupId, year, month);

        String monthName = Month.of(month)
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        return new MonthlyReport(
            monthName, year, groupId,
            expenses.size(), totalSpending,
            paidMap, shareMap
        );
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────

    private Map<User, Double> sortedDescending(Map<User, Double> input) {
        return input.entrySet().stream()
            .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Math.round(e.getValue() * 100) / 100.0,
                (a, b) -> a,
                LinkedHashMap::new
            ));
    }
}