package com.gavioesfc.sorteio.dto;

public record NotificacaoStatusResponse(
        boolean firebaseConfigurado,
        long dispositivos
) {}
