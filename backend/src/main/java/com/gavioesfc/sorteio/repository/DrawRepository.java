package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.Draw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    Page<Draw> findAllByOrderByDataHoraDesc(Pageable pageable);
}
