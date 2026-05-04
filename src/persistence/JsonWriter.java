package persistence;

import model.*;

import java.util.List;
import java.util.stream.Collectors;

public class JsonWriter {

    // ── User ──────────────────────────────────────
    public String usersToJson(List<User> users) {
        String entries = users.stream()
            .map(this::userToJson)
            .collect(Collectors.joining(",\n  "));
        return "[\n  " + entries + "\n]";
    }

    private String userToJson(User u) {
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"email\":\"%s\"}",
            escape(u.getId()), escape(u.getName()), escape(u.getEmail()));
    }

    // ── Group ─────────────────────────────────────
    public String groupsToJson(List<Group> groups) {
        String entries = groups.stream()
            .map(this::groupToJson)
            .collect(Collectors.joining(",\n  "));
        return "[\n  " + entries + "\n]";
    }

    private String groupToJson(Group g) {
        String memberIds = g.getMembers().stream()
            .map(u -> "\"" + escape(u.getId()) + "\"")
            .collect(Collectors.joining(","));
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"memberIds\":[%s]}",
            escape(g.getId()), escape(g.getName()), memberIds);
    }

    // ── Expense ───────────────────────────────────
    public String expensesToJson(List<Expense> expenses) {
        String entries = expenses.stream()
            .map(this::expenseToJson)
            .collect(Collectors.joining(",\n  "));
        return "[\n  " + entries + "\n]";
    }

    private String expenseToJson(Expense e) {
        String splitsJson = e.getSplits().stream()
            .map(this::splitToJson)
            .collect(Collectors.joining(","));

        return String.format(
            "{\"id\":\"%s\",\"description\":\"%s\",\"totalAmount\":%.2f," +
            "\"paidById\":\"%s\",\"groupId\":\"%s\",\"splitType\":\"%s\"," +
            "\"date\":\"%s\",\"splits\":[%s]}",
            escape(e.getId()),
            escape(e.getDescription()),
            e.getTotalAmount(),
            escape(e.getPaidBy().getId()),
            escape(e.getGroupId()),
            e.getSplitType().name(),
            e.getDate().toString(),
            splitsJson);
    }

    private String splitToJson(Split s) {
        return String.format(
            "{\"userId\":\"%s\",\"amount\":%.2f,\"percentage\":%.2f}",
            escape(s.getUser().getId()), s.getAmount(), s.getPercentage());
    }

    // Escape special characters to keep JSON valid
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}