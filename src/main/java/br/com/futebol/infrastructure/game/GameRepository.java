package br.com.futebol.infrastructure.game;

import br.com.futebol.domain.game.Game;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência da entidade Game.
 */
@ApplicationScoped
public class GameRepository implements PanacheRepositoryBase<Game, UUID> {

    /**
     * Lista todos os jogos ordenados por data.
     *
     * @return lista de jogos
     */
    public List<Game> findAllOrderedByDate() {
        return list("ORDER BY gameDate DESC");
    }

    /**
     * Busca um jogo pelo ID.
     *
     * @param id o ID do jogo
     * @return Optional contendo o jogo se encontrado
     */
    public Optional<Game> findByIdOptional(UUID id) {
        return find("id = ?1", id).firstResultOptional();
    }

    /**
     * Busca todos os jogos com released = true.
     *
     * @return lista de jogos com released = true
     */
    public List<Game> findAllReleased() {
        return list("released = ?1", true);
    }

    /**
     * Busca o único jogo com released = true.
     *
     * @return Optional contendo o jogo se encontrado
     */
    public Optional<Game> findReleased() {
        return find("released = ?1", true).firstResultOptional();
    }
}

