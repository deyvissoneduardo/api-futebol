package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para atualização de derrotas.
 * Valor positivo para somar, valor negativo para subtrair.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDefeatsRequest {

    /**
     * Valor a adicionar/subtrair às derrotas.
     * Valor positivo: soma (ex: 1 para adicionar 1 derrota)
     * Valor negativo: subtrai (ex: -1 para remover 1 derrota)
     */
    @NotNull(message = "Valor é obrigatório")
    private Integer value;
}

