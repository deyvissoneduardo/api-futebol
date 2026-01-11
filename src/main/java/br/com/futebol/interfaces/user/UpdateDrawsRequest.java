package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para atualização de empates.
 * Valor positivo para somar, valor negativo para subtrair.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDrawsRequest {

    /**
     * Valor a adicionar/subtrair aos empates.
     * Valor positivo: soma (ex: 1 para adicionar 1 empate)
     * Valor negativo: subtrai (ex: -1 para remover 1 empate)
     */
    @NotNull(message = "Valor é obrigatório")
    private Integer value;
}

