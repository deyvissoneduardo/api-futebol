package br.com.futebol.application.user;

import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.core.security.PasswordService;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.interfaces.user.CreateUserRequest;
import br.com.futebol.interfaces.user.UpdateUserRequest;
import br.com.futebol.interfaces.user.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para operações de CRUD de usuários.
 */
@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    /**
     * Lista todos os usuários ativos.
     *
     * @return lista de UserResponse
     */
    public List<UserResponse> findAll() {
        return userRepository.findAllActive().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca um usuário pelo ID.
     *
     * @param id o ID do usuário
     * @return UserResponse com os dados do usuário
     * @throws ResourceNotFoundException se o usuário não for encontrado
     */
    public UserResponse findById(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
        return toResponse(user);
    }

    /**
     * Busca um usuário pelo e-mail.
     *
     * @param email o e-mail do usuário
     * @return UserResponse com os dados do usuário
     * @throws ResourceNotFoundException se o usuário não for encontrado
     */
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", email));
        return toResponse(user);
    }

    /**
     * Cria um novo usuário.
     *
     * @param request os dados do novo usuário
     * @return UserResponse com os dados do usuário criado
     * @throws BusinessException se o e-mail já estiver em uso
     */
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        System.out.println("CHEGOU AQUI 02 " + request);
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já está em uso");
        }
        System.out.println("CHEGOU AQUI 03 " + request);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordService.hashPassword(request.getPassword()))
                .photo(request.getPhoto())
                .profile(request.getProfile() != null ? request.getProfile() : UserProfile.JOGADOR)
                .active(true)
                .build();

        System.out.println("CHEGOU AQUI 04 " + user);

        userRepository.persist(user);
        return toResponse(user);
    }

    /**
     * Atualiza um usuário existente.
     *
     * @param id o ID do usuário
     * @param request os dados atualizados
     * @return UserResponse com os dados do usuário atualizado
     * @throws ResourceNotFoundException se o usuário não for encontrado
     * @throws BusinessException se o e-mail já estiver em uso por outro usuário
     */
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new BusinessException("E-mail já está em uso");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordService.hashPassword(request.getPassword()));
        }

        if (request.getPhoto() != null) {
            user.setPhoto(request.getPhoto());
        }

        if (request.getProfile() != null) {
            user.setProfile(request.getProfile());
        }

        userRepository.persist(user);
        return toResponse(user);
    }

    /**
     * Deleta (desativa) um usuário.
     *
     * @param id o ID do usuário
     * @throws ResourceNotFoundException se o usuário não for encontrado
     */
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        user.setActive(false);
        userRepository.persist(user);
    }

    /**
     * Converte uma entidade User para UserResponse.
     *
     * @param user a entidade User
     * @return UserResponse
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .photo(user.getPhoto())
                .profile(user.getProfile())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

