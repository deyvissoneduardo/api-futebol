-- =============================================================================
-- V7__remove_user_fk_for_guests.sql
-- Remove a foreign key constraint de user_id para permitir UUIDs de convidados
-- =============================================================================

-- Remove a foreign key constraint que impede UUIDs de convidados
ALTER TABLE game_confirmations 
DROP CONSTRAINT IF EXISTS fk_game_confirmations_user;

-- Comentário atualizado
COMMENT ON COLUMN game_confirmations.user_id IS 'ID do usuário (quando is_guest=false) ou UUID único do convidado (quando is_guest=true)';

