package br.com.futebol.application.game;

import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ForbiddenException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.domain.game.Game;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.game.GameRepository;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.game.CreateGameRequest;
import br.com.futebol.interfaces.game.GameResponse;
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

    /**
     * Lista todos os jogos ordenados por data.
     *
     * @return lista de GameResponse
     */
    public List<GameResponse> findAll() {
        return gameRepository.findAllOrderedByDate().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
     *
     * @param request os dados do novo jogo
     * @param userId o ID do usuário que está criando o jogo
     * @return GameResponse com os dados do jogo criado
     * @throws ForbiddenException se o usuário não tiver permissão (não for ADMIN ou SUPER_ADMIN)
     */
    @Transactional
    public GameResponse create(CreateGameRequest request, UUID userId) {
        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem criar jogos");
        }

        // Combinar startDate e startHour em OffsetDateTime
        OffsetDateTime gameDate = parseGameDateTime(request.getStartDate(), request.getStartHour());

        Game game = Game.builder()
                .gameDate(gameDate)
                .released(false)
                .build();

        gameRepository.persist(game);
        return toResponse(game);
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
     * Libera a lista de confirmação de um jogo.
     *
     * @param id o ID do jogo
     * @param userId o ID do usuário que está liberando a lista
     * @return GameResponse com os dados do jogo atualizado
     * @throws ResourceNotFoundException se o jogo não for encontrado
     * @throws ForbiddenException se o usuário não tiver permissão (não for ADMIN ou SUPER_ADMIN)
     */
    @Transactional
    public GameResponse releaseGame(UUID id, UUID userId) {
        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new ForbiddenException("Apenas ADMIN ou SUPER_ADMIN podem liberar listas");
        }

        Game game = gameRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", id));

        game.setReleased(true);
        gameRepository.persist(game);
        return toResponse(game);
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

