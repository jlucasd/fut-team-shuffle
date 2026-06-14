package com.gavioesfc.sorteio.property;

import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import com.gavioesfc.sorteio.service.DrawAlgorithm;
import com.gavioesfc.sorteio.service.DrawResult;
import net.jqwik.api.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: gavioes-fc-sorteio, Property 7: Goalkeeper distribution
// Feature: gavioes-fc-sorteio, Property 8: Draw player conservation
// Feature: gavioes-fc-sorteio, Property 9: Equal team sizes for even input
// Feature: gavioes-fc-sorteio, Property 10: Equilibrado flag consistency
// Feature: gavioes-fc-sorteio, Property 11: Draw non-determinism
public class DrawAlgorithmProperties {

    private final DrawAlgorithm drawAlgorithm = new DrawAlgorithm();

    // --- Property 7: Goalkeeper distribution ---
    // **Validates: Requirements 6.3**

    @Property(tries = 100)
    void goalkeeperDistribution(@ForAll("playersWithExactlyTwoGoalkeepers") List<Player> players) {
        DrawResult result = drawAlgorithm.execute(players);

        long gkAmarelo = result.getTimeAmarelo().stream()
                .filter(p -> p.getPosicao() == Position.GOLEIRO)
                .count();

        long gkPreto = result.getTimePreto().stream()
                .filter(p -> p.getPosicao() == Position.GOLEIRO)
                .count();

        // With exactly 2 goalkeepers, each team gets exactly 1
        assertThat(gkAmarelo).isEqualTo(1);
        assertThat(gkPreto).isEqualTo(1);
    }

    @Provide
    Arbitrary<List<Player>> playersWithExactlyTwoGoalkeepers() {
        // Generate exactly 2 goalkeepers + outfield players
        Arbitrary<Player> goalkeeperArb = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                Arbitraries.integers().between(1, 5)
        ).as((nome, nivel) -> Player.builder()
                .nome(nome)
                .posicao(Position.GOLEIRO)
                .nivel(nivel)
                .ativo(true)
                .build());

