import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import model.Transaction;
import model.User;
import service.DebtSimplifier;

class AppTest {

    @Test
    void simplifyCreatesSingleSettlementTransaction() {
        User alice = new User("u1", "Alice", "alice@example.com");
        User bob = new User("u2", "Bob", "bob@example.com");

        Map<User, Double> balances = new LinkedHashMap<>();
        balances.put(alice, 150.0);
        balances.put(bob, -150.0);

        List<Transaction> transactions = new DebtSimplifier().simplify(balances);

        assertEquals(1, transactions.size());
        Transaction transaction = transactions.get(0);
        assertEquals(bob, transaction.getFrom());
        assertEquals(alice, transaction.getTo());
        assertEquals(150.0, transaction.getAmount(), 0.0001);
    }

    @Test
    void simplifyReturnsEmptyListWhenNothingIsOwed() {
        List<Transaction> transactions = new DebtSimplifier().simplify(Map.of());

        assertTrue(transactions.isEmpty());
    }
}