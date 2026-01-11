# Especificação: Dados Estatísticos de Usuários (ADMIN e JOGADOR)

## 📋 Visão Geral

Este documento especifica a funcionalidade de gerenciamento de dados estatísticos para usuários com perfis **ADMIN** e **JOGADOR**. Os dados devem ser armazenados, consultados e atualizados seguindo regras específicas de permissão.

## 🎯 Objetivo

Implementar um sistema de estatísticas para usuários ADMIN e JOGADOR, permitindo:
- Consulta de dados próprios (ADMIN e JOGADOR)
- Atualização de dados (apenas ADMIN pode modificar dados de ADMIN e JOGADOR)

## 📊 Dados Estatísticos

Cada usuário do tipo **ADMIN** e **JOGADOR** deve ter os seguintes campos:

### 1. Minutos Jogados
- **Tipo**: Duração (horas:minutos:segundos)
- **Formato de armazenamento**: `INTERVAL` no PostgreSQL ou campos separados (horas, minutos, segundos)
- **Regra de cálculo**: 
  - Sempre somar os minutos de forma acumulativa
  - Exemplo: Uma partida de 6 minutos (`0:06:00`) + outra partida de 6 minutos (`0:06:00`) = `00:12:00`
  - Acumular horas, minutos e segundos corretamente
  - Permitir soma e subtração (apenas ADMIN)
- **Valores padrão**: `00:00:00`

### 2. Gols
- **Tipo**: `INTEGER`
- **Regra**: Incremento (+1) ou decremento (-1)
- **Valor padrão**: `0`
- **Permissão**: Apenas ADMIN pode somar/subtrair

### 3. Reclamação
- **Tipo**: `INTEGER`
- **Regra**: Incremento (+1) ou decremento (-1)
- **Valor padrão**: `0`
- **Permissão**: Apenas ADMIN pode somar/subtrair

### 4. Vitória
- **Tipo**: `INTEGER`
- **Regra**: Incremento (+1) ou decremento (-1)
- **Valor padrão**: `0`
- **Permissão**: Apenas ADMIN pode somar/subtrair

### 5. Empate
- **Tipo**: `INTEGER`
- **Regra**: Incremento (+1) ou decremento (-1)
- **Valor padrão**: `0`
- **Permissão**: Apenas ADMIN pode somar/subtrair

### 6. Derrota
- **Tipo**: `INTEGER`
- **Regra**: Incremento (+1) ou decremento (-1)
- **Valor padrão**: `0`
- **Permissão**: Apenas ADMIN pode somar/subtrair

## 🔐 Regras de Permissão

### Consulta de Dados
- ✅ **ADMIN**: Pode consultar seus próprios dados
- ✅ **JOGADOR**: Pode consultar seus próprios dados
- ❌ **ADMIN**: NÃO pode consultar dados de outros usuários (a menos que seja SUPER_ADMIN, seguindo regras existentes)
- ❌ **JOGADOR**: NÃO pode consultar dados de outros usuários

### Atualização de Dados
- ✅ **ADMIN**: Pode atualizar dados de usuários ADMIN e JOGADOR
- ✅ **SUPER_ADMIN**: Pode atualizar dados de todos os perfis (herda permissões de ADMIN)
- ❌ **JOGADOR**: NÃO pode atualizar dados próprios ou de outros usuários

## 📝 Regras de Negócio

### Minutos Jogados
1. A soma de minutos deve considerar horas, minutos e segundos
2. Exemplo de cálculo:
   - Partida 1: `0:06:30` (6 minutos e 30 segundos)
   - Partida 2: `0:05:45` (5 minutos e 45 segundos)
   - Total: `00:12:15` (12 minutos e 15 segundos)
3. Conversão automática:
   - Se segundos >= 60: converter para minutos
   - Se minutos >= 60: converter para horas
4. Permitir valores negativos apenas para correção (subtração)
5. Valor mínimo após subtração: `00:00:00` (não permitir valores negativos finais)

### Outros Campos Numéricos (Gols, Reclamação, Vitória, Empate, Derrota)
1. Valores não podem ser negativos após operação de subtração
2. Se subtração resultar em valor negativo, definir como 0
3. Valores padrão são sempre 0 para novos usuários

## 🗄️ Estrutura do Banco de Dados

### Migration: V2__add_user_statistics_table.sql

```sql
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
```

## 🏗️ Arquitetura e Estrutura

### Domain Layer
- **Entidade**: `UserStatistics.java`
  - Campos: id, userId, minutesPlayed (Duration), goals, complaints, victories, draws, defeats
  - Relacionamento: @ManyToOne com User

