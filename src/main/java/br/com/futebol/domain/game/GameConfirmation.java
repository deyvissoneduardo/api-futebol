package br.com.futebol.domain.game;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade GameConfirmation representando uma confirmação de nome em um jogo.
 */
@Entity
@Table(name = "game_confirmations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameConfirmation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "confirmed_name", nullable = false, length = 255)
    private String confirmedName;

    @Column(name = "confirmed_at", nullable = false)
    @Builder.Default
    private OffsetDateTime confirmedAt = OffsetDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Override
    public String toString() {
        return "GameConfirmation{" +
                "id=" + id +
                ", gameId=" + gameId +
                ", userId=" + userId +
                ", confirmedName='" + confirmedName + '\'' +
                ", confirmedAt=" + confirmedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

