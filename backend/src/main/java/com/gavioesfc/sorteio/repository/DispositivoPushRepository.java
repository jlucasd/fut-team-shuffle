package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.DispositivoPush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface DispositivoPushRepository extends JpaRepository<DispositivoPush, Long> {

    Optional<DispositivoPush> findByToken(String token);

    void deleteByToken(String token);

    void deleteByTokenIn(Collection<String> tokens);
}
