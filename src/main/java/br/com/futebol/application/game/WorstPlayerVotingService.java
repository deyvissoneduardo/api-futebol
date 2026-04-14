package br.com.futebol.application.game;

import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ConflictException;
import br.com.futebol.core.exceptions.ForbiddenException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.game.GameConfirmation;
import br.com.futebol.domain.game.GameWorstPlayerVote;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameConfirmationRepository;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.game.GameWorstPlayerVoteRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.WorstPlayerCandidateResponse;
import br.com.futebol.interfaces.game.WorstPlayerHistoricalRankingResponse;
import br.com.futebol.interfaces.game.WorstPlayerRankingItemResponse;
import br.com.futebol.interfaces.game.WorstPlayerRankingResponse;
import br.com.futebol.interfaces.game.WorstPlayerVoteRequest;
import br.com.futebol.interfaces.game.WorstPlayerVoteResponse;
import br.com.futebol.interfaces.game.WorstPlayerVotersResponse;
import br.com.futebol.interfaces.game.WorstPlayerVotingStatusResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class WorstPlayerVotingService {

    @Inject
    GameRepository gameRepository;

    @Inject
    GameConfirmationRepository gameConfirmationRepository;

    @Inject
    GameWorstPlayerVoteRepository gameWorstPlayerVoteRepository;

    @Inject
    UserRepository userRepository;

    @Transactional
    public WorstPlayerVotingStatusResponse openVoting(UUID gameId, UUID requesterUserId) {
        validateAdminPermission(requesterUserId);

        Game game = findGame(gameId);
        if (Boolean.TRUE.equals(game.getWorstPlayerVotingEnabled())) {
            throw new ConflictException("Votacao de pior do jogo ja esta aberta");
        }
        if (game.getWorstPlayerVotingClosedAt() != null) {
            throw new ConflictException("Votacao de pior do jogo ja foi encerrada");
        }

        List<GameConfirmation> candidates = gameConfirmationRepository.findEligibleWorstPlayerByGameId(gameId);
        if (candidates.isEmpty()) {
            throw new BusinessException("Nao ha jogadores elegiveis confirmados para abrir a votacao");
        }

        game.setWorstPlayerVotingEnabled(true);
        game.setWorstPlayerVotingOpenedAt(OffsetDateTime.now());
        game.setWorstPlayerVotingClosedAt(null);
        gameRepository.persist(game);

        return toStatusResponse(game);
    }

    public List<WorstPlayerCandidateResponse> listCandidates(UUID gameId, UUID requesterUserId) {
        validateViewPermission(requesterUserId);
        findGame(gameId);

        return gameConfirmationRepository.findEligibleWorstPlayerByGameId(gameId).stream()
                .map(this::toCandidateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorstPlayerVoteResponse vote(UUID gameId, WorstPlayerVoteRequest request, UUID requesterUserId) {
        User voter = validateVoterPermission(requesterUserId);
        Game game = findGame(gameId);

        if (!Boolean.TRUE.equals(game.getWorstPlayerVotingEnabled())) {
            throw new ForbiddenException("Votacao de pior do jogo nao esta aberta");
        }
        if (game.getWorstPlayerVotingClosedAt() != null) {
            throw new ForbiddenException("Votacao de pior do jogo ja foi encerrada");
        }
        if (gameWorstPlayerVoteRepository.existsByGameIdAndVoterUserId(gameId, requesterUserId)) {
            throw new ConflictException("Usuario ja votou neste jogo");
        }

        GameConfirmation target = gameConfirmationRepository.findByIdOptional(request.getTargetConfirmationId())
                .orElseThrow(() -> new ResourceNotFoundException("Confirmacao", "id", request.getTargetConfirmationId()));

        if (!target.getGameId().equals(gameId)) {
            throw new BusinessException("Confirmacao informada nao pertence ao jogo");
        }
        if (Boolean.TRUE.equals(target.getIsGuest())) {
            throw new BusinessException("Convidados nao participam da votacao de pior do jogo");
        }

        GameWorstPlayerVote vote = GameWorstPlayerVote.builder()
                .gameId(gameId)
                .voterUserId(requesterUserId)
                .voterNameSnapshot(voter.getFullName())
                .targetConfirmationId(target.getId())
                .build();

        gameWorstPlayerVoteRepository.persist(vote);

        return WorstPlayerVoteResponse.builder()
                .voteId(vote.getId())
                .gameId(vote.getGameId())
                .voterUserId(vote.getVoterUserId())
                .targetConfirmationId(vote.getTargetConfirmationId())
                .createdAt(vote.getCreatedAt())
                .build();
    }

    @Transactional
    public WorstPlayerVotingStatusResponse closeVoting(UUID gameId, UUID requesterUserId) {
        validateAdminPermission(requesterUserId);

        Game game = findGame(gameId);
        if (!Boolean.TRUE.equals(game.getWorstPlayerVotingEnabled())) {
            throw new ConflictException("Votacao de pior do jogo nao esta aberta");
        }

        game.setWorstPlayerVotingEnabled(false);
        game.setWorstPlayerVotingClosedAt(OffsetDateTime.now());
        gameRepository.persist(game);

        return toStatusResponse(game);
    }

    public WorstPlayerRankingResponse getGameRanking(UUID gameId, UUID requesterUserId) {
        validateViewPermission(requesterUserId);
        findGame(gameId);

        List<GameWorstPlayerVote> votes = gameWorstPlayerVoteRepository.findByGameId(gameId);
        List<WorstPlayerRankingItemResponse> items = buildRankingItems(votes);

        return WorstPlayerRankingResponse.builder()
                .gameId(gameId)
                .description("Ranking de pior do jogo")
                .items(items)
                .total(items.size())
                .build();
    }

    public WorstPlayerHistoricalRankingResponse getHistoricalRanking(String startDate, String endDate, UUID requesterUserId) {
        validateViewPermission(requesterUserId);

        OffsetDateTime start = parseStartDate(startDate);
        OffsetDateTime end = parseEndDate(endDate);
        if (end.isBefore(start)) {
            throw new BusinessException("Data final deve ser maior ou igual a data inicial");
        }

        List<Game> games = gameRepository.findByGameDateBetween(start, end);
        List<UUID> gameIds = games.stream().map(Game::getId).toList();
        List<GameWorstPlayerVote> votes = gameWorstPlayerVoteRepository.findByGameIds(gameIds);
        List<WorstPlayerRankingItemResponse> items = buildRankingItems(votes);

        return WorstPlayerHistoricalRankingResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .description("Ranking historico de pior do jogo por data do jogo")
                .items(items)
                .total(items.size())
                .build();
    }

    public WorstPlayerVotersResponse getVotersByPlayer(UUID gameId, UUID confirmationId, UUID requesterUserId) {
        validateViewPermission(requesterUserId);
        findGame(gameId);

        GameConfirmation target = gameConfirmationRepository.findByIdOptional(confirmationId)
                .orElseThrow(() -> new ResourceNotFoundException("Confirmacao", "id", confirmationId));

        if (!target.getGameId().equals(gameId)) {
            throw new BusinessException("Confirmacao informada nao pertence ao jogo");
        }
        if (Boolean.TRUE.equals(target.getIsGuest())) {
            throw new BusinessException("Convidados nao participam da votacao de pior do jogo");
        }

        List<GameWorstPlayerVote> votes = gameWorstPlayerVoteRepository.findByGameIdAndTargetConfirmationId(gameId, confirmationId);
        List<String> voterNames = votes.stream()
                .map(GameWorstPlayerVote::getVoterNameSnapshot)
                .collect(Collectors.toList());

        return WorstPlayerVotersResponse.builder()
                .gameId(gameId)
                .confirmationId(confirmationId)
                .playerName(target.getConfirmedName())
                .voterNames(voterNames)
                .total(voterNames.size())
                .build();
    }

    public WorstPlayerVotingStatusResponse getVotingStatus(UUID gameId, UUID requesterUserId) {
        validateViewPermission(requesterUserId);
        return toStatusResponse(findGame(gameId));
    }

    private List<WorstPlayerRankingItemResponse> buildRankingItems(List<GameWorstPlayerVote> votes) {
        if (votes.isEmpty()) {
            return List.of();
        }

        Map<UUID, Long> votesByConfirmationId = votes.stream()
                .collect(Collectors.groupingBy(GameWorstPlayerVote::getTargetConfirmationId, LinkedHashMap::new, Collectors.counting()));

        Set<UUID> confirmationIds = votesByConfirmationId.keySet();
        Map<UUID, GameConfirmation> confirmationsById = gameConfirmationRepository.findByIds(confirmationIds).stream()
                .filter(confirmation -> !Boolean.TRUE.equals(confirmation.getIsGuest()))
                .collect(Collectors.toMap(GameConfirmation::getId, confirmation -> confirmation));

        List<WorstPlayerRankingItemResponse> items = new ArrayList<>();
        votesByConfirmationId.forEach((confirmationId, totalVotes) -> {
            GameConfirmation confirmation = confirmationsById.get(confirmationId);
            if (confirmation != null) {
                items.add(WorstPlayerRankingItemResponse.builder()
                        .gameId(confirmation.getGameId())
                        .confirmationId(confirmation.getId())
                        .userId(confirmation.getUserId())
                        .playerName(confirmation.getConfirmedName())
                        .votes(totalVotes.intValue())
                        .build());
            }
        });

        items.sort(Comparator.comparing(WorstPlayerRankingItemResponse::getVotes).reversed()
                .thenComparing(WorstPlayerRankingItemResponse::getPlayerName));

        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i + 1);
        }

        return items;
    }

    private WorstPlayerCandidateResponse toCandidateResponse(GameConfirmation confirmation) {
        return WorstPlayerCandidateResponse.builder()
                .confirmationId(confirmation.getId())
                .userId(confirmation.getUserId())
                .confirmedName(confirmation.getConfirmedName())
                .build();
    }

    private WorstPlayerVotingStatusResponse toStatusResponse(Game game) {
        return WorstPlayerVotingStatusResponse.builder()
                .gameId(game.getId())
                .votingEnabled(game.getWorstPlayerVotingEnabled())
                .openedAt(game.getWorstPlayerVotingOpenedAt())
                .closedAt(game.getWorstPlayerVotingClosedAt())
                .build();
    }

    private Game findGame(UUID gameId) {
        return gameRepository.findByIdOptional(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", gameId));
    }

    private void validateAdminPermission(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem executar esta operacao");
        }
    }

    private void validateViewPermission(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.JOGADOR) {
            throw new ForbiddenException("Apenas ADMIN ou JOGADOR podem consultar esta operacao");
        }
    }

    private User validateVoterPermission(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.JOGADOR) {
            throw new ForbiddenException("Apenas ADMIN ou JOGADOR podem votar");
        }

        return user;
    }

    private OffsetDateTime parseStartDate(String startDate) {
        try {
            return LocalDate.parse(startDate).atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (Exception e) {
            throw new BusinessException("Data inicial invalida. Use yyyy-MM-dd");
        }
    }

    private OffsetDateTime parseEndDate(String endDate) {
        try {
            return OffsetDateTime.of(LocalDate.parse(endDate), LocalTime.MAX, ZoneOffset.UTC);
        } catch (Exception e) {
            throw new BusinessException("Data final invalida. Use yyyy-MM-dd");
        }
    }
}
