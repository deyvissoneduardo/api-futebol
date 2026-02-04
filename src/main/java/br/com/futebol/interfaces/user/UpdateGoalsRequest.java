package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGoalsRequest {

    /**
     * Valor positivo: soma (ex: 1 para adicionar 1 gol)
     * Valor negativo: subtrai (ex: -1 para remover 1 gol)
     */
    @NotNull(message = "Valor e obrigatorio")
    private Integer value;
}