### Infrastructure Layer
- **Repository**: `UserStatisticsRepository.java`
  - Métodos: findByUserId, save, update

### Application Layer
- **Service**: `UserStatisticsService.java`
  - Métodos:
    - `findByUserId(UUID userId)`: Busca estatísticas por ID do usuário
    - `findCurrentUserStatistics(UUID userId)`: Busca estatísticas do usuário autenticado
    - `updateMinutes(UUID userId, String minutesToAdd)`: Atualiza minutos (ADMIN only)
    - `updateGoals(UUID userId, Integer value)`: Atualiza gols (ADMIN only)
    - `updateComplaints(UUID userId, Integer value)`: Atualiza reclamações (ADMIN only)
    - `updateVictories(UUID userId, Integer value)`: Atualiza vitórias (ADMIN only)
    - `updateDraws(UUID userId, Integer value)`: Atualiza empates (ADMIN only)
    - `updateDefeats(UUID userId, Integer value)`: Atualiza derrotas (ADMIN only)

### Interface Layer
- **DTO Request**: 
  - `UpdateStatisticsRequest.java`: DTO para atualização de estatísticas
  - Campos: minutesPlayed (String formato "HH:mm:ss"), goals, complaints, victories, draws, defeats (Integer)
- **DTO Response**: 
  - `UserStatisticsResponse.java`: DTO para resposta
  - Campos: id, userId, minutesPlayed (String), goals, complaints, victories, draws, defeats, createdAt, updatedAt
- **Resource**: `UserStatisticsResource.java`
  - Endpoints:
    - `GET /api/users/{userId}/statistics`: Buscar estatísticas de um usuário (ADMIN/JOGADOR - próprio)
    - `GET /api/users/me/statistics`: Buscar próprias estatísticas (ADMIN/JOGADOR)
    - `PUT /api/users/{userId}/statistics`: Atualizar estatísticas (ADMIN only)

## ✅ Critérios de Aceite

### CA1: Consulta de Estatísticas Próprias
- **Dado** que um usuário ADMIN está autenticado
- **Quando** consultar suas próprias estatísticas via `GET /api/users/me/statistics`
- **Então** deve retornar status 200 com os dados das estatísticas do usuário
- **E** deve incluir todos os campos: minutos jogados, gols, reclamação, vitória, empate, derrota

### CA2: Consulta de Estatísticas Próprias - JOGADOR
- **Dado** que um usuário JOGADOR está autenticado
- **Quando** consultar suas próprias estatísticas via `GET /api/users/me/statistics`
- **Então** deve retornar status 200 com os dados das estatísticas do usuário
- **E** deve incluir todos os campos

### CA3: Consulta de Estatísticas de Outro Usuário - ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **Quando** consultar estatísticas de outro usuário via `GET /api/users/{userId}/statistics`
- **Então** deve retornar status 403 (Forbidden) ou status 404 (se usuário não existir)
- **E** deve retornar mensagem de erro apropriada

### CA4: Consulta de Estatísticas de Outro Usuário - JOGADOR
- **Dado** que um usuário JOGADOR está autenticado
- **Quando** tentar consultar estatísticas de outro usuário via `GET /api/users/{userId}/statistics`
- **Então** deve retornar status 403 (Forbidden)
- **E** deve retornar mensagem de erro apropriada

### CA5: Atualização de Minutos Jogados - ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **Quando** atualizar minutos jogados de um usuário ADMIN via `PUT /api/users/{userId}/statistics` com `minutesPlayed: "0:06:00"`
- **E** o usuário já possui `00:05:30` de minutos
- **Então** deve atualizar para `00:11:30`
- **E** deve retornar status 200 com os dados atualizados

### CA6: Atualização de Minutos Jogados - Acumulação Correta
- **Dado** que um usuário ADMIN está autenticado
- **E** um usuário possui `00:58:30` de minutos jogados
- **Quando** adicionar `0:05:00` de minutos
- **Então** deve atualizar para `01:03:30` (conversão automática de minutos para horas)
- **E** deve retornar status 200

### CA7: Atualização de Minutos Jogados - Subtração
- **Dado** que um usuário ADMIN está autenticado
- **E** um usuário possui `00:10:00` de minutos jogados
- **Quando** subtrair `0:03:00` de minutos
- **Então** deve atualizar para `00:07:00`
- **E** deve retornar status 200

### CA8: Atualização de Minutos Jogados - Subtração Mínima
- **Dado** que um usuário ADMIN está autenticado
- **E** um usuário possui `00:02:00` de minutos jogados
- **Quando** subtrair `0:05:00` de minutos
- **Então** deve atualizar para `00:00:00` (não permitir valores negativos)
- **E** deve retornar status 200

