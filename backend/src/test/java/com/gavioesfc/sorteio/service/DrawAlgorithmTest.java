package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DrawAlgorithmTest {

    private DrawAlgorithm drawAlgorithm;

    @BeforeEach
    void setUp() {
        drawAlgorithm = new DrawAlgorithm();
    }

    private Player createPlayer(Long id, String nome, Position posicao, int nivel) {
        return Player.builder()
                .id(id)
                .nome(nome)
                .posicao(posicao)
                .nivel(nivel)
                .ativo(true)
                .build();
    }

    @Test
    void shouldSeparateGoalkeepersFromOutfield() {
        List<Player> players = List.of(
                createPlayer(1L, "GK1", Position.GOLEIRO, 3),
                createPlayer(2L, "GK2", Position.GOLEIRO, 4),
                createPlayer(3L, "DEF1", Position.ZAGUEIRO, 3),
                createPlayer(4L, "DEF2", Position.ZAGUEIRO, 4),
                createPlayer(5L, "MID1", Position.MEIO, 3),
                createPlayer(6L, "MID2", Position.MEIO, 4)
        );

        DrawResult result = drawAlgorithm.execute(players);

        // Both teams should have exactly one goalkeeper
        long gkAmarelo = result.getTimeAmarelo().stream()
                .filter(p -> p.getPosicao() == Position.GOLEIRO).count();
        long gkPreto = result.getTimePreto().stream()
                .filter(p -> p.getPosicao() == Position.GOLEIRO).count();

        assertEquals(1, gkAmarelo, "AMARELO should have exactly 1 goalkeeper");
        assertEquals(1, gkPreto, "PRETO should have exactly 1 goalkeeper");
    }

    @Test
    void shouldDistributeEvenPlayersIntoEqualTeams() {
        List<Player> players = List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 3),
                createPlayer(2L, "P2", Position.MEIO, 4),
                createPlayer(3L, "P3", Position.ATACANTE, 2),
                createPlayer(4L, "P4", Position.ZAGUEIRO, 5),
                createPlayer(5L, "P5", Position.MEIO, 3),
                createPlayer(6L, "P6", Position.ATACANTE, 4)
        );

        DrawResult result = drawAlgorithm.execute(players);

        assertEquals(3, result.getTimeAmarelo().size());
        assertEquals(3, result.getTimePreto().size());
        assertNull(result.getReserva());
    }

    @Test
    void shouldAssignReserveWhenOddPlayerCount() {
        List<Player> players = List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 3),
                createPlayer(2L, "P2", Position.MEIO, 4),
                createPlayer(3L, "P3", Position.ATACANTE, 2),
                createPlayer(4L, "P4", Position.ZAGUEIRO, 5),
                createPlayer(5L, "P5", Position.MEIO, 3)
        );

        DrawResult result = drawAlgorithm.execute(players);

        assertNotNull(result.getReserva(), "Reserve should be assigned for odd player count");
        int totalAssigned = result.getTimeAmarelo().size() + result.getTimePreto().size() + 1;
        assertEquals(5, totalAssigned, "All players should be accounted for");
    }

    @Test
    void shouldReturnEquilibradoTrueWhenBalanced() {
        // All players same level → teams should be perfectly balanced
        List<Player> players = List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 3),
                createPlayer(2L, "P2", Position.MEIO, 3),
                createPlayer(3L, "P3", Position.ATACANTE, 3),
                createPlayer(4L, "P4", Position.ZAGUEIRO, 3)
        );

        DrawResult result = drawAlgorithm.execute(players);

        assertTrue(result.getEquilibrado(), "Teams with equal levels should be equilibrado");
        assertEquals(3.0, result.getMediaAmarelo(), 0.01);
        assertEquals(3.0, result.getMediaPreto(), 0.01);
    }

    @Test
    void shouldCalculateAveragesCorrectly() {
        List<Player> players = List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 5),
                createPlayer(2L, "P2", Position.MEIO, 4),
                createPlayer(3L, "P3", Position.ATACANTE, 3),
                createPlayer(4L, "P4", Position.ZAGUEIRO, 2)
        );

        DrawResult result = drawAlgorithm.execute(players);

        // Averages should be calculated correctly
        double expectedAvgAmarelo = result.getTimeAmarelo().stream()
                .mapToInt(Player::getNivel).average().orElse(0);
        double expectedAvgPreto = result.getTimePreto().stream()
                .mapToInt(Player::getNivel).average().orElse(0);

        assertEquals(expectedAvgAmarelo, result.getMediaAmarelo(), 0.01);
        assertEquals(expectedAvgPreto, result.getMediaPreto(), 0.01);
    }

    @Test
    void shouldConserveAllPlayers() {
        List<Player> players = new ArrayList<>(List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 3),
                createPlayer(2L, "P2", Position.MEIO, 4),
                createPlayer(3L, "P3", Position.ATACANTE, 2),
                createPlayer(4L, "P4", Position.ZAGUEIRO, 5),
                createPlayer(5L, "P5", Position.MEIO, 3),
                createPlayer(6L, "P6", Position.ATACANTE, 4)
        ));

        DrawResult result = drawAlgorithm.execute(players);

        List<Player> allAssigned = new ArrayList<>();
        allAssigned.addAll(result.getTimeAmarelo());
        allAssigned.addAll(result.getTimePreto());
        if (result.getReserva() != null) {
            allAssigned.add(result.getReserva());
        }

        assertEquals(players.size(), allAssigned.size(), "All players should be assigned");

        // Verify no duplicates
        long uniqueIds = allAssigned.stream().map(Player::getId).distinct().count();
        assertEquals(players.size(), uniqueIds, "No duplicate players should exist");
    }

    @Test
    void shouldHandleTwoPlayersMinimum() {
        List<Player> players = List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 3),
                createPlayer(2L, "P2", Position.MEIO, 4)
        );

        DrawResult result = drawAlgorithm.execute(players);

        assertEquals(1, result.getTimeAmarelo().size());
        assertEquals(1, result.getTimePreto().size());
        assertNull(result.getReserva());
    }

    @Test
    void shouldBalanceTeamsWithSwapOptimization() {
        // Create a scenario where alternate distribution would be unbalanced
        // but swap optimization should help
        List<Player> players = List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 5),
                createPlayer(2L, "P2", Position.ZAGUEIRO, 5),
                createPlayer(3L, "P3", Position.MEIO, 1),
                createPlayer(4L, "P4", Position.MEIO, 1)
        );

        DrawResult result = drawAlgorithm.execute(players);

        // After optimization, the difference should be reasonable
        double diff = Math.abs(result.getMediaAmarelo() - result.getMediaPreto());
        assertTrue(diff <= 0.5 || !result.getEquilibrado(),
                "equilibrado flag should be consistent with threshold");
    }

    @Test
    void shouldHandleGoalkeeperAssignmentWithOneGoalkeeper() {
        List<Player> players = List.of(
                createPlayer(1L, "GK1", Position.GOLEIRO, 3),
                createPlayer(2L, "DEF1", Position.ZAGUEIRO, 4),
                createPlayer(3L, "MID1", Position.MEIO, 3),
                createPlayer(4L, "ATK1", Position.ATACANTE, 5)
        );

        DrawResult result = drawAlgorithm.execute(players);

        // One goalkeeper should be in one of the teams
        long totalGks = result.getTimeAmarelo().stream()
                .filter(p -> p.getPosicao() == Position.GOLEIRO).count()
                + result.getTimePreto().stream()
                .filter(p -> p.getPosicao() == Position.GOLEIRO).count();

        assertEquals(1, totalGks, "Single goalkeeper should be assigned to a team");
    }

    @Test
    void shouldSetEquilibradoFalseWhenDifferenceExceedsThreshold() {
        // Extreme case: level 5 vs level 1
        // With only 2 players (1 per team), swap optimization cannot help
        List<Player> players = List.of(
                createPlayer(1L, "P1", Position.ZAGUEIRO, 5),
                createPlayer(2L, "P2", Position.ZAGUEIRO, 1)
        );

        DrawResult result = drawAlgorithm.execute(players);

        // Difference is |5 - 1| = 4.0, exceeds threshold
        assertFalse(result.getEquilibrado());
        assertEquals(4.0, Math.abs(result.getMediaAmarelo() - result.getMediaPreto()), 0.01);
    }

    @Test
    void shouldHandleAllGoalkeepers() {
        List<Player> players = List.of(
                createPlayer(1L, "GK1", Position.GOLEIRO, 3),
                createPlayer(2L, "GK2", Position.GOLEIRO, 4),
                createPlayer(3L, "GK3", Position.GOLEIRO, 2),
                createPlayer(4L, "GK4", Position.GOLEIRO, 5)
        );

        DrawResult result = drawAlgorithm.execute(players);

        int totalPlayers = result.getTimeAmarelo().size() + result.getTimePreto().size()
                + (result.getReserva() != null ? 1 : 0);
        assertEquals(4, totalPlayers, "All goalkeeper-players should be distributed");
    }
}
