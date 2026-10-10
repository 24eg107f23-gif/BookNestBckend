package com.booknest.config;

import com.booknest.entity.AppUser;
import com.booknest.entity.Role;
import com.booknest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapLibrarian implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public BootstrapLibrarian(UserRepository users, PasswordEncoder passwordEncoder,
                              @Value("${BOOTSTRAP_LIBRARIAN_EMAIL:}") String email,
                              @Value("${BOOTSTRAP_LIBRARIAN_PASSWORD:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() != password.isBlank()) {
            throw new IllegalStateException("Set both BOOTSTRAP_LIBRARIAN_EMAIL and BOOTSTRAP_LIBRARIAN_PASSWORD.");
        }
        if (email.isBlank()) {
            return;
        }
        if (password.length() < 12) {
            throw new IllegalStateException("The bootstrap librarian password must be at least 12 characters.");
        }
        if (!users.existsByEmailIgnoreCase(email)) {
            users.save(new AppUser("BookNest Librarian", email.trim().toLowerCase(),
                    passwordEncoder.encode(password), Role.LIBRARIAN));
        }
    }
}
