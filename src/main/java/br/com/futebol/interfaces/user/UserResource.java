package br.com.futebol.interfaces.user;

import br.com.futebol.application.user.UserService;
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
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * Resource para operações de CRUD de usuários.
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuários", description = "Operações de gerenciamento de usuários")
@SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Listar usuários", description = "Retorna lista de todos os usuários ativos")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista de usuários",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response findAll() {
        List<UserResponse> users = userService.findAll();
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Buscar usuário", description = "Retorna um usuário pelo ID")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response findById(@PathParam("id") UUID id) {
        UserResponse user = userService.findById(id);
        return Response.ok(user).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Perfil do usuário logado", description = "Retorna os dados do usuário autenticado")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Dados do usuário",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado")
    })
    public Response me(@Context SecurityContext securityContext) {
        String userId = jwt.getSubject();
        UserResponse user = userService.findById(UUID.fromString(userId));
        return Response.ok(user).build();
    }


//    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
//    @SecurityRequirement(name = "jwt")
//    @APIResponses({
//            @APIResponse(responseCode = "201", description = "Usuário criado",
//                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
//            @APIResponse(responseCode = "400", description = "Dados inválidos"),
//            @APIResponse(responseCode = "401", description = "Não autorizado"),
//            @APIResponse(responseCode = "403", description = "Acesso negado")
//    })
    @POST
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário")
    public Response create(@Valid CreateUserRequest request) {
        System.out.println("CHEGOU AQUI");
        UserResponse user = userService.create(request);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Usuário atualizado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response update(@PathParam("id") UUID id, @Valid UpdateUserRequest request) {
        UserResponse user = userService.update(id, request);
        return Response.ok(user).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Deletar usuário", description = "Desativa um usuário (soft delete)")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Usuário deletado"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Response delete(@PathParam("id") UUID id) {
        userService.delete(id);
        return Response.noContent().build();
    }
}

