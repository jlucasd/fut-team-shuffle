package com.gavioesfc.sorteio.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Outcome of a birthday notification run.
 *
 * @param status ENVIADO, JA_ENVIADO, SEM_ANIVERSARIANTES, SEM_DISPOSITIVOS or FIREBASE_NAO_CONFIGURADO
 */
public record AniversarioDisparoResponse(
        LocalDate data,
        String status,
        List<String> hoje,
        List<String> amanha,
        int enviados,
        int falhas
) {}
