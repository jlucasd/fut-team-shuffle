package com.gavioesfc.sorteio.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Nome de usuário é obrigatório")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        String password
) {}
