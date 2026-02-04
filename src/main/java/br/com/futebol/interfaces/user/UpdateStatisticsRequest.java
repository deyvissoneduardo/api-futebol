package br.com.futebol.interfaces.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatisticsRequest {

    @Pattern(regexp = "^-?\\d{1,2}:\\d{2}:\\d{2}$", message = "Formato de minutos invalido. Use HH:mm:ss")
    private String minutesPlayed;

    @Min(value = 0, message = "Gols nao pode ser negativo")
    private Integer goals;

    @Min(value = 0, message = "Reclamações nao pode ser negativo")
    private Integer complaints;

    @Min(value = 0, message = "Vitórias nao pode ser negativo")
    private Integer victories;

    @Min(value = 0, message = "Empates nao pode ser negativo")
    private Integer draws;

    @Min(value = 0, message = "Derrotas nao pode ser negativo")
    private Integer defeats;
}

