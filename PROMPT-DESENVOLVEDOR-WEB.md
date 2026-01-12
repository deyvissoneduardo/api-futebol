# 📋 Prompt para Desenvolvedor Web - API Futebol

## ⚠️ REGRAS CRÍTICAS DE NEGÓCIO

### Perfil de Usuários na Web
**IMPORTANTE:** Na aplicação web, **TODOS os usuários são considerados do perfil JOGADOR**, independentemente do perfil real no sistema. Isso significa que:
- A aplicação web não precisa se preocupar com perfis ADMIN ou SUPER_ADMIN
- Todos os endpoints acessíveis pela web são para o perfil JOGADOR
- O sistema backend gerencia as permissões automaticamente baseado no token JWT

---

## 🔐 AUTENTICAÇÃO

### Token JWT
- **Formato:** Bearer Token
- **Header:** `Authorization: Bearer {token}`
- **Tempo de expiração:** 86400 segundos (24 horas)
- **Todos os endpoints protegidos requerem este header**

---

## 📡 ENDPOINTS DISPONÍVEIS PARA WEB

### 1. POST /api/auth/login
Realiza login e retorna token JWT.

**Requisição:**
```json
{
  "email": "usuario@email.com",
  "password": "senha123"
}
```

**Resposta de Sucesso (200):**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "João Silva",
    "email": "usuario@email.com",
    "profile": "JOGADOR"
  }
}
```

**Erros:**
- **400 Bad Request:** Dados inválidos (email ou senha em formato incorreto)
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "E-mail é obrigatório"
  }
  ```
- **401 Unauthorized:** Credenciais inválidas
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Credenciais inválidas"
  }
  ```

---

### 2. GET /api/users/me
Retorna os dados do usuário logado.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta de Sucesso (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "João Silva",
  "email": "usuario@email.com",
  "photo": "https://exemplo.com/foto.jpg",
  "profile": "JOGADOR",
  "active": true,
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T09:00:00Z"
}
```

**Erros:**
- **401 Unauthorized:** Token inválido ou ausente
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Acesso não autorizado"
  }
  ```

---

### 3. POST /api/users
Cria um novo usuário (registro/cadastro).

**Requisição:**
```json
{
  "fullName": "João Silva",
  "email": "joao@email.com",
  "password": "senha123",
  "photo": "https://exemplo.com/foto.jpg"
}
```

**Observações:**
- `fullName`: obrigatório, entre 3 e 255 caracteres
- `email`: obrigatório, formato de email válido, único no sistema
- `password`: obrigatório, entre 6 e 100 caracteres
- `photo`: opcional, URL da foto, máximo 500 caracteres
- `profile`: não enviar - será sempre JOGADOR no backend

**Resposta de Sucesso (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "photo": "https://exemplo.com/foto.jpg",
  "profile": "JOGADOR",
  "active": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Erros:**
- **400 Bad Request:** Dados inválidos
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Nome completo é obrigatório"
  }
  ```
- **400 Bad Request:** Email já em uso
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "E-mail já está em uso"
  }
  ```

---

### 4. PUT /api/users/:id
Atualiza os dados de um usuário.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**URL Parameters:**
- `id`: UUID do usuário (deve ser o mesmo do usuário logado)

**Requisição:**
```json
{
  "fullName": "João Silva Atualizado",
  "email": "joao.novo@email.com",
  "password": "novaSenha123",
  "photo": "https://exemplo.com/nova-foto.jpg"
}
```

**Observações:**
- Todos os campos são opcionais (atualização parcial)
- Apenas os campos enviados serão atualizados
- `fullName`: entre 3 e 255 caracteres (se fornecido)
- `email`: formato de email válido, único no sistema (se fornecido)
- `password`: entre 6 e 100 caracteres (se fornecido)
- `photo`: URL da foto, máximo 500 caracteres (se fornecido)

**Resposta de Sucesso (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "João Silva Atualizado",
  "email": "joao.novo@email.com",
  "photo": "https://exemplo.com/nova-foto.jpg",
  "profile": "JOGADOR",
  "active": true,
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T10:35:00Z"
}
```

