package com.gavioesfc.sorteio.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DrawRequest(
        @NotEmpty(message = "Selecione pelo menos 2 jogadores para o sorteio")
        List<Long> jogadorIds,

        /** Optional: when absent the equipe is inferred from the selected players. */
        Long equipeId
) {
    public DrawRequest(List<Long> jogadorIds) {
        this(jogadorIds, null);
    }
}
