package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.TeamAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamAssignmentRepository extends JpaRepository<TeamAssignment, Long> {

    List<TeamAssignment> findByDrawId(Long drawId);

    void deleteByDrawId(Long drawId);

    void deleteByPlayerId(Long playerId);

    boolean existsByPlayerId(Long playerId);
}
