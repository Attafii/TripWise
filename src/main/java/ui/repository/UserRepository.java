package ui.admin.repository;

import ui.admin.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    void save(User user);         // upsert
    void deleteById(String id);
}