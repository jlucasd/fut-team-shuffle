package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.dto.PageResponse;
import com.gavioesfc.sorteio.dto.PlayerCreateRequest;
import com.gavioesfc.sorteio.dto.PlayerResponse;
import com.gavioesfc.sorteio.dto.PlayerUpdateRequest;
import com.gavioesfc.sorteio.exception.BusinessRuleException;
import com.gavioesfc.sorteio.exception.ResourceNotFoundException;
import com.gavioesfc.sorteio.mapper.PlayerMapper;
import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import com.gavioesfc.sorteio.repository.PlayerSpecifications;
import com.gavioesfc.sorteio.repository.TeamAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamAssignmentRepository teamAssignmentRepository;
    private final PlayerMapper playerMapper;
    private final EquipeService equipeService;

    public PlayerService(PlayerRepository playerRepository, TeamAssignmentRepository teamAssignmentRepository,
                         PlayerMapper playerMapper, EquipeService equipeService) {
        this.playerRepository = playerRepository;
        this.teamAssignmentRepository = teamAssignmentRepository;
        this.playerMapper = playerMapper;
        this.equipeService = equipeService;
    }

    @Transactional
    public PlayerResponse create(PlayerCreateRequest request) {
        Player player = playerMapper.toEntity(request);
        player.setEquipe(equipeService.resolve(request.equipeId()));
        Player saved = playerRepository.save(player);
        return playerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<PlayerResponse> list(Long equipeId, Position posicao, Boolean ativo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Player> playerPage = playerRepository.findAll(
                PlayerSpecifications.filter(equipeId, posicao, ativo), pageable);

        return new PageResponse<>(
                playerPage.getContent().stream().map(playerMapper::toResponse).toList(),
                playerPage.getTotalElements(),
                playerPage.getTotalPages(),
                playerPage.getNumber(),
                playerPage.getSize()
        );
    }

    @Transactional(readOnly = true)
    public PlayerResponse getById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado"));
        return playerMapper.toResponse(player);
    }

    @Transactional
    public PlayerResponse update(Long id, PlayerUpdateRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado"));
        playerMapper.updateEntity(request, player);
        if (request.equipeId() != null) {
            player.setEquipe(equipeService.getEntity(request.equipeId()));
        }
        Player saved = playerRepository.save(player);
        return playerMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jogador não encontrado");
        }
        if (teamAssignmentRepository.existsByPlayerId(id)) {
            throw new BusinessRuleException("Não é possível excluir este jogador pois ele está associado a sorteios anteriores.");
        }
        playerRepository.deleteById(id);
    }

    @Transactional
    public PlayerResponse toggleStatus(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado"));
        player.setAtivo(!player.getAtivo());
        Player saved = playerRepository.save(player);
        return playerMapper.toResponse(saved);
    }
}
