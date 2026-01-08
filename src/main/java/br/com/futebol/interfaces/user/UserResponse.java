package br.com.futebol.interfaces.user;

import br.com.futebol.domain.user.UserProfile;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de resposta para usuário.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String photo;
    private UserProfile profile;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

