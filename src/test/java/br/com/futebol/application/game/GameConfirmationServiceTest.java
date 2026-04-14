package br.com.futebol.application.game;

import br.com.futebol.application.user.UserService;
import br.com.futebol.core.exceptions.ConflictException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.game.GameConfirmation;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameConfirmationRepository;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.AddConfirmedPlayerRequest;
import br.com.futebol.interfaces.game.GameConfirmationResponse;
import br.com.futebol.interfaces.game.GamePlayerSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameConfirmationServiceTest {

    private final GameConfirmationRepository gameConfirmationRepository = mock(GameConfirmationRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = mock(UserService.class);

    private GameConfirmationService service;

    @BeforeEach
    void setUp() {
        service = new GameConfirmationService();
        service.gameConfirmationRepository = gameConfirmationRepository;
        service.gameRepository = gameRepository;
        service.userRepository = userRepository;
        service.userService = userService;
    }

    @Test
    void shouldAddExistingConfirmedPlayer() {
        UUID gameId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        when(userRepository.findActiveById(adminId)).thenReturn(Optional.of(user(adminId, "Admin", UserProfile.ADMIN)));
        when(gameRepository.findByIdOptional(gameId)).thenReturn(Optional.of(Game.builder().id(gameId).released(true).build()));
        when(userRepository.findActiveById(playerId)).thenReturn(Optional.of(user(playerId, "Jogador 1", UserProfile.JOGADOR)));
        when(gameConfirmationRepository.existsByGameIdAndUserId(gameId, playerId)).thenReturn(false);
        when(gameConfirmationRepository.existsByGameIdAndConfirmedName(gameId, "Jogador 1")).thenReturn(false);
        doAnswer(invocation -> {
            GameConfirmation confirmation = invocation.getArgument(0);
            confirmation.setId(UUID.randomUUID());
            confirmation.setCreatedAt(OffsetDateTime.now());
            confirmation.setUpdatedAt(OffsetDateTime.now());
            return null;
        }).when(gameConfirmationRepository).persist(any(GameConfirmation.class));

        GameConfirmationResponse response = service.addExistingPlayerConfirmation(
                gameId,
                AddConfirmedPlayerRequest.builder().userId(playerId).build(),
                adminId
        );

        assertEquals(gameId, response.getGameId());
        assertEquals(playerId, response.getUserId());
        assertEquals("Jogador 1", response.getConfirmedName());
        assertFalse(response.getIsGuest());
        assertEquals(adminId, response.getConfirmedByUserId());
        verify(gameConfirmationRepository).persist(any(GameConfirmation.class));
    }

    @Test
    void shouldRejectDuplicateConfirmedPlayer() {
        UUID gameId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        when(userRepository.findActiveById(adminId)).thenReturn(Optional.of(user(adminId, "Admin", UserProfile.ADMIN)));
        when(gameRepository.findByIdOptional(gameId)).thenReturn(Optional.of(Game.builder().id(gameId).released(true).build()));
        when(userRepository.findActiveById(playerId)).thenReturn(Optional.of(user(playerId, "Jogador 1", UserProfile.JOGADOR)));
        when(gameConfirmationRepository.existsByGameIdAndUserId(gameId, playerId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.addExistingPlayerConfirmation(
                gameId,
                AddConfirmedPlayerRequest.builder().userId(playerId).build(),
                adminId
        ));
    }

    @Test
    void shouldFilterAlreadyConfirmedPlayersFromSearch() {
        UUID gameId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID confirmedUserId = UUID.randomUUID();
        UUID availableUserId = UUID.randomUUID();

        when(userRepository.findActiveById(adminId)).thenReturn(Optional.of(user(adminId, "Admin", UserProfile.ADMIN)));
        when(gameRepository.findByIdOptional(gameId)).thenReturn(Optional.of(Game.builder().id(gameId).released(true).build()));
        when(gameConfirmationRepository.findEligibleWorstPlayerByGameId(gameId)).thenReturn(List.of(
                GameConfirmation.builder().gameId(gameId).userId(confirmedUserId).confirmedName("Ja Confirmado").isGuest(false).build()
        ));
        when(userService.searchActivePlayersByName("Jo")).thenReturn(List.of(
                GamePlayerSearchResponse.builder().userId(confirmedUserId).fullName("Ja Confirmado").build(),
                GamePlayerSearchResponse.builder().userId(availableUserId).fullName("Jo Disponivel").build()
        ));

        List<GamePlayerSearchResponse> results = service.searchAvailablePlayers(gameId, "Jo", adminId);

        assertEquals(1, results.size());
        assertEquals(availableUserId, results.get(0).getUserId());
    }

    private User user(UUID id, String name, UserProfile profile) {
        return User.builder()
                .id(id)
                .fullName(name)
                .email(name.toLowerCase().replace(" ", "") + "@mail.com")
                .password("secret")
                .profile(profile)
                .active(true)
                .build();
    }
}
