package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDrawsRequest {

    /**
     * Valor positivo: soma (ex: 1 para adicionar 1 empate)
     * Valor negativo: subtrai (ex: -1 para remover 1 empate)
     */
    @NotNull(message = "Valor e obrigatorio")
    private Integer value;
}

