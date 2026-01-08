package br.com.futebol.unit.auth;

import br.com.futebol.application.user.AuthService;
import br.com.futebol.core.exceptions.UnauthorizedException;
import br.com.futebol.core.security.JwtService;
import br.com.futebol.core.security.PasswordService;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.auth.LoginRequest;
import br.com.futebol.interfaces.auth.LoginResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("AuthService - Testes Unitários")
class AuthServiceTest {

    @Inject
    AuthService authService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    PasswordService passwordService;

    @InjectMock
    JwtService jwtService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .fullName("Admin User")
                .email("admin@email.com")
                .password("hashedPassword")
                .profile(UserProfile.ADMIN)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Testes de Login")
    class LoginTests {

        @Test
        @DisplayName("Deve realizar login com sucesso")
        void deveRealizarLoginComSucesso() {
            // Arrange
            LoginRequest request = LoginRequest.builder()
                    .email("admin@email.com")
                    .password("senha123")
                    .build();

            when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(mockUser));
            when(passwordService.verifyPassword("senha123", "hashedPassword")).thenReturn(true);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token.here");

            // Act
            LoginResponse result = authService.login(request);

            // Assert
            assertNotNull(result);
            assertEquals("jwt.token.here", result.getToken());
            assertEquals("Bearer", result.getType());
            assertNotNull(result.getUser());
            assertEquals(mockUser.getId(), result.getUser().getId());
            assertEquals(mockUser.getFullName(), result.getUser().getFullName());
            assertEquals(mockUser.getEmail(), result.getUser().getEmail());
            assertEquals(mockUser.getProfile(), result.getUser().getProfile());
        }

        @Test
        @DisplayName("Deve lançar exceção para usuário não encontrado")
        void deveLancarExcecaoParaUsuarioNaoEncontrado() {
            // Arrange
            LoginRequest request = LoginRequest.builder()
                    .email("inexistente@email.com")
                    .password("senha123")
                    .build();

            when(userRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

            // Act & Assert
            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertEquals("Credenciais inválidas", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção para usuário inativo")
        void deveLancarExcecaoParaUsuarioInativo() {
            // Arrange
            mockUser.setActive(false);

            LoginRequest request = LoginRequest.builder()
                    .email("admin@email.com")
                    .password("senha123")
                    .build();

            when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(mockUser));

            // Act & Assert
            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertEquals("Usuário inativo", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção para senha inválida")
        void deveLancarExcecaoParaSenhaInvalida() {
            // Arrange
            LoginRequest request = LoginRequest.builder()
                    .email("admin@email.com")
                    .password("senhaErrada")
                    .build();

            when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(mockUser));
            when(passwordService.verifyPassword("senhaErrada", "hashedPassword")).thenReturn(false);

            // Act & Assert
            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertEquals("Credenciais inválidas", exception.getMessage());
        }

        @Test
        @DisplayName("Deve retornar token com informações corretas do usuário")
        void deveRetornarTokenComInformacoesCorretasDoUsuario() {
            // Arrange
            LoginRequest request = LoginRequest.builder()
                    .email("admin@email.com")
                    .password("senha123")
                    .build();

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
            when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateToken(any(User.class))).thenReturn("generated.jwt.token");

            // Act
            LoginResponse result = authService.login(request);

            // Assert
            assertNotNull(result.getUser());
            assertEquals(UserProfile.ADMIN, result.getUser().getProfile());
            assertEquals(86400L, result.getExpiresIn());
        }
    }
}

