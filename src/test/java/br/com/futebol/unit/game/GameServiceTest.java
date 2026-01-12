package br.com.futebol.unit.game;

import br.com.futebol.application.game.GameService;
import br.com.futebol.core.exceptions.ForbiddenException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.CreateGameRequest;
import br.com.futebol.interfaces.game.GameResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("GameService - Testes Unitários")
class GameServiceTest {

    @Inject
    GameService gameService;

    @InjectMock
    GameRepository gameRepository;

    @InjectMock
    UserRepository userRepository;

    private Game mockGame;
    private User mockAdminUser;
    private User mockJogadorUser;
    private UUID gameId;
    private UUID adminUserId;
    private UUID jogadorUserId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        jogadorUserId = UUID.randomUUID();

        mockGame = Game.builder()
                .id(gameId)
                .gameDate(OffsetDateTime.now().plusDays(7))
                .released(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockAdminUser = User.builder()
                .id(adminUserId)
                .fullName("Admin")
                .email("admin@email.com")
                .profile(UserProfile.ADMIN)
                .active(true)
                .build();

        mockJogadorUser = User.builder()
                .id(jogadorUserId)
                .fullName("Jogador")
                .email("jogador@email.com")
                .profile(UserProfile.JOGADOR)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Testes de Criar Jogo")
    class CreateGameTests {

        @Test
        @DisplayName("Deve criar jogo com sucesso quando usuário for ADMIN")
        void deveCriarJogoComSucessoQuandoUsuarioForAdmin() {
            // Arrange
            CreateGameRequest request = CreateGameRequest.builder()
                    .gameDate(OffsetDateTime.now().plusDays(7))
                    .build();

            when(userRepository.findById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(gameRepository.persist(any(Game.class))).thenAnswer(invocation -> {
                Game game = invocation.getArgument(0);
                game.setId(gameId);
                return game;
            });

            // Act
            GameResponse result = gameService.create(request, adminUserId);

            // Assert
            assertNotNull(result);
            assertEquals(request.getGameDate(), result.getGameDate());
            assertFalse(result.getReleased());
            verify(userRepository).findById(adminUserId);
            verify(gameRepository).persist(any(Game.class));
        }

        @Test
        @DisplayName("Deve lançar ForbiddenException quando JOGADOR tentar criar jogo")
        void deveLancarForbiddenExceptionQuandoJogadorTentarCriarJogo() {
            // Arrange
            CreateGameRequest request = CreateGameRequest.builder()
                    .gameDate(OffsetDateTime.now().plusDays(7))
                    .build();

            when(userRepository.findById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));

            // Act & Assert
            assertThrows(ForbiddenException.class, () -> {
                gameService.create(request, jogadorUserId);
            });

            verify(userRepository).findById(jogadorUserId);
            verify(gameRepository, never()).persist(any(Game.class));
        }
    }

    @Nested
    @DisplayName("Testes de Liberar Lista")
    class ReleaseGameTests {

        @Test
        @DisplayName("Deve liberar lista com sucesso quando usuário for ADMIN")
        void deveLiberarListaComSucessoQuandoUsuarioForAdmin() {
            // Arrange
            when(userRepository.findById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
            when(gameRepository.persist(any(Game.class))).thenReturn(mockGame);

            // Act
            GameResponse result = gameService.releaseGame(gameId, adminUserId);

            // Assert
            assertNotNull(result);
            assertTrue(result.getReleased());
            verify(userRepository).findById(adminUserId);
            verify(gameRepository).findById(gameId);
            verify(gameRepository).persist(any(Game.class));
        }

        @Test
        @DisplayName("Deve lançar ForbiddenException quando JOGADOR tentar liberar lista")
        void deveLancarForbiddenExceptionQuandoJogadorTentarLiberarLista() {
            // Arrange
            when(userRepository.findById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));

            // Act & Assert
            assertThrows(ForbiddenException.class, () -> {
                gameService.releaseGame(gameId, jogadorUserId);
            });

            verify(userRepository).findById(jogadorUserId);
            verify(gameRepository, never()).findById(any(UUID.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando jogo não existir")
        void deveLancarResourceNotFoundExceptionQuandoJogoNaoExistir() {
            // Arrange
            UUID nonExistentGameId = UUID.randomUUID();
            when(userRepository.findById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(gameRepository.findById(nonExistentGameId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                gameService.releaseGame(nonExistentGameId, adminUserId);
            });
        }
    }

    @Nested
    @DisplayName("Testes de Busca")
    class FindTests {

        @Test
        @DisplayName("Deve buscar jogo por ID com sucesso")
        void deveBuscarJogoPorIdComSucesso() {
            // Arrange
            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));

            // Act
            GameResponse result = gameService.findById(gameId);

            // Assert
            assertNotNull(result);
            assertEquals(gameId, result.getId());
            verify(gameRepository).findById(gameId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando jogo não existir")
        void deveLancarResourceNotFoundExceptionQuandoJogoNaoExistir() {
            // Arrange
            UUID nonExistentGameId = UUID.randomUUID();
            when(gameRepository.findById(nonExistentGameId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                gameService.findById(nonExistentGameId);
            });
        }

        @Test
        @DisplayName("Deve listar todos os jogos")
        void deveListarTodosOsJogos() {
            // Arrange
            when(gameRepository.findAllOrderedByDate()).thenReturn(List.of(mockGame));

            // Act
            List<GameResponse> result = gameService.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(gameRepository).findAllOrderedByDate();
        }
    }
}

