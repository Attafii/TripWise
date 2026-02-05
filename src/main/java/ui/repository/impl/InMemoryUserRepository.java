package ui.admin.repository.impl;

import ui.admin.model.Role;
import ui.admin.model.User;
import ui.admin.repository.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> data = new ConcurrentHashMap<>();

    public InMemoryUserRepository() {
        // seed admin + sample user
        User admin = new User("admin@tripwise.com", "Admin User", Role.ADMIN);
        data.put(admin.getId(), admin);

        User user = new User("user@tripwise.com", "Regular User", Role.USER);
        data.put(user.getId(), user);
    }

    @Override public List<User> findAll() { return new ArrayList<>(data.values()); }

    @Override public Optional<User> findByEmail(String email) {
        return data.values().stream().filter(u -> Objects.equals(email, u.getEmail())).findFirst();
    }

    @Override public void save(User user) {
        data.put(user.getId(), user); // upsert by id
    }

    @Override public void deleteById(String id) { data.remove(id); }
}