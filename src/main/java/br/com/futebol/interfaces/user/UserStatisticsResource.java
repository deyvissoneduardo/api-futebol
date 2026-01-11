package br.com.futebol.interfaces.user;

import br.com.futebol.application.user.UserStatisticsService;
import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.UnauthorizedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

/**
 * Resource para operações de estatísticas de usuários.
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Estatísticas de Usuários", description = "Operações de gerenciamento de estatísticas de usuários")
@SecurityRequirement(name = "jwt")
public class UserStatisticsResource {

    @Inject
    UserStatisticsService userStatisticsService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/{userId}/statistics")
    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Buscar estatísticas de um usuário",
            description = "Retorna as estatísticas de um usuário. Usuários só podem consultar suas próprias estatísticas."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Estatísticas encontradas",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado"),
            @APIResponse(responseCode = "400", description = "Usuário SUPER_ADMIN não possui estatísticas")
    })
    public Response getStatistics(@PathParam("userId") UUID userId, @Context SecurityContext securityContext) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        // Validação: usuários só podem consultar suas próprias estatísticas
        if (!authenticatedUserUuid.equals(userId)) {
            throw new UnauthorizedException("Você só pode consultar suas próprias estatísticas");
        }

        UserStatisticsResponse response = userStatisticsService.findByUserId(userId);
        return Response.ok(response).build();
    }

    @GET
    @Path("/me/statistics")
    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Buscar próprias estatísticas",
            description = "Retorna as estatísticas do usuário autenticado"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Estatísticas encontradas",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "400", description = "Usuário SUPER_ADMIN não possui estatísticas")
    })
    public Response getMyStatistics(@Context SecurityContext securityContext) {
        String userId = jwt.getSubject();
        UUID userUuid = UUID.fromString(userId);

        UserStatisticsResponse response = userStatisticsService.findCurrentUserStatistics(userUuid);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{userId}/statistics")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Atualizar todas as estatísticas de um usuário",
            description = "Atualiza todas as estatísticas de um usuário de uma vez. Apenas ADMIN pode atualizar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Estatísticas atualizadas",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response updateStatistics(
            @PathParam("userId") UUID userId,
            @Valid UpdateStatisticsRequest request,
            @Context SecurityContext securityContext
    ) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        UserStatisticsResponse response = userStatisticsService.updateStatistics(
                authenticatedUserUuid,
                userId,
                request
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{userId}/statistics/minutes")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Atualizar minutos jogados",
            description = "Adiciona ou subtrai minutos jogados. Valores negativos subtraem (ex: \"-0:05:00\"). Apenas ADMIN pode atualizar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Minutos atualizados",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response updateMinutes(
            @PathParam("userId") UUID userId,
            @Valid UpdateMinutesRequest request,
            @Context SecurityContext securityContext
    ) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        UserStatisticsResponse response = userStatisticsService.updateMinutes(
                authenticatedUserUuid,
                userId,
                request.getMinutesToAdd()
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{userId}/statistics/goals")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Atualizar gols",
            description = "Adiciona ou subtrai gols. Valores positivos somam (ex: 1), valores negativos subtraem (ex: -1). Apenas ADMIN pode atualizar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Gols atualizados",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response updateGoals(
            @PathParam("userId") UUID userId,
            @Valid UpdateGoalsRequest request,
            @Context SecurityContext securityContext
    ) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        UserStatisticsResponse response = userStatisticsService.updateGoals(
                authenticatedUserUuid,
                userId,
                request.getValue()
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{userId}/statistics/complaints")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Atualizar reclamações",
            description = "Adiciona ou subtrai reclamações. Valores positivos somam (ex: 1), valores negativos subtraem (ex: -1). Apenas ADMIN pode atualizar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Reclamações atualizadas",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response updateComplaints(
            @PathParam("userId") UUID userId,
            @Valid UpdateComplaintsRequest request,
            @Context SecurityContext securityContext
    ) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        UserStatisticsResponse response = userStatisticsService.updateComplaints(
                authenticatedUserUuid,
                userId,
                request.getValue()
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{userId}/statistics/victories")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Atualizar vitórias",
            description = "Adiciona ou subtrai vitórias. Valores positivos somam (ex: 1), valores negativos subtraem (ex: -1). Apenas ADMIN pode atualizar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Vitórias atualizadas",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response updateVictories(
            @PathParam("userId") UUID userId,
            @Valid UpdateVictoriesRequest request,
            @Context SecurityContext securityContext
    ) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        UserStatisticsResponse response = userStatisticsService.updateVictories(
                authenticatedUserUuid,
                userId,
                request.getValue()
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{userId}/statistics/draws")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Atualizar empates",
            description = "Adiciona ou subtrai empates. Valores positivos somam (ex: 1), valores negativos subtraem (ex: -1). Apenas ADMIN pode atualizar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Empates atualizados",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response updateDraws(
            @PathParam("userId") UUID userId,
            @Valid UpdateDrawsRequest request,
            @Context SecurityContext securityContext
    ) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        UserStatisticsResponse response = userStatisticsService.updateDraws(
                authenticatedUserUuid,
                userId,
                request.getValue()
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{userId}/statistics/defeats")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @Operation(
            summary = "Atualizar derrotas",
            description = "Adiciona ou subtrai derrotas. Valores positivos somam (ex: 1), valores negativos subtraem (ex: -1). Apenas ADMIN pode atualizar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Derrotas atualizadas",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response updateDefeats(
            @PathParam("userId") UUID userId,
            @Valid UpdateDefeatsRequest request,
            @Context SecurityContext securityContext
    ) {
        String authenticatedUserId = jwt.getSubject();
        UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

        UserStatisticsResponse response = userStatisticsService.updateDefeats(
                authenticatedUserUuid,
                userId,
                request.getValue()
        );

        return Response.ok(response).build();
    }
}

