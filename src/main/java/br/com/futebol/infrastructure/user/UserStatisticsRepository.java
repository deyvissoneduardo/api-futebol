package br.com.futebol.infrastructure.user;

import br.com.futebol.domain.user.UserStatistics;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência da entidade UserStatistics.
 */
@ApplicationScoped
public class UserStatisticsRepository implements PanacheRepositoryBase<UserStatistics, UUID> {

    /**
     * Busca estatísticas de um usuário pelo ID do usuário.
     *
     * @param userId o ID do usuário
     * @return Optional contendo as estatísticas se encontradas
     */
    public Optional<UserStatistics> findByUserId(UUID userId) {

        return find("userId", userId).firstResultOptional();
    }

    /**
     * Busca ranking de gols ordenado do maior para o menor.
     * Busca apenas estatísticas de usuários ADMIN e JOGADOR ativos.
     *
     * @return Lista de estatísticas ordenadas por gols
     */
    public List<UserStatistics> findRankingByGoals() {
        return list("ORDER BY goals DESC");
    }

    /**
     * Busca ranking de reclamações ordenado do maior para o menor.
     *
     * @return Lista de estatísticas ordenadas por reclamações
     */
    public List<UserStatistics> findRankingByComplaints() {
        return list("ORDER BY complaints DESC");
    }

    /**
     * Busca ranking de vitórias ordenado do maior para o menor.
     *
     * @return Lista de estatísticas ordenadas por vitórias
     */
    public List<UserStatistics> findRankingByVictories() {
        return list("ORDER BY victories DESC");
    }

    /**
     * Busca ranking de empates ordenado do maior para o menor.
     *
     * @return Lista de estatísticas ordenadas por empates
     */
    public List<UserStatistics> findRankingByDraws() {
        return list("ORDER BY draws DESC");
    }

    /**
     * Busca ranking de derrotas ordenado do maior para o menor.
     *
     * @return Lista de estatísticas ordenadas por derrotas
     */
    public List<UserStatistics> findRankingByDefeats() {
        return list("ORDER BY defeats DESC");
    }

    /**
     * Busca ranking de minutos jogados ordenado do maior para o menor.
     *
     * @return Lista de estatísticas ordenadas por minutos jogados
     */
    public List<UserStatistics> findRankingByMinutesPlayed() {
        return list("ORDER BY minutesPlayed DESC");
    }
}

