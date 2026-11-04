package com.gavioesfc.sorteio.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class AdminCredentialsTest {

    @Test
    void acceptsTheBcryptHashedPassword() {
        String hash = new BCryptPasswordEncoder().encode("segredo-forte");
        AdminCredentials credentials = new AdminCredentials("admin", "", hash);

        assertThat(credentials.matches("admin", "segredo-forte")).isTrue();
        assertThat(credentials.matches("admin", "admin123")).isFalse();
        assertThat(credentials.matches("outro", "segredo-forte")).isFalse();
    }

    @Test
    void acceptsThePlainPasswordWhenNoHashIsSet() {
        AdminCredentials credentials = new AdminCredentials("admin", "segredo-forte", "");

        assertThat(credentials.matches("admin", "segredo-forte")).isTrue();
        assertThat(credentials.matches("admin", "segredo")).isFalse();
    }

    @Test
    void rejectsEveryLoginWhenNoPasswordIsConfigured() {
        AdminCredentials credentials = new AdminCredentials("admin", "", "");

        assertThat(credentials.matches("admin", "")).isFalse();
        assertThat(credentials.matches("admin", "admin123")).isFalse();
        assertThat(credentials.matches(null, null)).isFalse();
    }
}
