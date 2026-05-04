package model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Expense {
    private final String      id;
    private final String      description;
    private final double      totalAmount;
    private final User        paidBy;
    private final String      groupId;       // avoid circular reference to Group
    private final List<Split> splits;
    private final SplitType   splitType;
    private final LocalDate   date;

    public Expense(String description,
                   double totalAmount,
                   User paidBy,
                   String groupId,
                   List<Split> splits,
                   SplitType splitType) {
        this.id          = UUID.randomUUID().toString();
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy      = paidBy;
        this.groupId     = groupId;
        this.splits      = splits;
        this.splitType   = splitType;
        this.date        = LocalDate.now();
    }

    // Used during deserialization
    public Expense(String id,
                   String description,
                   double totalAmount,
                   User paidBy,
                   String groupId,
                   List<Split> splits,
                   SplitType splitType,
                   LocalDate date) {
        this.id          = id;
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy      = paidBy;
        this.groupId     = groupId;
        this.splits      = splits;
        this.splitType   = splitType;
        this.date        = date;
    }

    public String      getId()          { return id; }
    public String      getDescription() { return description; }
    public double      getTotalAmount() { return totalAmount; }
    public User        getPaidBy()      { return paidBy; }
    public String      getGroupId()     { return groupId; }
    public SplitType   getSplitType()   { return splitType; }
    public LocalDate   getDate()        { return date; }

    public List<Split> getSplits() {
        return Collections.unmodifiableList(splits);
    }

    @Override
    public String toString() {
        return String.format(
            "Expense{id='%s', desc='%s', amount=%.2f, paidBy='%s', date=%s}",
            id, description, totalAmount, paidBy.getName(), date
        );
    }
}