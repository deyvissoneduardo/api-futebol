package br.com.futebol.domain.game;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_date", nullable = false)
    private OffsetDateTime gameDate;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Boolean released = false;

    @Column(name = "worst_player_voting_enabled", nullable = false)
    @Builder.Default
    private Boolean worstPlayerVotingEnabled = false;

    @Column(name = "worst_player_voting_opened_at")
    private OffsetDateTime worstPlayerVotingOpenedAt;

    @Column(name = "worst_player_voting_closed_at")
    private OffsetDateTime worstPlayerVotingClosedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", gameDate=" + gameDate +
                ", name='" + name + '\'' +
                ", released=" + released +
                ", worstPlayerVotingEnabled=" + worstPlayerVotingEnabled +
                ", worstPlayerVotingOpenedAt=" + worstPlayerVotingOpenedAt +
                ", worstPlayerVotingClosedAt=" + worstPlayerVotingClosedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

