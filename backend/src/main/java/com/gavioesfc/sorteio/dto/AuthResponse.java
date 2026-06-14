package com.gavioesfc.sorteio.dto;

public record AuthResponse(
        String token,
        long expiresIn
) {}
