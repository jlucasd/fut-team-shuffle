package com.gavioesfc.sorteio.controller;

import com.gavioesfc.sorteio.dto.AniversarioDisparoResponse;
import com.gavioesfc.sorteio.dto.DispositivoRequest;
import com.gavioesfc.sorteio.dto.NotificacaoStatusResponse;
import com.gavioesfc.sorteio.exception.BusinessRuleException;
import com.gavioesfc.sorteio.model.entity.DispositivoPush;
import com.gavioesfc.sorteio.repository.DispositivoPushRepository;
import com.gavioesfc.sorteio.service.BirthdayNotificationService;
import com.gavioesfc.sorteio.service.PushNotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final DispositivoPushRepository dispositivoRepository;
    private final PushNotificationService pushService;
    private final BirthdayNotificationService birthdayNotificationService;
    private final String cronSecret;

    public NotificacaoController(DispositivoPushRepository dispositivoRepository,
                                 PushNotificationService pushService,
                                 BirthdayNotificationService birthdayNotificationService,
                                 @Value("${app.notifications.cron-secret:}") String cronSecret) {
        this.dispositivoRepository = dispositivoRepository;
        this.pushService = pushService;
        this.birthdayNotificationService = birthdayNotificationService;
        this.cronSecret = cronSecret;
    }

    @GetMapping("/status")
    public ResponseEntity<NotificacaoStatusResponse> status() {
        return ResponseEntity.ok(new NotificacaoStatusResponse(pushService.isConfigured(), dispositivoRepository.count()));
    }

    /** Registers (or refreshes) a phone's FCM token. */
    @PostMapping("/dispositivos")
    @Transactional
    public ResponseEntity<Void> register(@Valid @RequestBody DispositivoRequest request) {
        DispositivoPush dispositivo = dispositivoRepository.findByToken(request.token())
                .orElseGet(() -> DispositivoPush.builder().token(request.token()).build());
        dispositivo.setPlataforma(request.plataforma());
        dispositivoRepository.save(dispositivo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/dispositivos")
    @Transactional
    public ResponseEntity<Void> unregister(@Valid @RequestBody DispositivoRequest request) {
        dispositivoRepository.deleteByToken(request.token());
        return ResponseEntity.noContent().build();
    }

    /** Sends a test push to every registered phone. */
    @PostMapping("/teste")
    public ResponseEntity<PushNotificationService.SendResult> test() {
        if (!pushService.isConfigured()) {
            throw new BusinessRuleException("Firebase não configurado no servidor (FIREBASE_CREDENTIALS).");
        }
        return ResponseEntity.ok(pushService.sendToAll("🔔 Gaviões FC", "As notificações de aniversário estão funcionando!"));
    }

    /**
     * Daily trigger for an external cron (keeps working when the host sleeps).
     * Public, but requires the {@code X-Cron-Secret} header to match {@code CRON_SECRET}.
     */
    @PostMapping("/aniversarios/disparar")
    public ResponseEntity<AniversarioDisparoResponse> trigger(
            @RequestHeader(value = "X-Cron-Secret", required = false) String secret) {
        if (cronSecret.isBlank() || secret == null || !MessageDigest.isEqual(
                cronSecret.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(birthdayNotificationService.sendDaily(birthdayNotificationService.today()));
    }
}
