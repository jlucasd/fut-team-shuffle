package com.gavioesfc.sorteio.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * The single admin login, configured through the environment:
 * {@code ADMIN_USERNAME} plus either {@code ADMIN_PASSWORD_HASH} (BCrypt,
 * preferred) or {@code ADMIN_PASSWORD}. With no password configured every
 * login is rejected (fail closed).
 */
@Component
public class AdminCredentials {

    private static final Logger log = LoggerFactory.getLogger(AdminCredentials.class);

    private final String username;
    private final String password;
    private final String passwordHash;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminCredentials(@Value("${app.auth.username}") String username,
                            @Value("${app.auth.password:}") String password,
                            @Value("${app.auth.password-hash:}") String passwordHash) {
        this.username = username;
        this.password = password;
        this.passwordHash = passwordHash;
    }

    @PostConstruct
    void warnIfUnconfigured() {
        if (password.isBlank() && passwordHash.isBlank()) {
            log.error("Login desativado: configure ADMIN_PASSWORD ou ADMIN_PASSWORD_HASH.");
        }
    }

    public boolean matches(String rawUsername, String rawPassword) {
        if (rawUsername == null || rawPassword == null) {
            return false;
        }
        boolean userOk = constantTimeEquals(username, rawUsername);
        boolean passwordOk;
        if (!passwordHash.isBlank()) {
            passwordOk = encoder.matches(rawPassword, passwordHash);
        } else if (!password.isBlank()) {
            passwordOk = constantTimeEquals(password, rawPassword);
        } else {
            passwordOk = false;
        }
        return userOk && passwordOk;
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
