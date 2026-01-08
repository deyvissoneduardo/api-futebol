package br.com.futebol.integration.user;

import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.interfaces.auth.LoginRequest;
import br.com.futebol.interfaces.user.CreateUserRequest;
import br.com.futebol.interfaces.user.UpdateUserRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@DisplayName("UserResource - Testes de Integração")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceIT {

    private static String superAdminToken;
    private static String createdUserId;

    @BeforeAll
    static void setUp() {
        // Realiza login como SUPER_ADMIN para obter o token
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@futebol.com")
                .password("admin123")
                .build();

        superAdminToken = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Nested
    @DisplayName("GET /api/users")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ListUsersTests {

        @Test
        @Order(1)
        @DisplayName("Deve listar usuários com token válido")
        void deveListarUsuariosComTokenValido() {
            given()
                .header("Authorization", "Bearer " + superAdminToken)
            .when()
                .get("/api/users")
            .then()
                .statusCode(200)
                .body("$", notNullValue())
                .body("size()", is(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 401 sem token")
        void deveRetornar401SemToken() {
            given()
            .when()
                .get("/api/users")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateUserTests {

        @Test
        @Order(1)
        @DisplayName("Deve criar usuário com sucesso")
        void deveCriarUsuarioComSucesso() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .fullName("Novo Usuário Teste")
                    .email("novousuario" + System.currentTimeMillis() + "@email.com")
                    .password("senha123")
                    .profile(UserProfile.JOGADOR)
                    .build();

            Response response = given()
                    .header("Authorization", "Bearer " + superAdminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/users")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("fullName", is(request.getFullName()))
                    .body("email", is(request.getEmail()))
                    .body("profile", is("JOGADOR"))
                    .body("active", is(true))
                    .extract()
                    .response();

            createdUserId = response.path("id");
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 400 para e-mail duplicado")
        void deveRetornar400ParaEmailDuplicado() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .fullName("Outro Usuário")
                    .email("admin@futebol.com") // E-mail já existe
                    .password("senha123")
                    .build();

            given()
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/users")
            .then()
                .statusCode(400)
                .body("message", is("E-mail já está em uso"));
        }

        @Test
        @Order(3)
        @DisplayName("Deve retornar 400 para campos obrigatórios vazios")
        void deveRetornar400ParaCamposObrigatoriosVazios() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .fullName("")
                    .email("")
                    .password("")
                    .build();

            given()
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/users")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("Deve retornar usuário existente")
        void deveRetornarUsuarioExistente() {
            // Primeiro, lista os usuários para obter um ID válido
            Response response = given()
                    .header("Authorization", "Bearer " + superAdminToken)
                    .when()
                    .get("/api/users")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            String userId = response.path("[0].id");

            given()
                .header("Authorization", "Bearer " + superAdminToken)
            .when()
                .get("/api/users/" + userId)
            .then()
                .statusCode(200)
                .body("id", is(userId));
        }

        @Test
        @DisplayName("Deve retornar 404 para usuário inexistente")
        void deveRetornar404ParaUsuarioInexistente() {
            given()
                .header("Authorization", "Bearer " + superAdminToken)
            .when()
                .get("/api/users/" + UUID.randomUUID())
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/users/me")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Deve retornar dados do usuário logado")
        void deveRetornarDadosDoUsuarioLogado() {
            given()
                .header("Authorization", "Bearer " + superAdminToken)
            .when()
                .get("/api/users/me")
            .then()
                .statusCode(200)
                .body("email", is("admin@futebol.com"))
                .body("profile", is("SUPER_ADMIN"));
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticação")
        void deveRetornar401SemAutenticacao() {
            given()
            .when()
                .get("/api/users/me")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUserTests {

        @Test
        @DisplayName("Deve atualizar usuário com sucesso")
        void deveAtualizarUsuarioComSucesso() {
            // Primeiro, lista os usuários para obter um ID válido
            Response response = given()
                    .header("Authorization", "Bearer " + superAdminToken)
                    .when()
                    .get("/api/users")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            String userId = response.path("[0].id");

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .fullName("Nome Atualizado")
                    .build();

            given()
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + userId)
            .then()
                .statusCode(200)
                .body("fullName", is("Nome Atualizado"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUserTests {

        @Test
        @DisplayName("Deve retornar 404 para usuário inexistente")
        void deveRetornar404ParaUsuarioInexistente() {
            given()
                .header("Authorization", "Bearer " + superAdminToken)
            .when()
                .delete("/api/users/" + UUID.randomUUID())
            .then()
                .statusCode(404);
        }
    }
}

