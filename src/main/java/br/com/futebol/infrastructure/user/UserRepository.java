package br.com.futebol.infrastructure.user;

import br.com.futebol.domain.user.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    /**
     * @param email o e-mail do usuario
     * @return Optional contendo o usuario se encontrado
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * @param email o e-mail a verificar
     * @return true se existir um usuario com o e-mail
     */
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    /**
     * @param email o e-mail a verificar
     * @param excludeId o ID do usuario a excluir da verificacao
     * @return true se existir outro usuario com o e-mail
     */
    public boolean existsByEmailAndIdNot(String email, UUID excludeId) {
        return count("email = ?1 and id != ?2", email, excludeId) > 0;
    }

    /**
     * @return lista de usuario ativos
     */
    public List<User> findAllActive() {
        return list("active", true);
    }

    /**
     * @param id o ID do usuario
     * @return Optional contendo o usuário se encontrado
     */
    public Optional<User> findByIdOptional(UUID id) {
        return find("id = ?1", id).firstResultOptional();
    }

    /**
     * @param id o ID do usuario
     * @return Optional contendo o usuario se encontrado e ativo
     */
    public Optional<User> findActiveById(UUID id) {
        return find("id = ?1 and active = true", id).firstResultOptional();
    }
}

