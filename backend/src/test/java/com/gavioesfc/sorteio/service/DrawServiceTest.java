package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.dto.DrawRequest;
import com.gavioesfc.sorteio.dto.DrawResponse;
import com.gavioesfc.sorteio.dto.PageResponse;
import com.gavioesfc.sorteio.dto.TeamEditRequest;
import com.gavioesfc.sorteio.exception.BusinessRuleException;
import com.gavioesfc.sorteio.exception.ResourceNotFoundException;
import com.gavioesfc.sorteio.mapper.DrawMapper;
import com.gavioesfc.sorteio.mapper.PlayerMapper;
import com.gavioesfc.sorteio.model.entity.Draw;
import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.entity.TeamAssignment;
import com.gavioesfc.sorteio.model.enums.Position;
import com.gavioesfc.sorteio.model.enums.Team;
import com.gavioesfc.sorteio.repository.DrawRepository;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import com.gavioesfc.sorteio.repository.TeamAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamAssignmentRepository teamAssignmentRepository;

    @Mock
    private DrawAlgorithm drawAlgorithm;

    @Mock
    private PlayerMapper playerMapper;

    private DrawMapper drawMapper;
    private DrawService drawService;

    @BeforeEach
    void setUp() {
        drawMapper = new DrawMapper(playerMapper);
        drawService = new DrawService(drawRepository, playerRepository, teamAssignmentRepository, drawAlgorithm, drawMapper);
    }

    private Player createPlayer(Long id, String nome, Position posicao, int nivel, boolean ativo) {
        return Player.builder()
                .id(id)
                .nome(nome)
                .posicao(posicao)
                .nivel(nivel)
                .ativo(ativo)
                .build();
    }

    @Test
    void executeDraw_withLessThan2Players_throwsBusinessRuleException() {
        DrawRequest request = new DrawRequest(List.of(1L));

        assertThatThrownBy(() -> drawService.executeDraw(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Selecione pelo menos 2 jogadores para o sorteio");
    }

    @Test
    void executeDraw_withInactivePlayers_throwsBusinessRuleException() {
        Player active = createPlayer(1L, "Jogador 1", Position.MEIO, 3, true);
        Player inactive = createPlayer(2L, "Jogador 2", Position.ATACANTE, 4, false);

        when(playerRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(active, inactive));

        DrawRequest request = new DrawRequest(List.of(1L, 2L));

        assertThatThrownBy(() -> drawService.executeDraw(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Jogadores inativos não podem participar do sorteio");
    }

    @Test
    void executeDraw_withValidPlayers_returnsDrawResponse() {
        Player p1 = createPlayer(1L, "Jogador 1", Position.MEIO, 3, true);
        Player p2 = createPlayer(2L, "Jogador 2", Position.ATACANTE, 4, true);

        when(playerRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(p1, p2));

        DrawResult drawResult = new DrawResult(List.of(p1), List.of(p2), null, 3.0, 4.0, false);
        when(drawAlgorithm.execute(anyList())).thenReturn(drawResult);

        Draw savedDraw = Draw.builder().id(1L).dataHora(LocalDateTime.now()).mediaAmarelo(3.0).mediaPreto(4.0).equilibrado(false).build();
        when(drawRepository.save(any(Draw.class))).thenReturn(savedDraw);

        TeamAssignment ta1 = TeamAssignment.builder().id(1L).draw(savedDraw).player(p1).team(Team.AMARELO).build();
        TeamAssignment ta2 = TeamAssignment.builder().id(2L).draw(savedDraw).player(p2).team(Team.PRETO).build();
        when(teamAssignmentRepository.saveAll(anyList())).thenReturn(List.of(ta1, ta2));

        DrawRequest request = new DrawRequest(List.of(1L, 2L));
        DrawResponse response = drawService.executeDraw(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.mediaAmarelo()).isEqualTo(3.0);
        assertThat(response.mediaPreto()).isEqualTo(4.0);
        assertThat(response.equilibrado()).isFalse();

        verify(drawRepository).save(any(Draw.class));
        verify(teamAssignmentRepository).saveAll(anyList());
    }

    @Test
    void getById_withNonExistentId_throwsResourceNotFoundException() {
        when(drawRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> drawService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sorteio não encontrado");
    }

    @Test
    void getById_withExistingId_returnsDrawResponse() {
        Draw draw = Draw.builder().id(1L).dataHora(LocalDateTime.now()).mediaAmarelo(3.0).mediaPreto(3.5).equilibrado(true).build();
        when(drawRepository.findById(1L)).thenReturn(Optional.of(draw));
        when(teamAssignmentRepository.findByDrawId(1L)).thenReturn(List.of());

        DrawResponse response = drawService.getById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.equilibrado()).isTrue();
    }

    @Test
    void getHistory_returnsPaginatedResults() {
        Draw draw = Draw.builder().id(1L).dataHora(LocalDateTime.now()).mediaAmarelo(3.0).mediaPreto(3.0).equilibrado(true).build();
        PageImpl<Draw> page = new PageImpl<>(List.of(draw), PageRequest.of(0, 20), 1);

        when(drawRepository.findAllByOrderByDataHoraDesc(any())).thenReturn(page);
        when(teamAssignmentRepository.findByDrawId(1L)).thenReturn(List.of());

        PageResponse<DrawResponse> response = drawService.getHistory(0, 20);

        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(0);
    }

    @Test
    void updateTeams_withNonExistentDraw_throwsResourceNotFoundException() {
        when(drawRepository.findById(anyLong())).thenReturn(Optional.empty());
        TeamEditRequest request = new TeamEditRequest(List.of(1L), List.of(2L), null);

        assertThatThrownBy(() -> drawService.updateTeams(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sorteio não encontrado");
    }

    @Test
    void updateTeams_recalculatesAveragesAndEquilibrado() {
        Draw draw = Draw.builder().id(1L).dataHora(LocalDateTime.now()).mediaAmarelo(3.0).mediaPreto(4.0).equilibrado(false).build();
        when(drawRepository.findById(1L)).thenReturn(Optional.of(draw));

        Player p1 = createPlayer(1L, "Jogador 1", Position.MEIO, 3, true);
        Player p2 = createPlayer(2L, "Jogador 2", Position.ATACANTE, 3, true);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(p2));

        TeamAssignment ta1 = TeamAssignment.builder().id(1L).draw(draw).player(p1).team(Team.AMARELO).build();
        TeamAssignment ta2 = TeamAssignment.builder().id(2L).draw(draw).player(p2).team(Team.PRETO).build();
        when(teamAssignmentRepository.saveAll(anyList())).thenReturn(List.of(ta1, ta2));
        when(drawRepository.save(any(Draw.class))).thenReturn(draw);

        TeamEditRequest request = new TeamEditRequest(List.of(1L), List.of(2L), null);
        DrawResponse response = drawService.updateTeams(1L, request);

        assertThat(response).isNotNull();
        // Both players have level 3, so averages should be equal -> equilibrado = true
        assertThat(draw.getMediaAmarelo()).isEqualTo(3.0);
        assertThat(draw.getMediaPreto()).isEqualTo(3.0);
        assertThat(draw.getEquilibrado()).isTrue();
    }
}
