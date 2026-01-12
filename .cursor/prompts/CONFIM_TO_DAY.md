# 🎯 Prompt: Confirmação de Nome em Lista de Jogo

---

## 1. 🌍 Contexto

**Linguagem/Framework:**
- Java 21 com Quarkus 3.30.6
- PostgreSQL como banco de dados
- Maven para gerenciamento de dependências

**Arquitetura/Padrão:**
- Clean Architecture conforme definido em `.cursor/rules/01-architecture.mdc`
- Estrutura de camadas: `config/`, `core/`, `domain/`, `application/`, `infrastructure/`, `interfaces/`

**Público-alvo:**
- Desenvolvedores backend do projeto api-futebol

**Dependências Já Instaladas:**
```xml
- quarkus-rest (JAX-RS REST)
- quarkus-rest-jackson (serialização JSON)
- quarkus-flyway (migrations)
- quarkus-hibernate-validator (validações)
- quarkus-smallrye-openapi (Swagger/OpenAPI)
- quarkus-hibernate-orm-panache (ORM)
- quarkus-smallrye-jwt (autenticação JWT)
- quarkus-jdbc-postgresql (conexão PostgreSQL)
- quarkus-arc (CDI)
- quarkus-junit5 (testes)
- rest-assured (testes de integração)
- lombok (redução de boilerplate)
```

---

## 2. 🎯 Objetivo

**O que precisa ser entregue:**
1. Entidade `Game` (Jogo) com data e hora
2. Entidade `GameConfirmation` (Confirmação de Nome) vinculada a um jogo
3. Sistema de liberação de lista pelo ADMIN
4. Endpoint para confirmar nome (ADMIN e JOGADOR)
5. Validação de unicidade de nome por jogo
6. Bloqueio automático após início do jogo
7. Endpoint administrativo para consultar lista completa (apenas ADMIN)
8. Persistência semanal (histórico sem apagar dados)
9. Testes unitários e de integração completos

**Propósito da tarefa:**
- Permitir que usuários confirmem seus nomes em jogos semanais
- Garantir controle de acesso baseado em perfil (ADMIN/JOGADOR)
- Manter histórico completo de confirmações

**Resultado esperado:**
- Código funcional seguindo Clean Architecture
- Migrations do Flyway criadas
- Testes passando
- Documentação OpenAPI funcionando
- Todas as regras de negócio implementadas

---

## 3. ⚙️ Instruções Específicas

### 3.1 Entidade Game (Jogo)

**Campos obrigatórios:**
- `id`: UUID (chave primária)
- `gameDate`: OffsetDateTime (data e hora do jogo, não nulo)
- `released`: Boolean (indica se a lista está liberada, default false)
- `createdAt`: OffsetDateTime
- `updatedAt`: OffsetDateTime

**Regras:**
- Cada jogo é único por data/hora
- Apenas ADMIN pode alterar o status `released`

### 3.2 Entidade GameConfirmation (Confirmação)

**Campos obrigatórios:**
- `id`: UUID (chave primária)
- `gameId`: UUID (foreign key para Game, não nulo)
- `userId`: UUID (foreign key para User, não nulo)
- `confirmedName`: String (nome confirmado, não nulo)
- `confirmedAt`: OffsetDateTime (data/hora da confirmação)
- `createdAt`: OffsetDateTime
- `updatedAt`: OffsetDateTime

**Constraints:**
- UNIQUE (game_id, confirmed_name) - garante unicidade de nome por jogo
- UNIQUE (game_id, user_id) - garante que um usuário só confirma uma vez por jogo

**Regras:**
- Não pode ser apagado nem atualizado (histórico permanente)
- Vinculado a um jogo específico (dia e hora)

### 3.3 Estrutura de Arquivos a Criar

```
src/main/java/br/com/futebol/
├── domain/
│   └── game/
│       ├── Game.java (Entidade)
│       └── GameConfirmation.java (Entidade)
├── application/
│   └── game/
│       ├── GameService.java
│       └── GameConfirmationService.java
├── infrastructure/
│   └── game/
│       ├── GameRepository.java
│       └── GameConfirmationRepository.java
└── interfaces/
    └── game/
        ├── GameResource.java
        ├── GameConfirmationResource.java
        ├── CreateGameRequest.java
        ├── ReleaseGameRequest.java
        ├── ConfirmNameRequest.java
        ├── GameResponse.java
        ├── GameConfirmationResponse.java
        └── GameConfirmationListResponse.java

src/main/resources/db/migration/
├── V{N}__create_games_table.sql
└── V{N+1}__create_game_confirmations_table.sql
```

