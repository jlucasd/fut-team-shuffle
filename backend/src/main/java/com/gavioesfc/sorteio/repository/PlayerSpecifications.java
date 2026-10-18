package com.gavioesfc.sorteio.repository;

import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import org.springframework.data.jpa.domain.Specification;

/** Optional filters for the player listing; a {@code null} argument means "no filter". */
public final class PlayerSpecifications {

    private PlayerSpecifications() {
    }

    public static Specification<Player> filter(Long equipeId, Position posicao, Boolean ativo) {
        return Specification.where(hasEquipe(equipeId))
                .and(hasPosicao(posicao))
                .and(hasAtivo(ativo));
    }

    private static Specification<Player> hasEquipe(Long equipeId) {
        return equipeId == null ? null : (root, query, cb) -> cb.equal(root.get("equipe").get("id"), equipeId);
    }

    private static Specification<Player> hasPosicao(Position posicao) {
        return posicao == null ? null : (root, query, cb) -> cb.equal(root.get("posicao"), posicao);
    }

    private static Specification<Player> hasAtivo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }
}
