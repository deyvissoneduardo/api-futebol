package br.com.futebol.interfaces.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGameRequest {

    @NotBlank(message = "Data de inicio é obrigatoria")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Data deve estar no formato yyyy-MM-dd")
    private String startDate;

    @NotBlank(message = "Hora de inicio é obrigatoria")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "Hora deve estar no formato HH:mm")
    private String startHour;
}

