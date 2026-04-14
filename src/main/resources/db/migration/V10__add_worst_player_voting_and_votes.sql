ALTER TABLE games
ADD COLUMN worst_player_voting_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE games
ADD COLUMN worst_player_voting_opened_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE games
ADD COLUMN worst_player_voting_closed_at TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN games.worst_player_voting_enabled IS 'Indica se a votacao de pior do jogo esta aberta';
COMMENT ON COLUMN games.worst_player_voting_opened_at IS 'Data/hora de abertura da votacao de pior do jogo';
COMMENT ON COLUMN games.worst_player_voting_closed_at IS 'Data/hora de encerramento da votacao de pior do jogo';

CREATE TABLE game_worst_player_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id UUID NOT NULL,
    voter_user_id UUID NOT NULL,
    voter_name_snapshot VARCHAR(255) NOT NULL,
    target_confirmation_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_game_worst_player_votes_game
        FOREIGN KEY (game_id)
        REFERENCES games(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_game_worst_player_votes_voter_user
        FOREIGN KEY (voter_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_game_worst_player_votes_target_confirmation
        FOREIGN KEY (target_confirmation_id)
        REFERENCES game_confirmations(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_game_worst_player_votes_game_voter
        UNIQUE (game_id, voter_user_id)
);

CREATE INDEX idx_game_worst_player_votes_game_id ON game_worst_player_votes(game_id);
CREATE INDEX idx_game_worst_player_votes_voter_user_id ON game_worst_player_votes(voter_user_id);
CREATE INDEX idx_game_worst_player_votes_target_confirmation_id ON game_worst_player_votes(target_confirmation_id);
CREATE INDEX idx_game_worst_player_votes_created_at ON game_worst_player_votes(created_at);

COMMENT ON TABLE game_worst_player_votes IS 'Historico de votos de pior do jogo por partida';
COMMENT ON COLUMN game_worst_player_votes.game_id IS 'ID do jogo';
COMMENT ON COLUMN game_worst_player_votes.voter_user_id IS 'ID do usuario que votou';
COMMENT ON COLUMN game_worst_player_votes.voter_name_snapshot IS 'Nome do usuario no momento em que votou';
COMMENT ON COLUMN game_worst_player_votes.target_confirmation_id IS 'ID da confirmacao do jogador alvo dentro do jogo';

CREATE OR REPLACE FUNCTION update_game_worst_player_votes_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_game_worst_player_votes_updated_at
    BEFORE UPDATE ON game_worst_player_votes
    FOR EACH ROW
    EXECUTE FUNCTION update_game_worst_player_votes_updated_at();
