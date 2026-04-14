package br.com.futebol.domain.game;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_worst_player_votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameWorstPlayerVote extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "voter_user_id", nullable = false)
    private UUID voterUserId;

    @Column(name = "voter_name_snapshot", nullable = false, length = 255)
    private String voterNameSnapshot;

    @Column(name = "target_confirmation_id", nullable = false)
    private UUID targetConfirmationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
