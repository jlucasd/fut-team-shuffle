package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Page<Player> findByPosicao(Position posicao, Pageable pageable);

    Page<Player> findByAtivo(Boolean ativo, Pageable pageable);

    Page<Player> findByPosicaoAndAtivo(Position posicao, Boolean ativo, Pageable pageable);
}
