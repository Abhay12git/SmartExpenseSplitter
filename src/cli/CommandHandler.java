package cli;

import model.*;
import service.*;

import java.util.*;

public class CommandHandler {

    private final UserService      userService;
    private final GroupService     groupService;
    private final ExpenseService   expenseService;
    private final BalanceService   balanceService;
    private final DebtSimplifier   debtSimplifier;
    private final AnalyticsService analyticsService;

    public CommandHandler(UserService      userService,
                          GroupService     groupService,
                          ExpenseService   expenseService,
                          BalanceService   balanceService,
                          DebtSimplifier   debtSimplifier,
                          AnalyticsService analyticsService) {
        this.userService      = userService;
        this.groupService     = groupService;
        this.expenseService   = expenseService;
        this.balanceService   = balanceService;
        this.debtSimplifier   = debtSimplifier;
        this.analyticsService = analyticsService;
    }

    // ─────────────────────────────────────────────
    // Main dispatch
    // ─────────────────────────────────────────────

    public void handle(String input) {
        if (input == null || input.isBlank()) return;

        String[] parts   = input.trim().split("\\s+");
        String   command = parts[0].toLowerCase();

        try {
            switch (command) {
                // Users
                case "add-user"              -> handleAddUser(parts);
                case "list-users"            -> handleListUsers();
                // Groups
                case "add-group"             -> handleAddGroup(parts);
                case "add-member"            -> handleAddMember(parts);
                case "list-groups"           -> handleListGroups();
                // Expenses
                case "add-expense"           -> handleAddExpense(parts);
                case "list-expenses"         -> handleListExpenses(parts);
                // Balances
                case "show-balances"         -> handleShowBalances(parts);
                case "show-balance-between"  -> handleBalanceBetween(parts);
                case "settle"                -> handleSettle(parts);
                // Analytics
                case "analytics-paid"        -> handleAnalyticsPaid();
                case "analytics-owed"        -> handleAnalyticsOwed();
                case "analytics-share"       -> handleAnalyticsShare(parts);
                case "monthly-report"        -> handleMonthlyReport(parts);
                case "largest-debtor"        -> handleLargestDebtor();
                case "largest-creditor"      -> handleLargestCreditor();
                // System
                case "help"                  -> printHelp();
                case "exit"                  -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println(
                    "Unknown command: '" + command + "'. Type 'help' for commands.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[SYSTEM ERROR] " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // USER HANDLERS
    // ─────────────────────────────────────────────

    private void handleAddUser(String[] parts) {
        requireArgs(parts, 3, "add-user <name> <email>");
        User user = userService.createUser(parts[1], parts[2]);
        System.out.printf("✓ User created: %s (id: %s)%n",
            user.getName(), user.getId());
    }

    private void handleListUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users registered yet.");
            return;
        }
        System.out.println("\n── Users ───────────────────────────────");
        users.forEach(u -> System.out.printf(
            "  [%s]  %-15s  %s%n", u.getId(), u.getName(), u.getEmail()));
        System.out.println("────────────────────────────────────────");
    }

    // ─────────────────────────────────────────────
    // GROUP HANDLERS
    // ─────────────────────────────────────────────

    private void handleAddGroup(String[] parts) {
        // add-group <name> <uid1> <uid2> ...
        if (parts.length < 4) {
            throw new IllegalArgumentException(
                "Usage: add-group <groupName> <userId1> <userId2> ...");
        }
        String       name      = parts[1];
        List<String> memberIds = Arrays.asList(parts).subList(2, parts.length);
        Group group = groupService.createGroup(name, memberIds);
        System.out.printf("✓ Group '%s' created (id: %s) with %d members.%n",
            group.getName(), group.getId(), group.getMembers().size());
    }

    private void handleAddMember(String[] parts) {
        requireArgs(parts, 3, "add-member <groupId> <userId>");
        groupService.addMember(parts[1], parts[2]);
        System.out.println("✓ Member added to group.");
    }

    private void handleListGroups() {
        List<Group> groups = groupService.getAllGroups();
        if (groups.isEmpty()) {
            System.out.println("No groups created yet.");
            return;
        }
        System.out.println("\n── Groups ──────────────────────────────");
        groups.forEach(g -> {
            System.out.printf("  [%s]  %s%n", g.getId(), g.getName());
            g.getMembers().forEach(m ->
                System.out.printf("         • %-15s [%s]%n",
                    m.getName(), m.getId()));
        });
        System.out.println("────────────────────────────────────────");
    }

    // ─────────────────────────────────────────────
    // EXPENSE HANDLERS
    // ─────────────────────────────────────────────

    /**
     * EQUAL:
     *   add-expense <groupId> <desc> <amount> <paidById> EQUAL <uid1> <uid2>...
     *
     * PERCENTAGE:
     *   add-expense <groupId> <desc> <amount> <paidById> PERCENTAGE
     *               <uid1> <pct1> <uid2> <pct2> ...
     *
     * EXACT:
     *   add-expense <groupId> <desc> <amount> <paidById> EXACT
     *               <uid1> <amt1> <uid2> <amt2> ...
     */
    private void handleAddExpense(String[] parts) {
        if (parts.length < 7) {
            throw new IllegalArgumentException(
                "Usage: add-expense <groupId> <desc> <amount> " +
                "<paidById> <EQUAL|PERCENTAGE|EXACT> <participants...>");
        }

        String    groupId   = parts[1];
        String    desc      = parts[2];
        double    amount    = parseAmount(parts[3]);
        String    paidById  = parts[4];
        SplitType splitType = parseSplitType(parts[5]);

        User paidBy = userService.getById(paidById);

        List<User>        participants = new ArrayList<>();
        Map<User, Double> inputs       = new LinkedHashMap<>();

        switch (splitType) {
            case EQUAL -> {
                // Remaining tokens are userIds
                for (int i = 6; i < parts.length; i++) {
                    participants.add(userService.getById(parts[i]));
                }
            }
            case PERCENTAGE, EXACT -> {
                // Tokens alternate: userId value userId value ...
                if ((parts.length - 6) % 2 != 0) {
                    throw new IllegalArgumentException(
                        "PERCENTAGE/EXACT requires pairs: <userId> <value> ...");
                }
                for (int i = 6; i < parts.length; i += 2) {
                    User   u   = userService.getById(parts[i]);
                    double val = parseAmount(parts[i + 1]);
                    participants.add(u);
                    inputs.put(u, val);
                }
            }
        }

        Expense expense = expenseService.addExpense(
            groupId, desc, amount, paidBy, splitType, participants, inputs);

        System.out.printf(
            "✓ Expense added: '%s' %.2f rupees paid by %s (id: %s)%n",
            expense.getDescription(), expense.getTotalAmount(),
            paidBy.getName(), expense.getId());

        System.out.println("  Splits:");
        expense.getSplits().forEach(s ->
            System.out.printf("    %-15s - %.2f rupees%n",
                s.getUser().getName(), s.getAmount()));
    }

    private void handleListExpenses(String[] parts) {
        requireArgs(parts, 2, "list-expenses <groupId>");
        List<Expense> expenses = expenseService.getExpensesForGroup(parts[1]);
        if (expenses.isEmpty()) {
            System.out.println("No expenses in this group.");
            return;
        }
        System.out.println("\n── Expenses ────────────────────────────");
        expenses.forEach(e -> System.out.printf(
            "  [%s]  %-20s %.2f rupees  paid by %-12s  %s%n",
            e.getId(), e.getDescription(), e.getTotalAmount(),
            e.getPaidBy().getName(), e.getDate()));
        System.out.println("────────────────────────────────────────");
    }

    // ─────────────────────────────────────────────
    // BALANCE HANDLERS
    // ─────────────────────────────────────────────

    private void handleShowBalances(String[] parts) {
        requireArgs(parts, 2, "show-balances <groupId>");
        Map<User, Double> balances = balanceService.getGroupBalances(parts[1]);

        if (balances.isEmpty()) {
            System.out.println("✓ All settled up in this group!");
            return;
        }

        System.out.println("\n── Net Balances ────────────────────────");
        balances.forEach((user, balance) -> {
            String status = balance > 0
                ? String.format("is owed  %.2f rupees", balance)
                : String.format("owes     %.2f rupees", Math.abs(balance));
            System.out.printf("  %-15s  %s%n", user.getName(), status);
        });
        System.out.println("────────────────────────────────────────");
    }

    private void handleBalanceBetween(String[] parts) {
        requireArgs(parts, 3, "show-balance-between <userId1> <userId2>");
        User   userA   = userService.getById(parts[1]);
        User   userB   = userService.getById(parts[2]);
        double balance = balanceService.getBalanceBetween(userA, userB);

        if (balance == 0.0) {
            System.out.printf("✓ %s and %s are settled.%n",
                userA.getName(), userB.getName());
        } else if (balance > 0) {
            System.out.printf("  %s is owed %.2f rupees by %s%n",
                userA.getName(), balance, userB.getName());
        } else {
            System.out.printf("  %s owes %.2f rupees to %s%n",
                userA.getName(), Math.abs(balance), userB.getName());
        }
    }

    private void handleSettle(String[] parts) {
        requireArgs(parts, 2, "settle <groupId>");
        Map<User, Double>  balances     = balanceService.getGroupBalances(parts[1]);
        List<Transaction>  transactions = debtSimplifier.simplify(balances);

        if (transactions.isEmpty()) {
            System.out.println("✓ Nothing to settle — group is balanced!");
            return;
        }

        System.out.println("\n── Optimized Settlement Plan ───────────");
        System.out.printf("  %d transaction(s) needed:%n", transactions.size());
        transactions.forEach(t -> System.out.println(t));
        System.out.println("────────────────────────────────────────");
    }

    // ─────────────────────────────────────────────
    // ANALYTICS HANDLERS
    // ─────────────────────────────────────────────

    private void handleAnalyticsPaid() {
        Map<User, Double> data = analyticsService.totalPaidPerUser();
        System.out.println("\n── Total Paid Per User ─────────────────");
        data.forEach((u, v) ->
            System.out.printf("  %-15s  %.2f rupees%n", u.getName(), v));
        System.out.println("────────────────────────────────────────");
    }

    private void handleAnalyticsOwed() {
        Map<User, Double> data = analyticsService.totalOwedPerUser();
        System.out.println("\n── Total Owed Per User ─────────────────");
        data.forEach((u, v) ->
            System.out.printf("  %-15s  %.2f rupees%n", u.getName(), v));
        System.out.println("────────────────────────────────────────");
    }

    private void handleAnalyticsShare(String[] parts) {
        requireArgs(parts, 2, "analytics-share <groupId>");
        Map<User, Double> data = analyticsService.spendingSharePercent(parts[1]);
        System.out.println("\n── Spending Share (%) ──────────────────");
        data.forEach((u, v) ->
            System.out.printf("  %-15s  %.2f%%%n", u.getName(), v));
        System.out.println("────────────────────────────────────────");
    }

    private void handleMonthlyReport(String[] parts) {
        requireArgs(parts, 4, "monthly-report <groupId> <year> <month>");
        String groupId = parts[1];
        int    year    = parseInt(parts[2], "year");
        int    month   = parseInt(parts[3], "month");

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12.");
        }

        MonthlyReport report =
            analyticsService.generateMonthlyReport(groupId, year, month);
        System.out.println(report);
    }

    private void handleLargestDebtor() {
        analyticsService.largestDebtor().ifPresentOrElse(
            e -> System.out.printf(
                "Largest debtor: %s  owes %.2f rupees%n",
                e.getKey().getName(), Math.abs(e.getValue())),
            () -> System.out.println("✓ No one owes anything!")
        );
    }

    private void handleLargestCreditor() {
        analyticsService.largestCreditor().ifPresentOrElse(
            e -> System.out.printf(
                "Largest creditor: %s  is owed %.2f rupees%n",
                e.getKey().getName(), e.getValue()),
            () -> System.out.println("✓ No outstanding credits!")
        );
    }

    // ─────────────────────────────────────────────
    // HELP
    // ─────────────────────────────────────────────

    private void printHelp() {
        System.out.println("""

        ╔══════════════════════════════════════════════════════╗
        ║          Smart Expense Splitter — Commands           ║
        ╠══════════════════════════════════════════════════════╣
        ║ USERS                                                ║
        ║   add-user <name> <email>                            ║
        ║   list-users                                         ║
        ╠══════════════════════════════════════════════════════╣
        ║ GROUPS                                               ║
        ║   add-group <name> <uid1> <uid2> ...                 ║
        ║   add-member <groupId> <userId>                      ║
        ║   list-groups                                        ║
        ╠══════════════════════════════════════════════════════╣
        ║ EXPENSES                                             ║
        ║   add-expense <gid> <desc> <amt> <paidBy>            ║
        ║               EQUAL <uid1> <uid2> ...                ║
        ║               PERCENTAGE <uid1> <pct1> ...           ║
        ║               EXACT <uid1> <amt1> ...                ║
        ║   list-expenses <groupId>                            ║
        ╠══════════════════════════════════════════════════════╣
        ║ BALANCES                                             ║
        ║   show-balances <groupId>                            ║
        ║   show-balance-between <uid1> <uid2>                 ║
        ║   settle <groupId>                                   ║
        ╠══════════════════════════════════════════════════════╣
        ║ ANALYTICS                                            ║
        ║   analytics-paid                                     ║
        ║   analytics-owed                                     ║
        ║   analytics-share <groupId>                          ║
        ║   monthly-report <groupId> <year> <month>            ║
        ║   largest-debtor                                     ║
        ║   largest-creditor                                   ║
        ╠══════════════════════════════════════════════════════╣
        ║ SYSTEM                                               ║
        ║   help  |  exit                                      ║
        ╚══════════════════════════════════════════════════════╝
        """);
    }

    // ─────────────────────────────────────────────
    // UTILITIES
    // ─────────────────────────────────────────────

    private void requireArgs(String[] parts, int min, String usage) {
        if (parts.length < min) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }

    private double parseAmount(String s) {
        try {
            double val = Double.parseDouble(s);
            if (val <= 0) throw new IllegalArgumentException(
                "Amount must be greater than zero: " + s);
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + s);
        }
    }

    private int parseInt(String s, String fieldName) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid " + fieldName + ": " + s);
        }
    }

    private SplitType parseSplitType(String s) {
        try {
            return SplitType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid split type '" + s + "'. Use: EQUAL, PERCENTAGE, EXACT");
        }
    }
}