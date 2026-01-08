package br.com.futebol.interfaces.auth;

import br.com.futebol.domain.user.UserProfile;
import lombok.*;

import java.util.UUID;

/**
 * DTO de resposta para login.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String type;
    private Long expiresIn;
    private UserInfo user;

    /**
     * Informações básicas do usuário logado.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private UUID id;
        private String fullName;
        private String email;
        private UserProfile profile;
    }
}

