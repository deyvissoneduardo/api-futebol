package br.com.futebol.interfaces.game;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamePlayerSearchResponse {

    private UUID userId;

    private String fullName;
}
