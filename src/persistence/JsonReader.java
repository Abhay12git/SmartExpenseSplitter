package persistence;

import model.*;

import java.time.LocalDate;
import java.util.*;

public class JsonReader {

    // ── Users ─────────────────────────────────────
    public List<User> parseUsers(String json) {
        List<User> users = new ArrayList<>();
        for (String obj : splitObjects(json)) {
            String id    = extract(obj, "id");
            String name  = extract(obj, "name");
            String email = extract(obj, "email");
            users.add(new User(id, name, email));
        }
        return users;
    }

    // ── Groups (member resolution happens in FileStorage) ──
    public List<RawGroup> parseGroups(String json) {
        List<RawGroup> groups = new ArrayList<>();
        for (String obj : splitObjects(json)) {
            String       id        = extract(obj, "id");
            String       name      = extract(obj, "name");
            List<String> memberIds = extractArray(obj, "memberIds");
            groups.add(new RawGroup(id, name, memberIds));
        }
        return groups;
    }

    // ── Expenses (user resolution happens in FileStorage) ──
    public List<RawExpense> parseExpenses(String json) {
        List<RawExpense> expenses = new ArrayList<>();
        for (String obj : splitObjects(json)) {
            String       id          = extract(obj, "id");
            String       description = extract(obj, "description");
            double       amount      = Double.parseDouble(extract(obj, "totalAmount"));
            String       paidById    = extract(obj, "paidById");
            String       groupId     = extract(obj, "groupId");
            SplitType    splitType   = SplitType.valueOf(extract(obj, "splitType"));
            LocalDate    date        = LocalDate.parse(extract(obj, "date"));
            List<RawSplit> splits    = parseSplits(extractBlock(obj, "splits"));
            expenses.add(new RawExpense(
                id, description, amount, paidById, groupId, splitType, date, splits));
        }
        return expenses;
    }

    private List<RawSplit> parseSplits(String splitsJson) {
        List<RawSplit> splits = new ArrayList<>();
        for (String obj : splitObjects(splitsJson)) {
            String userId     = extract(obj, "userId");
            double amount     = Double.parseDouble(extract(obj, "amount"));
            double percentage = Double.parseDouble(extract(obj, "percentage"));
            splits.add(new RawSplit(userId, amount, percentage));
        }
        return splits;
    }

    // ─────────────────────────────────────────────
    // Parsing Utilities
    // ─────────────────────────────────────────────

    /**
     * Splits a JSON array string into individual object strings.
     * Handles nested braces correctly.
     */
    List<String> splitObjects(String json) {
        List<String> objects = new ArrayList<>();
        if (json == null) return objects;

        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]"))   json = json.substring(0, json.length() - 1);
        json = json.trim();
        if (json.isEmpty())       return objects;

        int depth  = 0;
        int start  = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if      (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') {
                depth--;
                if (depth == 0) objects.add(json.substring(start, i + 1));
            }
        }
        return objects;
    }

    /**
     * Extracts a string or number value for a key from a flat JSON object.
     * Handles both "key":"value" and "key":123.45
     */
    String extract(String json, String key) {
        String pattern = "\"" + key + "\"";
        int    idx     = json.indexOf(pattern);
        if (idx == -1) return "";

        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;

        if (json.charAt(idx) == '"') {
            // String value
            int end = idx + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(idx + 1, end)
                       .replace("\\\"", "\"")
                       .replace("\\n", "\n")
                       .replace("\\\\", "\\");
        } else {
            // Numeric / boolean / enum value
            int end = idx;
            while (end < json.length() &&
                   ",}]\n".indexOf(json.charAt(end)) == -1) end++;
            return json.substring(idx, end).trim();
        }
    }

    /**
     * Extracts a JSON array of strings: ["a","b","c"]
     */
    List<String> extractArray(String json, String key) {
        String  block  = extractBlock(json, key);
        List<String> result = new ArrayList<>();
        if (block == null || block.equals("[]")) return result;

        block = block.trim();
        if (block.startsWith("[")) block = block.substring(1);
        if (block.endsWith("]"))   block = block.substring(0, block.length() - 1);

        for (String token : block.split(",")) {
            token = token.trim().replace("\"", "");
            if (!token.isEmpty()) result.add(token);
        }
        return result;
    }

    /**
     * Extracts a nested JSON block (array or object) for a given key.
     */
    String extractBlock(String json, String key) {
        String pattern = "\"" + key + "\"";
        int    idx     = json.indexOf(pattern);
        if (idx == -1) return null;

        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;

        char   open  = json.charAt(idx);
        char   close = (open == '[') ? ']' : '}';
        int    depth = 0;
        int    start = idx;

        for (int i = idx; i < json.length(); i++) {
            char c = json.charAt(i);
            if      (c == open)  depth++;
            else if (c == close) { depth--; if (depth == 0) return json.substring(start, i + 1); }
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // Raw DTOs (intermediate before User resolution)
    // ─────────────────────────────────────────────

    public record RawGroup(String id, String name, List<String> memberIds) {}

    public record RawExpense(String id, String description, double totalAmount,
                             String paidById, String groupId, SplitType splitType,
                             LocalDate date, List<RawSplit> splits) {}

    public record RawSplit(String userId, double amount, double percentage) {}
}