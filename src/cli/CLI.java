package cli;

import java.util.Scanner;

public class CLI {
    private final CommandHandler handler;

    public CLI(CommandHandler handler) {
        this.handler = handler;
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            printBanner();
            System.out.println("Type 'help' for commands.\n");

            while (scanner.hasNextLine()) {
                System.out.print("expense-splitter> ");
                String input = scanner.nextLine();
                handler.handle(input);
            }
        }
    }

    private void printBanner() {
        System.out.println("""
        ╔══════════════════════════════════════════╗
        ║     Smart Expense Splitter v1.0          ║
        ║     Debt Optimization + Analytics        ║
        ╚══════════════════════════════════════════╝
        """);
    }
}