**Erros:**
- **400 Bad Request:** Dados inválidos
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Nome deve ter entre 3 e 255 caracteres"
  }
  ```
- **400 Bad Request:** Email já em uso
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "E-mail já está em uso"
  }
  ```
- **401 Unauthorized:** Token inválido ou ausente
- **403 Forbidden:** Usuário não tem permissão (tentando atualizar outro usuário)
- **404 Not Found:** Usuário não encontrado
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Usuário não encontrado com id: 550e8400-e29b-41d4-a716-446655440000"
  }
  ```

---

### 5. GET /api/users/me/statistics
Retorna as estatísticas do usuário logado.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta de Sucesso (200):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "minutesPlayed": "15:30:45",
  "goals": 10,
  "complaints": 2,
  "victories": 5,
  "draws": 1,
  "defeats": 3,
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T09:00:00Z"
}
```

**Observações:**
- `minutesPlayed`: formato "HH:mm:ss" (horas:minutos:segundos)
- Valores numéricos podem ser 0 ou maiores
- Se o usuário não tiver estatísticas, será retornado erro 400

**Erros:**
- **400 Bad Request:** Usuário SUPER_ADMIN não possui estatísticas (não aplicável para web)
- **401 Unauthorized:** Token inválido ou ausente
- **404 Not Found:** Estatísticas não encontradas
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Estatísticas não encontradas para o usuário"
  }
  ```

---

### 6. GET /api/ranking/goals
Retorna ranking de gols ordenado do maior para o menor.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta de Sucesso (200):**
```json
{
  "type": "goals",
  "description": "Ranking de Gols",
  "items": [
    {
      "position": 1,
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "João Silva",
      "userEmail": "joao@email.com",
      "value": 15,
      "formattedValue": "15"
    },
    {
      "position": 2,
      "userId": "660e8400-e29b-41d4-a716-446655440001",
      "userName": "Maria Santos",
      "userEmail": "maria@email.com",
      "value": 12,
      "formattedValue": "12"
    }
  ],
  "total": 2
}
```

**Erros:**
- **401 Unauthorized:** Token inválido ou ausente
- **403 Forbidden:** Acesso negado

---

### 7. GET /api/ranking/complaints
Retorna ranking de reclamações ordenado do maior para o menor.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta de Sucesso (200):**
```json
{
  "type": "complaints",
  "description": "Ranking de Reclamações",
  "items": [
    {
      "position": 1,
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "João Silva",
      "userEmail": "joao@email.com",
      "value": 5,
      "formattedValue": "5"
    }
  ],
  "total": 1
}
```

---

### 8. GET /api/ranking/victories
Retorna ranking de vitórias ordenado do maior para o menor.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta:** Mesmo formato do ranking de gols, com `type: "victories"` e `description: "Ranking de Vitórias"`

---

### 9. GET /api/ranking/draws
Retorna ranking de empates ordenado do maior para o menor.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta:** Mesmo formato do ranking de gols, com `type: "draws"` e `description: "Ranking de Empates"`

---

### 10. GET /api/ranking/defeats
Retorna ranking de derrotas ordenado do maior para o menor.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta:** Mesmo formato do ranking de gols, com `type: "defeats"` e `description: "Ranking de Derrotas"`

---

### 11. GET /api/ranking/minutes-played
Retorna ranking de minutos jogados ordenado do maior para o menor.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta de Sucesso (200):**
```json
{
  "type": "minutes-played",
  "description": "Ranking de Minutos Jogados",
  "items": [
    {
      "position": 1,
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "João Silva",
      "userEmail": "joao@email.com",
      "value": 55845,
      "formattedValue": "15:30:45"
    }
  ],
  "total": 1
}
```

**Observações:**
- `value`: minutos totais em segundos (número inteiro)
- `formattedValue`: formato "HH:mm:ss" para exibição

---

### 12. GET /api/games
Lista o único jogo com `released = true` (o jogo ativo disponível para confirmações).

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Resposta de Sucesso (200):**
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "gameDate": "2024-01-20T19:00:00Z",
    "released": true,
    "createdAt": "2024-01-15T10:00:00Z",
    "updatedAt": "2024-01-15T10:00:00Z"
  }
]
```

