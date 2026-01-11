package br.com.futebol.domain.user;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade UserStatistics representando as estatísticas de um usuário.
 * Apenas usuários com perfil ADMIN ou JOGADOR possuem estatísticas.
 */
@Entity
@Table(name = "user_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatistics extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * Minutos jogados armazenados como INTERVAL no PostgreSQL.
     * Mapeado para Duration no Java usando converter customizado.
     */
    @Column(name = "minutes_played", nullable = false, columnDefinition = "INTERVAL")
//    @Convert(converter = br.com.futebol.infrastructure.user.DurationIntervalConverter.class)
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Builder.Default
    private Duration minutesPlayed = Duration.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer goals = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer complaints = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer victories = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer draws = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer defeats = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

