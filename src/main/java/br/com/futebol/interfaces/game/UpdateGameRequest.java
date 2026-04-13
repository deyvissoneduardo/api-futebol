package br.com.futebol.interfaces.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGameRequest {

    @NotBlank(message = "Nome é obrigatorio")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
    private String name;

    @NotBlank(message = "Data de inicio é obrigatoria")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Data deve estar no formato yyyy-MM-dd")
    private String startDate;

    @NotBlank(message = "Hora de inicio é obrigatoria")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "Hora deve estar no formato HH:mm")
    private String startHour;
}
