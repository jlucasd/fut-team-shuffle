package com.gavioesfc.sorteio.controller;

import com.gavioesfc.sorteio.dto.AuthResponse;
import com.gavioesfc.sorteio.dto.LoginRequest;
import com.gavioesfc.sorteio.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String HARDCODED_USERNAME = "admin";
    private static final String HARDCODED_PASSWORD = "admin123";

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!HARDCODED_USERNAME.equals(request.username()) || !HARDCODED_PASSWORD.equals(request.password())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtUtil.generateToken(request.username());
        return ResponseEntity.ok(new AuthResponse(token, jwtUtil.getExpiration()));
    }
}
