package com.gavioesfc.sorteio.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DrawResponse(
        Long id,
        LocalDateTime dataHora,
        List<PlayerResponse> timeAmarelo,
        List<PlayerResponse> timePreto,
        List<PlayerResponse> reservas,
        Double mediaAmarelo,
        Double mediaPreto,
        Boolean equilibrado
) {}
