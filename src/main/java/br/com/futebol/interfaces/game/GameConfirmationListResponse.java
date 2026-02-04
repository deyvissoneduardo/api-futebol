package br.com.futebol.interfaces.game;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameConfirmationListResponse {

    private UUID gameId;

    private List<GameConfirmationResponse> confirmations;

    private Integer total;
}

