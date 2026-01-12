-- =============================================================================
-- V4__create_game_confirmations_table.sql
-- Criação da tabela de confirmações de nomes em jogos
-- =============================================================================

CREATE TABLE game_confirmations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id UUID NOT NULL,
    user_id UUID NOT NULL,
    confirmed_name VARCHAR(255) NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_game_confirmations_game 
        FOREIGN KEY (game_id) 
        REFERENCES games(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_game_confirmations_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Garante unicidade de nome por jogo
    CONSTRAINT uk_game_confirmations_game_name 
        UNIQUE (game_id, confirmed_name),
    
    -- Garante que um usuário só confirma uma vez por jogo
    CONSTRAINT uk_game_confirmations_game_user 
        UNIQUE (game_id, user_id)
);

-- Índices para otimização
CREATE INDEX idx_game_confirmations_game_id ON game_confirmations(game_id);
CREATE INDEX idx_game_confirmations_user_id ON game_confirmations(user_id);
CREATE INDEX idx_game_confirmations_confirmed_at ON game_confirmations(confirmed_at);

-- Comentários
COMMENT ON TABLE game_confirmations IS 'Tabela de confirmações de nomes em jogos';
COMMENT ON COLUMN game_confirmations.game_id IS 'ID do jogo';
COMMENT ON COLUMN game_confirmations.user_id IS 'ID do usuário que confirmou';
COMMENT ON COLUMN game_confirmations.confirmed_name IS 'Nome confirmado pelo usuário';
COMMENT ON COLUMN game_confirmations.confirmed_at IS 'Data e hora da confirmação';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_game_confirmations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_game_confirmations_updated_at
    BEFORE UPDATE ON game_confirmations
    FOR EACH ROW
    EXECUTE FUNCTION update_game_confirmations_updated_at();

