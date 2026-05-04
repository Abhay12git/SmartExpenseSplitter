package service;

import model.User;
import repository.UserRepository;
import java.util.List;

public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User createUser(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be blank.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        if (userRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                "A user with email '" + email + "' already exists.");
        }
        User user = new User(name.trim(), email.trim().toLowerCase());
        userRepo.save(user);
        return user;
    }

    public User getById(String id) {
        return userRepo.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("No user found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}