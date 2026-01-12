package br.com.futebol.application.user;

import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.core.exceptions.UnauthorizedException;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.domain.user.UserStatistics;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.infrastructure.user.UserStatisticsRepository;
import br.com.futebol.interfaces.user.RankingItemResponse;
import br.com.futebol.interfaces.user.RankingResponse;
import br.com.futebol.interfaces.user.UpdateStatisticsRequest;
import br.com.futebol.interfaces.user.UserStatisticsResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Serviço para operações de estatísticas de usuários.
 */
@ApplicationScoped
public class UserStatisticsService {

    @Inject
    UserStatisticsRepository userStatisticsRepository;

    @Inject
    UserRepository userRepository;

    /**
     * Busca estatísticas de um usuário pelo ID.
     * Cria automaticamente se não existir.
     *
     * @param userId o ID do usuário
     * @return UserStatisticsResponse com os dados das estatísticas
     * @throws ResourceNotFoundException se o usuário não for encontrado
     * @throws BusinessException se o usuário for SUPER_ADMIN
     */
    public UserStatisticsResponse findByUserId(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        // SUPER_ADMIN não possui estatísticas
        if (user.getProfile() == UserProfile.SUPER_ADMIN) {
            throw new BusinessException("Usuários SUPER_ADMIN não possuem estatísticas");
        }
        UserStatistics statistics = userStatisticsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStatistics(user));

