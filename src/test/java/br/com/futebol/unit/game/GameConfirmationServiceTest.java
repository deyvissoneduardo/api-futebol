package br.com.futebol.unit.game;

import br.com.futebol.application.game.GameConfirmationService;
import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ConflictException;
import br.com.futebol.core.exceptions.ForbiddenException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.game.GameConfirmation;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameConfirmationRepository;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.ConfirmNameRequest;
import br.com.futebol.interfaces.game.GameConfirmationResponse;
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
@DisplayName("GameConfirmationService - Testes Unitários")
class GameConfirmationServiceTest {

    @Inject
    GameConfirmationService gameConfirmationService;

    @InjectMock
    GameConfirmationRepository gameConfirmationRepository;

    @InjectMock
    GameRepository gameRepository;

    @InjectMock
    UserRepository userRepository;

    private Game mockReleasedGame;
    private Game mockNotReleasedGame;
    private Game mockStartedGame;
    private User mockAdminUser;
    private User mockJogadorUser;
    private GameConfirmation mockConfirmation;
    private UUID gameId;
    private UUID adminUserId;
    private UUID jogadorUserId;
    private String confirmedName;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        jogadorUserId = UUID.randomUUID();
        confirmedName = "João Silva";

        mockReleasedGame = Game.builder()
                .id(gameId)
                .gameDate(OffsetDateTime.now().plusDays(7))
                .released(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockNotReleasedGame = Game.builder()
                .id(gameId)
                .gameDate(OffsetDateTime.now().plusDays(7))
                .released(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockStartedGame = Game.builder()
                .id(gameId)
                .gameDate(OffsetDateTime.now().minusHours(1))
                .released(true)
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

        mockConfirmation = GameConfirmation.builder()
                .id(UUID.randomUUID())
                .gameId(gameId)
                .userId(adminUserId)
                .confirmedName(confirmedName)
                .confirmedAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Testes de Confirmar Nome")
    class ConfirmNameTests {

        @Test
        @DisplayName("Deve confirmar nome com sucesso quando lista estiver liberada e jogo no futuro")
        void deveConfirmarNomeComSucessoQuandoListaEstiverLiberadaEJogoNoFuturo() {
            // Arrange
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName(confirmedName)
                    .build();

            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockReleasedGame));
            when(gameConfirmationRepository.existsByGameIdAndUserId(gameId, adminUserId)).thenReturn(false);
            when(gameConfirmationRepository.existsByGameIdAndConfirmedName(gameId, confirmedName)).thenReturn(false);
            when(gameConfirmationRepository.persist(any(GameConfirmation.class))).thenAnswer(invocation -> {
                GameConfirmation confirmation = invocation.getArgument(0);
                confirmation.setId(UUID.randomUUID());
                return confirmation;
            });

            // Act
            GameConfirmationResponse result = gameConfirmationService.confirmName(gameId, request, adminUserId);

            // Assert
            assertNotNull(result);
            assertEquals(confirmedName, result.getConfirmedName());
            assertEquals(gameId, result.getGameId());
            assertEquals(adminUserId, result.getUserId());
            verify(gameRepository).findById(gameId);
            verify(gameConfirmationRepository).existsByGameIdAndUserId(gameId, adminUserId);
            verify(gameConfirmationRepository).existsByGameIdAndConfirmedName(gameId, confirmedName);
            verify(gameConfirmationRepository).persist(any(GameConfirmation.class));
        }

        @Test
        @DisplayName("Deve lançar ForbiddenException quando lista não estiver liberada")
        void deveLancarForbiddenExceptionQuandoListaNaoEstiverLiberada() {
            // Arrange
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName(confirmedName)
                    .build();

            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockNotReleasedGame));

            // Act & Assert
            assertThrows(ForbiddenException.class, () -> {
                gameConfirmationService.confirmName(gameId, request, adminUserId);
            });

