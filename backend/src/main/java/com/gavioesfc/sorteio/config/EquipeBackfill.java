package com.gavioesfc.sorteio.config;

import com.gavioesfc.sorteio.model.entity.Equipe;
import com.gavioesfc.sorteio.repository.DrawRepository;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import com.gavioesfc.sorteio.service.EquipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Migrates data created before multi-team support: every player and draw
 * without an equipe is assigned to the default equipe ("Gaviões FC").
 *
 * Idempotent and non-destructive: it only touches rows whose {@code equipe_id}
 * is still null, so it is safe to run on every startup.
 */
@Component
@Order(0)
public class EquipeBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EquipeBackfill.class);

    private final EquipeService equipeService;
    private final PlayerRepository playerRepository;
    private final DrawRepository drawRepository;

    public EquipeBackfill(EquipeService equipeService,
                          PlayerRepository playerRepository,
                          DrawRepository drawRepository) {
        this.equipeService = equipeService;
        this.playerRepository = playerRepository;
        this.drawRepository = drawRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Equipe defaultEquipe = equipeService.getOrCreateDefault();
        int players = playerRepository.assignEquipeWhereMissing(defaultEquipe);
        int draws = drawRepository.assignEquipeWhereMissing(defaultEquipe);

        if (players > 0 || draws > 0) {
            log.info("EquipeBackfill: {} jogador(es) e {} sorteio(s) associados ao time '{}'.",
                    players, draws, defaultEquipe.getNome());
        }
    }
}
