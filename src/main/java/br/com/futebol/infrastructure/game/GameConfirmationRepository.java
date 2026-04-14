package br.com.futebol.infrastructure.game;

import br.com.futebol.domain.game.GameConfirmation;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

@ApplicationScoped
public class GameConfirmationRepository implements PanacheRepositoryBase<GameConfirmation, UUID> {

    /**
     * @param gameId o ID do jogo
     * @return lista de confirmacoes do jogo
     */
    public List<GameConfirmation> findByGameId(UUID gameId) {
        return list("gameId = ?1 order by confirmedAt asc", gameId);
    }

    /**
     * @param gameId o ID do jogo
     * @param userId o ID do usuario
     * @return lista de confirmacoes do usuario para o jogo
     */
    public List<GameConfirmation> findByGameIdAndUserId(UUID gameId, UUID userId) {
        return list("gameId = ?1 AND userId = ?2", gameId, userId);
    }

    /**
     * @param gameId o ID do jogo
     * @param confirmedName o nome confirmado
     * @return true se existir uma confirmacao com o nome
     */
    public boolean existsByGameIdAndConfirmedName(UUID gameId, String confirmedName) {
        return count("gameId = ?1 AND confirmedName = ?2", gameId, confirmedName) > 0;
    }

    /**
     * @param gameId o ID do jogo
     * @param userId o ID do usuario
     * @return true se o usuario já confirmou
     */
    public boolean existsByGameIdAndUserId(UUID gameId, UUID userId) {
        return count("gameId = ?1 AND userId = ?2", gameId, userId) > 0;
    }

    /**
     * @param gameId o ID do jogo
     * @param userId o ID do usuario
     * @return lista de confirmações relacionadas ao usuario
     */
    public List<GameConfirmation> findByGameIdAndUserRelated(UUID gameId, UUID userId) {
        return list("gameId = ?1 AND (userId = ?2 OR confirmedByUserId = ?2)", gameId, userId);
    }

    /**
     * @param id o ID da confirmacao
     * @return Optional contendo a confirmacao
     */
    public Optional<GameConfirmation> findByIdOptional(UUID id) {
        return find("id = ?1", id).firstResultOptional();
    }

    /**
     * @param gameId o ID do jogo
     * @return lista de confirmações de usuarios cadastrados
     */
    public List<GameConfirmation> findEligibleWorstPlayerByGameId(UUID gameId) {
        return list("gameId = ?1 and isGuest = false order by confirmedName asc", gameId);
    }

    /**
     * @param ids conjunto de IDs de confirmacoes
     * @return lista de confirmações
     */
    public List<GameConfirmation> findByIds(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return list("id in ?1", ids);
    }
}

