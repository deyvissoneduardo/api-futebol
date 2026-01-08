package br.com.futebol.integration.health;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@DisplayName("HealthResource - Testes de Integração")
class HealthResourceIT {

    @Test
    @DisplayName("Deve retornar status UP no health check padrão do Quarkus")
    void deveRetornarStatusUpNoHealthCheckPadrao() {
        given()
            .when()
                .get("/q/health")
            .then()
                .statusCode(200)
                .body("status", is("UP"));
    }

    @Test
    @DisplayName("Deve retornar status UP no liveness check")
    void deveRetornarStatusUpNoLivenessCheck() {
        given()
            .when()
                .get("/q/health/live")
            .then()
                .statusCode(200)
                .body("status", is("UP"));
    }

    @Test
    @DisplayName("Deve retornar status UP no readiness check")
    void deveRetornarStatusUpNoReadinessCheck() {
        given()
            .when()
                .get("/q/health/ready")
            .then()
                .statusCode(200)
                .body("status", is("UP"));
    }

    @Test
    @DisplayName("Deve retornar status UP no endpoint customizado de health")
    void deveRetornarStatusUpNoEndpointCustomizado() {
        given()
            .when()
                .get("/api/health")
            .then()
                .statusCode(200)
                .body("status", is("UP"))
                .body("timestamp", notNullValue())
                .body("service", is("api-futebol"));
    }
}

