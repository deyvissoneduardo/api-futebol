package br.com.futebol.infrastructure.game;

import br.com.futebol.domain.game.GameConfirmation;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência da entidade GameConfirmation.
 */
@ApplicationScoped
public class GameConfirmationRepository implements PanacheRepositoryBase<GameConfirmation, UUID> {

    /**
     * Busca confirmações por jogo.
     *
     * @param gameId o ID do jogo
     * @return lista de confirmações do jogo
     */
    public List<GameConfirmation> findByGameId(UUID gameId) {
        return list("gameId", gameId);
    }

    /**
     * Busca todas as confirmações de um usuário para um jogo.
     *
     * @param gameId o ID do jogo
     * @param userId o ID do usuário
     * @return lista de confirmações do usuário para o jogo
     */
    public List<GameConfirmation> findByGameIdAndUserId(UUID gameId, UUID userId) {
        return list("gameId = ?1 AND userId = ?2", gameId, userId);
    }

    /**
     * Verifica se existe uma confirmação com o nome informado para o jogo.
     *
     * @param gameId o ID do jogo
     * @param confirmedName o nome confirmado
     * @return true se existir uma confirmação com o nome
     */
    public boolean existsByGameIdAndConfirmedName(UUID gameId, String confirmedName) {
        return count("gameId = ?1 AND confirmedName = ?2", gameId, confirmedName) > 0;
    }

    /**
     * Verifica se o usuário já confirmou para o jogo.
     *
     * @param gameId o ID do jogo
     * @param userId o ID do usuário
     * @return true se o usuário já confirmou
     */
    public boolean existsByGameIdAndUserId(UUID gameId, UUID userId) {
        return count("gameId = ?1 AND userId = ?2", gameId, userId) > 0;
    }

    /**
     * Busca todas as confirmações relacionadas a um usuário para um jogo.
     * Inclui confirmações próprias (userId) e de convidados confirmados por ele (confirmedByUserId).
     *
     * @param gameId o ID do jogo
     * @param userId o ID do usuário
     * @return lista de confirmações relacionadas ao usuário
     */
    public List<GameConfirmation> findByGameIdAndUserRelated(UUID gameId, UUID userId) {
        return list("gameId = ?1 AND (userId = ?2 OR confirmedByUserId = ?2)", gameId, userId);
    }
}

