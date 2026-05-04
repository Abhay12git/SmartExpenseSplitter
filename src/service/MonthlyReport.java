package service;

import model.User;
import java.util.Map;

public class MonthlyReport {

    private final String           month;
    private final int              year;
    private final String           groupId;
    private final int              totalExpenses;
    private final double           totalSpending;
    private final Map<User,Double> paidMap;
    private final Map<User,Double> shareMap;

    public MonthlyReport(String month, int year, String groupId,
                         int totalExpenses, double totalSpending,
                         Map<User, Double> paidMap,
                         Map<User, Double> shareMap) {
        this.month         = month;
        this.year          = year;
        this.groupId       = groupId;
        this.totalExpenses = totalExpenses;
        this.totalSpending = totalSpending;
        this.paidMap       = paidMap;
        this.shareMap      = shareMap;
    }

    public String           getMonth()         { return month; }
    public int              getYear()           { return year; }
    public String           getGroupId()        { return groupId; }
    public int              getTotalExpenses()  { return totalExpenses; }
    public double           getTotalSpending()  { return totalSpending; }
    public Map<User,Double> getPaidMap()        { return paidMap; }
    public Map<User,Double> getShareMap()       { return shareMap; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
            "%n┌─── Monthly Report: %s %d ──────────────────┐%n",
            month, year));
        sb.append(String.format(
            "│  Total Expenses : %-5d                         │%n",
            totalExpenses));
        sb.append(String.format(
            "│  Total Spending : ₹%-10.2f                  │%n",
            totalSpending));
        sb.append("│                                                  │\n");
        sb.append("│  Amount Paid Out:                                │\n");
        paidMap.forEach((u, v) ->
            sb.append(String.format(
                "│    %-15s → ₹%-10.2f               │%n",
                u.getName(), v)));
        sb.append("│                                                  │\n");
        sb.append("│  Share of Expenses:                              │\n");
        shareMap.forEach((u, v) ->
            sb.append(String.format(
                "│    %-15s → ₹%-10.2f               │%n",
                u.getName(), v)));
        sb.append("└──────────────────────────────────────────────────┘");
        return sb.toString();
    }
}