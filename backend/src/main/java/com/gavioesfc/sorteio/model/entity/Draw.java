package com.gavioesfc.sorteio.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "draws")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Draw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHora;

    private Double mediaAmarelo;

    private Double mediaPreto;

    private Boolean equilibrado;

    @OneToMany(mappedBy = "draw", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamAssignment> assignments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }
}
