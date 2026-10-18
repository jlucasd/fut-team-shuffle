package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.NotificacaoEnviada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface NotificacaoEnviadaRepository extends JpaRepository<NotificacaoEnviada, Long> {

    boolean existsByDataReferencia(LocalDate dataReferencia);
}
