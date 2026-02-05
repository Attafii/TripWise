package ui.admin.model;

import java.util.UUID;

public class User {
    private final String id = UUID.randomUUID().toString();
    private String email;
    private String fullName;
    private Role role = Role.USER;
    private boolean active = true;

    public User() {}

    public User(String email, String fullName, Role role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
