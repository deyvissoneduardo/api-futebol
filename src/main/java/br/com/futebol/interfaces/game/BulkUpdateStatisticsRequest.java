package br.com.futebol.interfaces.game;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUpdateStatisticsRequest {

    @NotEmpty(message = "Lista de estatisticas nao pode estar vazia")
    @Valid
    private List<PlayerStatisticsUpdate> statistics;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerStatisticsUpdate {

        @NotNull(message = "userId é obrigatorio")
        private UUID userId;

        @Pattern(regexp = "^-?\\d{1,2}:\\d{2}:\\d{2}$", message = "Formato de minutos invalido. Use HH:mm:ss")
        private String minutesPlayed;

        @Min(value = 0, message = "Gols nao pode ser negativo")
        private Integer goals;

        @Min(value = 0, message = "Reclamacoes nao pode ser negativo")
        private Integer complaints;

        @Min(value = 0, message = "Vitorias nao pode ser negativo")
        private Integer victories;

        @Min(value = 0, message = "Empates nao pode ser negativo")
        private Integer draws;

        @Min(value = 0, message = "Derrotas nao pode ser negativo")
        private Integer defeats;
    }
}

