package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.model.entity.DispositivoPush;
import com.gavioesfc.sorteio.repository.DispositivoPushRepository;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Sends FCM notifications to every registered device and prunes dead tokens. */
@Service
public class PushNotificationService {

    /** FCM accepts at most 500 tokens per multicast request. */
    private static final int MULTICAST_LIMIT = 500;

    private static final Set<MessagingErrorCode> INVALID_TOKEN_ERRORS =
            Set.of(MessagingErrorCode.UNREGISTERED, MessagingErrorCode.INVALID_ARGUMENT);

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final ObjectProvider<FirebaseMessaging> firebaseMessaging;
    private final DispositivoPushRepository dispositivoRepository;

    public PushNotificationService(ObjectProvider<FirebaseMessaging> firebaseMessaging,
                                   DispositivoPushRepository dispositivoRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.dispositivoRepository = dispositivoRepository;
    }

    public boolean isConfigured() {
        return firebaseMessaging.getIfAvailable() != null;
    }

    public record SendResult(int enviados, int falhas) {}

    @Transactional
    public SendResult sendToAll(String title, String body) {
        FirebaseMessaging messaging = firebaseMessaging.getIfAvailable();
        if (messaging == null) {
            throw new IllegalStateException("Firebase não configurado");
        }

        List<String> tokens = dispositivoRepository.findAll().stream().map(DispositivoPush::getToken).toList();
        int enviados = 0;
        int falhas = 0;
        List<String> invalidTokens = new ArrayList<>();

        for (int start = 0; start < tokens.size(); start += MULTICAST_LIMIT) {
            List<String> chunk = tokens.subList(start, Math.min(start + MULTICAST_LIMIT, tokens.size()));
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(chunk)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder().setChannelId("aniversarios").build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setSound("default").build())
                            .build())
                    .build();
            try {
                BatchResponse response = messaging.sendEachForMulticast(message);
                enviados += response.getSuccessCount();
                falhas += response.getFailureCount();
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    FirebaseMessagingException error = responses.get(i).getException();
                    if (error != null && INVALID_TOKEN_ERRORS.contains(error.getMessagingErrorCode())) {
                        invalidTokens.add(chunk.get(i));
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.error("Falha ao enviar notificações push", e);
                falhas += chunk.size();
            }
        }

        if (!invalidTokens.isEmpty()) {
            dispositivoRepository.deleteByTokenIn(invalidTokens);
            log.info("{} dispositivo(s) com token inválido removido(s).", invalidTokens.size());
        }
        return new SendResult(enviados, falhas);
    }
}
