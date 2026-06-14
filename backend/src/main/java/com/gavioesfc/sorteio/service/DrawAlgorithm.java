package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Draw algorithm that distributes players into two balanced teams of 6 each
 * (1 goalkeeper + 5 outfield players per team).
 *
 * Strategy:
 * 1. Each team gets exactly 1 goalkeeper and 5 outfield players
 * 2. Outfield players are distributed balancing by position first, then by level
 * 3. Players that don't fit in the 6-per-team structure go to reserves
 * 4. Post-distribution swap optimization (same position only) to equalize averages
 */
@Service
public class DrawAlgorithm {

    private static final double BALANCE_THRESHOLD = 0.5;
    private static final int TEAM_SIZE = 6; // 1 goleiro + 5 linha
    private static final int OUTFIELD_PER_TEAM = 5;

    public DrawResult execute(List<Player> players) {
        // Shuffle for randomness
        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);

        // Separate goalkeepers from outfield
        List<Player> goalkeepers = new ArrayList<>();
        List<Player> outfield = new ArrayList<>();
        for (Player p : shuffled) {
            if (p.getPosicao() == Position.GOLEIRO) {
                goalkeepers.add(p);
            } else {
                outfield.add(p);
            }
        }

        List<Player> timeAmarelo = new ArrayList<>();
        List<Player> timePreto = new ArrayList<>();
        List<Player> reservas = new ArrayList<>();

        // Assign goalkeepers: 1 per team, extras go to reserve
        if (goalkeepers.size() >= 2) {
            // Sort by level desc, best two get assigned
            goalkeepers.sort(Comparator.comparingInt(Player::getNivel).reversed());
            // Balance: better GK to team with assignment advantage
            timeAmarelo.add(goalkeepers.get(0));
            timePreto.add(goalkeepers.get(1));
            // Extras to reserve
            for (int i = 2; i < goalkeepers.size(); i++) {
                reservas.add(goalkeepers.get(i));
            }
        } else if (goalkeepers.size() == 1) {
            timeAmarelo.add(goalkeepers.get(0));
        }

        // Distribute outfield: 5 per team, rest to reserves
        // Group outfield by position
        Map<Position, List<Player>> byPosition = new EnumMap<>(Position.class);
        byPosition.put(Position.ZAGUEIRO, new ArrayList<>());
        byPosition.put(Position.MEIO, new ArrayList<>());
        byPosition.put(Position.ATACANTE, new ArrayList<>());

        for (Player p : outfield) {
            byPosition.get(p.getPosicao()).add(p);
        }

        // Sort each group by level descending
        for (List<Player> group : byPosition.values()) {
            group.sort(Comparator.comparingInt(Player::getNivel).reversed());
        }

        // Distribute positionally balanced: for each position, alternate between teams
        List<Player> amareloOutfield = new ArrayList<>();
        List<Player> pretoOutfield = new ArrayList<>();

        for (Map.Entry<Position, List<Player>> entry : byPosition.entrySet()) {
            List<Player> group = entry.getValue();
            for (Player p : group) {
                int sumA = amareloOutfield.stream().mapToInt(Player::getNivel).sum();
                int sumP = pretoOutfield.stream().mapToInt(Player::getNivel).sum();
                int countA = (int) amareloOutfield.stream()
                        .filter(pl -> pl.getPosicao() == p.getPosicao()).count();
                int countP = (int) pretoOutfield.stream()
                        .filter(pl -> pl.getPosicao() == p.getPosicao()).count();

                // Positional balance first
                if (countA < countP) {
                    amareloOutfield.add(p);
                } else if (countP < countA) {
                    pretoOutfield.add(p);
                } else {
                    // Same count — go to team with lower sum
                    if (sumA <= sumP) {
                        amareloOutfield.add(p);
                    } else {
                        pretoOutfield.add(p);
                    }
                }
            }
        }

        // Now enforce 5 outfield per team: trim extras to reserve
        // If a team has more than 5, move the lowest-level players to reserve
        trimToSize(amareloOutfield, OUTFIELD_PER_TEAM, reservas);
        trimToSize(pretoOutfield, OUTFIELD_PER_TEAM, reservas);

        // If a team has fewer than 5, try to pull from the other team's extras or reserves
        // (this handles edge cases with very few players)

        // Add outfield to teams
        timeAmarelo.addAll(amareloOutfield);
        timePreto.addAll(pretoOutfield);

        // Swap optimization: only same-position swaps between outfield players
        performPositionalSwapOptimization(timeAmarelo, timePreto);

        // Calculate averages
        double avgAmarelo = calculateAverage(timeAmarelo);
        double avgPreto = calculateAverage(timePreto);
        boolean equilibrado = Math.abs(avgAmarelo - avgPreto) <= BALANCE_THRESHOLD;

        return new DrawResult(timeAmarelo, timePreto, reservas, avgAmarelo, avgPreto, equilibrado);
    }

    /**
     * Trims a list to maxSize, moving the lowest-level players to reserves.
     */
    private void trimToSize(List<Player> team, int maxSize, List<Player> reservas) {
        if (team.size() <= maxSize) return;

        // Sort by level ascending — remove the weakest
        team.sort(Comparator.comparingInt(Player::getNivel));
        while (team.size() > maxSize) {
            reservas.add(team.remove(0));
        }
    }

    private double calculateAverage(List<Player> team) {
        if (team.isEmpty()) return 0.0;
        return (double) team.stream().mapToInt(Player::getNivel).sum() / team.size();
    }

    private void performPositionalSwapOptimization(List<Player> amarelo, List<Player> preto) {
        boolean improved = true;
        int maxIterations = 200;
        int iteration = 0;

        while (improved && iteration < maxIterations) {
            improved = false;
            iteration++;

            double currentDiff = Math.abs(calculateAverage(amarelo) - calculateAverage(preto));
            if (currentDiff <= BALANCE_THRESHOLD) break;

            for (int i = 0; i < amarelo.size() && !improved; i++) {
                for (int j = 0; j < preto.size() && !improved; j++) {
                    Player playerA = amarelo.get(i);
                    Player playerB = preto.get(j);

                    // Only swap same position (skip goalkeepers to keep 1 per team)
                    if (playerA.getPosicao() != playerB.getPosicao()) continue;
                    if (playerA.getPosicao() == Position.GOLEIRO) continue;
                    if (playerA.getNivel() == playerB.getNivel()) continue;

                    amarelo.set(i, playerB);
                    preto.set(j, playerA);

                    double newDiff = Math.abs(calculateAverage(amarelo) - calculateAverage(preto));
                    if (newDiff < currentDiff) {
                        improved = true;
                    } else {
                        amarelo.set(i, playerA);
                        preto.set(j, playerB);
                    }
                }
            }
        }
    }
}
