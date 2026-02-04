package br.com.futebol.infrastructure.game;

import br.com.futebol.domain.game.Game;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GameRepository implements PanacheRepositoryBase<Game, UUID> {

    /**
     * @return lista de jogos
     */
    public List<Game> findAllOrderedByDate() {
        return list("ORDER BY gameDate DESC");
    }

    /**
     * @param id o ID do jogo
     * @return Optional contendo o jogo se encontrado
     */
    public Optional<Game> findByIdOptional(UUID id) {
        return find("id = ?1", id).firstResultOptional();
    }

    /**
     * @return lista de jogos com released = true
     */
    public List<Game> findAllReleased() {
        return list("released = ?1", true);
    }

    /**
     * @return Optional contendo o jogo se encontrado
     */
    public Optional<Game> findReleased() {
        return find("released = ?1", true).firstResultOptional();
    }
}

