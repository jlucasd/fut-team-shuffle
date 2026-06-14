package com.gavioesfc.sorteio.property;

import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Feature: gavioes-fc-sorteio, Property 15: Authentication enforcement
@JqwikSpringSupport
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthProperties {

    @Autowired
    private MockMvc mockMvc;

    private static final String[] PROTECTED_ENDPOINTS_GET = {
            "/api/jogadores",
            "/api/jogadores/1",
            "/api/sorteio/historico",
            "/api/sorteio/1"
    };

    private static final String[] PROTECTED_ENDPOINTS_POST = {
            "/api/jogadores",
            "/api/sorteio"
    };

    // --- Property 15: Authentication enforcement ---
    // **Validates: Requirements 13.2, 13.3**
    // Note: Spring Security returns 403 for unauthenticated stateless requests
    // when no explicit AuthenticationEntryPoint is configured.

    @Property(tries = 100)
    void protectedEndpointsRejectWithoutToken(
            @ForAll("protectedGetEndpoint") String endpoint) throws Exception {
        // No token at all → access denied
        mockMvc.perform(get(endpoint))
                .andExpect(status().isForbidden());
    }

    @Property(tries = 100)
    void protectedEndpointsRejectWithInvalidToken(
            @ForAll("protectedGetEndpoint") String endpoint,
            @ForAll("invalidToken") String token) throws Exception {
        mockMvc.perform(get(endpoint)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Property(tries = 100)
    void protectedPostEndpointsRejectWithoutToken(
            @ForAll("protectedPostEndpoint") String endpoint) throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Property(tries = 100)
    void protectedPostEndpointsRejectWithInvalidToken(
            @ForAll("protectedPostEndpoint") String endpoint,
            @ForAll("invalidToken") String token) throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Property(tries = 100)
    void protectedDeleteEndpointRejectWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/jogadores/1"))
                .andExpect(status().isForbidden());
    }

    @Property(tries = 100)
    void protectedPutEndpointRejectWithInvalidToken(
            @ForAll("invalidToken") String token) throws Exception {
        mockMvc.perform(put("/api/jogadores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Provide
    Arbitrary<String> protectedGetEndpoint() {
        return Arbitraries.of(PROTECTED_ENDPOINTS_GET);
    }

    @Provide
    Arbitrary<String> protectedPostEndpoint() {
        return Arbitraries.of(PROTECTED_ENDPOINTS_POST);
    }

    @Provide
    Arbitrary<String> invalidToken() {
        return Arbitraries.oneOf(
                // Random garbage strings
                Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(50),
                // Malformed JWTs
                Arbitraries.of(
                        "invalid.token.here",
                        "eyJhbGciOiJIUzI1NiJ9.invalid.payload",
                        "Bearer",
                        "null"
                ),
                // Expired-like tokens (just random base64-ish strings)
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .withCharRange('A', 'Z')
                        .withCharRange('0', '9')
                        .ofMinLength(20)
                        .ofMaxLength(100)
                        .map(s -> s + "." + s + "." + s)
        );
    }
}
