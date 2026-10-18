package com.gavioesfc.sorteio.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** A phone (FCM registration token) that receives birthday push notifications. */
@Entity
@Table(name = "dispositivos_push")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoPush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 1024)
    private String token;

    private String plataforma;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
