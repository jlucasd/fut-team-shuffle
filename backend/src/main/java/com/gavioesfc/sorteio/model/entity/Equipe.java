package com.gavioesfc.sorteio.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A club/team whose members are registered in the app. Players, draws and
 * birthdays are scoped to an equipe. Not to be confused with
 * {@link com.gavioesfc.sorteio.model.enums.Team}, which is the side
 * (amarelo/preto/reserva) a player gets in a draw.
 */
@Entity
@Table(name = "equipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
