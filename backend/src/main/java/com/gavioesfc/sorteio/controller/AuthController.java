package com.gavioesfc.sorteio.controller;

import com.gavioesfc.sorteio.dto.AuthResponse;
import com.gavioesfc.sorteio.dto.LoginRequest;
import com.gavioesfc.sorteio.security.AdminCredentials;
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

    private final JwtUtil jwtUtil;
    private final AdminCredentials adminCredentials;

    public AuthController(JwtUtil jwtUtil, AdminCredentials adminCredentials) {
        this.jwtUtil = jwtUtil;
        this.adminCredentials = adminCredentials;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!adminCredentials.matches(request.username(), request.password())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtUtil.generateToken(request.username());
        return ResponseEntity.ok(new AuthResponse(token, jwtUtil.getExpiration()));
    }
}
