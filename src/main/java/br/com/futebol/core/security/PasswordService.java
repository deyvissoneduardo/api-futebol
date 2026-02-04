package br.com.futebol.core.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {

    /**
     * @param plainPassword a senha em texto plano
     * @return o hash BCrypt da senha
     */
    public String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    /**
     * @param plainPassword a senha em texto plano
     * @param hashedPassword o hash BCrypt da senha
     * @return true se a senha corresponder ao hash
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}

