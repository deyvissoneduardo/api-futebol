package br.com.futebol.core.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        log.error("Exceção capturada: {}", exception.getMessage(), exception);

        if (exception instanceof ResourceNotFoundException) {
            return buildResponse(Response.Status.NOT_FOUND, exception.getMessage());
        }

        if (exception instanceof ConflictException) {
            return buildResponse(Response.Status.CONFLICT, exception.getMessage());
        }

        if (exception instanceof BusinessException) {
            return buildResponse(Response.Status.BAD_REQUEST, exception.getMessage());
        }

        if (exception instanceof UnauthorizedException) {
            return buildResponse(Response.Status.UNAUTHORIZED, exception.getMessage());
        }

        if (exception instanceof ForbiddenException) {
            return buildResponse(Response.Status.FORBIDDEN, exception.getMessage());
        }

        if (exception instanceof ConstraintViolationException cve) {
            String errors = cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return buildResponse(Response.Status.BAD_REQUEST, errors);
        }

        if (exception instanceof jakarta.ws.rs.NotAuthorizedException) {
            return buildResponse(Response.Status.UNAUTHORIZED, "Acesso nao autorizado");
        }

        if (exception instanceof jakarta.ws.rs.ForbiddenException) {
            return buildResponse(Response.Status.FORBIDDEN, "Acesso negado");
        }

        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private Response buildResponse(Response.Status status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.getStatusCode());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return Response.status(status)
                .entity(body)
                .build();
    }
}

