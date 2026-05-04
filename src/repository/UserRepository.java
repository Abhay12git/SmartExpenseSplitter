package repository;

import model.User;
import java.util.*;

public class UserRepository {
    private final Map<String, User> store = new LinkedHashMap<>();

    public void save(User user) {
        store.put(user.getId(), user);
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();
    }

    public List<User> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    public boolean exists(String id) {
        return store.containsKey(id);
    }

    public Map<String, User> getStore() {
        return Collections.unmodifiableMap(store);
    }

    // Used by persistence layer during load
    public void loadAll(Collection<User> users) {
        users.forEach(u -> store.put(u.getId(), u));
    }
}