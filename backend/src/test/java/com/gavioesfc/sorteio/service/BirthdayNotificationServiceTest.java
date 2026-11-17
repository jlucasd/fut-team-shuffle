package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.dto.AniversarioDisparoResponse;
import com.gavioesfc.sorteio.model.entity.Equipe;
import com.gavioesfc.sorteio.model.entity.NotificacaoEnviada;
import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.repository.DispositivoPushRepository;
import com.gavioesfc.sorteio.repository.NotificacaoEnviadaRepository;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirthdayNotificationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalTime SEND_TIME = LocalTime.of(7, 0);

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DispositivoPushRepository dispositivoRepository;

    @Mock
    private NotificacaoEnviadaRepository notificacaoEnviadaRepository;

    @Mock
    private PushNotificationService pushService;

    private BirthdayNotificationService service;

    @BeforeEach
    void setUp() {
        service = serviceAt(LocalTime.of(7, 30));
    }

    /** Service whose clock is frozen at TODAY + the given local time. */
    private BirthdayNotificationService serviceAt(LocalTime localTime) {
        Clock clock = Clock.fixed(ZonedDateTime.of(TODAY, localTime, ZONE).toInstant(), ZONE);
        return new BirthdayNotificationService(playerRepository, dispositivoRepository,
                notificacaoEnviadaRepository, pushService, SEND_TIME, clock);
    }

    private Player player(String nome, int month, int day, String equipe) {
        return Player.builder()
                .nome(nome)
                .dataNascimento(LocalDate.of(2000, month, day))
                .equipe(Equipe.builder().nome(equipe).build())
                .build();
    }

    @Test
    void sendsTodayAndTomorrowBirthdaysToAllDevicesOncePerDay() {
        when(playerRepository.findAllWithBirthday()).thenReturn(List.of(
                player("Jair", 10, 22, "Gaviões FC"),
                player("Ana", 10, 23, "Veteranos"),
                player("Robson", 3, 3, "Gaviões FC")));
        when(pushService.isConfigured()).thenReturn(true);
        when(dispositivoRepository.count()).thenReturn(2L);
        when(pushService.sendToAll(anyString(), anyString())).thenReturn(new PushNotificationService.SendResult(2, 0));

        AniversarioDisparoResponse response = service.sendDaily(TODAY);

        assertThat(response.status()).isEqualTo("ENVIADO");
        assertThat(response.hoje()).containsExactly("Jair (Gaviões FC)");
        assertThat(response.amanha()).containsExactly("Ana (Veteranos)");
        verify(pushService).sendToAll("🎉 Aniversário hoje!", "Hoje: Jair (Gaviões FC) • Amanhã: Ana (Veteranos)");
        verify(notificacaoEnviadaRepository).save(any(NotificacaoEnviada.class));
    }

    @Test
    void doesNotSendTwiceOnTheSameDay() {
        when(playerRepository.findAllWithBirthday()).thenReturn(List.of(player("Jair", 10, 22, "Gaviões FC")));
        when(notificacaoEnviadaRepository.existsByDataReferencia(TODAY)).thenReturn(true);

        assertThat(service.sendDaily(TODAY).status()).isEqualTo("JA_ENVIADO");
        verify(pushService, never()).sendToAll(anyString(), anyString());
    }

    @Test
    void skipsWithoutMarkingWhenFirebaseIsNotConfigured() {
        when(playerRepository.findAllWithBirthday()).thenReturn(List.of(player("Jair", 10, 22, "Gaviões FC")));
        when(pushService.isConfigured()).thenReturn(false);

        assertThat(service.sendDaily(TODAY).status()).isEqualTo("FIREBASE_NAO_CONFIGURADO");
        verify(notificacaoEnviadaRepository, never()).save(any());
    }

    @Test
    void todayFollowsTheConfiguredZone() {
        assertThat(service.today()).isEqualTo(TODAY);
    }

    @Test
    void doesNothingBeforeTheConfiguredTime() {
        assertThat(serviceAt(LocalTime.of(6, 59)).sendDailyIfDue()).isEmpty();
        verifyNoInteractions(playerRepository, pushService);
    }

    @Test
    void sendsAtTheFirstCheckAfterTheConfiguredTime() {
        when(playerRepository.findAllWithBirthday()).thenReturn(List.of(player("Jair", 10, 22, "Gaviões FC")));
        when(pushService.isConfigured()).thenReturn(true);
        when(dispositivoRepository.count()).thenReturn(1L);
        when(pushService.sendToAll(anyString(), anyString())).thenReturn(new PushNotificationService.SendResult(1, 0));

        // 11:40: the server was asleep at 07:00 and only woke up now
        Optional<AniversarioDisparoResponse> response = serviceAt(LocalTime.of(11, 40)).sendDailyIfDue();

        assertThat(response).isPresent();
        assertThat(response.get().status()).isEqualTo("ENVIADO");
        verify(notificacaoEnviadaRepository).save(any(NotificacaoEnviada.class));
    }

    @Test
    void dueCheckIsCheapWhenAlreadySentToday() {
        when(notificacaoEnviadaRepository.existsByDataReferencia(TODAY)).thenReturn(true);

        assertThat(service.sendDailyIfDue()).isEmpty();
        verifyNoInteractions(playerRepository, pushService);
    }

    @Test
    void dueCheckIsSilentWithoutFirebase() {
        when(pushService.isConfigured()).thenReturn(false);

        assertThat(service.sendDailyIfDue()).isEmpty();
        verifyNoInteractions(playerRepository);
        verify(notificacaoEnviadaRepository, never()).save(any());
    }
}