**Observações:**
- **IMPORTANTE:** O sistema garante que existe apenas **um único jogo** com `released = true` por vez
- A listagem retorna apenas o jogo com `released = true` (o jogo ativo)
- Se não houver nenhum jogo com `released = true`, retorna array vazio `[]`
- `released: true` = lista está liberada (permite confirmações)
- `released: false` = lista está bloqueada (não permite novas confirmações)
- **Regra de Negócio:** Quando um novo jogo é criado, se já existir outro jogo com `released = true`, o sistema automaticamente altera o(s) jogo(s) anterior(es) para `released = false`, garantindo que apenas o novo jogo fique com `released = true`

**Erros:**
- **401 Unauthorized:** Token inválido ou ausente

---

### 13. POST /api/games/:gameId/confirmations
Confirma um nome para um jogo.

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**URL Parameters:**
- `gameId`: UUID do jogo

**Requisição:**
```json
{
  "confirmedName": "João Silva",
  "isGuest": false
}
```

**Observações:**
- `confirmedName`: obrigatório, nome a ser confirmado (máximo 255 caracteres)
- `isGuest`: opcional, padrão `false`
  - `false`: confirmação para o próprio usuário logado
  - `true`: confirmação para um convidado (gera UUID único para o convidado)

**Resposta de Sucesso (201):**
```json
{
  "id": "990e8400-e29b-41d4-a716-446655440004",
  "gameId": "770e8400-e29b-41d4-a716-446655440002",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "confirmedName": "João Silva",
  "isGuest": false,
  "confirmedByUserId": null,
  "confirmedAt": "2024-01-15T10:40:00Z",
  "createdAt": "2024-01-15T10:40:00Z",
  "updatedAt": "2024-01-15T10:40:00Z"
}
```

**Observações:**
- Se `isGuest: true`, o `userId` será um UUID único gerado para o convidado
- `confirmedByUserId` será preenchido quando for convidado (ID do usuário que confirmou)
- O mesmo usuário pode confirmar múltiplos nomes (útil para convidados), desde que sejam nomes diferentes
- Um nome não pode ser confirmado duas vezes no mesmo jogo

**Erros:**
- **400 Bad Request:** Dados inválidos
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Nome confirmado é obrigatório"
  }
  ```
- **403 Forbidden:** Lista não está liberada (released = false)
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Lista não está liberada"
  }
  ```
- **404 Not Found:** Jogo não encontrado
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Jogo não encontrado com id: 770e8400-e29b-41d4-a716-446655440002"
  }
  ```
- **409 Conflict:** Nome já confirmado para este jogo
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 409,
    "error": "Conflict",
    "message": "Nome já confirmado para este jogo. Escolha outro nome."
  }
  ```

---

### 14. GET /api/games/:gameId/confirmations/me
Retorna todas as confirmações do usuário logado para um jogo específico (inclui confirmações próprias e de convidados confirmados por ele).

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**URL Parameters:**
- `gameId`: UUID do jogo

**Resposta de Sucesso (200):**
```json
[
  {
    "id": "990e8400-e29b-41d4-a716-446655440004",
    "gameId": "770e8400-e29b-41d4-a716-446655440002",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "confirmedName": "João Silva",
    "isGuest": false,
    "confirmedByUserId": null,
    "confirmedAt": "2024-01-15T10:40:00Z",
    "createdAt": "2024-01-15T10:40:00Z",
    "updatedAt": "2024-01-15T10:40:00Z"
  },
  {
    "id": "aa0e8400-e29b-41d4-a716-446655440005",
    "gameId": "770e8400-e29b-41d4-a716-446655440002",
    "userId": "bb0e8400-e29b-41d4-a716-446655440006",
    "confirmedName": "Pedro Convidado",
    "isGuest": true,
    "confirmedByUserId": "550e8400-e29b-41d4-a716-446655440000",
    "confirmedAt": "2024-01-15T10:45:00Z",
    "createdAt": "2024-01-15T10:45:00Z",
    "updatedAt": "2024-01-15T10:45:00Z"
  }
]
```

