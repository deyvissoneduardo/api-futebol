package br.com.futebol.interfaces.user;

import lombok.*;

import java.util.List;

/**
 * DTO de resposta para ranking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingResponse {

    private String type; // Tipo de ranking: "goals", "victories", "minutes", etc
    private String description; // Descrição do ranking
    private List<RankingItemResponse> items;
    private Integer total; // Total de usuários no ranking
}

