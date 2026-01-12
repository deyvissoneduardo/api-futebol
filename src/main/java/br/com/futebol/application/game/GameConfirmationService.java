package br.com.futebol.application.game;

import br.com.futebol.core.exceptions.BusinessException;
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

/**
 * Serviço para operações de confirmação de nomes em jogos.
 */
@ApplicationScoped
public class GameConfirmationService {

    @Inject
    GameConfirmationRepository gameConfirmationRepository;

    @Inject
    GameRepository gameRepository;

    @Inject
    UserRepository userRepository;

    /**
     * Confirma um nome para um jogo.
     *
     * @param gameId o ID do jogo
     * @param request os dados da confirmação
     * @param userId o ID do usuário que está confirmando
     * @return GameConfirmationResponse com os dados da confirmação criada
     * @throws ResourceNotFoundException se o jogo não for encontrado
     * @throws ForbiddenException se a lista não estiver liberada
     * @throws BusinessException se o jogo já iniciou
     * @throws ConflictException se o nome já estiver confirmado ou o usuário já confirmou
     */
    @Transactional
    public GameConfirmationResponse confirmName(UUID gameId, ConfirmNameRequest request, UUID userId) {
        Game game = gameRepository.findByIdOptional(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", gameId));

        // Validar se a lista está liberada
        if (!game.getReleased()) {
            throw new ForbiddenException("Lista não está liberada");
        }

        // Validar se o jogo ainda não iniciou
        if (game.getGameDate().isBefore(OffsetDateTime.now()) || game.getGameDate().isEqual(OffsetDateTime.now())) {
            throw new BusinessException("Lista encerrada - jogo já iniciou");
        }

        // Validar se o usuário já confirmou para este jogo
        if (gameConfirmationRepository.existsByGameIdAndUserId(gameId, userId)) {
            throw new ConflictException("Você já confirmou seu nome para este jogo");
        }

        // Validar se o nome já está confirmado para este jogo
        if (gameConfirmationRepository.existsByGameIdAndConfirmedName(gameId, request.getConfirmedName())) {
            throw new ConflictException("Nome já confirmado para este jogo. Escolha outro nome.");
        }

        GameConfirmation confirmation = GameConfirmation.builder()
                .gameId(gameId)
                .userId(userId)
                .confirmedName(request.getConfirmedName())
                .confirmedAt(OffsetDateTime.now())
                .build();

        gameConfirmationRepository.persist(confirmation);
        return toResponse(confirmation);
    }

    /**
     * Lista todas as confirmações de um jogo (apenas para ADMIN/SUPER_ADMIN).
     *
     * @param gameId o ID do jogo
     * @param userId o ID do usuário que está consultando
     * @return GameConfirmationListResponse com a lista de confirmações
     * @throws ResourceNotFoundException se o jogo não for encontrado
     * @throws ForbiddenException se o usuário não for ADMIN ou SUPER_ADMIN
     */
    public GameConfirmationListResponse listConfirmations(UUID gameId, UUID userId) {
        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem consultar a lista completa de confirmações");
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
     * Busca a confirmação do usuário logado para um jogo.
     *
     * @param gameId o ID do jogo
     * @param userId o ID do usuário
     * @return GameConfirmationResponse com os dados da confirmação
     * @throws ResourceNotFoundException se o jogo ou a confirmação não for encontrada
     */
    public GameConfirmationResponse findMyConfirmation(UUID gameId, UUID userId) {
        gameRepository.findByIdOptional(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", gameId));

        GameConfirmation confirmation = gameConfirmationRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Você ainda não confirmou seu nome para este jogo"));

        return toResponse(confirmation);
    }

    /**
     * Converte uma entidade GameConfirmation para GameConfirmationResponse.
     *
     * @param confirmation a entidade GameConfirmation
     * @return GameConfirmationResponse
     */
    private GameConfirmationResponse toResponse(GameConfirmation confirmation) {
        return GameConfirmationResponse.builder()
                .id(confirmation.getId())
                .gameId(confirmation.getGameId())
                .userId(confirmation.getUserId())
                .confirmedName(confirmation.getConfirmedName())
                .confirmedAt(confirmation.getConfirmedAt())
                .createdAt(confirmation.getCreatedAt())
                .updatedAt(confirmation.getUpdatedAt())
                .build();
    }
}

