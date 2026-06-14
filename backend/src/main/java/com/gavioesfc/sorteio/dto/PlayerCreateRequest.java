package com.gavioesfc.sorteio.dto;

import com.gavioesfc.sorteio.model.enums.Position;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlayerCreateRequest(
        @NotBlank(message = "Nome não pode ser vazio")
        String nome,

        @NotNull(message = "Posição é obrigatória")
        Position posicao,

        @NotNull(message = "Nível é obrigatório")
        @Min(value = 1, message = "Nível deve ser entre 1 e 5")
        @Max(value = 5, message = "Nível deve ser entre 1 e 5")
        Integer nivel,

        @NotNull(message = "Status ativo é obrigatório")
        Boolean ativo
) {}
