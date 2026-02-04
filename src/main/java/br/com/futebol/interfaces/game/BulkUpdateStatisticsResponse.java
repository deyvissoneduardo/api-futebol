package br.com.futebol.interfaces.game;

import br.com.futebol.interfaces.user.UserStatisticsResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

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