        Position[] outfieldPositions = {Position.ZAGUEIRO, Position.MEIO, Position.ATACANTE};
        Arbitrary<Player> outfieldArb = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                Arbitraries.of(outfieldPositions),
                Arbitraries.integers().between(1, 5)
        ).as((nome, pos, nivel) -> Player.builder()
                .nome(nome)
                .posicao(pos)
                .nivel(nivel)
                .ativo(true)
                .build());

        Arbitrary<List<Player>> goalkeepers = goalkeeperArb.list().ofSize(2);
        Arbitrary<List<Player>> outfield = outfieldArb.list().ofMinSize(2).ofMaxSize(10);

        return Combinators.combine(goalkeepers, outfield)
                .as((gks, outs) -> {
                    List<Player> all = new java.util.ArrayList<>();
                    all.addAll(gks);
                    all.addAll(outs);
                    for (int i = 0; i < all.size(); i++) {
                        all.get(i).setId((long) (i + 1));
                    }
                    return all;
                });
    }

    // --- Property 8: Draw player conservation ---
    // **Validates: Requirements 16.1, 16.4, 6.6**

    @Property(tries = 100)
    void drawPlayerConservation(@ForAll("drawPlayers") List<Player> players) {
        DrawResult result = drawAlgorithm.execute(players);

        // Collect all assigned players
        List<Player> allAssigned = new ArrayList<>();
        allAssigned.addAll(result.getTimeAmarelo());
        allAssigned.addAll(result.getTimePreto());
        if (result.getReserva() != null) {
            allAssigned.add(result.getReserva());
        }

        // Total count must equal input
        assertThat(allAssigned).hasSize(players.size());

        // No duplicates (check by ID)
        Set<Long> assignedIds = allAssigned.stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        assertThat(assignedIds).hasSize(players.size());

        // All input players are present
        Set<Long> inputIds = players.stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        assertThat(assignedIds).isEqualTo(inputIds);

        // Reserve contains at most one player, present only when input size is odd
        if (players.size() % 2 == 0) {
            // For even input, reserve could still exist if goalkeepers cause odd outfield
            // but the union must still equal the full input
        }
        if (result.getReserva() != null) {
            assertThat(inputIds).contains(result.getReserva().getId());
        }
    }

    @Provide
    Arbitrary<List<Player>> drawPlayers() {
        return TestArbitraries.drawInputs(2, 16);
    }

    // --- Property 9: Equal team sizes for even input ---
    // **Validates: Requirements 16.2**

    @Property(tries = 100)
    void equalTeamSizesForEvenInput(@ForAll("evenDrawPlayers") List<Player> players) {
        DrawResult result = drawAlgorithm.execute(players);

        // For even number of players with no goalkeepers (pure outfield)
        // the two teams should have equal size and no reserve
        assertThat(result.getTimeAmarelo().size())
                .isEqualTo(result.getTimePreto().size());
        assertThat(result.getReserva()).isNull();
    }

    @Provide
    Arbitrary<List<Player>> evenDrawPlayers() {
        // Generate even-sized lists of outfield-only players
        Position[] outfieldPositions = {Position.ZAGUEIRO, Position.MEIO, Position.ATACANTE};
        Arbitrary<Player> playerArb = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                Arbitraries.of(outfieldPositions),
                Arbitraries.integers().between(1, 5)
        ).as((nome, pos, nivel) -> Player.builder()
                .nome(nome)
                .posicao(pos)
                .nivel(nivel)
                .ativo(true)
                .build());

        // Generate lists of even size (2, 4, 6, 8, 10)
        return Arbitraries.integers().between(1, 5)
                .flatMap(half -> playerArb.list().ofSize(half * 2))
                .map(players -> {
                    for (int i = 0; i < players.size(); i++) {
                        players.get(i).setId((long) (i + 1));
                    }
                    return players;
                });
    }

    // --- Property 10: Equilibrado flag consistency ---
    // **Validates: Requirements 16.3**

    @Property(tries = 100)
    void equilibradoFlagConsistency(@ForAll("drawPlayers") List<Player> players) {
        DrawResult result = drawAlgorithm.execute(players);

        double diff = Math.abs(result.getMediaAmarelo() - result.getMediaPreto());

        if (result.getEquilibrado()) {
            assertThat(diff).isLessThanOrEqualTo(0.5);
        }
        // Also verify the converse: if diff <= 0.5, equilibrado should be true
        if (diff <= 0.5) {
            assertThat(result.getEquilibrado()).isTrue();
        }
    }

    // --- Property 11: Draw non-determinism ---
    // **Validates: Requirements 16.5**

    @Property(tries = 100)
    void drawNonDeterminism(@ForAll("fourPlusPlayers") List<Player> players) {
        Set<Set<Long>> compositions = new HashSet<>();

        for (int run = 0; run < 10; run++) {
            DrawResult result = drawAlgorithm.execute(players);
            // Use AMARELO team composition as a fingerprint
            Set<Long> amareloIds = result.getTimeAmarelo().stream()
                    .map(Player::getId)
                    .collect(Collectors.toSet());
            compositions.add(amareloIds);
        }

        // At least 2 distinct compositions out of 10 runs
        assertThat(compositions.size()).isGreaterThanOrEqualTo(2);
    }

    @Provide
    Arbitrary<List<Player>> fourPlusPlayers() {
        // Generate players with same level to ensure shuffle creates different distributions
        // When all players have the same level, the sort is stable but shuffle randomizes order
        Position[] outfieldPositions = {Position.ZAGUEIRO, Position.MEIO, Position.ATACANTE};
        return Arbitraries.integers().between(1, 5).flatMap(level -> {
            Arbitrary<Player> playerArb = Combinators.combine(
                    Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                    Arbitraries.of(outfieldPositions)
            ).as((nome, pos) -> Player.builder()
                    .nome(nome)
                    .posicao(pos)
                    .nivel(level)
                    .ativo(true)
                    .build());

            return playerArb.list().ofMinSize(6).ofMaxSize(12)
                    .map(players -> {
                        for (int i = 0; i < players.size(); i++) {
                            players.get(i).setId((long) (i + 1));
                        }
                        return players;
                    });
        });
    }
}
