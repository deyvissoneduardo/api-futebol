package br.com.futebol.interfaces.game;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorstPlayerRankingResponse {

    private UUID gameId;

    private String description;

    private List<WorstPlayerRankingItemResponse> items;

    private Integer total;
}
