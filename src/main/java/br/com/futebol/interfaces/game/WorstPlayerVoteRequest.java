package br.com.futebol.interfaces.game;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorstPlayerVoteRequest {

    @NotNull(message = "targetConfirmationId e obrigatorio")
    private UUID targetConfirmationId;
}
