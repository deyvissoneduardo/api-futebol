package br.com.futebol.interfaces.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmNameRequest {

    @NotBlank(message = "Nome confirmado e obrigatorio")
    @Size(max = 255, message = "Nome confirmado deve ter no máximo 255 caracteres")
    private String confirmedName;

    /**
     * Indica se a confirmação é para um convidado.
     * Quando true, o sistema cria um UUID único para o convidado.
     */
    @Builder.Default
    private Boolean isGuest = false;
}

