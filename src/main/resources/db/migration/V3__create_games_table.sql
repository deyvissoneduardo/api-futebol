-- =============================================================================
-- V3__create_games_table.sql
-- Criação da tabela de jogos
-- =============================================================================

CREATE TABLE games (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_date TIMESTAMP WITH TIME ZONE NOT NULL,
    released BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para otimização
CREATE INDEX idx_games_game_date ON games(game_date);
CREATE INDEX idx_games_released ON games(released);

-- Comentários
COMMENT ON TABLE games IS 'Tabela de jogos semanais';
COMMENT ON COLUMN games.game_date IS 'Data e hora do jogo';
COMMENT ON COLUMN games.released IS 'Indica se a lista de confirmação está liberada';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_games_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_games_updated_at
    BEFORE UPDATE ON games
    FOR EACH ROW
    EXECUTE FUNCTION update_games_updated_at();

