package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para atualização de reclamações.
 * Valor positivo para somar, valor negativo para subtrair.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateComplaintsRequest {

    /**
     * Valor a adicionar/subtrair às reclamações.
     * Valor positivo: soma (ex: 1 para adicionar 1 reclamação)
     * Valor negativo: subtrai (ex: -1 para remover 1 reclamação)
     */
    @NotNull(message = "Valor é obrigatório")
    private Integer value;
}

