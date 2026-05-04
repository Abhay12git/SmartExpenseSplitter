package service;

import model.Group;
import model.User;
import repository.GroupRepository;
import repository.UserRepository;
import java.util.List;

public class GroupService {
    private final GroupRepository groupRepo;
    private final UserRepository  userRepo;

    public GroupService(GroupRepository groupRepo, UserRepository userRepo) {
        this.groupRepo = groupRepo;
        this.userRepo  = userRepo;
    }

    public Group createGroup(String name, List<String> memberIds) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be blank.");
        }
        if (groupRepo.findByName(name).isPresent()) {
            throw new IllegalArgumentException(
                "Group '" + name + "' already exists.");
        }
        if (memberIds == null || memberIds.size() < 2) {
            throw new IllegalArgumentException(
                "A group must have at least 2 members.");
        }

        Group group = new Group(name.trim());
        for (String uid : memberIds) {
            User user = userRepo.findById(uid)
                .orElseThrow(() ->
                    new IllegalArgumentException("No user found with id: " + uid));
            group.addMember(user);
        }
        groupRepo.save(group);
        return group;
    }

    public void addMember(String groupId, String userId) {
        Group group = getById(groupId);
        User  user  = userRepo.findById(userId)
            .orElseThrow(() ->
                new IllegalArgumentException("No user found with id: " + userId));
        if (group.hasMember(userId)) {
            throw new IllegalArgumentException(
                user.getName() + " is already in group " + group.getName());
        }
        group.addMember(user);
    }

    public Group getById(String id) {
        return groupRepo.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("No group found with id: " + id));
    }

    public List<Group> getAllGroups() {
        return groupRepo.findAll();
    }
}