### CA9: Atualização de Gols - ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **E** um usuário possui 5 gols
- **Quando** incrementar 2 gols via `PUT /api/users/{userId}/statistics` com `goals: 7`
- **Então** deve atualizar para 7 gols
- **E** deve retornar status 200

### CA10: Atualização de Gols - Subtração Mínima
- **Dado** que um usuário ADMIN está autenticado
- **E** um usuário possui 2 gols
- **Quando** tentar atualizar para -1 gols
- **Então** deve atualizar para 0 gols (não permitir valores negativos)
- **E** deve retornar status 200

### CA11: Atualização de Estatísticas - JOGADOR Negado
- **Dado** que um usuário JOGADOR está autenticado
- **Quando** tentar atualizar suas próprias estatísticas via `PUT /api/users/{userId}/statistics`
- **Então** deve retornar status 403 (Forbidden)
- **E** deve retornar mensagem de erro apropriada

### CA12: Atualização de Estatísticas - ADMIN Negado para SUPER_ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **Quando** tentar atualizar estatísticas de um usuário SUPER_ADMIN
- **Então** deve retornar status 403 (Forbidden)
- **E** deve retornar mensagem de erro apropriada

### CA13: Atualização de Todas as Estatísticas - ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **Quando** atualizar todas as estatísticas de uma vez (gols, reclamação, vitória, empate, derrota)
- **Então** deve atualizar todos os campos corretamente
- **E** deve retornar status 200 com todos os dados atualizados

### CA14: Criação Automática de Estatísticas
- **Dado** que um novo usuário ADMIN ou JOGADOR foi criado
- **Quando** consultar suas estatísticas
- **Então** deve retornar valores padrão: `00:00:00` minutos, 0 gols, 0 reclamações, 0 vitórias, 0 empates, 0 derrotas
- **E** deve criar registro automaticamente se não existir

### CA15: Estatísticas Não Existem para SUPER_ADMIN
- **Dado** que um usuário SUPER_ADMIN existe
- **Quando** tentar consultar suas estatísticas
- **Então** deve retornar status 400 ou 404 com mensagem indicando que SUPER_ADMIN não possui estatísticas

## 🧪 Testes de Unidade

### Teste 1: UserStatisticsService - Soma de Minutos
- **Método**: `updateMinutes`
- **Cenário**: Somar `0:06:00` a `00:05:30`
- **Resultado Esperado**: `00:11:30`

### Teste 2: UserStatisticsService - Conversão de Minutos para Horas
- **Método**: `updateMinutes`
- **Cenário**: Somar `0:05:00` a `00:58:30`
- **Resultado Esperado**: `01:03:30`

### Teste 3: UserStatisticsService - Subtração de Minutos
- **Método**: `updateMinutes`
- **Cenário**: Subtrair `0:03:00` de `00:10:00`
- **Resultado Esperado**: `00:07:00`

### Teste 4: UserStatisticsService - Subtração Mínima de Minutos
- **Método**: `updateMinutes`
- **Cenário**: Subtrair `0:05:00` de `00:02:00`
- **Resultado Esperado**: `00:00:00`

### Teste 5: UserStatisticsService - Atualização de Gols
- **Método**: `updateGoals`
- **Cenário**: Atualizar gols de 5 para 7
- **Resultado Esperado**: 7 gols

### Teste 6: UserStatisticsService - Valor Negativo de Gols
- **Método**: `updateGoals`
- **Cenário**: Tentar atualizar gols para -1
- **Resultado Esperado**: 0 gols

### Teste 7: UserStatisticsService - Validação de Permissão ADMIN
- **Método**: Todos os métodos de atualização
- **Cenário**: Usuário não-ADMIN tentando atualizar
- **Resultado Esperado**: Lançar `UnauthorizedException` ou `BusinessException`

### Teste 8: UserStatisticsService - Validação de Perfil do Usuário Alvo
- **Método**: Todos os métodos de atualização
- **Cenário**: Tentar atualizar estatísticas de usuário SUPER_ADMIN
- **Resultado Esperado**: Lançar `BusinessException` com mensagem apropriada

### Teste 9: UserStatisticsService - Criação Automática
- **Método**: `findByUserId`
- **Cenário**: Buscar estatísticas de usuário que não possui registro
- **Resultado Esperado**: Criar registro com valores padrão e retornar

### Teste 10: UserStatisticsService - Formatação de Minutos
- **Método**: Conversão de Duration para String
- **Cenário**: Converter `Duration.ofHours(1).plusMinutes(5).plusSeconds(30)`
- **Resultado Esperado**: `"01:05:30"`

