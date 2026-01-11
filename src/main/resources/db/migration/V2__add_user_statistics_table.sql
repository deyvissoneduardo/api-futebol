-- =============================================================================
-- V2__add_user_statistics_table.sql
-- Criação da tabela de estatísticas de usuários
-- =============================================================================

-- Tabela para armazenar estatísticas dos usuários
CREATE TABLE user_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    minutes_played INTERVAL NOT NULL DEFAULT '00:00:00',
    goals INTEGER NOT NULL DEFAULT 0,
    complaints INTEGER NOT NULL DEFAULT 0,
    victories INTEGER NOT NULL DEFAULT 0,
    draws INTEGER NOT NULL DEFAULT 0,
    defeats INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_statistics_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Índices para otimização
CREATE INDEX idx_user_statistics_user_id ON user_statistics(user_id);
CREATE INDEX idx_user_statistics_goals ON user_statistics(goals);
CREATE INDEX idx_user_statistics_victories ON user_statistics(victories);

-- Comentários
COMMENT ON TABLE user_statistics IS 'Tabela de estatísticas dos usuários (ADMIN e JOGADOR)';
COMMENT ON COLUMN user_statistics.user_id IS 'ID do usuário (único)';
COMMENT ON COLUMN user_statistics.minutes_played IS 'Total de minutos jogados (formato INTERVAL)';
COMMENT ON COLUMN user_statistics.goals IS 'Total de gols marcados';
COMMENT ON COLUMN user_statistics.complaints IS 'Total de reclamações';
COMMENT ON COLUMN user_statistics.victories IS 'Total de vitórias';
COMMENT ON COLUMN user_statistics.draws IS 'Total de empates';
COMMENT ON COLUMN user_statistics.defeats IS 'Total de derrotas';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_user_statistics_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_user_statistics_updated_at
    BEFORE UPDATE ON user_statistics
    FOR EACH ROW
    EXECUTE FUNCTION update_user_statistics_updated_at();

