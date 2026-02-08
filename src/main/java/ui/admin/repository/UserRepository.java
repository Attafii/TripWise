package ui.admin.repository;

import ui.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    void save(User user);
    void deleteById(String id);

    default void deleteByEmail(String email) {
        throw new UnsupportedOperationException("deleteByEmail not implemented.");
    }
}