### 3.4 Endpoints da API

| Método | Rota | Descrição | Auth | Roles |
|--------|------|-----------|------|-------|
| POST | `/api/games` | Criar jogo | ✅ | ADMIN, SUPER_ADMIN |
| GET | `/api/games` | Listar jogos | ✅ | ADMIN, JOGADOR |
| GET | `/api/games/{id}` | Buscar jogo | ✅ | ADMIN, JOGADOR |
| PUT | `/api/games/{id}/release` | Liberar lista | ✅ | ADMIN, SUPER_ADMIN |
| POST | `/api/games/{gameId}/confirmations` | Confirmar nome | ✅ | ADMIN, JOGADOR |
| GET | `/api/games/{gameId}/confirmations` | Listar confirmações (ADMIN) | ✅ | ADMIN, SUPER_ADMIN |
| GET | `/api/games/{gameId}/confirmations/me` | Minha confirmação | ✅ | ADMIN, JOGADOR |

### 3.5 Regras de Negócio Detalhadas

#### RB-01 – Autenticação obrigatória
- Todas as rotas devem ser autenticadas (exceto health check)
- Usar JWT token válido

#### RB-02 – Liberação da lista pelo ADMIN
- Apenas ADMIN/SUPER_ADMIN pode liberar lista
- Enquanto `released = false`, nenhum usuário pode confirmar
- Retornar erro 403 com mensagem clara quando lista não está liberada

#### RB-03 – Confirmação de nome
- ADMIN e JOGADOR podem confirmar
- Validar que lista está liberada
- Validar que jogo ainda não iniciou (gameDate > now)
- Campo `confirmedName` é texto livre (String)

#### RB-04 – Unicidade do nome por jogo
- Antes de confirmar, verificar se nome já existe para o mesmo jogo
- Se existir, retornar erro 409 (Conflict) com mensagem solicitando outro nome
- Usar constraint UNIQUE no banco para garantir atomicidade

#### RB-05 – Bloqueio automático após início do jogo
- Verificar se `gameDate <= now()` antes de permitir confirmação
- Se jogo já iniciou, retornar erro 400 com mensagem "Lista encerrada"
- Bloqueio é automático (não precisa flag adicional)

#### RB-06 – Persistência semanal (histórico)
- Confirmações nunca são apagadas
- Confirmações nunca são atualizadas
- Cada confirmação é um novo registro vinculado ao jogo
- Histórico completo deve ser mantido

#### RB-07 – Consulta administrativa da lista
- Endpoint `/api/games/{gameId}/confirmations` apenas para ADMIN
- Retornar todos os nomes confirmados para o jogo
- Incluir data/hora da confirmação e identificador do usuário

---

## 4. ✓ Regras: DEVE / NÃO DEVE

### ✅ DEVE:

- **DEVE** seguir a estrutura de Clean Architecture definida em `.cursor/rules/01-architecture.mdc`
- **DEVE** seguir os padrões de código definidos em `.cursor/rules/02-coding-standards.mdc`
- **DEVE** usar Lombok (@Builder, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)
- **DEVE** documentar todos os endpoints com OpenAPI (@Tag, @Operation, @APIResponses)
- **DEVE** usar DTOs para Request e Response (nunca expor entidades diretamente)
- **DEVE** validar inputs com Bean Validation (@NotBlank, @NotNull, @Size, etc)
- **DEVE** usar UUID para chaves primárias
- **DEVE** criar migrations Flyway para o banco
- **DEVE** implementar constraint UNIQUE (game_id, confirmed_name) no banco
- **DEVE** implementar constraint UNIQUE (game_id, user_id) no banco
- **DEVE** usar transações (@Transactional) para garantir atomicidade na verificação de unicidade
- **DEVE** implementar tratamento de exceções global
- **DEVE** criar testes unitários para Services
- **DEVE** criar testes de integração para Resources
- **DEVE** usar o padrão AAA (Arrange, Act, Assert) nos testes
- **DEVE** usar @DisplayName em português nos testes
- **DEVE** validar permissões com @RolesAllowed nos endpoints
- **DEVE** retornar mensagens de erro claras em português
- **DEVE** usar OffsetDateTime para datas/horas
- **DEVE** implementar validação de data/hora do jogo antes de permitir confirmação
- **DEVE** garantir que confirmações não sejam apagadas nem atualizadas

