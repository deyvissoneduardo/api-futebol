package br.com.futebol.interfaces.user;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de resposta para estatísticas de usuário.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsResponse {

    private UUID id;
    private UUID userId;
    private String minutesPlayed; // Formato "HH:mm:ss"
    private Integer goals;
    private Integer complaints;
    private Integer victories;
    private Integer draws;
    private Integer defeats;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

