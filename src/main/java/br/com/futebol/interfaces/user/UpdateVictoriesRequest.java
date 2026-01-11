package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para atualização de vitórias.
 * Valor positivo para somar, valor negativo para subtrair.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVictoriesRequest {

    /**
     * Valor a adicionar/subtrair às vitórias.
     * Valor positivo: soma (ex: 1 para adicionar 1 vitória)
     * Valor negativo: subtrai (ex: -1 para remover 1 vitória)
     */
    @NotNull(message = "Valor é obrigatório")
    private Integer value;
}

