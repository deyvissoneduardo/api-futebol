-- =============================================================================
-- V1__create_users_table.sql
-- Criação da tabela de usuários
-- =============================================================================

-- Criação do tipo ENUM para o perfil do usuário
CREATE TYPE user_profile AS ENUM ('SUPER_ADMIN', 'ADMIN', 'JOGADOR');

-- Criação da tabela users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    photo VARCHAR(500),
    profile user_profile NOT NULL DEFAULT 'JOGADOR',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para otimização de consultas
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_profile ON users(profile);

-- Comentários na tabela e colunas
COMMENT ON TABLE users IS 'Tabela de usuários do sistema';
COMMENT ON COLUMN users.id IS 'Identificador único do usuário (UUID)';
COMMENT ON COLUMN users.full_name IS 'Nome completo do usuário';
COMMENT ON COLUMN users.email IS 'E-mail do usuário (único)';
COMMENT ON COLUMN users.password IS 'Senha do usuário (hash BCrypt)';
COMMENT ON COLUMN users.photo IS 'URL da foto do perfil do usuário';
COMMENT ON COLUMN users.profile IS 'Perfil/papel do usuário no sistema';
COMMENT ON COLUMN users.active IS 'Indica se o usuário está ativo';
COMMENT ON COLUMN users.created_at IS 'Data e hora de criação do registro';
COMMENT ON COLUMN users.updated_at IS 'Data e hora da última atualização';

-- Inserir usuário SUPER_ADMIN padrão (senha: admin123)
-- Hash BCrypt gerado para 'admin123'
INSERT INTO users (full_name, email, password, profile, active)
VALUES (
    'Administrador do Sistema',
    'admin@futebol.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqI6S3RqK4PFDGg.pXhCMGBFqQPga',
    'SUPER_ADMIN',
    TRUE
);

