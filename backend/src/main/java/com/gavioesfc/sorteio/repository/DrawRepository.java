package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.Equipe;
import com.gavioesfc.sorteio.model.entity.Draw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    Page<Draw> findAllByOrderByDataHoraDesc(Pageable pageable);

    Page<Draw> findByEquipeIdOrderByDataHoraDesc(Long equipeId, Pageable pageable);

    boolean existsByEquipeId(Long equipeId);

    @Modifying
    @Query("update Draw d set d.equipe = :equipe where d.equipe is null")
    int assignEquipeWhereMissing(@Param("equipe") Equipe equipe);
}
