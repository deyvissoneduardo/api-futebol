package br.com.futebol.unit.user;

import br.com.futebol.application.user.UserStatisticsService;
import br.com.futebol.core.exceptions.BusinessException;
import br.com.futebol.core.exceptions.ResourceNotFoundException;
import br.com.futebol.core.exceptions.UnauthorizedException;
import br.com.futebol.domain.user.User;
import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.domain.user.UserStatistics;
import br.com.futebol.infrastructure.user.UserRepository;
import br.com.futebol.infrastructure.user.UserStatisticsRepository;
import br.com.futebol.interfaces.user.UpdateStatisticsRequest;
import br.com.futebol.interfaces.user.UserStatisticsResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("UserStatisticsService - Testes Unitários")
class UserStatisticsServiceTest {

    @Inject
    UserStatisticsService userStatisticsService;

    @InjectMock
    UserStatisticsRepository userStatisticsRepository;

    @InjectMock
    UserRepository userRepository;

    private User mockAdminUser;
    private User mockJogadorUser;
    private User mockSuperAdminUser;
    private UserStatistics mockStatistics;
    private UUID adminUserId;
    private UUID jogadorUserId;
    private UUID superAdminUserId;

    @BeforeEach
    void setUp() {
        adminUserId = UUID.randomUUID();
        jogadorUserId = UUID.randomUUID();
        superAdminUserId = UUID.randomUUID();

        mockAdminUser = User.builder()
                .id(adminUserId)
                .fullName("Admin User")
                .email("admin@test.com")
                .password("hashed")
                .profile(UserProfile.ADMIN)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockJogadorUser = User.builder()
                .id(jogadorUserId)
                .fullName("Jogador User")
                .email("jogador@test.com")
                .password("hashed")
                .profile(UserProfile.JOGADOR)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockSuperAdminUser = User.builder()
                .id(superAdminUserId)
                .fullName("Super Admin")
                .email("superadmin@test.com")
                .password("hashed")
                .profile(UserProfile.SUPER_ADMIN)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockStatistics = UserStatistics.builder()
                .id(UUID.randomUUID())
                .userId(jogadorUserId)
                .minutesPlayed(Duration.ofMinutes(5).plusSeconds(30))
                .goals(5)
                .complaints(2)
                .victories(3)
                .draws(1)
                .defeats(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Testes de Busca de Estatísticas")
    class FindByUserIdTests {

        @Test
        @DisplayName("Deve retornar estatísticas quando existem")
        void deveRetornarEstatisticasQuandoExistem() {
            // Arrange
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(mockStatistics));

            // Act
            UserStatisticsResponse response = userStatisticsService.findByUserId(jogadorUserId);

            // Assert
            assertNotNull(response);
            assertEquals(jogadorUserId, response.getUserId());
            assertEquals(5, response.getGoals());
            assertEquals("00:05:30", response.getMinutesPlayed());
        }

        @Test
        @DisplayName("Deve criar estatísticas padrão quando não existem")
        void deveCriarEstatisticasPadraoQuandoNaoExistem() {
            // Arrange
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.empty());
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.findByUserId(jogadorUserId);

            // Assert
            assertNotNull(response);
            assertEquals("00:00:00", response.getMinutesPlayed());
            assertEquals(0, response.getGoals());
            verify(userStatisticsRepository).persist(any(UserStatistics.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário é SUPER_ADMIN")
        void deveLancarExcecaoQuandoUsuarioESuperAdmin() {
            // Arrange
            when(userRepository.findActiveById(superAdminUserId)).thenReturn(Optional.of(mockSuperAdminUser));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userStatisticsService.findByUserId(superAdminUserId));
            assertEquals("Usuários SUPER_ADMIN não possuem estatísticas", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            // Arrange
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> userStatisticsService.findByUserId(jogadorUserId));
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Minutos")
    class UpdateMinutesTests {

        @Test
        @DisplayName("Deve somar minutos corretamente")
        void deveSomarMinutosCorretamente() {
            // Arrange
            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(mockStatistics));
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.updateMinutes(
                    adminUserId, jogadorUserId, "0:06:00");

            // Assert
            assertEquals("00:11:30", response.getMinutesPlayed());
        }

        @Test
        @DisplayName("Deve converter minutos para horas automaticamente")
        void deveConverterMinutosParaHorasAutomaticamente() {
            // Arrange
            UserStatistics stats = UserStatistics.builder()
                    .id(UUID.randomUUID())
                    .userId(jogadorUserId)
                    .minutesPlayed(Duration.ofMinutes(58).plusSeconds(30))
                    .goals(0)
                    .complaints(0)
                    .victories(0)
                    .draws(0)
                    .defeats(0)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(stats));
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.updateMinutes(
                    adminUserId, jogadorUserId, "0:05:00");

            // Assert
            assertEquals("01:03:30", response.getMinutesPlayed());
        }

        @Test
        @DisplayName("Deve subtrair minutos corretamente")
        void deveSubtrairMinutosCorretamente() {
            // Arrange
            UserStatistics stats = UserStatistics.builder()
                    .id(UUID.randomUUID())
                    .userId(jogadorUserId)
                    .minutesPlayed(Duration.ofMinutes(10))
                    .goals(0)
                    .complaints(0)
                    .victories(0)
                    .draws(0)
                    .defeats(0)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(stats));
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.updateMinutes(
                    adminUserId, jogadorUserId, "-0:03:00");

            // Assert
            assertEquals("00:07:00", response.getMinutesPlayed());
        }

        @Test
        @DisplayName("Deve definir como zero quando subtração resulta em negativo")
        void deveDefinirComoZeroQuandoSubtracaoResultaEmNegativo() {
            // Arrange
            UserStatistics stats = UserStatistics.builder()
                    .id(UUID.randomUUID())
                    .userId(jogadorUserId)
                    .minutesPlayed(Duration.ofMinutes(2))
                    .goals(0)
                    .complaints(0)
                    .victories(0)
                    .draws(0)
                    .defeats(0)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(stats));
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.updateMinutes(
                    adminUserId, jogadorUserId, "-0:05:00");

            // Assert
            assertEquals("00:00:00", response.getMinutesPlayed());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não é ADMIN")
        void deveLancarExcecaoQuandoUsuarioNaoEAdmin() {
            // Arrange
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));

            // Act & Assert
            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> userStatisticsService.updateMinutes(jogadorUserId, jogadorUserId, "0:05:00"));
            assertEquals("Apenas ADMIN pode atualizar estatísticas", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar SUPER_ADMIN")
        void deveLancarExcecaoAoTentarAtualizarSuperAdmin() {
            // Arrange
            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(superAdminUserId)).thenReturn(Optional.of(mockSuperAdminUser));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userStatisticsService.updateMinutes(adminUserId, superAdminUserId, "0:05:00"));
            assertEquals("Não é possível atualizar estatísticas de usuários SUPER_ADMIN", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Gols")
    class UpdateGoalsTests {

        @Test
        @DisplayName("Deve atualizar gols corretamente")
        void deveAtualizarGolsCorretamente() {
            // Arrange
            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(mockStatistics));
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.updateGoals(
                    adminUserId, jogadorUserId, 7);

            // Assert
            assertEquals(7, response.getGoals());
        }

        @Test
        @DisplayName("Deve definir como zero quando valor é negativo")
        void deveDefinirComoZeroQuandoValorENegativo() {
            // Arrange
            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(mockStatistics));
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.updateGoals(
                    adminUserId, jogadorUserId, -1);

            // Assert
            assertEquals(0, response.getGoals());
        }
    }

    @Nested
    @DisplayName("Testes de Atualização Completa")
    class UpdateStatisticsTests {

        @Test
        @DisplayName("Deve atualizar todas as estatísticas de uma vez")
        void deveAtualizarTodasEstatisticasDeUmaVez() {
            // Arrange
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .minutesPlayed("0:10:00")
                    .goals(10)
                    .complaints(5)
                    .victories(8)
                    .draws(2)
                    .defeats(1)
                    .build();

            when(userRepository.findActiveById(adminUserId)).thenReturn(Optional.of(mockAdminUser));
            when(userRepository.findActiveById(jogadorUserId)).thenReturn(Optional.of(mockJogadorUser));
            when(userStatisticsRepository.findByUserId(jogadorUserId)).thenReturn(Optional.of(mockStatistics));
            doNothing().when(userStatisticsRepository).persist(any(UserStatistics.class));

            // Act
            UserStatisticsResponse response = userStatisticsService.updateStatistics(
                    adminUserId, jogadorUserId, request);

            // Assert
            assertEquals("00:15:30", response.getMinutesPlayed());
            assertEquals(10, response.getGoals());
            assertEquals(5, response.getComplaints());
            assertEquals(8, response.getVictories());
            assertEquals(2, response.getDraws());
            assertEquals(1, response.getDefeats());
        }
    }
}

