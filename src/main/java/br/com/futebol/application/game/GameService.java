package br.com.futebol.application.game;

import br.com.futebol.application.user.UserStatisticsService;
import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ForbiddenException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.game.GameConfirmation;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameConfirmationRepository;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.BulkUpdateStatisticsRequest;
import br.com.futebol.interfaces.game.BulkUpdateStatisticsResponse;
import br.com.futebol.interfaces.game.CreateGameRequest;
import br.com.futebol.interfaces.game.CreateGameResponse;
import br.com.futebol.interfaces.game.GameResponse;
import br.com.futebol.interfaces.user.UpdateStatisticsRequest;
import br.com.futebol.interfaces.user.UserStatisticsResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para operações de jogos.
 */
@ApplicationScoped
public class GameService {

    @Inject
    GameRepository gameRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    GameConfirmationRepository gameConfirmationRepository;

    @Inject
    UserStatisticsService userStatisticsService;

    /**
     * Lista o único jogo com released = true.
     * Apenas um jogo pode estar com released = true por vez.
     *
     * @return lista contendo apenas o jogo com released = true, ou lista vazia se não houver
     */
    public List<GameResponse> findAll() {
        return gameRepository.findReleased()
                .map(game -> List.of(toResponse(game)))
                .orElse(List.of());
    }

    /**
     * Busca um jogo pelo ID.
     *
     * @param id o ID do jogo
     * @return GameResponse com os dados do jogo
     * @throws ResourceNotFoundException se o jogo não for encontrado
     */
    public GameResponse findById(UUID id) {
        Game game = gameRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", id));
        return toResponse(game);
    }

    /**
     * Cria um novo jogo.
     * Se já existir um jogo com released = true, ele será automaticamente alterado para released = false,
     * e o novo jogo será o único com released = true.
     *
     * @param request os dados do novo jogo
     * @param userId o ID do usuário que está criando o jogo
     * @return CreateGameResponse com os dados do jogo criado e mensagem informativa se necessário
     * @throws ForbiddenException se o usuário não tiver permissão (não for ADMIN ou SUPER_ADMIN)
     */
    @Transactional
    public CreateGameResponse create(CreateGameRequest request, UUID userId) {
        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem criar jogos");
        }

        // Combinar startDate e startHour em OffsetDateTime
        OffsetDateTime gameDate = parseGameDateTime(request.getStartDate(), request.getStartHour());

        // Verificar se já existe um jogo com released = true
        List<Game> releasedGames = gameRepository.findAllReleased();
        String message = null;

        if (!releasedGames.isEmpty()) {
            // Alterar todos os games com released = true para false
            for (Game releasedGame : releasedGames) {
                releasedGame.setReleased(false);
                gameRepository.persist(releasedGame);
            }
            // Criar mensagem informativa
            if (releasedGames.size() == 1) {
                message = String.format("O gameId %s foi alterado para released = false. O novo game é o único com released = true.", 
                        releasedGames.get(0).getId());
            } else {
                message = String.format("Os gameIds foram alterados para released = false. O novo game é o único com released = true.");
            }
        }

        // Jogo criado com released = true por padrão, permitindo confirmações
        Game game = Game.builder()
                .gameDate(gameDate)
                .released(true)
                .build();

        gameRepository.persist(game);

