package br.com.futebol.domain.user;

public enum UserProfile {
    
    SUPER_ADMIN("Super Administrador"),
    ADMIN("Administrador"),
    JOGADOR("Jogador");

    private final String description;

    UserProfile(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