### ❌ NÃO DEVE:

- **NÃO DEVE** expor entidades JPA diretamente nos endpoints
- **NÃO DEVE** permitir confirmação sem lista liberada
- **NÃO DEVE** permitir confirmação após início do jogo
- **NÃO DEVE** permitir nomes duplicados no mesmo jogo
- **NÃO DEVE** permitir que JOGADOR libere lista
- **NÃO DEVE** permitir que JOGADOR acesse endpoint administrativo de lista completa
- **NÃO DEVE** apagar confirmações (histórico permanente)
- **NÃO DEVE** atualizar confirmações existentes
- **NÃO DEVE** ignorar validações de segurança
- **NÃO DEVE** criar código duplicado (DRY)
- **NÃO DEVE** modificar arquivos de migration já existentes
- **NÃO DEVE** usar String para armazenar data/hora (usar OffsetDateTime)
- **NÃO DEVE** permitir confirmação sem autenticação
- **NÃO DEVE** retornar informações sensíveis em logs
- **NÃO DEVE** implementar sem testes (unidade e integração)

---

## 5. 🗄️ Estrutura do Banco de Dados

### Migration: V{N}__create_games_table.sql

```sql
-- =============================================================================
-- V{N}__create_games_table.sql
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
```

### Migration: V{N+1}__create_game_confirmations_table.sql

```sql
-- =============================================================================
-- V{N+1}__create_game_confirmations_table.sql
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
```

---

## 6. ✅ Critérios de Aceite

### CA1: Criar Jogo - ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **Quando** criar um jogo via `POST /api/games` com `gameDate: "2024-12-25T10:00:00Z"`
- **Então** deve retornar status 201 com dados do jogo criado
- **E** o jogo deve ter `released: false` por padrão

### CA2: Criar Jogo - JOGADOR Negado
- **Dado** que um usuário JOGADOR está autenticado
- **Quando** tentar criar um jogo via `POST /api/games`
- **Então** deve retornar status 403 (Forbidden)

### CA3: Liberar Lista - ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **E** existe um jogo com `released: false`
- **Quando** liberar a lista via `PUT /api/games/{id}/release`
- **Então** deve retornar status 200
- **E** o jogo deve ter `released: true`

### CA4: Liberar Lista - JOGADOR Negado
- **Dado** que um usuário JOGADOR está autenticado
- **Quando** tentar liberar lista via `PUT /api/games/{id}/release`
- **Então** deve retornar status 403 (Forbidden)

### CA5: Confirmar Nome - Lista Não Liberada
- **Dado** que um usuário ADMIN está autenticado
- **E** existe um jogo com `released: false`
- **Quando** tentar confirmar nome via `POST /api/games/{gameId}/confirmations`
- **Então** deve retornar status 403
- **E** deve retornar mensagem "Lista não está liberada"

### CA6: Confirmar Nome - Jogo Já Iniciado
- **Dado** que um usuário ADMIN está autenticado
- **E** existe um jogo com `released: true` e `gameDate` no passado
- **Quando** tentar confirmar nome via `POST /api/games/{gameId}/confirmations`
- **Então** deve retornar status 400
- **E** deve retornar mensagem "Lista encerrada - jogo já iniciou"

### CA7: Confirmar Nome - Sucesso
- **Dado** que um usuário ADMIN está autenticado
- **E** existe um jogo com `released: true` e `gameDate` no futuro
- **Quando** confirmar nome "João Silva" via `POST /api/games/{gameId}/confirmations`
- **Então** deve retornar status 201
- **E** deve retornar dados da confirmação criada

### CA8: Confirmar Nome - Nome Duplicado
- **Dado** que um usuário ADMIN está autenticado
- **E** existe um jogo com `released: true` e `gameDate` no futuro
- **E** já existe confirmação com nome "João Silva" para este jogo
- **Quando** tentar confirmar nome "João Silva" novamente
- **Então** deve retornar status 409 (Conflict)
- **E** deve retornar mensagem "Nome já confirmado para este jogo. Escolha outro nome."

### CA9: Confirmar Nome - JOGADOR
- **Dado** que um usuário JOGADOR está autenticado
- **E** existe um jogo com `released: true` e `gameDate` no futuro
- **Quando** confirmar nome "Maria Santos" via `POST /api/games/{gameId}/confirmations`
- **Então** deve retornar status 201
- **E** deve retornar dados da confirmação criada

