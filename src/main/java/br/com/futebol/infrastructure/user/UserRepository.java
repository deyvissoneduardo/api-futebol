package br.com.futebol.infrastructure.user;

import br.com.futebol.domain.user.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência da entidade User.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    /**
     * Busca um usuário pelo e-mail.
     *
     * @param email o e-mail do usuário
     * @return Optional contendo o usuário se encontrado
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Verifica se existe um usuário com o e-mail informado.
     *
     * @param email o e-mail a verificar
     * @return true se existir um usuário com o e-mail
     */
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    /**
     * Verifica se existe outro usuário com o e-mail informado (exceto o próprio).
     *
     * @param email o e-mail a verificar
     * @param excludeId o ID do usuário a excluir da verificação
     * @return true se existir outro usuário com o e-mail
     */
    public boolean existsByEmailAndIdNot(String email, UUID excludeId) {
        return count("email = ?1 and id != ?2", email, excludeId) > 0;
    }

    /**
     * Lista todos os usuários ativos.
     *
     * @return lista de usuários ativos
     */
    public List<User> findAllActive() {
        return list("active", true);
    }

    /**
     * Busca um usuário ativo pelo ID.
     *
     * @param id o ID do usuário
     * @return Optional contendo o usuário se encontrado e ativo
     */
    public Optional<User> findActiveById(UUID id) {
        return find("id = ?1 and active = true", id).firstResultOptional();
    }
}