## 🔄 Testes de Integração

### Teste 1: GET /api/users/me/statistics - ADMIN
- **Setup**: Criar usuário ADMIN autenticado com estatísticas pré-cadastradas
- **Request**: `GET /api/users/me/statistics` com token JWT
- **Assertions**:
  - Status: 200
  - Body contém todos os campos de estatísticas
  - Valores correspondem aos dados no banco

### Teste 2: GET /api/users/me/statistics - JOGADOR
- **Setup**: Criar usuário JOGADOR autenticado
- **Request**: `GET /api/users/me/statistics` com token JWT
- **Assertions**:
  - Status: 200
  - Body contém valores padrão (00:00:00, 0, 0, 0, 0, 0)

### Teste 3: GET /api/users/{userId}/statistics - ADMIN Tentando Acessar Outro
- **Setup**: Criar dois usuários ADMIN, autenticar um
- **Request**: `GET /api/users/{outroUserId}/statistics` com token JWT
- **Assertions**:
  - Status: 403 ou 404

### Teste 4: PUT /api/users/{userId}/statistics - ADMIN Atualizando Próprio
- **Setup**: Criar usuário ADMIN autenticado
- **Request**: `PUT /api/users/{userId}/statistics` com body contendo `minutesPlayed: "0:06:00"`
- **Assertions**:
  - Status: 200
  - Body contém minutos atualizados
  - Banco de dados atualizado corretamente

### Teste 5: PUT /api/users/{userId}/statistics - ADMIN Atualizando JOGADOR
- **Setup**: Criar usuário ADMIN autenticado e usuário JOGADOR
- **Request**: `PUT /api/users/{jogadorId}/statistics` com atualização de gols
- **Assertions**:
  - Status: 200
  - Gols atualizados no banco de dados

### Teste 6: PUT /api/users/{userId}/statistics - JOGADOR Tentando Atualizar
- **Setup**: Criar usuário JOGADOR autenticado
- **Request**: `PUT /api/users/{userId}/statistics` com token JWT
- **Assertions**:
  - Status: 403

### Teste 7: PUT /api/users/{userId}/statistics - Validação de Campos
- **Setup**: Criar usuário ADMIN autenticado
- **Request**: `PUT /api/users/{userId}/statistics` com valores inválidos
- **Assertions**:
  - Status: 400
  - Mensagem de erro apropriada

### Teste 8: PUT /api/users/{userId}/statistics - Soma Acumulativa de Minutos
- **Setup**: Criar usuário ADMIN autenticado e usuário com `00:05:30` de minutos
- **Request**: `PUT /api/users/{userId}/statistics` com `minutesPlayed: "0:06:00"`
- **Assertions**:
  - Status: 200
  - Minutos atualizados para `00:11:30`

### Teste 9: PUT /api/users/{userId}/statistics - Conversão de Minutos para Horas
- **Setup**: Criar usuário ADMIN autenticado e usuário com `00:58:30` de minutos
- **Request**: `PUT /api/users/{userId}/statistics` com `minutesPlayed: "0:05:00"`
- **Assertions**:
  - Status: 200
  - Minutos atualizados para `01:03:30`

### Teste 10: PUT /api/users/{userId}/statistics - Prevenção de Valores Negativos
- **Setup**: Criar usuário ADMIN autenticado e usuário com 2 gols
- **Request**: `PUT /api/users/{userId}/statistics` com `goals: -1`
- **Assertions**:
  - Status: 200
  - Gols atualizados para 0 (não negativo)

## ✅ DEVE FAZER

1. ✅ **DEVE** criar tabela `user_statistics` com todos os campos especificados
2. ✅ **DEVE** criar migration Flyway seguindo padrão `V{N}__{nome}.sql`
3. ✅ **DEVE** usar tipo `INTERVAL` do PostgreSQL para armazenar minutos jogados
4. ✅ **DEVE** implementar validação de permissão antes de qualquer atualização
5. ✅ **DEVE** validar que apenas ADMIN pode atualizar estatísticas
6. ✅ **DEVE** validar que usuário alvo é ADMIN ou JOGADOR (não SUPER_ADMIN)
7. ✅ **DEVE** implementar soma acumulativa correta para minutos jogados
8. ✅ **DEVE** converter automaticamente segundos para minutos e minutos para horas
9. ✅ **DEVE** prevenir valores negativos em todos os campos numéricos
10. ✅ **DEVE** criar registro de estatísticas automaticamente quando usuário consulta pela primeira vez
11. ✅ **DEVE** retornar valores padrão para novos usuários
12. ✅ **DEVE** implementar testes de unidade para todas as regras de negócio
13. ✅ **DEVE** implementar testes de integração para todos os endpoints
14. ✅ **DEVE** usar DTOs (Request/Response) para comunicação com a API
15. ✅ **DEVE** seguir padrão arquitetural existente (Domain, Infrastructure, Application, Interface)
16. ✅ **DEVE** usar annotations do Jakarta Validation para validação de entrada
17. ✅ **DEVE** retornar mensagens de erro apropriadas em português
18. ✅ **DEVE** usar `@RolesAllowed` para controle de acesso nos endpoints
19. ✅ **DEVE** implementar tratamento de exceções no `GlobalExceptionHandler`
20. ✅ **DEVE** documentar endpoints com OpenAPI annotations