### CA10: Confirmar Nome - Usuário Já Confirmou
- **Dado** que um usuário ADMIN está autenticado
- **E** já confirmou nome para um jogo específico
- **Quando** tentar confirmar outro nome para o mesmo jogo
- **Então** deve retornar status 409 (Conflict)
- **E** deve retornar mensagem "Você já confirmou seu nome para este jogo"

### CA11: Listar Confirmações - ADMIN
- **Dado** que um usuário ADMIN está autenticado
- **E** existem 5 confirmações para um jogo
- **Quando** consultar via `GET /api/games/{gameId}/confirmations`
- **Então** deve retornar status 200
- **E** deve retornar lista com todas as 5 confirmações
- **E** cada confirmação deve incluir: nome, data/hora da confirmação, ID do usuário

### CA12: Listar Confirmações - JOGADOR Negado
- **Dado** que um usuário JOGADOR está autenticado
- **Quando** tentar consultar lista completa via `GET /api/games/{gameId}/confirmations`
- **Então** deve retornar status 403 (Forbidden)

### CA13: Minha Confirmação - ADMIN/JOGADOR
- **Dado** que um usuário está autenticado
- **E** confirmou nome para um jogo
- **Quando** consultar via `GET /api/games/{gameId}/confirmations/me`
- **Então** deve retornar status 200
- **E** deve retornar dados da confirmação do usuário

### CA14: Minha Confirmação - Não Confirmou
- **Dado** que um usuário está autenticado
- **E** não confirmou nome para um jogo
- **Quando** consultar via `GET /api/games/{gameId}/confirmations/me`
- **Então** deve retornar status 404
- **E** deve retornar mensagem "Você ainda não confirmou seu nome para este jogo"

### CA15: Histórico Permanente
- **Dado** que existem confirmações de semanas anteriores
- **Quando** criar um novo jogo para a próxima semana
- **E** confirmar nomes para o novo jogo
- **Então** as confirmações antigas devem permanecer no banco
- **E** as novas confirmações devem ser registros separados

---

## 7. 🧪 Testes de Unidade

### Teste 1: GameService - Criar Jogo
- **Método**: `createGame`
- **Cenário**: Criar jogo com data/hora válida
- **Resultado Esperado**: Jogo criado com `released: false`

### Teste 2: GameService - Liberar Lista
- **Método**: `releaseGame`
- **Cenário**: Liberar lista de jogo existente
- **Resultado Esperado**: Jogo com `released: true`

### Teste 3: GameService - Validação de Permissão ADMIN
- **Método**: `releaseGame`
- **Cenário**: JOGADOR tentando liberar lista
- **Resultado Esperado**: Lançar `UnauthorizedException`

### Teste 4: GameConfirmationService - Confirmar Nome - Sucesso
- **Método**: `confirmName`
- **Cenário**: Lista liberada, jogo no futuro, nome único
- **Resultado Esperado**: Confirmação criada com sucesso

### Teste 5: GameConfirmationService - Confirmar Nome - Lista Não Liberada
- **Método**: `confirmName`
- **Cenário**: Tentar confirmar com `released: false`
- **Resultado Esperado**: Lançar `BusinessException` com mensagem apropriada

### Teste 6: GameConfirmationService - Confirmar Nome - Jogo Já Iniciado
- **Método**: `confirmName`
- **Cenário**: Tentar confirmar com `gameDate` no passado
- **Resultado Esperado**: Lançar `BusinessException` com mensagem "Lista encerrada"

### Teste 7: GameConfirmationService - Confirmar Nome - Nome Duplicado
- **Método**: `confirmName`
- **Cenário**: Tentar confirmar nome que já existe para o jogo
- **Resultado Esperado**: Lançar `BusinessException` com status 409

### Teste 8: GameConfirmationService - Confirmar Nome - Usuário Já Confirmou
- **Método**: `confirmName`
- **Cenário**: Tentar confirmar novamente para o mesmo jogo
- **Resultado Esperado**: Lançar `BusinessException` com mensagem apropriada

### Teste 9: GameConfirmationService - Listar Confirmações - ADMIN
- **Método**: `listConfirmations`
- **Cenário**: ADMIN consultando lista completa
- **Resultado Esperado**: Retornar todas as confirmações do jogo

