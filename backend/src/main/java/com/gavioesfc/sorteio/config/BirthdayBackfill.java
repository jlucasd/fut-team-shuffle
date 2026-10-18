package com.gavioesfc.sorteio.config;

import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import com.gavioesfc.sorteio.service.EquipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One-time, idempotent backfill of members' birthdays taken from the club's
 * "Aniversariantes Sócios Time Gaviões" list. Only the day/month is meaningful,
 * so a neutral year (2000) is used and only the day/month is ever displayed.
 *
 * Runs on every startup but only fills players whose {@code dataNascimento} is
 * still empty, so it never overwrites data edited through the app. Matching is
 * done by name (case-insensitive) so it works regardless of database IDs, and
 * only within the default equipe (the club the list belongs to), so members of
 * other teams with the same name are never touched.
 */
@Component
@Order(1)
public class BirthdayBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BirthdayBackfill.class);
    private static final int NEUTRAL_YEAR = 2000;

    /** Player name -> birthday (day/month). */
    private static final Map<String, MonthDay> BIRTHDAYS = new LinkedHashMap<>();

    static {
        BIRTHDAYS.put("Valmir", MonthDay.of(2, 27));
        BIRTHDAYS.put("Robson", MonthDay.of(3, 3));
        BIRTHDAYS.put("Ramon", MonthDay.of(3, 16));
        BIRTHDAYS.put("Benicio", MonthDay.of(6, 6));
        BIRTHDAYS.put("Alex", MonthDay.of(8, 27));
        BIRTHDAYS.put("Jair", MonthDay.of(10, 23));
        BIRTHDAYS.put("Vanderlei", MonthDay.of(11, 4));
        BIRTHDAYS.put("João Lucas", MonthDay.of(12, 22));
    }

    private final PlayerRepository playerRepository;
    private final EquipeService equipeService;

    public BirthdayBackfill(PlayerRepository playerRepository, EquipeService equipeService) {
        this.playerRepository = playerRepository;
        this.equipeService = equipeService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long defaultEquipeId = equipeService.getOrCreateDefault().getId();
        List<Player> players = playerRepository.findAll();
        int updated = 0;

        for (Player player : players) {
            if (player.getDataNascimento() != null || player.getNome() == null) {
                continue;
            }
            if (player.getEquipe() == null || !defaultEquipeId.equals(player.getEquipe().getId())) {
                continue;
            }
            MonthDay md = lookup(player.getNome());
            if (md == null) {
                continue;
            }
            player.setDataNascimento(LocalDate.of(NEUTRAL_YEAR, md.getMonth(), md.getDayOfMonth()));
            playerRepository.save(player);
            updated++;
        }

        if (updated > 0) {
            log.info("BirthdayBackfill: {} jogador(es) tiveram a data de nascimento preenchida.", updated);
        }
    }

    private MonthDay lookup(String nome) {
        String target = nome.trim();
        for (Map.Entry<String, MonthDay> entry : BIRTHDAYS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(target)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
