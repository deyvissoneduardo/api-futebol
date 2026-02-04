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

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    /**
     * @return lista de UserResponse
     */
    public List<UserResponse> findAll() {
        return userRepository.findAllActive().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * @param id o ID do usuario
     * @return UserResponse com os dados do usuario
     * @throws ResourceNotFoundException se o usuario não for encontrado
     */
    public UserResponse findById(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return toResponse(user);
    }

    /**
     * @param email o e-mail do usuario
     * @return UserResponse com os dados do usuario
     * @throws ResourceNotFoundException se o usuario nao for encontrado
     */
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return toResponse(user);
    }

    /**
     * @param request os dados do novo usuario
     * @return UserResponse com os dados do usuario criado
     * @throws BusinessException se o e-mail ja estiver em uso
     */
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail ja está em uso");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordService.hashPassword(request.getPassword()))
                .photo(request.getPhoto())
                .profile(request.getProfile() != null ? request.getProfile() : UserProfile.JOGADOR)
                .active(true)
                .build();

        userRepository.persist(user);
        return toResponse(user);
    }

    /**
     * @param id o ID do usuario
     * @param request os dados atualizados
     * @return UserResponse com os dados do usuario atualizado
     * @throws ResourceNotFoundException se o usuario não for encontrado
     * @throws BusinessException se o e-mail ja estiver em uso por outro usuario
     */
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new BusinessException("E-mail ja está em uso");
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
     * @param id o ID do usuario
     * @throws ResourceNotFoundException se o usuario nao for encontrado
     */
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        user.setActive(false);
        userRepository.persist(user);
    }

    /**
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