## ❌ NÃO DEVE FAZER

1. ❌ **NÃO DEVE** permitir que JOGADOR atualize suas próprias estatísticas
2. ❌ **NÃO DEVE** permitir que ADMIN atualize estatísticas de SUPER_ADMIN
3. ❌ **NÃO DEVE** permitir valores negativos finais em nenhum campo
4. ❌ **NÃO DEVE** permitir que usuários consultem estatísticas de outros usuários (exceto próprio)
5. ❌ **NÃO DEVE** criar estatísticas para usuários SUPER_ADMIN
6. ❌ **NÃO DEVE** usar tipo String para armazenar minutos jogados no banco (usar INTERVAL)
7. ❌ **NÃO DEVE** implementar lógica de negócio na camada de interface
8. ❌ **NÃO DEVE** expor entidades JPA diretamente na API (usar DTOs)
9. ❌ **NÃO DEVE** ignorar validações de permissão
10. ❌ **NÃO DEVE** permitir valores nulos em campos obrigatórios
11. ❌ **NÃO DEVE** implementar sem testes (unidade e integração)
12. ❌ **NÃO DEVE** usar `@Transactional` na camada de interface
13. ❌ **NÃO DEVE** permitir atualização sem autenticação
14. ❌ **NÃO DEVE** perder precisão na conversão de minutos (segundos devem ser preservados)
15. ❌ **NÃO DEVE** criar múltiplos registros de estatísticas para o mesmo usuário (usar UNIQUE constraint)

## 🔧 Regras Técnicas

### Padrões de Código
- Seguir convenções Java existentes no projeto
- Usar Lombok para reduzir boilerplate
- Usar Builder pattern quando apropriado
- Seguir princípios SOLID

### Validações
- Usar `@NotNull`, `@Min`, `@Max` quando apropriado
- Validações customizadas para formato de minutos (HH:mm:ss)
- Validação de perfil de usuário antes de operações

### Tratamento de Exceções
- `ResourceNotFoundException`: Quando usuário não existe
- `UnauthorizedException`: Quando usuário não tem permissão
- `BusinessException`: Quando regra de negócio é violada

### Formato de Dados
- **Minutos jogados no Request**: String no formato `"HH:mm:ss"` (ex: `"01:05:30"`)
- **Minutos jogados no Response**: String no formato `"HH:mm:ss"` (ex: `"01:05:30"`)
- **Minutos jogados no Banco**: Tipo `INTERVAL` do PostgreSQL
- **Outros campos**: Integer (não nullable, default 0)

## 📚 Dependências Necessárias

- ✅ Quarkus (já presente)
- ✅ Hibernate ORM com Panache (já presente)
- ✅ Flyway (já presente)
- ✅ Jakarta Validation (já presente)
- ✅ Lombok (já presente)
- ✅ PostgreSQL Driver (já presente)
- ✅ JWT (já presente)

## 🚀 Ordem de Implementação Sugerida

1. Criar migration `V2__add_user_statistics_table.sql`
2. Criar entidade `UserStatistics` no domain
3. Criar `UserStatisticsRepository` no infrastructure
4. Criar `UserStatisticsService` no application com todas as regras
5. Criar DTOs (Request e Response)
6. Criar `UserStatisticsResource` no interface
7. Implementar testes de unidade
8. Implementar testes de integração
9. Validar todos os critérios de aceite
10. Documentar endpoints no OpenAPI

## 📝 Notas Adicionais

- A implementação deve ser compatível com a estrutura existente do projeto
- Manter consistência com padrões de nomenclatura já utilizados
- Garantir que todas as operações sejam transacionais quando necessário
- Considerar performance nas consultas (índices já criados na migration)
- Usar `@Transactional` apenas na camada de Application quando necessário

