-- =============================================================================
-- V8__create_team_divisions_tables.sql
-- Criação das tabelas de divisão de times
-- =============================================================================

-- Tabela principal de divisões de times
CREATE TABLE team_divisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id UUID NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    players_per_team INTEGER NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_team_divisions_game 
        FOREIGN KEY (game_id) 
        REFERENCES games(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_team_divisions_user 
        FOREIGN KEY (created_by_user_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT uk_team_divisions_game_version 
        UNIQUE (game_id, version)
);

-- Tabela de jogadores por time
CREATE TABLE team_division_players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_division_id UUID NOT NULL,
    team_number INTEGER NOT NULL,
    player_name VARCHAR(255) NOT NULL,
    player_user_id UUID,  -- NULL se for jogador não confirmado
    position_in_team INTEGER NOT NULL,  -- Ordem dentro do time (1, 2, 3...)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_team_division_players_division 
        FOREIGN KEY (team_division_id) 
        REFERENCES team_divisions(id) 
        ON DELETE CASCADE
);

-- Índices para otimização
CREATE INDEX idx_team_divisions_game_id ON team_divisions(game_id);
CREATE INDEX idx_team_divisions_is_current ON team_divisions(is_current);
CREATE INDEX idx_team_divisions_version ON team_divisions(version);
CREATE INDEX idx_team_division_players_division_id ON team_division_players(team_division_id);
CREATE INDEX idx_team_division_players_team_number ON team_division_players(team_number);

-- Comentários
COMMENT ON TABLE team_divisions IS 'Tabela de divisões de times para jogos';
COMMENT ON COLUMN team_divisions.version IS 'Versão da divisão (permite histórico)';
COMMENT ON COLUMN team_divisions.is_current IS 'Indica se esta é a versão atual da divisão';
COMMENT ON COLUMN team_divisions.players_per_team IS 'Número máximo de jogadores por time';
COMMENT ON TABLE team_division_players IS 'Jogadores de cada time na divisão';
COMMENT ON COLUMN team_division_players.player_user_id IS 'ID do usuário (NULL se for jogador não confirmado)';
COMMENT ON COLUMN team_division_players.team_number IS 'Número do time (1, 2, 3...)';
COMMENT ON COLUMN team_division_players.position_in_team IS 'Posição do jogador dentro do time';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_team_divisions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_team_divisions_updated_at
    BEFORE UPDATE ON team_divisions
    FOR EACH ROW
    EXECUTE FUNCTION update_team_divisions_updated_at();

