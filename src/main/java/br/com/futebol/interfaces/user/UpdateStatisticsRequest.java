package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * DTO de requisição para atualização de estatísticas de usuário.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatisticsRequest {

    /**
     * Minutos a adicionar/subtrair no formato "HH:mm:ss".
     * Valores negativos são permitidos para subtração (ex: "-0:05:00").
     */
    @Pattern(regexp = "^-?\\d{1,2}:\\d{2}:\\d{2}$", message = "Formato de minutos inválido. Use HH:mm:ss")
    private String minutesPlayed;

    /**
     * Novo valor de gols (não pode ser negativo).
     */
    @Min(value = 0, message = "Gols não pode ser negativo")
    private Integer goals;

    /**
     * Novo valor de reclamações (não pode ser negativo).
     */
    @Min(value = 0, message = "Reclamações não pode ser negativo")
    private Integer complaints;

    /**
     * Novo valor de vitórias (não pode ser negativo).
     */
    @Min(value = 0, message = "Vitórias não pode ser negativo")
    private Integer victories;

    /**
     * Novo valor de empates (não pode ser negativo).
     */
    @Min(value = 0, message = "Empates não pode ser negativo")
    private Integer draws;

    /**
     * Novo valor de derrotas (não pode ser negativo).
     */
    @Min(value = 0, message = "Derrotas não pode ser negativo")
    private Integer defeats;
}

