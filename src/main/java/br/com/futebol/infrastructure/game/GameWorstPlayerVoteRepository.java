package br.com.futebol.infrastructure.game;

import br.com.futebol.domain.game.GameWorstPlayerVote;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GameWorstPlayerVoteRepository implements PanacheRepositoryBase<GameWorstPlayerVote, UUID> {

    public boolean existsByGameIdAndVoterUserId(UUID gameId, UUID voterUserId) {
        return count("gameId = ?1 and voterUserId = ?2", gameId, voterUserId) > 0;
    }

    public List<GameWorstPlayerVote> findByGameId(UUID gameId) {
        return list("gameId = ?1 order by createdAt asc", gameId);
    }

    public List<GameWorstPlayerVote> findByGameIdAndTargetConfirmationId(UUID gameId, UUID targetConfirmationId) {
        return list("gameId = ?1 and targetConfirmationId = ?2 order by createdAt asc", gameId, targetConfirmationId);
    }

    public List<GameWorstPlayerVote> findByGameIds(List<UUID> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) {
            return List.of();
        }
        return list("gameId in ?1 order by createdAt desc", gameIds);
    }
}
