package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.Equipe;
import com.gavioesfc.sorteio.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {

    @Query("select p from Player p left join fetch p.equipe where p.dataNascimento is not null")
    List<Player> findAllWithBirthday();

    boolean existsByEquipeId(Long equipeId);

    @Modifying
    @Query("update Player p set p.equipe = :equipe where p.equipe is null")
    int assignEquipeWhereMissing(@Param("equipe") Equipe equipe);
}
