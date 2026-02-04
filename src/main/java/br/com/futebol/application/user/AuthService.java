package br.com.futebol.application.user;

import br.com.futebol.core.exceptions.UnauthorizedException;
import br.com.futebol.core.security.JwtService;
import br.com.futebol.core.security.PasswordService;
import br.com.futebol.domain.user.User;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.auth.LoginRequest;
import br.com.futebol.interfaces.auth.LoginResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    JwtService jwtService;

    /**
     * @param request os dados de login (email e senha)
     * @return LoginResponse com o token JWT
     * @throws UnauthorizedException se as credenciais forem invalidas
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para o email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Usuario nao encontrado: {}", request.getEmail());
                    return new UnauthorizedException("Credenciais invalidas");
                });

        if (!user.getActive()) {
            log.warn("Tentativa de login de usuario inativo: {}", request.getEmail());
            throw new UnauthorizedException("Usuario inativo");
        }

        if (!passwordService.verifyPassword(request.getPassword(), user.getPassword())) {
            log.warn("Senha invalida para o usuario: {}", request.getEmail());
            throw new UnauthorizedException("Credenciais invalidas");
        }

        String token = jwtService.generateToken(user);
        log.info("Login realizado com sucesso para: {}", request.getEmail());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(86400L)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .profile(user.getProfile())
                        .build())
                .build();
    }
}

