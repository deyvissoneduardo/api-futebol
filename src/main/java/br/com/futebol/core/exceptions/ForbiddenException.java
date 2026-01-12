package br.com.futebol.core.exceptions;

/**
 * Exceção para erros de acesso negado (403 Forbidden).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