### Teste 10: GameConfirmationService - Listar Confirmações - JOGADOR Negado
- **Método**: `listConfirmations`
- **Cenário**: JOGADOR tentando acessar lista completa
- **Resultado Esperado**: Lançar `UnauthorizedException`

### Teste 11: GameConfirmationService - Minha Confirmação
- **Método**: `findMyConfirmation`
- **Cenário**: Usuário que confirmou consultando própria confirmação
- **Resultado Esperado**: Retornar confirmação do usuário

### Teste 12: GameConfirmationService - Minha Confirmação - Não Existe
- **Método**: `findMyConfirmation`
- **Cenário**: Usuário que não confirmou consultando
- **Resultado Esperado**: Lançar `ResourceNotFoundException`

---

## 8. 🔄 Testes de Integração

### Teste 1: POST /api/games - ADMIN Criar Jogo
- **Setup**: Criar usuário ADMIN autenticado
- **Request**: `POST /api/games` com body contendo `gameDate`
- **Assertions**:
  - Status: 201
  - Body contém dados do jogo criado
  - `released: false` por padrão

### Teste 2: POST /api/games - JOGADOR Negado
- **Setup**: Criar usuário JOGADOR autenticado
- **Request**: `POST /api/games` com token JWT
- **Assertions**:
  - Status: 403

### Teste 3: PUT /api/games/{id}/release - ADMIN Liberar Lista
- **Setup**: Criar usuário ADMIN autenticado e jogo com `released: false`
- **Request**: `PUT /api/games/{id}/release` com token JWT
- **Assertions**:
  - Status: 200
  - Banco de dados atualizado com `released: true`

### Teste 4: POST /api/games/{gameId}/confirmations - Confirmar Nome Sucesso
- **Setup**: Criar usuário ADMIN autenticado, jogo com `released: true` e `gameDate` no futuro
- **Request**: `POST /api/games/{gameId}/confirmations` com `confirmedName: "João Silva"`
- **Assertions**:
  - Status: 201
  - Body contém dados da confirmação
  - Banco de dados contém registro da confirmação

### Teste 5: POST /api/games/{gameId}/confirmations - Lista Não Liberada
- **Setup**: Criar usuário ADMIN autenticado, jogo com `released: false`
- **Request**: `POST /api/games/{gameId}/confirmations` com `confirmedName`
- **Assertions**:
  - Status: 403
  - Mensagem de erro apropriada

### Teste 6: POST /api/games/{gameId}/confirmations - Jogo Já Iniciado
- **Setup**: Criar usuário ADMIN autenticado, jogo com `released: true` e `gameDate` no passado
- **Request**: `POST /api/games/{gameId}/confirmations` com `confirmedName`
- **Assertions**:
  - Status: 400
  - Mensagem "Lista encerrada"

### Teste 7: POST /api/games/{gameId}/confirmations - Nome Duplicado
- **Setup**: Criar usuário ADMIN autenticado, jogo liberado, confirmação existente com nome "João"
- **Request**: `POST /api/games/{gameId}/confirmations` com `confirmedName: "João"`
- **Assertions**:
  - Status: 409
  - Mensagem solicitando outro nome

### Teste 8: POST /api/games/{gameId}/confirmations - Usuário Já Confirmou
- **Setup**: Criar usuário ADMIN autenticado, jogo liberado, confirmação existente do mesmo usuário
- **Request**: `POST /api/games/{gameId}/confirmations` com `confirmedName`
- **Assertions**:
  - Status: 409
  - Mensagem informando que já confirmou

### Teste 9: GET /api/games/{gameId}/confirmations - ADMIN Listar
- **Setup**: Criar usuário ADMIN autenticado, jogo com 3 confirmações
- **Request**: `GET /api/games/{gameId}/confirmations` com token JWT
- **Assertions**:
  - Status: 200
  - Body contém array com 3 confirmações
  - Cada confirmação contém: nome, data/hora, ID do usuário

### Teste 10: GET /api/games/{gameId}/confirmations - JOGADOR Negado
- **Setup**: Criar usuário JOGADOR autenticado
- **Request**: `GET /api/games/{gameId}/confirmations` com token JWT
- **Assertions**:
  - Status: 403

### Teste 11: GET /api/games/{gameId}/confirmations/me - Minha Confirmação
- **Setup**: Criar usuário autenticado, jogo, confirmação do usuário
- **Request**: `GET /api/games/{gameId}/confirmations/me` com token JWT
- **Assertions**:
  - Status: 200
  - Body contém confirmação do usuário

