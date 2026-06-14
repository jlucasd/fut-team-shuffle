package com.gavioesfc.sorteio.dto;

import java.util.List;

public record TeamEditRequest(
        List<Long> timeAmarelo,
        List<Long> timePreto,
        Long reserva
) {}