        return CreateGameResponse.builder()
                .id(game.getId())
                .gameDate(game.getGameDate())
                .released(game.getReleased())
                .createdAt(game.getCreatedAt())
                .updatedAt(game.getUpdatedAt())
                .message(message)
                .build();
    }

    /**
     * Combina a data e hora fornecidas em um OffsetDateTime.
     * Interpreta a data/hora como UTC para salvar no banco.
     *
     * @param startDate data no formato yyyy-MM-dd
     * @param startHour hora no formato HH:mm
     * @return OffsetDateTime combinando data e hora em UTC
     * @throws BusinessException se a data ou hora estiverem em formato inválido
     */
    private OffsetDateTime parseGameDateTime(String startDate, String startHour) {
        try {
            LocalDate date = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime time = LocalTime.parse(startHour, DateTimeFormatter.ofPattern("HH:mm"));
            
            // Cria OffsetDateTime em UTC (Z = +00:00)
            // A data/hora fornecida é interpretada como UTC
            return OffsetDateTime.of(date, time, ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Data ou hora inválida: " + e.getMessage());
        }
    }

    /**
     * Inicia o jogo, bloqueando novas confirmações (muda released para false).
     *
     * @param id o ID do jogo
     * @param userId o ID do usuário que está iniciando o jogo
     * @return GameResponse com os dados do jogo atualizado
     * @throws ResourceNotFoundException se o jogo não for encontrado
     * @throws ForbiddenException se o usuário não tiver permissão (não for ADMIN ou SUPER_ADMIN)
     */
    @Transactional
    public GameResponse releaseGame(UUID id, UUID userId) {
        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem iniciar jogos");
        }

        Game game = gameRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", id));

        // Iniciar jogo: muda released para false, bloqueando novas confirmações
        game.setReleased(false);
        gameRepository.persist(game);
        return toResponse(game);
    }

    /**
     * Atualiza estatísticas de todos os jogadores confirmados em um jogo.
     * Apenas ADMIN ou SUPER_ADMIN podem executar esta operação.
     *
     * @param gameId o ID do jogo
     * @param request os dados de atualização de estatísticas
     * @param userId o ID do usuário que está executando a operação
     * @return BulkUpdateStatisticsResponse com as estatísticas atualizadas
     * @throws ResourceNotFoundException se o jogo não for encontrado
     * @throws ForbiddenException se o usuário não tiver permissão
     * @throws BusinessException se algum userId não estiver confirmado no jogo
     */
    @Transactional
    public BulkUpdateStatisticsResponse bulkUpdateStatistics(UUID gameId, BulkUpdateStatisticsRequest request, UUID userId) {
        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem atualizar estatísticas");
        }

        Game game = gameRepository.findByIdOptional(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", gameId));

        // Buscar todas as confirmações do jogo
        List<GameConfirmation> confirmations = gameConfirmationRepository.findByGameId(gameId);
        List<UUID> confirmedUserIds = confirmations.stream()
                .map(GameConfirmation::getUserId)
                .collect(Collectors.toList());

        // Validar que todos os userIds na requisição estão confirmados
        for (BulkUpdateStatisticsRequest.PlayerStatisticsUpdate update : request.getStatistics()) {
            if (!confirmedUserIds.contains(update.getUserId())) {
                throw new BusinessException(
                        String.format("Usuário %s não está confirmado nesta partida", update.getUserId())
                );
            }
        }

        // Atualizar estatísticas de cada jogador
        List<UserStatisticsResponse> updatedStatistics = request.getStatistics().stream()
                .map(update -> {
                    UpdateStatisticsRequest statsRequest = UpdateStatisticsRequest.builder()
                            .minutesPlayed(update.getMinutesPlayed())
                            .goals(update.getGoals())
                            .complaints(update.getComplaints())
                            .victories(update.getVictories())
                            .draws(update.getDraws())
                            .defeats(update.getDefeats())
                            .build();

                    return userStatisticsService.updateStatistics(userId, update.getUserId(), statsRequest);
                })
                .collect(Collectors.toList());

        return BulkUpdateStatisticsResponse.builder()
                .gameId(gameId)
                .updatedCount(updatedStatistics.size())
                .statistics(updatedStatistics)
                .build();
    }

    /**
     * Converte uma entidade Game para GameResponse.
     *
     * @param game a entidade Game
     * @return GameResponse
     */
    private GameResponse toResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .gameDate(game.getGameDate())
                .released(game.getReleased())
                .createdAt(game.getCreatedAt())
                .updatedAt(game.getUpdatedAt())
                .build();
    }
}

