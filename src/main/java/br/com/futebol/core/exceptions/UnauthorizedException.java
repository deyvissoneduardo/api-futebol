package br.com.futebol.core.exceptions;

/**
 * Exceção para erros de autenticação/autorização.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Credenciais inválidas");
    }
}

