-- =============================================================================
-- V5__remove_unique_user_per_game_constraint.sql
-- Remove a constraint que impede um usuário de confirmar múltiplos nomes
-- Permite que o mesmo usuário confirme vários nomes (útil para convidados)
-- =============================================================================

-- Remove a constraint que garante que um usuário só confirma uma vez por jogo
ALTER TABLE game_confirmations 
DROP CONSTRAINT IF EXISTS uk_game_confirmations_game_user;

-- Comentário atualizado
COMMENT ON TABLE game_confirmations IS 'Tabela de confirmações de nomes em jogos. Um usuário pode confirmar múltiplos nomes desde que sejam diferentes.';

