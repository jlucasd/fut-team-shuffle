package com.gavioesfc.sorteio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DispositivoRequest(
        @NotBlank(message = "Token do dispositivo é obrigatório")
        @Size(max = 1024)
        String token,

        String plataforma
) {}
