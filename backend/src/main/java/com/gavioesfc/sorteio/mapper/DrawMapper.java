package com.gavioesfc.sorteio.mapper;

import com.gavioesfc.sorteio.dto.DrawResponse;
import com.gavioesfc.sorteio.dto.PlayerResponse;
import com.gavioesfc.sorteio.model.entity.Draw;
import com.gavioesfc.sorteio.model.entity.TeamAssignment;
import com.gavioesfc.sorteio.model.enums.Team;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DrawMapper {

    private final PlayerMapper playerMapper;

    public DrawMapper(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    public DrawResponse toResponse(Draw draw, List<TeamAssignment> assignments) {
        List<PlayerResponse> timeAmarelo = assignments.stream()
                .filter(a -> a.getTeam() == Team.AMARELO)
                .map(a -> playerMapper.toResponse(a.getPlayer()))
                .toList();

        List<PlayerResponse> timePreto = assignments.stream()
                .filter(a -> a.getTeam() == Team.PRETO)
                .map(a -> playerMapper.toResponse(a.getPlayer()))
                .toList();

        List<PlayerResponse> reservas = assignments.stream()
                .filter(a -> a.getTeam() == Team.RESERVA)
                .map(a -> playerMapper.toResponse(a.getPlayer()))
                .toList();

        return new DrawResponse(
                draw.getId(),
                draw.getDataHora(),
                timeAmarelo,
                timePreto,
                reservas,
                draw.getMediaAmarelo(),
                draw.getMediaPreto(),
                draw.getEquilibrado()
        );
    }
}
