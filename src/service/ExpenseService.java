package service;

import model.*;
import repository.ExpenseRepository;
import repository.GroupRepository;
import strategy.SplitStrategy;
import strategy.SplitStrategyFactory;

import java.util.List;
import java.util.Map;

public class ExpenseService {
    private final ExpenseRepository expenseRepo;
    private final GroupRepository   groupRepo;

    public ExpenseService(ExpenseRepository expenseRepo,
                          GroupRepository groupRepo) {
        this.expenseRepo = expenseRepo;
        this.groupRepo   = groupRepo;
    }

    public Expense addExpense(String      groupId,
                              String      description,
                              double      totalAmount,
                              User        paidBy,
                              SplitType   splitType,
                              List<User>  participants,
                              Map<User, Double> inputs) {
        // 1. Validate group exists
        Group group = groupRepo.findById(groupId)
            .orElseThrow(() ->
                new IllegalArgumentException("No group found with id: " + groupId));

        // 2. Validate paidBy is a group member
        if (!group.hasMember(paidBy.getId())) {
            throw new IllegalArgumentException(
                paidBy.getName() + " is not a member of group " + group.getName());
        }

        // 3. Validate all participants are group members
        for (User p : participants) {
            if (!group.hasMember(p.getId())) {
                throw new IllegalArgumentException(
                    p.getName() + " is not a member of group " + group.getName());
            }
        }

        // 4. Resolve strategy and compute splits
        SplitStrategy strategy = SplitStrategyFactory.getStrategy(splitType);
        List<Split>   splits   = strategy.calculateSplits(
                                     totalAmount, participants, inputs);

        // 5. Build and persist expense
        Expense expense = new Expense(
            description, totalAmount, paidBy, groupId, splits, splitType);
        expenseRepo.save(expense);
        return expense;
    }

    public List<Expense> getExpensesForGroup(String groupId) {
        return expenseRepo.findByGroupId(groupId);
    }
}