package repository;

import model.Expense;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseRepository {
    private final Map<String, Expense> store = new LinkedHashMap<>();

    public void save(Expense expense) {
        store.put(expense.getId(), expense);
    }

    public Optional<Expense> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Expense> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    public List<Expense> findByGroupId(String groupId) {
        return store.values().stream()
                    .filter(e -> e.getGroupId().equals(groupId))
                    .collect(Collectors.toList());
    }

    public List<Expense> findByUserId(String userId) {
        // Expenses where this user is a participant (in any split)
        return store.values().stream()
                    .filter(e -> e.getSplits().stream()
                                  .anyMatch(s -> s.getUser().getId().equals(userId)))
                    .collect(Collectors.toList());
    }

    public List<Expense> findByGroupAndMonth(String groupId, int year, int month) {
        return store.values().stream()
                    .filter(e -> e.getGroupId().equals(groupId)
                              && e.getDate().getYear()        == year
                              && e.getDate().getMonthValue()  == month)
                    .collect(Collectors.toList());
    }

    public List<Expense> findByMonth(int year, int month) {
        return store.values().stream()
                    .filter(e -> e.getDate().getYear()       == year
                              && e.getDate().getMonthValue() == month)
                    .collect(Collectors.toList());
    }

    public Map<String, Expense> getStore() {
        return Collections.unmodifiableMap(store);
    }

    public void loadAll(Collection<Expense> expenses) {
        expenses.forEach(e -> store.put(e.getId(), e));
    }
}