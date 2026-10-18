package com.gavioesfc.sorteio.service;

import com.gavioesfc.sorteio.dto.EquipeRequest;
import com.gavioesfc.sorteio.dto.EquipeResponse;
import com.gavioesfc.sorteio.exception.BusinessRuleException;
import com.gavioesfc.sorteio.exception.ResourceNotFoundException;
import com.gavioesfc.sorteio.model.entity.Equipe;
import com.gavioesfc.sorteio.repository.DrawRepository;
import com.gavioesfc.sorteio.repository.EquipeRepository;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipeService {

    /** Name of the equipe that pre-existing (single-team) data is migrated into. */
    public static final String DEFAULT_EQUIPE_NOME = "Gaviões FC";

    private final EquipeRepository equipeRepository;
    private final PlayerRepository playerRepository;
    private final DrawRepository drawRepository;

    public EquipeService(EquipeRepository equipeRepository,
                         PlayerRepository playerRepository,
                         DrawRepository drawRepository) {
        this.equipeRepository = equipeRepository;
        this.playerRepository = playerRepository;
        this.drawRepository = drawRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> list() {
        return equipeRepository.findAllByOrderByNomeAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public EquipeResponse create(EquipeRequest request) {
        String nome = request.nome().trim();
        if (equipeRepository.existsByNomeIgnoreCase(nome)) {
            throw new BusinessRuleException("Já existe um time com esse nome.");
        }
        return toResponse(equipeRepository.save(Equipe.builder().nome(nome).build()));
    }

    @Transactional
    public EquipeResponse update(Long id, EquipeRequest request) {
        Equipe equipe = getEntity(id);
        String nome = request.nome().trim();
        if (equipeRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new BusinessRuleException("Já existe um time com esse nome.");
        }
        equipe.setNome(nome);
        return toResponse(equipeRepository.save(equipe));
    }

    @Transactional
    public void delete(Long id) {
        Equipe equipe = getEntity(id);
        if (playerRepository.existsByEquipeId(id) || drawRepository.existsByEquipeId(id)) {
            throw new BusinessRuleException("Não é possível excluir um time que possui jogadores ou sorteios.");
        }
        equipeRepository.delete(equipe);
    }

    @Transactional(readOnly = true)
    public Equipe getEntity(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado"));
    }

    /** The given equipe, or the default one when {@code id} is null. */
    @Transactional
    public Equipe resolve(Long id) {
        return id != null ? getEntity(id) : getOrCreateDefault();
    }

    /** The oldest equipe; creates {@value #DEFAULT_EQUIPE_NOME} if there is none yet. */
    @Transactional
    public Equipe getOrCreateDefault() {
        return equipeRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> equipeRepository.save(Equipe.builder().nome(DEFAULT_EQUIPE_NOME).build()));
    }

    private EquipeResponse toResponse(Equipe equipe) {
        return new EquipeResponse(equipe.getId(), equipe.getNome());
    }
}
