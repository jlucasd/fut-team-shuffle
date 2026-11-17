package com.gavioesfc.sorteio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Checks every few minutes (and shortly after start-up) whether today's
 * birthday push is due and not yet sent. Because it runs right after boot, a
 * host that sleeps (e.g. Render free tier) sends the push as soon as anything
 * wakes it up after the configured time, e.g. the keep-alive ping.
 * {@code POST /api/notificacoes/aniversarios/disparar} remains available for an
 * external cron.
 */
@Component
public class BirthdayNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(BirthdayNotificationScheduler.class);

    private final BirthdayNotificationService birthdayNotificationService;

    public BirthdayNotificationScheduler(BirthdayNotificationService birthdayNotificationService) {
        this.birthdayNotificationService = birthdayNotificationService;
    }

    @Scheduled(initialDelayString = "PT15S", fixedDelayString = "PT5M")
    public void sendDailyBirthdayNotifications() {
        try {
            birthdayNotificationService.sendDailyIfDue()
                    .ifPresent(r -> log.info("Aniversários {}: {} ({} enviado(s)).", r.data(), r.status(), r.enviados()));
        } catch (RuntimeException e) {
            // Keep the scheduler alive; the next tick tries again
            log.error("Falha ao verificar notificações de aniversário", e);
        }
    }
}
