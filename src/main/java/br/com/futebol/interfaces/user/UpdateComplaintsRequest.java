package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateComplaintsRequest {

    /**
     * Valor positivo: soma (ex: 1 para adicionar 1 reclamação)
     * Valor negativo: subtrai (ex: -1 para remover 1 reclamação)
     */
    @NotNull(message = "Valor e obrigatorio")
    private Integer value;
}

