-- =============================================================================
-- V6__add_guest_fields_to_game_confirmations.sql
-- Adiciona campos para suporte a convidados nas confirmações
-- =============================================================================

-- Adiciona campo para identificar se é convidado
ALTER TABLE game_confirmations 
ADD COLUMN is_guest BOOLEAN NOT NULL DEFAULT FALSE;

-- Adiciona campo para rastrear quem confirmou o convidado (quando is_guest = true)
ALTER TABLE game_confirmations 
ADD COLUMN confirmed_by_user_id UUID;

-- Adiciona foreign key para confirmed_by_user_id
ALTER TABLE game_confirmations
ADD CONSTRAINT fk_game_confirmations_confirmed_by_user 
    FOREIGN KEY (confirmed_by_user_id) 
    REFERENCES users(id) 
    ON DELETE SET NULL;

-- Índice para otimização
CREATE INDEX idx_game_confirmations_confirmed_by_user_id ON game_confirmations(confirmed_by_user_id);
CREATE INDEX idx_game_confirmations_is_guest ON game_confirmations(is_guest);

-- Comentários
COMMENT ON COLUMN game_confirmations.is_guest IS 'Indica se a confirmação é para um convidado';
COMMENT ON COLUMN game_confirmations.confirmed_by_user_id IS 'ID do usuário que confirmou o convidado (quando is_guest = true)';

