package br.com.futebol.core.exceptions;

/**
 * Exceção para erros de conflito (409 Conflict).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

