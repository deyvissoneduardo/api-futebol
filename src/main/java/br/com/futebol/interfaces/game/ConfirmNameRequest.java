package br.com.futebol.interfaces.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO de requisição para confirmação de nome em jogo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmNameRequest {

    @NotBlank(message = "Nome confirmado é obrigatório")
    @Size(max = 255, message = "Nome confirmado deve ter no máximo 255 caracteres")
    private String confirmedName;
}

