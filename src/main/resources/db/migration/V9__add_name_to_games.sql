-- =============================================================================
-- V9__add_name_to_games.sql
-- Adiciona o nome do jogo
-- =============================================================================

ALTER TABLE games
    ADD COLUMN name VARCHAR(120) NOT NULL;

COMMENT ON COLUMN games.name IS 'Nome do jogo';