        return toResponse(statistics);
    }

    /**
     * Busca estatísticas do usuário autenticado.
     *
     * @param userId o ID do usuário autenticado
     * @return UserStatisticsResponse com os dados das estatísticas
     */
    public UserStatisticsResponse findCurrentUserStatistics(UUID userId) {
        return findByUserId(userId);
    }

    /**
     * Atualiza minutos jogados de um usuário.
     * Apenas ADMIN pode atualizar.
     *
     * @param authenticatedUserId o ID do usuário autenticado
     * @param targetUserId o ID do usuário alvo
     * @param minutesToAdd string no formato "HH:mm:ss" (pode ser negativo para subtração)
     * @return UserStatisticsResponse com os dados atualizados
     */
    @Transactional
    public UserStatisticsResponse updateMinutes(UUID authenticatedUserId, UUID targetUserId, String minutesToAdd) {
        validateAdminPermission(authenticatedUserId);
        validateTargetUser(targetUserId);

        UserStatistics statistics = getOrCreateStatistics(targetUserId);
        Duration durationToAdd = parseDuration(minutesToAdd);
        Duration newDuration = statistics.getMinutesPlayed().plus(durationToAdd);

        // Não permitir valores negativos
        if (newDuration.isNegative()) {
            newDuration = Duration.ZERO;
        }

        statistics.setMinutesPlayed(newDuration);
        userStatisticsRepository.persist(statistics);

        return toResponse(statistics);
    }

    /**
     * Atualiza gols de um usuário (soma/subtrai).
     * Apenas ADMIN pode atualizar.
     *
     * @param authenticatedUserId o ID do usuário autenticado
     * @param targetUserId o ID do usuário alvo
     * @param value valor a somar/subtrair (positivo soma, negativo subtrai)
     * @return UserStatisticsResponse com os dados atualizados
     */
    @Transactional
    public UserStatisticsResponse updateGoals(UUID authenticatedUserId, UUID targetUserId, Integer value) {
        validateAdminPermission(authenticatedUserId);
        validateTargetUser(targetUserId);
        System.out.println("CHEGOU AQUI 01 " + authenticatedUserId + targetUserId);
        UserStatistics statistics = getOrCreateStatistics(targetUserId);
        int newValue = statistics.getGoals() + value;
        statistics.setGoals(Math.max(0, newValue));
        userStatisticsRepository.persist(statistics);

        return toResponse(statistics);
    }

    /**
     * Atualiza reclamações de um usuário (soma/subtrai).
     * Apenas ADMIN pode atualizar.
     *
     * @param authenticatedUserId o ID do usuário autenticado
     * @param targetUserId o ID do usuário alvo
     * @param value valor a somar/subtrair (positivo soma, negativo subtrai)
     * @return UserStatisticsResponse com os dados atualizados
     */
    @Transactional
    public UserStatisticsResponse updateComplaints(UUID authenticatedUserId, UUID targetUserId, Integer value) {
        validateAdminPermission(authenticatedUserId);
        validateTargetUser(targetUserId);

        UserStatistics statistics = getOrCreateStatistics(targetUserId);
        int newValue = statistics.getComplaints() + value;
        statistics.setComplaints(Math.max(0, newValue));
        userStatisticsRepository.persist(statistics);

        return toResponse(statistics);
    }

    /**
     * Atualiza vitórias de um usuário (soma/subtrai).
     * Apenas ADMIN pode atualizar.
     *
     * @param authenticatedUserId o ID do usuário autenticado
     * @param targetUserId o ID do usuário alvo
     * @param value valor a somar/subtrair (positivo soma, negativo subtrai)
     * @return UserStatisticsResponse com os dados atualizados
     */
    @Transactional
    public UserStatisticsResponse updateVictories(UUID authenticatedUserId, UUID targetUserId, Integer value) {
        validateAdminPermission(authenticatedUserId);
        validateTargetUser(targetUserId);

        UserStatistics statistics = getOrCreateStatistics(targetUserId);
        int newValue = statistics.getVictories() + value;
        statistics.setVictories(Math.max(0, newValue));
        userStatisticsRepository.persist(statistics);

        return toResponse(statistics);
    }

    /**
     * Atualiza empates de um usuário (soma/subtrai).
     * Apenas ADMIN pode atualizar.
     *
     * @param authenticatedUserId o ID do usuário autenticado
     * @param targetUserId o ID do usuário alvo
     * @param value valor a somar/subtrair (positivo soma, negativo subtrai)
     * @return UserStatisticsResponse com os dados atualizados
     */
    @Transactional
    public UserStatisticsResponse updateDraws(UUID authenticatedUserId, UUID targetUserId, Integer value) {
        validateAdminPermission(authenticatedUserId);
        validateTargetUser(targetUserId);

        UserStatistics statistics = getOrCreateStatistics(targetUserId);
        int newValue = statistics.getDraws() + value;
        statistics.setDraws(Math.max(0, newValue));
        userStatisticsRepository.persist(statistics);

        return toResponse(statistics);
    }

    /**
     * Atualiza derrotas de um usuário (soma/subtrai).
     * Apenas ADMIN pode atualizar.
     *
     * @param authenticatedUserId o ID do usuário autenticado
     * @param targetUserId o ID do usuário alvo
     * @param value valor a somar/subtrair (positivo soma, negativo subtrai)
     * @return UserStatisticsResponse com os dados atualizados
     */
    @Transactional
    public UserStatisticsResponse updateDefeats(UUID authenticatedUserId, UUID targetUserId, Integer value) {
        validateAdminPermission(authenticatedUserId);
        validateTargetUser(targetUserId);

        UserStatistics statistics = getOrCreateStatistics(targetUserId);
        int newValue = statistics.getDefeats() + value;
        statistics.setDefeats(Math.max(0, newValue));
        userStatisticsRepository.persist(statistics);

        return toResponse(statistics);
    }

    /**
     * Atualiza todas as estatísticas de uma vez.
     * Apenas ADMIN pode atualizar.
     *
     * @param authenticatedUserId o ID do usuário autenticado
     * @param targetUserId o ID do usuário alvo
     * @param request o DTO com os dados a atualizar
     * @return UserStatisticsResponse com os dados atualizados
     */
    @Transactional
    public UserStatisticsResponse updateStatistics(UUID authenticatedUserId, UUID targetUserId, UpdateStatisticsRequest request) {
        validateAdminPermission(authenticatedUserId);
        validateTargetUser(targetUserId);

        UserStatistics statistics = getOrCreateStatistics(targetUserId);

        if (request.getMinutesPlayed() != null) {
            Duration durationToAdd = parseDuration(request.getMinutesPlayed());
            Duration newDuration = statistics.getMinutesPlayed().plus(durationToAdd);
            if (newDuration.isNegative()) {
                newDuration = Duration.ZERO;
            }
            statistics.setMinutesPlayed(newDuration);
        }

        if (request.getGoals() != null) {
            statistics.setGoals(Math.max(0, request.getGoals()));
        }

        if (request.getComplaints() != null) {
            statistics.setComplaints(Math.max(0, request.getComplaints()));
        }

        if (request.getVictories() != null) {
            statistics.setVictories(Math.max(0, request.getVictories()));
        }

        if (request.getDraws() != null) {
            statistics.setDraws(Math.max(0, request.getDraws()));
        }

        if (request.getDefeats() != null) {
            statistics.setDefeats(Math.max(0, request.getDefeats()));
        }

        userStatisticsRepository.persist(statistics);
        return toResponse(statistics);
    }

    /**
     * Valida se o usuário autenticado é ADMIN ou SUPER_ADMIN.
     *
     * @param userId o ID do usuário autenticado
     * @throws UnauthorizedException se o usuário não for ADMIN ou SUPER_ADMIN
     */
    private void validateAdminPermission(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.SUPER_ADMIN) {
            throw new UnauthorizedException("Apenas ADMIN pode atualizar estatísticas");
        }
    }

    /**
     * Valida se o usuário alvo é ADMIN ou JOGADOR (não pode ser SUPER_ADMIN).
     *
     * @param targetUserId o ID do usuário alvo
     * @throws BusinessException se o usuário for SUPER_ADMIN
     */
    private void validateTargetUser(UUID targetUserId) {
        User targetUser = userRepository.findActiveById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", targetUserId));

        if (targetUser.getProfile() == UserProfile.SUPER_ADMIN) {
            throw new BusinessException("Não é possível atualizar estatísticas de usuários SUPER_ADMIN");
        }
    }

    /**
     * Obtém ou cria estatísticas para um usuário.
     *
     * @param userId o ID do usuário
     * @return UserStatistics existente ou criado
     */
    private UserStatistics getOrCreateStatistics(UUID userId) {
        return userStatisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findActiveById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));
                    return createDefaultStatistics(user);
                });
    }

    /**
     * Cria estatísticas padrão para um usuário.
     *
     * @param user o usuário
     * @return UserStatistics criado
     */
    private UserStatistics createDefaultStatistics(User user) {
        UserStatistics statistics = UserStatistics.builder()
                .userId(user.getId())
                .minutesPlayed(Duration.ZERO)
                .goals(0)
                .complaints(0)
                .victories(0)
                .draws(0)
                .defeats(0)
                .build();

        userStatisticsRepository.persist(statistics);
        return statistics;
    }

    /**
     * Converte uma string no formato "HH:mm:ss" para Duration.
     * Suporta valores negativos para subtração.
     *
     * @param timeString string no formato "HH:mm:ss" ou "H:mm:ss"
     * @return Duration
     * @throws BusinessException se o formato for inválido
     */
    private Duration parseDuration(String timeString) {
        if (timeString == null || timeString.isBlank()) {
            return Duration.ZERO;
        }

        try {
            // Remove espaços e verifica se é negativo
            String trimmed = timeString.trim();
            boolean isNegative = trimmed.startsWith("-");
            if (isNegative) {
                trimmed = trimmed.substring(1);
            }

            // Parse do formato HH:mm:ss
            String[] parts = trimmed.split(":");
            if (parts.length != 3) {
                throw new BusinessException("Formato de minutos inválido. Use HH:mm:ss");
            }

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);

            Duration duration = Duration.ofHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds);

            return isNegative ? duration.negated() : duration;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new BusinessException("Formato de minutos inválido. Use HH:mm:ss");
        }
    }

    /**
     * Converte Duration para string no formato "HH:mm:ss".
     *
     * @param duration a duração
     * @return string formatada
     */
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return "00:00:00";
        }

        long totalSeconds = duration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Busca ranking de gols ordenado do maior para o menor.
     *
     * @return RankingResponse com ranking de gols
     */
    public RankingResponse getRankingByGoals() {
        List<UserStatistics> statistics = userStatisticsRepository.findRankingByGoals();
        List<RankingItemResponse> items = buildRankingItems(statistics, "goals");
        
        return RankingResponse.builder()
                .type("goals")
                .description("Ranking de Gols")
                .items(items)
                .total(items.size())
                .build();
    }

    /**
     * Busca ranking de reclamações ordenado do maior para o menor.
     *
     * @return RankingResponse com ranking de reclamações
     */
    public RankingResponse getRankingByComplaints() {
        List<UserStatistics> statistics = userStatisticsRepository.findRankingByComplaints();
        List<RankingItemResponse> items = buildRankingItems(statistics, "complaints");
        
        return RankingResponse.builder()
                .type("complaints")
                .description("Ranking de Reclamações")
                .items(items)
                .total(items.size())
                .build();
    }

    /**
     * Busca ranking de vitórias ordenado do maior para o menor.
     *
     * @return RankingResponse com ranking de vitórias
     */
    public RankingResponse getRankingByVictories() {
        List<UserStatistics> statistics = userStatisticsRepository.findRankingByVictories();
        List<RankingItemResponse> items = buildRankingItems(statistics, "victories");
        
        return RankingResponse.builder()
                .type("victories")
                .description("Ranking de Vitórias")
                .items(items)
                .total(items.size())
                .build();
    }

    /**
     * Busca ranking de empates ordenado do maior para o menor.
     *
     * @return RankingResponse com ranking de empates
     */
    public RankingResponse getRankingByDraws() {
        List<UserStatistics> statistics = userStatisticsRepository.findRankingByDraws();
        List<RankingItemResponse> items = buildRankingItems(statistics, "draws");
        
        return RankingResponse.builder()
                .type("draws")
                .description("Ranking de Empates")
                .items(items)
                .total(items.size())
                .build();
    }

    /**
     * Busca ranking de derrotas ordenado do maior para o menor.
     *
     * @return RankingResponse com ranking de derrotas
     */
    public RankingResponse getRankingByDefeats() {
        List<UserStatistics> statistics = userStatisticsRepository.findRankingByDefeats();
        List<RankingItemResponse> items = buildRankingItems(statistics, "defeats");
        
        return RankingResponse.builder()
                .type("defeats")
                .description("Ranking de Derrotas")
                .items(items)
                .total(items.size())
                .build();
    }

    /**
     * Busca ranking de minutos jogados ordenado do maior para o menor.
     *
     * @return RankingResponse com ranking de minutos jogados
     */
    public RankingResponse getRankingByMinutesPlayed() {
        List<UserStatistics> statistics = userStatisticsRepository.findRankingByMinutesPlayed();
        List<RankingItemResponse> items = buildRankingItems(statistics, "minutes");
        
        return RankingResponse.builder()
                .type("minutes-played")
                .description("Ranking de Minutos Jogados")
                .items(items)
                .total(items.size())
                .build();
    }

    /**
     * Constrói lista de itens de ranking a partir das estatísticas.
     *
     * @param statistics lista de estatísticas ordenadas
     * @param type tipo de ranking (goals, complaints, victories, etc)
     * @return lista de RankingItemResponse
     */
    private List<RankingItemResponse> buildRankingItems(List<UserStatistics> statistics, String type) {
        return IntStream.range(0, statistics.size())
                .mapToObj(index -> {
                    UserStatistics stat = statistics.get(index);
                    Optional<User> userOpt = userRepository.findActiveById(stat.getUserId());
                    
                    if (userOpt.isEmpty()) {
                        return null;
                    }
                    
                    User user = userOpt.get();
                    
                    // Verificar se é ADMIN ou JOGADOR ativo
                    if (user.getProfile() != UserProfile.ADMIN && user.getProfile() != UserProfile.JOGADOR) {
                        return null;
                    }
                    
                    long value;
                    String formattedValue;
                    
                    switch (type) {
                        case "goals":
                            value = stat.getGoals();
                            formattedValue = String.valueOf(stat.getGoals());
                            break;
                        case "complaints":
                            value = stat.getComplaints();
                            formattedValue = String.valueOf(stat.getComplaints());
                            break;
                        case "victories":
                            value = stat.getVictories();
                            formattedValue = String.valueOf(stat.getVictories());
                            break;
                        case "draws":
                            value = stat.getDraws();
                            formattedValue = String.valueOf(stat.getDraws());
                            break;
                        case "defeats":
                            value = stat.getDefeats();
                            formattedValue = String.valueOf(stat.getDefeats());
                            break;
                        case "minutes":
                            value = stat.getMinutesPlayed().getSeconds();
                            formattedValue = formatDuration(stat.getMinutesPlayed());
                            break;
                        default:
                            value = 0;
                            formattedValue = "0";
                    }
                    
                    return RankingItemResponse.builder()
                            .position(index + 1)
                            .userId(user.getId())
                            .userName(user.getFullName())
                            .userEmail(user.getEmail())
                            .value(value)
                            .formattedValue(formattedValue)
                            .build();
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    /**
     * Converte uma entidade UserStatistics para UserStatisticsResponse.
     *
     * @param statistics a entidade UserStatistics
     * @return UserStatisticsResponse
     */
    private UserStatisticsResponse toResponse(UserStatistics statistics) {
        return UserStatisticsResponse.builder()
                .id(statistics.getId())
                .userId(statistics.getUserId())
                .minutesPlayed(formatDuration(statistics.getMinutesPlayed()))
                .goals(statistics.getGoals())
                .complaints(statistics.getComplaints())
                .victories(statistics.getVictories())
                .draws(statistics.getDraws())
                .defeats(statistics.getDefeats())
                .createdAt(statistics.getCreatedAt())
                .updatedAt(statistics.getUpdatedAt())
                .build();
    }
}

