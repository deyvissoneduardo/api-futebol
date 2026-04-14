package br.com.futebol.interfaces.game;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorstPlayerRankingItemResponse {

    private Integer position;

    private UUID gameId;

    private UUID confirmationId;

    private UUID userId;

    private String playerName;

    private Integer votes;
}
