package com.gavioesfc.sorteio.property;

import com.gavioesfc.sorteio.dto.PlayerCreateRequest;
import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.util.List;

/**
 * Custom jqwik arbitraries for property-based tests.
 * Provides generators for valid/invalid players and draw inputs.
 */
public class TestArbitraries {

    private static final Position[] ALL_POSITIONS = Position.values();

    /**
     * Generates valid PlayerCreateRequest objects with:
     * - nome: alphabetic string 3-20 chars
     * - posicao: random valid Position
     * - nivel: integer 1-5
     * - ativo: true (for draw-ready players)
     */
    public static Arbitrary<PlayerCreateRequest> validPlayers() {
        Arbitrary<String> nomes = Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(20);

        Arbitrary<Position> positions = Arbitraries.of(ALL_POSITIONS);

        Arbitrary<Integer> niveis = Arbitraries.integers().between(1, 5);

        Arbitrary<Boolean> ativos = Arbitraries.of(true);

        return Combinators.combine(nomes, positions, niveis, ativos)
                .as(PlayerCreateRequest::new);
    }

    /**
     * Generates valid PlayerCreateRequest objects with any ativo status.
     */
    public static Arbitrary<PlayerCreateRequest> validPlayersAnyStatus() {
        Arbitrary<String> nomes = Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(20);

        Arbitrary<Position> positions = Arbitraries.of(ALL_POSITIONS);

        Arbitrary<Integer> niveis = Arbitraries.integers().between(1, 5);

        Arbitrary<Boolean> ativos = Arbitraries.of(true, false);

        return Combinators.combine(nomes, positions, niveis, ativos)
                .as(PlayerCreateRequest::new);
    }

    /**
     * Generates invalid PlayerCreateRequest objects with at least one invalid field:
     * - blank/empty nome
     * - nivel outside 1-5 range
     * - null required fields
     */
    public static Arbitrary<PlayerCreateRequest> invalidPlayers() {
        return Arbitraries.oneOf(
                // Blank nome
                blankNomePlayers(),
                // Nivel out of range (0 or 6+)
                invalidNivelPlayers(),
                // Null posicao
                nullPosicaoPlayers(),
                // Null nivel
                nullNivelPlayers(),
                // Null ativo
                nullAtivoPlayers()
        );
    }

    private static Arbitrary<PlayerCreateRequest> blankNomePlayers() {
        Arbitrary<String> blankNomes = Arbitraries.of("", "   ", " ");
        Arbitrary<Position> positions = Arbitraries.of(ALL_POSITIONS);
        Arbitrary<Integer> niveis = Arbitraries.integers().between(1, 5);

        return Combinators.combine(blankNomes, positions, niveis)
                .as((nome, pos, nivel) -> new PlayerCreateRequest(nome, pos, nivel, true));
    }

    private static Arbitrary<PlayerCreateRequest> invalidNivelPlayers() {
        Arbitrary<String> nomes = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
        Arbitrary<Position> positions = Arbitraries.of(ALL_POSITIONS);
        Arbitrary<Integer> invalidNiveis = Arbitraries.oneOf(
                Arbitraries.integers().between(-10, 0),
                Arbitraries.integers().between(6, 100)
        );

        return Combinators.combine(nomes, positions, invalidNiveis)
                .as((nome, pos, nivel) -> new PlayerCreateRequest(nome, pos, nivel, true));
    }

    private static Arbitrary<PlayerCreateRequest> nullPosicaoPlayers() {
        Arbitrary<String> nomes = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
        Arbitrary<Integer> niveis = Arbitraries.integers().between(1, 5);

        return Combinators.combine(nomes, niveis)
                .as((nome, nivel) -> new PlayerCreateRequest(nome, null, nivel, true));
    }

    private static Arbitrary<PlayerCreateRequest> nullNivelPlayers() {
        Arbitrary<String> nomes = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
        Arbitrary<Position> positions = Arbitraries.of(ALL_POSITIONS);

        return Combinators.combine(nomes, positions)
                .as((nome, pos) -> new PlayerCreateRequest(nome, pos, null, true));
    }

    private static Arbitrary<PlayerCreateRequest> nullAtivoPlayers() {
        Arbitrary<String> nomes = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
        Arbitrary<Position> positions = Arbitraries.of(ALL_POSITIONS);
        Arbitrary<Integer> niveis = Arbitraries.integers().between(1, 5);

        return Combinators.combine(nomes, positions, niveis)
                .as((nome, pos, nivel) -> new PlayerCreateRequest(nome, pos, nivel, null));
    }

    /**
     * Generates lists of valid Player entities for draw algorithm testing.
     * Players have unique IDs, valid positions, and nivel 1-5.
     *
     * @param min minimum number of players (inclusive)
     * @param max maximum number of players (inclusive)
     */
    public static Arbitrary<List<Player>> drawInputs(int min, int max) {
        Arbitrary<Player> playerArb = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                Arbitraries.of(ALL_POSITIONS),
                Arbitraries.integers().between(1, 5)
        ).as((nome, pos, nivel) -> Player.builder()
                .nome(nome)
                .posicao(pos)
                .nivel(nivel)
                .ativo(true)
                .build());

        return playerArb.list().ofMinSize(min).ofMaxSize(max)
                .map(players -> {
                    // Assign unique IDs
                    for (int i = 0; i < players.size(); i++) {
                        players.get(i).setId((long) (i + 1));
                    }
                    return players;
                });
    }

    /**
     * Generates lists of valid Player entities with at least 2 goalkeepers.
     *
     * @param min minimum number of outfield players
     * @param max maximum number of outfield players
     */
    public static Arbitrary<List<Player>> drawInputsWithGoalkeepers(int min, int max) {
        // Generate at least 2 goalkeepers
        Arbitrary<Player> goalkeeperArb = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                Arbitraries.integers().between(1, 5)
        ).as((nome, nivel) -> Player.builder()
                .nome(nome)
                .posicao(Position.GOLEIRO)
                .nivel(nivel)
                .ativo(true)
                .build());

        // Outfield positions only
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

        Arbitrary<List<Player>> goalkeepers = goalkeeperArb.list().ofMinSize(2).ofMaxSize(3);
        Arbitrary<List<Player>> outfield = outfieldArb.list().ofMinSize(min).ofMaxSize(max);

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
}
