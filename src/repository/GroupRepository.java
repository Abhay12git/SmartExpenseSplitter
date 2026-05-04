package repository;

import model.Group;
import java.util.*;

public class GroupRepository {
    private final Map<String, Group> store = new LinkedHashMap<>();

    public void save(Group group) {
        store.put(group.getId(), group);
    }

    public Optional<Group> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Group> findByName(String name) {
        return store.values().stream()
                    .filter(g -> g.getName().equalsIgnoreCase(name))
                    .findFirst();
    }

    public List<Group> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    public boolean exists(String id) {
        return store.containsKey(id);
    }

    public Map<String, Group> getStore() {
        return Collections.unmodifiableMap(store);
    }

    public void loadAll(Collection<Group> groups) {
        groups.forEach(g -> store.put(g.getId(), g));
    }
}