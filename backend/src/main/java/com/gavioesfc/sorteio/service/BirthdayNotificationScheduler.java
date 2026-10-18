package com.gavioesfc.sorteio.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Fires the daily birthday push while the server is running. Hosts that put
 * the app to sleep (e.g. Render free tier) should also call
 * {@code POST /api/notificacoes/aniversarios/disparar} from an external cron.
 */
@Component
public class BirthdayNotificationScheduler {

    private final BirthdayNotificationService birthdayNotificationService;

    public BirthdayNotificationScheduler(BirthdayNotificationService birthdayNotificationService) {
        this.birthdayNotificationService = birthdayNotificationService;
    }

    @Scheduled(cron = "${app.notifications.cron}", zone = "${app.notifications.zone}")
    public void sendDailyBirthdayNotifications() {
        birthdayNotificationService.sendDaily(birthdayNotificationService.today());
    }
}
