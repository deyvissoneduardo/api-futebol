package br.com.futebol.interfaces.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "E-mail é obrigatorio")
    @Email(message = "E-mail invalido")
    private String email;

    @NotBlank(message = "Senha e obrigatoria")
    private String password;
}