### Teste 12: GET /api/games/{gameId}/confirmations/me - Não Confirmou
- **Setup**: Criar usuário autenticado, jogo, sem confirmação do usuário
- **Request**: `GET /api/games/{gameId}/confirmations/me` com token JWT
- **Assertions**:
  - Status: 404

### Teste 13: Validação de Unicidade Transacional
- **Setup**: Criar jogo liberado, simular dois requests simultâneos com mesmo nome
- **Request**: Dois `POST /api/games/{gameId}/confirmations` simultâneos com mesmo nome
- **Assertions**:
  - Apenas uma confirmação deve ser criada
  - A segunda deve retornar 409

### Teste 14: Histórico Permanente
- **Setup**: Criar jogo semana 1 com confirmações, criar jogo semana 2
- **Request**: Confirmar nomes para semana 2
- **Assertions**:
  - Confirmações da semana 1 permanecem no banco
  - Confirmações da semana 2 são registros separados

---

## 9. 🚀 Ordem de Implementação Sugerida

1. **Criar migrations**
   - `V{N}__create_games_table.sql`
   - `V{N+1}__create_game_confirmations_table.sql`

2. **Criar camada Domain**
   - `Game.java` (entidade)
   - `GameConfirmation.java` (entidade)

3. **Criar camada Infrastructure**
   - `GameRepository.java`
   - `GameConfirmationRepository.java`

4. **Criar camada Application**
   - `GameService.java` (criar, liberar, validar)
   - `GameConfirmationService.java` (confirmar, listar, validar regras)

5. **Criar DTOs**
   - Request: `CreateGameRequest`, `ReleaseGameRequest`, `ConfirmNameRequest`
   - Response: `GameResponse`, `GameConfirmationResponse`, `GameConfirmationListResponse`

6. **Criar camada Interface**
   - `GameResource.java` (endpoints de jogo)
   - `GameConfirmationResource.java` (endpoints de confirmação)

7. **Implementar testes unitários**
   - `GameServiceTest.java`
   - `GameConfirmationServiceTest.java`

8. **Implementar testes de integração**
   - `GameResourceIT.java`
   - `GameConfirmationResourceIT.java`

9. **Validar todos os critérios de aceite**

10. **Documentar endpoints no OpenAPI**

---

## 10. 📝 Observações Finais

### Validações Importantes

1. **Verificação de Data/Hora do Jogo:**
   - Sempre comparar `gameDate` com `OffsetDateTime.now()` antes de permitir confirmação
   - Usar timezone correto (OffsetDateTime)

2. **Transações:**
   - Usar `@Transactional` na camada Application para garantir atomicidade
   - Constraint UNIQUE no banco como segunda camada de proteção

3. **Mensagens de Erro:**
   - Todas em português
   - Claras e objetivas
   - Incluir informações úteis para o usuário

4. **Performance:**
   - Índices criados nas migrations para otimizar consultas
   - Usar queries eficientes no repository

5. **Segurança:**
   - Sempre validar permissões com `@RolesAllowed`
   - Validar autenticação em todos os endpoints
   - Não expor informações sensíveis

### Dependências Necessárias

Todas as dependências necessárias já estão instaladas no projeto. Não é necessário adicionar novas dependências.

### Compatibilidade

A implementação deve ser compatível com:
- Java 21
- Quarkus 3.30.6
- PostgreSQL 16+
- Estrutura existente do projeto

---

## 11. 🔍 Checklist Final

- [ ] Migrations criadas e testadas
- [ ] Entidades criadas com todos os campos
- [ ] Repositories implementados
- [ ] Services com todas as regras de negócio
- [ ] DTOs criados (Request e Response)
- [ ] Resources com todos os endpoints
- [ ] Validações de permissão implementadas
- [ ] Validação de unicidade de nome funcionando
- [ ] Bloqueio automático após início do jogo funcionando
- [ ] Histórico permanente garantido
- [ ] Testes unitários passando
- [ ] Testes de integração passando
- [ ] Documentação OpenAPI completa
- [ ] Todos os critérios de aceite validados
- [ ] Mensagens de erro em português
- [ ] Constraints do banco criadas corretamente

---

**Lembre-se:** Sempre seguir os padrões do projeto e garantir que todas as regras de negócio sejam implementadas corretamente. Em caso de dúvidas, consultar as regras em `.cursor/rules/`.

