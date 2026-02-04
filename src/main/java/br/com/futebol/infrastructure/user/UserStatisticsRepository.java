package br.com.futebol.infrastructure.user;

import br.com.futebol.domain.user.UserStatistics;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@ApplicationScoped
public class UserStatisticsRepository implements PanacheRepositoryBase<UserStatistics, UUID> {

    /**
     * @param userId o ID do usuario
     * @return Optional contendo as estatisticas se encontradas
     */
    public Optional<UserStatistics> findByUserId(UUID userId) {

        return find("userId", userId).firstResultOptional();
    }

    /**
     * @return Lista de estatísticas ordenadas por gols
     */
    public List<UserStatistics> findRankingByGoals() {
        return list("ORDER BY goals DESC");
    }

    /**
     * Busca ranking de reclamações ordenado do maior para o menor.
     *
     * @return Lista de estatisticas ordenadas por reclamacoes
     */
    public List<UserStatistics> findRankingByComplaints() {
        return list("ORDER BY complaints DESC");
    }

    /**
     * @return Lista de estatisticas ordenadas por vitorias
     */
    public List<UserStatistics> findRankingByVictories() {
        return list("ORDER BY victories DESC");
    }

    /**
     * @return Lista de estatisticas ordenadas por empates
     */
    public List<UserStatistics> findRankingByDraws() {
        return list("ORDER BY draws DESC");
    }

    /**
     * @return Lista de estatisticas ordenadas por derrotas
     */
    public List<UserStatistics> findRankingByDefeats() {
        return list("ORDER BY defeats DESC");
    }

    /**
     * @return Lista de estatisticas ordenadas por minutos jogados
     */
    public List<UserStatistics> findRankingByMinutesPlayed() {
        return list("ORDER BY minutesPlayed DESC");
    }
}

