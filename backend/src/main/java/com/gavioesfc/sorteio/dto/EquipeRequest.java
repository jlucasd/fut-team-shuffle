package com.gavioesfc.sorteio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipeRequest(
        @NotBlank(message = "Nome do time não pode ser vazio")
        @Size(max = 80, message = "Nome do time deve ter no máximo 80 caracteres")
        String nome
) {}
