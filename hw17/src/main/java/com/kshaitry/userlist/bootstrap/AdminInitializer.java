package com.kshaitry.userlist.bootstrap;

import com.kshaitry.userlist.entity.Role;
import com.kshaitry.userlist.entity.User;
import com.kshaitry.userlist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:}")
    private String adminUsername;

    @Value("${admin.password:}")
    private String adminPassword;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_USERNAME/ADMIN_PASSWORD not provided, skipping admin bootstrap");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName("Administrator");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Bootstrapped admin user '{}'", adminUsername);
    }
}
