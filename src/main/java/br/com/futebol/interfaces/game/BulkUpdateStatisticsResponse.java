package br.com.futebol.interfaces.game;

import br.com.futebol.interfaces.user.UserStatisticsResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta para atualização em lote de estatísticas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUpdateStatisticsResponse {

    private UUID gameId;

    private Integer updatedCount;

    private List<UserStatisticsResponse> statistics;
}

