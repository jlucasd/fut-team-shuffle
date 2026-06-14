package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.model.entity.Player;

import java.util.List;

/**
 * Holds the result of a team draw operation.
 * Each team has exactly TEAM_SIZE players (1 goleiro + 5 linha).
 * Remaining players go to reservas list.
 */
public class DrawResult {

    private final List<Player> timeAmarelo;
    private final List<Player> timePreto;
    private final List<Player> reservas;
    private final Double mediaAmarelo;
    private final Double mediaPreto;
    private final Boolean equilibrado;

    public DrawResult(List<Player> timeAmarelo, List<Player> timePreto, List<Player> reservas,
                      Double mediaAmarelo, Double mediaPreto, Boolean equilibrado) {
        this.timeAmarelo = timeAmarelo;
        this.timePreto = timePreto;
        this.reservas = reservas;
        this.mediaAmarelo = mediaAmarelo;
        this.mediaPreto = mediaPreto;
        this.equilibrado = equilibrado;
    }

    public List<Player> getTimeAmarelo() {
        return timeAmarelo;
    }

    public List<Player> getTimePreto() {
        return timePreto;
    }

    public List<Player> getReservas() {
        return reservas;
    }

    /** @deprecated Use getReservas() instead */
    public Player getReserva() {
        return reservas != null && !reservas.isEmpty() ? reservas.get(0) : null;
    }

    public Double getMediaAmarelo() {
        return mediaAmarelo;
    }

    public Double getMediaPreto() {
        return mediaPreto;
    }

    public Boolean getEquilibrado() {
        return equilibrado;
    }
}
