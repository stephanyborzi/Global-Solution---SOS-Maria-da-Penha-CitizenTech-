package com.citizentech.sos.config;

import com.citizentech.sos.model.Profile;
import com.citizentech.sos.model.User;
import com.citizentech.sos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AdminSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UserRepository userRepo, PasswordEncoder encoder,
                       @Value("${admin.email}") String adminEmail,
                       @Value("${admin.password:}") String adminPassword) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        String email = adminEmail.trim().toLowerCase();
        if (userRepo.existsByEmail(email)) return;

        boolean generated = adminPassword == null || adminPassword.isBlank();
        String password = generated ? randomPassword() : adminPassword;
        userRepo.save(new User("Administrator", email, encoder.encode(password), Profile.ADMIN));

        if (generated) {
            log.warn("Initial ADMIN created. email={} password={} (shown only once; set ADMIN_PASSWORD to choose your own)",
                    email, password);
        } else {
            log.info("Initial ADMIN created with email {}", email);
        }
    }

    private String randomPassword() {
        byte[] bytes = new byte[12];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
