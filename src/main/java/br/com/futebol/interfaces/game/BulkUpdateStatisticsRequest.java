package br.com.futebol.interfaces.game;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * DTO de requisição para atualização em lote de estatísticas de jogadores confirmados em um jogo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUpdateStatisticsRequest {

    @NotEmpty(message = "Lista de estatísticas não pode estar vazia")
    @Valid
    private List<PlayerStatisticsUpdate> statistics;

    /**
     * DTO interno para atualização de estatísticas de um jogador.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerStatisticsUpdate {

        @NotNull(message = "userId é obrigatório")
        private UUID userId;

        @Pattern(regexp = "^-?\\d{1,2}:\\d{2}:\\d{2}$", message = "Formato de minutos inválido. Use HH:mm:ss")
        private String minutesPlayed;

        @Min(value = 0, message = "Gols não pode ser negativo")
        private Integer goals;

        @Min(value = 0, message = "Reclamações não pode ser negativo")
        private Integer complaints;

        @Min(value = 0, message = "Vitórias não pode ser negativo")
        private Integer victories;

        @Min(value = 0, message = "Empates não pode ser negativo")
        private Integer draws;

        @Min(value = 0, message = "Derrotas não pode ser negativo")
        private Integer defeats;
    }
}

