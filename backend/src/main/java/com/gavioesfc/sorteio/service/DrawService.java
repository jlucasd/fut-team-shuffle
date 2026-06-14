package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.dto.DrawRequest;
import com.gavioesfc.sorteio.dto.DrawResponse;
import com.gavioesfc.sorteio.dto.PageResponse;
import com.gavioesfc.sorteio.dto.TeamEditRequest;
import com.gavioesfc.sorteio.exception.BusinessRuleException;
import com.gavioesfc.sorteio.exception.ResourceNotFoundException;
import com.gavioesfc.sorteio.mapper.DrawMapper;
import com.gavioesfc.sorteio.model.entity.Draw;
import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.entity.TeamAssignment;
import com.gavioesfc.sorteio.model.enums.Team;
import com.gavioesfc.sorteio.repository.DrawRepository;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import com.gavioesfc.sorteio.repository.TeamAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DrawService {

    private static final double BALANCE_THRESHOLD = 0.5;

    private final DrawRepository drawRepository;
    private final PlayerRepository playerRepository;
    private final TeamAssignmentRepository teamAssignmentRepository;
    private final DrawAlgorithm drawAlgorithm;
    private final DrawMapper drawMapper;

    public DrawService(DrawRepository drawRepository,
                       PlayerRepository playerRepository,
                       TeamAssignmentRepository teamAssignmentRepository,
                       DrawAlgorithm drawAlgorithm,
                       DrawMapper drawMapper) {
        this.drawRepository = drawRepository;
        this.playerRepository = playerRepository;
        this.teamAssignmentRepository = teamAssignmentRepository;
        this.drawAlgorithm = drawAlgorithm;
        this.drawMapper = drawMapper;
    }

    @Transactional
    public DrawResponse executeDraw(DrawRequest request) {
        List<Long> jogadorIds = request.jogadorIds();

        // Validate minimum 2 players
        if (jogadorIds.size() < 2) {
            throw new BusinessRuleException("Selecione pelo menos 2 jogadores para o sorteio");
        }

        // Fetch players
        List<Player> players = playerRepository.findAllById(jogadorIds);

        if (players.size() != jogadorIds.size()) {
            throw new ResourceNotFoundException("Um ou mais jogadores não foram encontrados");
        }

        // Validate all players are active
        boolean hasInactive = players.stream().anyMatch(p -> !p.getAtivo());
        if (hasInactive) {
            throw new BusinessRuleException("Jogadores inativos não podem participar do sorteio");
        }

        // Execute algorithm
        DrawResult result = drawAlgorithm.execute(players);

        // Persist Draw entity
        Draw draw = Draw.builder()
                .mediaAmarelo(result.getMediaAmarelo())
                .mediaPreto(result.getMediaPreto())
                .equilibrado(result.getEquilibrado())
                .build();
        Draw savedDraw = drawRepository.save(draw);

        // Create team assignments
        List<TeamAssignment> assignments = new ArrayList<>();

        for (Player player : result.getTimeAmarelo()) {
            assignments.add(TeamAssignment.builder()
                    .draw(savedDraw)
                    .player(player)
                    .team(Team.AMARELO)
                    .build());
        }

        for (Player player : result.getTimePreto()) {
            assignments.add(TeamAssignment.builder()
                    .draw(savedDraw)
                    .player(player)
                    .team(Team.PRETO)
                    .build());
        }

        if (result.getReservas() != null && !result.getReservas().isEmpty()) {
            for (Player player : result.getReservas()) {
                assignments.add(TeamAssignment.builder()
                        .draw(savedDraw)
                        .player(player)
                        .team(Team.RESERVA)
                        .build());
            }
        }

        List<TeamAssignment> savedAssignments = teamAssignmentRepository.saveAll(assignments);

        return drawMapper.toResponse(savedDraw, savedAssignments);
    }

    @Transactional(readOnly = true)
    public PageResponse<DrawResponse> getHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Draw> drawPage = drawRepository.findAllByOrderByDataHoraDesc(pageable);

        List<DrawResponse> content = drawPage.getContent().stream()
                .map(draw -> {
                    List<TeamAssignment> assignments = teamAssignmentRepository.findByDrawId(draw.getId());
                    return drawMapper.toResponse(draw, assignments);
                })
                .toList();

        return new PageResponse<>(
                content,
                drawPage.getTotalElements(),
                drawPage.getTotalPages(),
                drawPage.getNumber(),
                drawPage.getSize()
        );
    }

    @Transactional(readOnly = true)
    public DrawResponse getById(Long id) {
        Draw draw = drawRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sorteio não encontrado"));
        List<TeamAssignment> assignments = teamAssignmentRepository.findByDrawId(draw.getId());
        return drawMapper.toResponse(draw, assignments);
    }

    @Transactional
    public DrawResponse updateTeams(Long id, TeamEditRequest request) {
        Draw draw = drawRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sorteio não encontrado"));

        // Delete existing assignments
        teamAssignmentRepository.deleteByDrawId(draw.getId());
        teamAssignmentRepository.flush();

        // Create new assignments
        List<TeamAssignment> assignments = new ArrayList<>();

        List<Player> amareloPlayers = new ArrayList<>();
        if (request.timeAmarelo() != null) {
            for (Long playerId : request.timeAmarelo()) {
                Player player = playerRepository.findById(playerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado"));
                amareloPlayers.add(player);
                assignments.add(TeamAssignment.builder()
                        .draw(draw)
                        .player(player)
                        .team(Team.AMARELO)
                        .build());
            }
        }

        List<Player> pretoPlayers = new ArrayList<>();
        if (request.timePreto() != null) {
            for (Long playerId : request.timePreto()) {
                Player player = playerRepository.findById(playerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado"));
                pretoPlayers.add(player);
                assignments.add(TeamAssignment.builder()
                        .draw(draw)
                        .player(player)
                        .team(Team.PRETO)
                        .build());
            }
        }

        if (request.reserva() != null) {
            Player reservaPlayer = playerRepository.findById(request.reserva())
                    .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado"));
            assignments.add(TeamAssignment.builder()
                    .draw(draw)
                    .player(reservaPlayer)
                    .team(Team.RESERVA)
                    .build());
        }

        // Recalculate averages
        double mediaAmarelo = calculateAverage(amareloPlayers);
        double mediaPreto = calculateAverage(pretoPlayers);
        boolean equilibrado = Math.abs(mediaAmarelo - mediaPreto) <= BALANCE_THRESHOLD;

        // Update draw
        draw.setMediaAmarelo(mediaAmarelo);
        draw.setMediaPreto(mediaPreto);
        draw.setEquilibrado(equilibrado);
        drawRepository.save(draw);

        List<TeamAssignment> savedAssignments = teamAssignmentRepository.saveAll(assignments);

        return drawMapper.toResponse(draw, savedAssignments);
    }

    private double calculateAverage(List<Player> players) {
        if (players.isEmpty()) {
            return 0.0;
        }
        double sum = players.stream().mapToInt(Player::getNivel).sum();
        return sum / players.size();
    }
}
