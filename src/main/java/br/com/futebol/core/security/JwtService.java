package br.com.futebol.core.security;

import br.com.futebol.domain.user.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "86400")
    long tokenLifespan;

    /**
     * @param user o usuário autenticado
     * @return o token JWT gerado
     */
    public String generateToken(User user) {
        Set<String> roles = new HashSet<>();
        roles.add(user.getProfile().name());

        return Jwt.issuer(issuer)
                .subject(user.getId().toString())
                .upn(user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("profile", user.getProfile().name())
                .groups(roles)
                .expiresIn(Duration.ofSeconds(tokenLifespan))
                .sign();
    }
}

