package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Group {
    private final String     id;
    private final String     name;
    private final List<User> members;

    // Expenses are NOT stored here — fetched from ExpenseRepository by groupId.
    // This avoids object graph bloat and keeps Group as a lightweight entity.

    public Group(String name) {
        this.id      = UUID.randomUUID().toString();
        this.name    = name;
        this.members = new ArrayList<>();
    }

    public Group(String id, String name, List<User> members) {
        this.id      = id;
        this.name    = name;
        this.members = new ArrayList<>(members);
    }

    public String getId()   { return id; }
    public String getName() { return name; }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(User user) {
        boolean alreadyPresent = members.stream()
            .anyMatch(m -> m.getId().equals(user.getId()));
        if (!alreadyPresent) {
            members.add(user);
        }
    }

    public boolean hasMember(String userId) {
        return members.stream().anyMatch(m -> m.getId().equals(userId));
    }

    @Override
    public String toString() {
        return String.format("Group{id='%s', name='%s', members=%d}",
            id, name, members.size());
    }
}