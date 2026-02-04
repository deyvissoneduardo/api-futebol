package br.com.futebol.application.game;

import br.com.futebol.core.exceptions.ConflictException;
import br.com.futebol.core.exceptions.ForbiddenException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.game.GameConfirmation;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameConfirmationRepository;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.ConfirmNameRequest;
import br.com.futebol.interfaces.game.GameConfirmationListResponse;
import br.com.futebol.interfaces.game.GameConfirmationResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameConfirmationService {

    @Inject
    GameConfirmationRepository gameConfirmationRepository;

    @Inject
    GameRepository gameRepository;

    @Inject
    UserRepository userRepository;

    /**
     * Quando isGuest = true, o sistema cria um UUID único para o convidado,
     * @param gameId o ID do jogo
     * @param request os dados da confirmacao (pode incluir isGuest para convidados)
     * @param userId o ID do usuario que está confirmando
     * @return GameConfirmationResponse com os dados da confirmacao criada
     * @throws ResourceNotFoundException se o jogo não for encontrado
     * @throws ForbiddenException se a lista nao estiver liberada (released = false)
     * @throws ConflictException se o nome ja estiver confirmado para este jogo
     */
    @Transactional
    public GameConfirmationResponse confirmName(UUID gameId, ConfirmNameRequest request, UUID userId) {
        Game game = gameRepository.findByIdOptional(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", gameId));

        if (!game.getReleased()) {
            throw new ForbiddenException("Lista nao está liberada");
        }

        if (gameConfirmationRepository.existsByGameIdAndConfirmedName(gameId, request.getConfirmedName())) {
            throw new ConflictException("Nome ja confirmado para este jogo. Escolha outro nome.");
        }

        UUID finalUserId;
        UUID confirmedByUserId = null;
        Boolean isGuest = request.getIsGuest() != null && request.getIsGuest();

        if (isGuest) {
            finalUserId = UUID.randomUUID();
            confirmedByUserId = userId;
        } else {
            finalUserId = userId;
        }

        GameConfirmation confirmation = GameConfirmation.builder()
                .gameId(gameId)
                .userId(finalUserId)
                .confirmedName(request.getConfirmedName())
                .isGuest(isGuest)
                .confirmedByUserId(confirmedByUserId)
                .confirmedAt(OffsetDateTime.now())
                .build();

        gameConfirmationRepository.persist(confirmation);
        return toResponse(confirmation);
    }

    /**
     * @param gameId o ID do jogo
     * @param userId o ID do usuario que está consultando
     * @return GameConfirmationListResponse com a lista de confirmações
     * @throws ResourceNotFoundException se o jogo não for encontrado
     * @throws ForbiddenException se o usuario não for ADMIN ou SUPER_ADMIN
     */
    public GameConfirmationListResponse listConfirmations(UUID gameId, UUID userId) {
        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem consultar a lista completa de confirmacoes");
        }

        gameRepository.findByIdOptional(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", gameId));

        List<GameConfirmation> confirmations = gameConfirmationRepository.findByGameId(gameId);
        List<GameConfirmationResponse> confirmationResponses = confirmations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return GameConfirmationListResponse.builder()
                .gameId(gameId)
                .confirmations(confirmationResponses)
                .total(confirmationResponses.size())
                .build();
    }

    /**
     * @param gameId o ID do jogo
     * @param userId o ID do usuario
     * @return Lista de GameConfirmationResponse com as confirmacoes relacionadas ao usuario
     * @throws ResourceNotFoundException se o jogo não for encontrado
     */
    public List<GameConfirmationResponse> findMyConfirmations(UUID gameId, UUID userId) {
        gameRepository.findByIdOptional(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", gameId));

        List<GameConfirmation> confirmations = gameConfirmationRepository.findByGameIdAndUserRelated(gameId, userId);
        
        return confirmations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * @param confirmation a entidade GameConfirmation
     * @return GameConfirmationResponse
     */
    private GameConfirmationResponse toResponse(GameConfirmation confirmation) {
        return GameConfirmationResponse.builder()
                .id(confirmation.getId())
                .gameId(confirmation.getGameId())
                .userId(confirmation.getUserId())
                .confirmedName(confirmation.getConfirmedName())
                .isGuest(confirmation.getIsGuest())
                .confirmedByUserId(confirmation.getConfirmedByUserId())
                .confirmedAt(confirmation.getConfirmedAt())
                .createdAt(confirmation.getCreatedAt())
                .updatedAt(confirmation.getUpdatedAt())
                .build();
    }
}

