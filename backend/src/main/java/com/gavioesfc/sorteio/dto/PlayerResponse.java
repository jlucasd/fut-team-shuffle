package com.gavioesfc.sorteio.dto;

import com.gavioesfc.sorteio.model.enums.Position;

import java.time.LocalDate;

public record PlayerResponse(
        Long id,
        String nome,
        Position posicao,
        Integer nivel,
        Boolean ativo,
        LocalDate dataNascimento
) {}