            verify(gameRepository).findById(gameId);
            verify(gameConfirmationRepository, never()).persist(any(GameConfirmation.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando jogo já iniciou")
        void deveLancarBusinessExceptionQuandoJogoJaIniciou() {
            // Arrange
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName(confirmedName)
                    .build();

            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockStartedGame));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                gameConfirmationService.confirmName(gameId, request, adminUserId);
            });

            assertTrue(exception.getMessage().contains("Lista encerrada"));
            verify(gameRepository).findById(gameId);
            verify(gameConfirmationRepository, never()).persist(any(GameConfirmation.class));
        }

        @Test
        @DisplayName("Deve lançar ConflictException quando nome já estiver confirmado")
        void deveLancarConflictExceptionQuandoNomeJaEstiverConfirmado() {
            // Arrange
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName(confirmedName)
                    .build();

            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockReleasedGame));
            when(gameConfirmationRepository.existsByGameIdAndUserId(gameId, adminUserId)).thenReturn(false);
            when(gameConfirmationRepository.existsByGameIdAndConfirmedName(gameId, confirmedName)).thenReturn(true);

            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                gameConfirmationService.confirmName(gameId, request, adminUserId);
            });

            assertTrue(exception.getMessage().contains("Nome já confirmado"));
            verify(gameConfirmationRepository, never()).persist(any(GameConfirmation.class));
        }

        @Test
        @DisplayName("Deve lançar ConflictException quando usuário já confirmou")
        void deveLancarConflictExceptionQuandoUsuarioJaConfirmou() {
            // Arrange
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName(confirmedName)
                    .build();

            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockReleasedGame));
            when(gameConfirmationRepository.existsByGameIdAndUserId(gameId, adminUserId)).thenReturn(true);

            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                gameConfirmationService.confirmName(gameId, request, adminUserId);
            });

            assertTrue(exception.getMessage().contains("já confirmou"));
            verify(gameConfirmationRepository, never()).persist(any(GameConfirmation.class));
        }
    }

    @Nested
    @DisplayName("Testes de Listar Confirmações")
    class ListConfirmationsTests {

        @Test
        @DisplayName("Deve listar confirmações quando usuário for ADMIN")
        void deveListarConfirmacoesQuandoUsuarioForAdmin() {
            // Arrange
            when(userRepository.findById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockReleasedGame));
            when(gameConfirmationRepository.findByGameId(gameId)).thenReturn(List.of(mockConfirmation));

            // Act
            var result = gameConfirmationService.listConfirmations(gameId, adminUserId);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(gameId, result.getGameId());
            verify(userRepository).findById(adminUserId);
            verify(gameRepository).findById(gameId);
            verify(gameConfirmationRepository).findByGameId(gameId);
        }

        @Test
        @DisplayName("Deve lançar ForbiddenException quando JOGADOR tentar listar confirmações")
        void deveLancarForbiddenExceptionQuandoJogadorTentarListarConfirmacoes() {
            // Arrange
            when(userRepository.findById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));

            // Act & Assert
            assertThrows(ForbiddenException.class, () -> {
                gameConfirmationService.listConfirmations(gameId, jogadorUserId);
            });

            verify(userRepository).findById(jogadorUserId);
            verify(gameConfirmationRepository, never()).findByGameId(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("Testes de Minha Confirmação")
    class FindMyConfirmationTests {

        @Test
        @DisplayName("Deve retornar confirmação quando usuário tiver confirmado")
        void deveRetornarConfirmacaoQuandoUsuarioTiverConfirmado() {
            // Arrange
            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockReleasedGame));
            when(gameConfirmationRepository.findByGameIdAndUserId(gameId, adminUserId))
                    .thenReturn(Optional.of(mockConfirmation));

            // Act
            GameConfirmationResponse result = gameConfirmationService.findMyConfirmation(gameId, adminUserId);

            // Assert
            assertNotNull(result);
            assertEquals(mockConfirmation.getId(), result.getId());
            assertEquals(confirmedName, result.getConfirmedName());
            verify(gameRepository).findById(gameId);
            verify(gameConfirmationRepository).findByGameIdAndUserId(gameId, adminUserId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando usuário não tiver confirmado")
        void deveLancarResourceNotFoundExceptionQuandoUsuarioNaoTiverConfirmado() {
            // Arrange
            when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockReleasedGame));
            when(gameConfirmationRepository.findByGameIdAndUserId(gameId, adminUserId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                gameConfirmationService.findMyConfirmation(gameId, adminUserId);
            });

            assertTrue(exception.getMessage().contains("ainda não confirmou"));
        }
    }
}

