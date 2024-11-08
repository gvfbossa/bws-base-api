package br.com.bossawebsolutions.base_api.model;

import java.time.LocalDateTime;
import java.util.Set;

public class TestAppUser implements AppUser {
    private String username;
    private String password;
    private Set<String> role;

    public TestAppUser() { }

    public TestAppUser(String username, String password, Set<String> role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public Long getId() {
        return 0L;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Set<String> getRoles() {
        return role;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return LocalDateTime.now();
    }
}
