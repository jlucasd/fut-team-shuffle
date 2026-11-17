package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.dto.AniversarioDisparoResponse;
import com.gavioesfc.sorteio.model.entity.NotificacaoEnviada;
import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.repository.DispositivoPushRepository;
import com.gavioesfc.sorteio.repository.NotificacaoEnviadaRepository;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Daily birthday push: tells every registered phone who celebrates today and
 * tomorrow. Runs at most once per day (see {@link NotificacaoEnviada}).
 *
 * <p>The push is due from {@code app.notifications.time} (early morning). It is
 * sent at the first opportunity after that time, so a server that was asleep
 * (e.g. Render free tier) still sends it as soon as it wakes up.
 */
@Service
public class BirthdayNotificationService {

    private static final Logger log = LoggerFactory.getLogger(BirthdayNotificationService.class);

    private final PlayerRepository playerRepository;
    private final DispositivoPushRepository dispositivoRepository;
    private final NotificacaoEnviadaRepository notificacaoEnviadaRepository;
    private final PushNotificationService pushService;
    private final LocalTime sendTime;
    private final Clock clock;

    @Autowired
    public BirthdayNotificationService(PlayerRepository playerRepository,
                                       DispositivoPushRepository dispositivoRepository,
                                       NotificacaoEnviadaRepository notificacaoEnviadaRepository,
                                       PushNotificationService pushService,
                                       @Value("${app.notifications.zone}") String zone,
                                       @Value("${app.notifications.time}") String time) {
        this(playerRepository, dispositivoRepository, notificacaoEnviadaRepository, pushService,
                LocalTime.parse(time), Clock.system(ZoneId.of(zone)));
    }

    BirthdayNotificationService(PlayerRepository playerRepository,
                                DispositivoPushRepository dispositivoRepository,
                                NotificacaoEnviadaRepository notificacaoEnviadaRepository,
                                PushNotificationService pushService,
                                LocalTime sendTime,
                                Clock clock) {
        this.playerRepository = playerRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.notificacaoEnviadaRepository = notificacaoEnviadaRepository;
        this.pushService = pushService;
        this.sendTime = sendTime;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    /**
     * Sends today's push if the configured time has passed and it was not sent
     * yet. Called periodically by the scheduler; cheap when nothing is due.
     */
    @Transactional
    public Optional<AniversarioDisparoResponse> sendDailyIfDue() {
        LocalDateTime now = LocalDateTime.now(clock);
        if (now.toLocalTime().isBefore(sendTime)) {
            return Optional.empty();
        }
        LocalDate today = now.toLocalDate();
        if (notificacaoEnviadaRepository.existsByDataReferencia(today)) {
            return Optional.empty();
        }
        if (!pushService.isConfigured()) {
            log.debug("Aniversários {}: Firebase não configurado, nada enviado.", today);
            return Optional.empty();
        }
        return Optional.of(sendDaily(today));
    }

    @Transactional
    public AniversarioDisparoResponse sendDaily(LocalDate today) {
        LocalDate tomorrow = today.plusDays(1);
        List<Player> players = playerRepository.findAllWithBirthday();
        List<String> hoje = celebrating(players, today);
        List<String> amanha = celebrating(players, tomorrow);

        if (notificacaoEnviadaRepository.existsByDataReferencia(today)) {
            return result(today, "JA_ENVIADO", hoje, amanha, 0, 0);
        }
        if (!pushService.isConfigured()) {
            log.warn("Notificações de aniversário ignoradas: Firebase não configurado (FIREBASE_CREDENTIALS).");
            return result(today, "FIREBASE_NAO_CONFIGURADO", hoje, amanha, 0, 0);
        }
        if (hoje.isEmpty() && amanha.isEmpty()) {
            markSent(today);
            return result(today, "SEM_ANIVERSARIANTES", hoje, amanha, 0, 0);
        }
        if (dispositivoRepository.count() == 0) {
            return result(today, "SEM_DISPOSITIVOS", hoje, amanha, 0, 0);
        }

        PushNotificationService.SendResult sent = pushService.sendToAll(title(hoje), body(hoje, amanha));
        if (sent.enviados() > 0) {
            markSent(today);
        }
        log.info("Aniversários {}: {} push enviado(s), {} falha(s).", today, sent.enviados(), sent.falhas());
        return result(today, "ENVIADO", hoje, amanha, sent.enviados(), sent.falhas());
    }

    private List<String> celebrating(List<Player> players, LocalDate day) {
        return players.stream()
                .filter(p -> BirthdayCalendar.isBirthdayOn(p.getDataNascimento(), day))
                .map(this::label)
                .sorted()
                .toList();
    }

    private String label(Player player) {
        return player.getEquipe() != null
                ? player.getNome() + " (" + player.getEquipe().getNome() + ")"
                : player.getNome();
    }

    private static String title(List<String> hoje) {
        return hoje.isEmpty() ? "🎂 Aniversário amanhã!" : "🎉 Aniversário hoje!";
    }

    private static String body(List<String> hoje, List<String> amanha) {
        List<String> parts = new ArrayList<>();
        if (!hoje.isEmpty()) {
            parts.add("Hoje: " + String.join(", ", hoje));
        }
        if (!amanha.isEmpty()) {
            parts.add("Amanhã: " + String.join(", ", amanha));
        }
        return String.join(" • ", parts);
    }

    private void markSent(LocalDate day) {
        notificacaoEnviadaRepository.save(NotificacaoEnviada.builder().dataReferencia(day).build());
    }

    private static AniversarioDisparoResponse result(LocalDate day, String status, List<String> hoje,
                                                     List<String> amanha, int enviados, int falhas) {
        return new AniversarioDisparoResponse(day, status, hoje, amanha, enviados, falhas);
    }
}
