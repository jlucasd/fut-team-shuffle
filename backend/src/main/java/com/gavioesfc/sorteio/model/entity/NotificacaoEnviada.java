package com.gavioesfc.sorteio.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Marks a day whose birthday notifications were already sent, so the internal
 * scheduler and the external cron trigger never notify twice on the same day.
 */
@Entity
@Table(name = "notificacoes_enviadas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoEnviada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate dataReferencia;

    private LocalDateTime enviadaEm;

    @PrePersist
    protected void onCreate() {
        this.enviadaEm = LocalDateTime.now();
    }
}
