package br.com.futebol.interfaces.game;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddConfirmedPlayerRequest {

    @NotNull(message = "userId e obrigatorio")
    private UUID userId;
}
