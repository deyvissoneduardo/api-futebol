package br.com.futebol.interfaces.game;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorstPlayerVotersResponse {

    private UUID gameId;

    private UUID confirmationId;

    private String playerName;

    private List<String> voterNames;

    private Integer total;
}
