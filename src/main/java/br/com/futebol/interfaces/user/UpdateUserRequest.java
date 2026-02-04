package br.com.futebol.interfaces.user;

import br.com.futebol.domain.user.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String fullName;

    @Email(message = "E-mail invalido")
    private String email;

    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    private String password;

    @Size(max = 500, message = "URL da foto deve ter no máximo 500 caracteres")
    private String photo;

    private UserProfile profile;
}