**Observações:**
- Retorna confirmações próprias (userId = ID do usuário logado)
- Retorna confirmações de convidados confirmados por este usuário (confirmedByUserId = ID do usuário logado)
- Se não houver confirmações, retorna array vazio `[]`

**Erros:**
- **401 Unauthorized:** Token inválido ou ausente
- **404 Not Found:** Jogo não encontrado
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Jogo não encontrado com id: 770e8400-e29b-41d4-a716-446655440002"
  }
  ```

---

## 🔍 SOLUÇÃO: COMO IDENTIFICAR O JOGO CORRETO PARA CONFIRMAÇÃO

### Problema
Cada jogo possui um UUID único, e esse valor muda para cada novo jogo. Como o desenvolvedor web pode identificar qual jogo está ativo/disponível para confirmações?

### Solução Simplificada

**IMPORTANTE:** O sistema garante que existe apenas **um único jogo** com `released = true` por vez. Isso simplifica muito a identificação do jogo ativo!

1. **Listar o jogo ativo** usando `GET /api/games`
2. **O endpoint retorna automaticamente** apenas o jogo com `released = true` (se existir)
3. **Não é necessário filtrar** - o backend já faz isso para você

### Exemplo de Implementação

```javascript
// Código simplificado para identificar o jogo ativo
async function getActiveGame() {
  const games = await fetch('/api/games', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  // O backend já retorna apenas o jogo com released = true
  // Se não houver jogo ativo, retorna array vazio []
  if (games.length === 0) {
    return null; // Nenhum jogo disponível para confirmações
  }
  
  // Retornar o primeiro (e único) jogo da lista
  return games[0];
}

// Usar o gameId retornado para confirmar
const activeGame = await getActiveGame();
if (activeGame) {
  const gameId = activeGame.id;
  // Usar gameId na confirmação: POST /api/games/${gameId}/confirmations
}
```

### Regra de Negócio: Um Único Jogo Ativo

**Como funciona:**
- Apenas **um jogo** pode ter `released = true` por vez
- Quando um novo jogo é criado (via POST /api/games - apenas ADMIN/SUPER_ADMIN):
  - Se já existir outro jogo com `released = true`, o sistema **automaticamente** altera o(s) jogo(s) anterior(es) para `released = false`
  - O novo jogo é criado com `released = true` e se torna o único jogo ativo
  - A resposta da criação pode incluir uma mensagem informativa (`message`) quando outros games foram alterados

**Benefícios para o desenvolvedor web:**
- ✅ Não precisa filtrar ou ordenar - o backend já retorna apenas o jogo ativo
- ✅ Não precisa lidar com múltiplos jogos ativos - isso é impossível
- ✅ Código mais simples e direto
- ✅ Menos chamadas à API necessárias

### Validações Adicionais
- **Sempre validar** que o jogo existe e está liberado antes de permitir confirmação
- Se a confirmação retornar erro 403 (Lista não está liberada), atualizar a lista de jogos
- **Armazenar o gameId** localmente (sessionStorage/localStorage) para evitar múltiplas chamadas à API, mas sempre validar antes de usar

### Fluxo Completo Recomendado
1. Usuário acessa a tela de confirmações
2. Buscar lista de jogos (`GET /api/games`) - retorna apenas o jogo ativo (se existir)
3. Se houver jogo ativo, exibir informações do jogo (data, horário)
4. Permitir confirmação de nome
5. Ao confirmar, usar o `gameId` do jogo retornado
6. Se retornar erro 403 ou 404, atualizar lista e tentar novamente

---

## 📋 MODELOS E ESTRUTURAS DE DADOS

### UserProfile (Enum)
Valores possíveis:
- `JOGADOR`
- `ADMIN`
- `SUPER_ADMIN`

**Na web, sempre considerar como JOGADOR.**

### Formatos de Data/Hora
- **ISO 8601 com timezone:** `2024-01-15T10:30:00Z`
- **Data do jogo:** `yyyy-MM-dd` (ex: `2024-01-20`)
- **Hora do jogo:** `HH:mm` (ex: `19:00`)
- **Minutos jogados:** `HH:mm:ss` (ex: `15:30:45`)

### UUID
- Formato: `550e8400-e29b-41d4-a716-446655440000`
- Sempre usar UUIDs válidos nas requisições

---

## ⚠️ TRATAMENTO DE ERROS

### Padrão de Resposta de Erro
Todas as respostas de erro seguem este formato:
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Mensagem descritiva do erro"
}
```

### Códigos HTTP Mais Comuns
- **200 OK:** Sucesso
- **201 Created:** Recurso criado com sucesso
- **400 Bad Request:** Dados inválidos ou erro de validação
- **401 Unauthorized:** Token inválido, ausente ou expirado
- **403 Forbidden:** Acesso negado (sem permissão)
- **404 Not Found:** Recurso não encontrado
- **409 Conflict:** Conflito (ex: email já em uso, nome já confirmado)

### Ações Recomendadas por Código
- **401:** Redirecionar para tela de login
- **403:** Exibir mensagem de acesso negado, não permitir ação
- **404:** Exibir mensagem de "recurso não encontrado"
- **409:** Exibir mensagem específica do conflito, permitir correção
- **400:** Exibir mensagens de validação, permitir correção

---

## 🔄 FLUXOS COMUNS

### Fluxo de Login e Autenticação
1. Usuário preenche email e senha
2. POST /api/auth/login
3. Salvar token no localStorage/sessionStorage
4. Incluir token no header Authorization de todas as requisições subsequentes
5. Se token expirar (401), redirecionar para login

### Fluxo de Registro
1. Usuário preenche dados (nome, email, senha, foto opcional)
2. POST /api/users
3. Se sucesso, redirecionar para login ou fazer login automático
4. Se erro 400 (email já em uso), exibir mensagem e permitir correção

### Fluxo de Confirmação de Nome
1. Usuário acessa tela de confirmações
2. GET /api/games (identificar jogo ativo)
3. GET /api/games/:gameId/confirmations/me (verificar confirmações existentes)
4. Usuário preenche nome e confirma
5. POST /api/games/:gameId/confirmations
6. Se sucesso, atualizar lista de confirmações
7. Se erro 403, informar que lista foi bloqueada
8. Se erro 409, informar que nome já está confirmado

### Fluxo de Visualização de Estatísticas
1. Usuário acessa tela de estatísticas
2. GET /api/users/me/statistics
3. Exibir dados formatados;
4. Se erro 404, exibir mensagem "Estatísticas não disponíveis"

### Fluxo de Visualização de Ranking
1. Usuário acessa tela de ranking
2. GET /api/ranking/{tipo} (goals, victories, complaints, etc.)
3. Exibir lista ordenada
4. Destacar posição do usuário logado (comparar userId)

---

## 📝 NOTAS IMPORTANTES

1. **Sempre incluir header Authorization** em requisições protegidas
2. **Validar formato de UUID** antes de enviar nas URLs
3. **Tratar erros adequadamente** com mensagens amigáveis ao usuário
4. **Cachear dados quando apropriado** para melhor performance (token, dados do usuário)
5. **Atualizar tokens** quando expirarem (redirecionar para login)
6. **Validar dados antes de enviar** para melhor UX (formato de email, tamanho de campos, etc.)
7. **Considerar timezone** ao exibir datas (backend retorna em UTC)
8. **Formatar valores** adequadamente (minutos como HH:mm:ss, valores numéricos)

---

## 🔗 BASE URL

A base URL da API deve ser configurável (desenvolvimento, homologação, produção). Exemplos:
- Desenvolvimento: `http://localhost:8080`
- Homologação: `https://api-hml.futebol.com`
- Produção: `https://api.futebol.com`

Todos os endpoints devem ser acessados com a base URL + o caminho do endpoint (ex: `{baseUrl}/api/auth/login`).

