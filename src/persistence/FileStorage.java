package persistence;

import model.*;
import repository.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileStorage {

    private static final String DIR          = "data/";
    private static final String USERS_FILE   = DIR + "users.json";
    private static final String GROUPS_FILE  = DIR + "groups.json";
    private static final String EXPENSE_FILE = DIR + "expenses.json";

    private final UserRepository    userRepo;
    private final GroupRepository   groupRepo;
    private final ExpenseRepository expenseRepo;
    private final JsonWriter        writer;
    private final JsonReader        reader;

    public FileStorage(UserRepository    userRepo,
                       GroupRepository   groupRepo,
                       ExpenseRepository expenseRepo) {
        this.userRepo    = userRepo;
        this.groupRepo   = groupRepo;
        this.expenseRepo = expenseRepo;
        this.writer      = new JsonWriter();
        this.reader      = new JsonReader();
    }

    // ─────────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────────

    public void saveAll() {
        try {
            Files.createDirectories(Paths.get(DIR));
            writeFile(USERS_FILE,   writer.usersToJson(userRepo.findAll()));
            writeFile(GROUPS_FILE,  writer.groupsToJson(groupRepo.findAll()));
            writeFile(EXPENSE_FILE, writer.expensesToJson(expenseRepo.findAll()));
            System.out.println("Data saved.");
        } catch (IOException e) {
            System.out.println("[SAVE ERROR] " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // LOAD  (order matters: Users → Groups → Expenses)
    // ─────────────────────────────────────────────

    public void loadAll() {
        try {
            Files.createDirectories(Paths.get(DIR));
            loadUsers();
            loadGroups();
            loadExpenses();
            System.out.println("Data loaded.");
        } catch (IOException e) {
            System.out.println("[LOAD ERROR] " + e.getMessage());
        }
    }

    private void loadUsers() throws IOException {
        if (!fileExists(USERS_FILE)) return;
        String     json  = readFile(USERS_FILE);
        List<User> users = reader.parseUsers(json);
        userRepo.loadAll(users);
    }

    private void loadGroups() throws IOException {
        if (!fileExists(GROUPS_FILE)) return;
        String json = readFile(GROUPS_FILE);

        for (JsonReader.RawGroup raw : reader.parseGroups(json)) {
            List<User> members = new ArrayList<>();
            for (String uid : raw.memberIds()) {
                userRepo.findById(uid).ifPresent(members::add);
            }
            Group group = new Group(raw.id(), raw.name(), members);
            groupRepo.save(group);
        }
    }

    private void loadExpenses() throws IOException {
        if (!fileExists(EXPENSE_FILE)) return;
        String json = readFile(EXPENSE_FILE);

        for (JsonReader.RawExpense raw : reader.parseExpenses(json)) {
            User paidBy = userRepo.findById(raw.paidById()).orElse(null);
            if (paidBy == null) continue;   // skip orphaned expense

            List<Split> splits = new ArrayList<>();
            for (JsonReader.RawSplit rs : raw.splits()) {
                userRepo.findById(rs.userId()).ifPresent(u ->
                    splits.add(new Split(u, rs.amount(), rs.percentage())));
            }

            Expense expense = new Expense(
                raw.id(), raw.description(), raw.totalAmount(),
                paidBy, raw.groupId(), splits, raw.splitType(), raw.date());
            expenseRepo.save(expense);
        }
    }

    // ─────────────────────────────────────────────
    // File Utilities
    // ─────────────────────────────────────────────

    private void writeFile(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }

    private String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    private boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }
}