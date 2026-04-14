package br.com.futebol.application.game;

import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ConflictException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.game.GameConfirmation;
import br.com.futebol.domain.game.GameWorstPlayerVote;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameConfirmationRepository;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.game.GameWorstPlayerVoteRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.WorstPlayerVoteRequest;
import br.com.futebol.interfaces.game.WorstPlayerVoteResponse;
import br.com.futebol.interfaces.game.WorstPlayerVotingStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorstPlayerVotingServiceTest {

    private final GameRepository gameRepository = mock(GameRepository.class);
    private final GameConfirmationRepository gameConfirmationRepository = mock(GameConfirmationRepository.class);
    private final GameWorstPlayerVoteRepository voteRepository = mock(GameWorstPlayerVoteRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private WorstPlayerVotingService service;

    @BeforeEach
    void setUp() {
        service = new WorstPlayerVotingService();
        service.gameRepository = gameRepository;
        service.gameConfirmationRepository = gameConfirmationRepository;
        service.gameWorstPlayerVoteRepository = voteRepository;
        service.userRepository = userRepository;
    }

    @Test
    void shouldOpenVotingWhenThereAreEligiblePlayers() {
        UUID gameId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).worstPlayerVotingEnabled(false).released(false).build();

        when(userRepository.findActiveById(adminId)).thenReturn(Optional.of(user(adminId, "Admin", UserProfile.ADMIN)));
        when(gameRepository.findByIdOptional(gameId)).thenReturn(Optional.of(game));
        when(gameConfirmationRepository.findEligibleWorstPlayerByGameId(gameId)).thenReturn(List.of(
                GameConfirmation.builder().id(UUID.randomUUID()).gameId(gameId).userId(UUID.randomUUID()).confirmedName("Jogador").isGuest(false).build()
        ));

        WorstPlayerVotingStatusResponse response = service.openVoting(gameId, adminId);

        assertTrue(response.getVotingEnabled());
        assertNotNull(response.getOpenedAt());
        verify(gameRepository).persist(game);
    }

    @Test
    void shouldAllowSelfVote() {
        UUID gameId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();
        UUID confirmationId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).worstPlayerVotingEnabled(true).build();
        User voter = user(voterId, "Jogador 1", UserProfile.JOGADOR);
        GameConfirmation target = GameConfirmation.builder()
                .id(confirmationId)
                .gameId(gameId)
                .userId(voterId)
                .confirmedName("Jogador 1")
                .isGuest(false)
                .build();

        when(userRepository.findActiveById(voterId)).thenReturn(Optional.of(voter));
        when(gameRepository.findByIdOptional(gameId)).thenReturn(Optional.of(game));
        when(voteRepository.existsByGameIdAndVoterUserId(gameId, voterId)).thenReturn(false);
        when(gameConfirmationRepository.findByIdOptional(confirmationId)).thenReturn(Optional.of(target));
        doAnswer(invocation -> {
            GameWorstPlayerVote vote = invocation.getArgument(0);
            vote.setId(UUID.randomUUID());
            vote.setCreatedAt(OffsetDateTime.now());
            return null;
        }).when(voteRepository).persist(any(GameWorstPlayerVote.class));

        WorstPlayerVoteResponse response = service.vote(
                gameId,
                WorstPlayerVoteRequest.builder().targetConfirmationId(confirmationId).build(),
                voterId
        );

        assertEquals(voterId, response.getVoterUserId());
        assertEquals(confirmationId, response.getTargetConfirmationId());
        verify(voteRepository).persist(any(GameWorstPlayerVote.class));
    }

    @Test
    void shouldRejectSecondVoteFromSameUser() {
        UUID gameId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();
        UUID confirmationId = UUID.randomUUID();

        when(userRepository.findActiveById(voterId)).thenReturn(Optional.of(user(voterId, "Jogador 1", UserProfile.JOGADOR)));
        when(gameRepository.findByIdOptional(gameId)).thenReturn(Optional.of(Game.builder().id(gameId).worstPlayerVotingEnabled(true).build()));
        when(voteRepository.existsByGameIdAndVoterUserId(gameId, voterId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.vote(
                gameId,
                WorstPlayerVoteRequest.builder().targetConfirmationId(confirmationId).build(),
                voterId
        ));
    }

    @Test
    void shouldRejectGuestTarget() {
        UUID gameId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();
        UUID confirmationId = UUID.randomUUID();

        when(userRepository.findActiveById(voterId)).thenReturn(Optional.of(user(voterId, "Jogador 1", UserProfile.JOGADOR)));
        when(gameRepository.findByIdOptional(gameId)).thenReturn(Optional.of(Game.builder().id(gameId).worstPlayerVotingEnabled(true).build()));
        when(voteRepository.existsByGameIdAndVoterUserId(gameId, voterId)).thenReturn(false);
        when(gameConfirmationRepository.findByIdOptional(confirmationId)).thenReturn(Optional.of(
                GameConfirmation.builder().id(confirmationId).gameId(gameId).isGuest(true).confirmedName("Convidado").build()
        ));

        assertThrows(BusinessException.class, () -> service.vote(
                gameId,
                WorstPlayerVoteRequest.builder().targetConfirmationId(confirmationId).build(),
                voterId
        ));
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
