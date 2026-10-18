package com.gavioesfc.sorteio.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Initialises Firebase Admin only when a service account is configured
 * ({@code FIREBASE_CREDENTIALS}: the service-account JSON, raw or base64).
 * Without it no {@link FirebaseMessaging} bean exists and push is skipped.
 */
@Configuration
@ConditionalOnExpression("!'${app.firebase.credentials:}'.isBlank()")
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseMessaging firebaseMessaging(@Value("${app.firebase.credentials}") String credentials) throws IOException {
        String json = credentials.trim();
        if (!json.startsWith("{")) {
            json = new String(Base64.getDecoder().decode(json), StandardCharsets.UTF_8);
        }

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(googleCredentials).build();

        FirebaseApp app = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(options)
                : FirebaseApp.getInstance();
        log.info("Firebase inicializado: notificações push habilitadas.");
        return FirebaseMessaging.getInstance(app);
    }
}
