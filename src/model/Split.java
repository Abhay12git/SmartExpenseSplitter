package model;

public class Split {
    private final User   user;
    private       double amount;      // resolved rupee amount this user owes
    private       double percentage;  // only meaningful for PERCENTAGE splits

    public Split(User user, double amount) {
        this.user       = user;
        this.amount     = amount;
        this.percentage = 0.0;
    }

    public Split(User user, double amount, double percentage) {
        this.user       = user;
        this.amount     = amount;
        this.percentage = percentage;
    }

    public User   getUser()       { return user; }
    public double getAmount()     { return amount; }
    public double getPercentage() { return percentage; }

    public void setAmount(double amount)         { this.amount = amount; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    @Override
    public String toString() {
        return String.format("Split{user='%s', amount=%.2f}", user.getName(), amount);
    }
}