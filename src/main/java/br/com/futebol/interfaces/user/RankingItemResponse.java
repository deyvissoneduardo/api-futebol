package br.com.futebol.interfaces.user;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingItemResponse {

    private Integer position;
    private UUID userId;
    private String userName;
    private String userEmail;
    private Long value; // Valor da estatística (gols, minutos, etc)
    private String formattedValue; // Valor formatado para exibição (ex: "01:05:30" para minutos)
}

