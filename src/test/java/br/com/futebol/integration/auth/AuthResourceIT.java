package br.com.futebol.integration.auth;

import br.com.futebol.interfaces.auth.LoginRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@DisplayName("AuthResource - Testes de Integração")
class AuthResourceIT {

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Deve realizar login com credenciais válidas")
        void deveRealizarLoginComCredenciaisValidas() {
            LoginRequest request = LoginRequest.builder()
                    .email("admin@futebol.com")
                    .password("admin123")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("type", is("Bearer"))
                .body("expiresIn", is(86400))
                .body("user.email", is("admin@futebol.com"))
                .body("user.profile", is("SUPER_ADMIN"));
        }

        @Test
        @DisplayName("Deve retornar 401 para usuário não encontrado")
        void deveRetornar401ParaUsuarioNaoEncontrado() {
            LoginRequest request = LoginRequest.builder()
                    .email("inexistente@email.com")
                    .password("senha123")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .body("message", is("Credenciais inválidas"));
        }

        @Test
        @DisplayName("Deve retornar 401 para senha incorreta")
        void deveRetornar401ParaSenhaIncorreta() {
            LoginRequest request = LoginRequest.builder()
                    .email("admin@futebol.com")
                    .password("senhaErrada")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .body("message", is("Credenciais inválidas"));
        }

        @Test
        @DisplayName("Deve retornar 400 para e-mail inválido")
        void deveRetornar400ParaEmailInvalido() {
            LoginRequest request = LoginRequest.builder()
                    .email("email-invalido")
                    .password("senha123")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 400 para campos vazios")
        void deveRetornar400ParaCamposVazios() {
            LoginRequest request = LoginRequest.builder()
                    .email("")
                    .password("")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(400);
        }
    }
}

