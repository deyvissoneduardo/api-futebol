package br.com.futebol.core.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s nao encontrado com %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

