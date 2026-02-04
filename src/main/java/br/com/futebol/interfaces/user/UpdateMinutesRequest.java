package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMinutesRequest {

    /**
     * Minutos a adicionar/subtrair no formato "HH:mm:ss".
     * Valores negativos são permitidos para subtração (ex: "-0:05:00").
     */
    @NotNull(message = "Minutos jogados e obrigatório")
    @Pattern(regexp = "^-?\\d{1,2}:\\d{2}:\\d{2}$", message = "Formato de minutos invalido. Use HH:mm:ss")
    private String minutesToAdd;
}

