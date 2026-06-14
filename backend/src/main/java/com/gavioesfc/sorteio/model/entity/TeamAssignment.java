package com.gavioesfc.sorteio.model.entity;

import com.gavioesfc.sorteio.model.enums.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draw_id")
    private Draw draw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @Enumerated(EnumType.STRING)
    private Team team;
}
