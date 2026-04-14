package br.com.futebol.interfaces.game;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorstPlayerHistoricalRankingResponse {

    private String startDate;

    private String endDate;

    private String description;

    private List<WorstPlayerRankingItemResponse> items;

    private Integer total;
}
