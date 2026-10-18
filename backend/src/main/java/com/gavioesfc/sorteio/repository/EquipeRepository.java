package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    List<Equipe> findAllByOrderByNomeAsc();

    Optional<Equipe> findFirstByOrderByIdAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
