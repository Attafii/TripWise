package ui.service;

import ui.model.User;
import ui.admin.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) { this.repo = repo; }

    public List<User> all() { return repo.findAll(); }
    public Optional<User> byEmail(String email) { return repo.findByEmail(email); }
    public void create(User u) { repo.save(u); }
    public void update(User u) { repo.save(u); }
    public void delete(String id) { repo.deleteById(id); }
}