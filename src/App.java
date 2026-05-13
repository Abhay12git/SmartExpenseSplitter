import cli.CLI;
import cli.CommandHandler;
import persistence.FileStorage;
import repository.*;
import service.*;

public class App {
    public static void main(String[] args) {

        // Repositories
        UserRepository    userRepo    = new UserRepository();
        GroupRepository   groupRepo   = new GroupRepository();
        ExpenseRepository expenseRepo = new ExpenseRepository();

        // Persistence — load on startup
        FileStorage storage = new FileStorage(userRepo, groupRepo, expenseRepo);
        storage.loadAll();

        // Services
        UserService      userService      = new UserService(userRepo);
        GroupService     groupService     = new GroupService(groupRepo, userRepo);
        ExpenseService   expenseService   = new ExpenseService(expenseRepo, groupRepo);
        BalanceService   balanceService   = new BalanceService(expenseRepo);
        DebtSimplifier   debtSimplifier   = new DebtSimplifier();
        AnalyticsService analyticsService = new AnalyticsService(
            expenseRepo, balanceService, groupService);

        // Auto-save on JVM shutdown (Ctrl+C or exit)
        Runtime.getRuntime().addShutdownHook(
            new Thread(storage::saveAll));

        // CLI
        CommandHandler handler = new CommandHandler(
            userService, groupService, expenseService,
            balanceService, debtSimplifier, analyticsService);

        new CLI(handler).start();
    }
}