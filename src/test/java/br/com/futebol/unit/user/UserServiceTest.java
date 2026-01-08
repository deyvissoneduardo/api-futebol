package br.com.futebol.unit.user;

import br.com.futebol.application.user.UserService;
import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.core.security.PasswordService;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.user.CreateUserRequest;
import br.com.futebol.interfaces.user.UpdateUserRequest;
import br.com.futebol.interfaces.user.UserResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("UserService - Testes Unitários")
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    PasswordService passwordService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .fullName("João Silva")
                .email("joao@email.com")
                .password("hashedPassword")
                .profile(UserProfile.JOGADOR)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Testes de Listagem de Usuários")
    class FindAllTests {

        @Test
        @DisplayName("Deve retornar lista de usuários ativos")
        void deveRetornarListaDeUsuariosAtivos() {
            // Arrange
            when(userRepository.findAllActive()).thenReturn(List.of(mockUser));

            // Act
            List<UserResponse> result = userService.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(mockUser.getEmail(), result.get(0).getEmail());
            verify(userRepository).findAllActive();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver usuários")
        void deveRetornarListaVaziaQuandoNaoHouverUsuarios() {
            // Arrange
            when(userRepository.findAllActive()).thenReturn(List.of());

            // Act
            List<UserResponse> result = userService.findAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes de Busca por ID")
    class FindByIdTests {

        @Test
        @DisplayName("Deve retornar usuário quando encontrado")
        void deveRetornarUsuarioQuandoEncontrado() {
            // Arrange
            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(mockUser));

            // Act
            UserResponse result = userService.findById(userId);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser.getId(), result.getId());
            assertEquals(mockUser.getEmail(), result.getEmail());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            // Arrange
            when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
        }
    }

    @Nested
    @DisplayName("Testes de Criação de Usuário")
    class CreateTests {

        @Test
        @DisplayName("Deve criar usuário com sucesso")
        void deveCriarUsuarioComSucesso() {
            // Arrange
            CreateUserRequest request = CreateUserRequest.builder()
                    .fullName("Maria Santos")
                    .email("maria@email.com")
                    .password("senha123")
                    .profile(UserProfile.JOGADOR)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
            doNothing().when(userRepository).persist(any(User.class));

            // Act
            UserResponse result = userService.create(request);

            // Assert
            assertNotNull(result);
            assertEquals(request.getFullName(), result.getFullName());
            assertEquals(request.getEmail(), result.getEmail());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).persist(userCaptor.capture());
            assertEquals("hashedPassword", userCaptor.getValue().getPassword());
        }

        @Test
        @DisplayName("Deve lançar exceção quando e-mail já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            // Arrange
            CreateUserRequest request = CreateUserRequest.builder()
                    .fullName("Maria Santos")
                    .email("joao@email.com")
                    .password("senha123")
                    .build();

            when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> userService.create(request));
            assertEquals("E-mail já está em uso", exception.getMessage());
        }

        @Test
        @DisplayName("Deve usar perfil padrão JOGADOR quando não informado")
        void deveUsarPerfilPadraoQuandoNaoInformado() {
            // Arrange
            CreateUserRequest request = CreateUserRequest.builder()
                    .fullName("Pedro Lima")
                    .email("pedro@email.com")
                    .password("senha123")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");

            // Act
            UserResponse result = userService.create(request);

            // Assert
            assertEquals(UserProfile.JOGADOR, result.getProfile());
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Usuário")
    class UpdateTests {

        @Test
        @DisplayName("Deve atualizar usuário com sucesso")
        void deveAtualizarUsuarioComSucesso() {
            // Arrange
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .fullName("João Silva Atualizado")
                    .build();

            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(mockUser));

            // Act
            UserResponse result = userService.update(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals("João Silva Atualizado", result.getFullName());
            verify(userRepository).persist(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar e-mail já em uso")
        void deveLancarExcecaoAoAtualizarEmailJaEmUso() {
            // Arrange
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email("outro@email.com")
                    .build();

            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(mockUser));
            when(userRepository.existsByEmailAndIdNot("outro@email.com", userId)).thenReturn(true);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> userService.update(userId, request));
            assertEquals("E-mail já está em uso", exception.getMessage());
        }

        @Test
        @DisplayName("Deve atualizar senha quando informada")
        void deveAtualizarSenhaQuandoInformada() {
            // Arrange
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .password("novaSenha123")
                    .build();

            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(mockUser));
            when(passwordService.hashPassword("novaSenha123")).thenReturn("novoHash");

            // Act
            userService.update(userId, request);

            // Assert
            verify(passwordService).hashPassword("novaSenha123");
            assertEquals("novoHash", mockUser.getPassword());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Usuário")
    class DeleteTests {

        @Test
        @DisplayName("Deve desativar usuário com sucesso (soft delete)")
        void deveDesativarUsuarioComSucesso() {
            // Arrange
            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(mockUser));

            // Act
            userService.delete(userId);

            // Assert
            assertFalse(mockUser.getActive());
            verify(userRepository).persist(mockUser);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar usuário não encontrado")
        void deveLancarExcecaoAoDeletarUsuarioNaoEncontrado() {
            // Arrange
            when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> userService.delete(userId));
        }
    }
}

