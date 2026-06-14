package com.gavioesfc.sorteio.dto;

import com.gavioesfc.sorteio.model.enums.Position;

public record PlayerResponse(
        Long id,
        String nome,
        Position posicao,
        Integer nivel,
        Boolean ativo
) {}
