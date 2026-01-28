# 📋 Prompt para Desenvolvedor - API Futebol - Perfil ADMIN

## ⚠️ REGRAS CRÍTICAS DE NEGÓCIO

### Perfil ADMIN
**IMPORTANTE:** O perfil ADMIN possui permissões especiais para gerenciar jogos e estatísticas:
- Apenas ADMIN e SUPER_ADMIN podem criar partidas
- Apenas ADMIN e SUPER_ADMIN podem visualizar a lista completa de confirmações
- Apenas ADMIN e SUPER_ADMIN podem atualizar estatísticas de jogadores
- Apenas ADMIN e SUPER_ADMIN podem fechar a lista de confirmação de um jogo

---

## 🔐 AUTENTICAÇÃO

### Token JWT
- **Formato:** Bearer Token
- **Header:** `Authorization: Bearer {token}`
- **Tempo de expiração:** 86400 segundos (24 horas)
- **Todos os endpoints protegidos requerem este header**
- **O token deve conter o perfil ADMIN ou SUPER_ADMIN no claim `role` ou `groups`**

---

## 📡 ENDPOINTS DISPONÍVEIS PARA ADMIN

### 1. POST /api/games
Cria uma nova partida. Quando uma nova partida é criada, ela automaticamente recebe `released = true`, permitindo que jogadores confirmem seus nomes. Se já existir outra partida com `released = true`, ela será automaticamente alterada para `released = false`.

**Permissões:** ADMIN, SUPER_ADMIN

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)
- `Content-Type: application/json` (obrigatório)

**Request Body:**
```json
{
  "startDate": "2024-01-20",
  "startHour": "19:00"
}
```

**Campos do Request:**
- `startDate` (String, obrigatório): Data do jogo no formato `yyyy-MM-dd` (ex: "2024-01-20")
- `startHour` (String, obrigatório): Hora do jogo no formato `HH:mm` (ex: "19:00")

**Validações:**
- `startDate` deve estar no formato `yyyy-MM-dd`
- `startHour` deve estar no formato `HH:mm`
- Ambos os campos são obrigatórios

**Resposta de Sucesso (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "gameDate": "2024-01-20T19:00:00Z",
  "released": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "message": null
}
```

**Resposta de Sucesso com Mensagem Informativa (201 Created):**
Quando já existe uma partida com `released = true`, ela é automaticamente alterada e uma mensagem é retornada:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "gameDate": "2024-01-20T19:00:00Z",
  "released": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "message": "O gameId 550e8400-e29b-41d4-a716-446655440000 foi alterado para released = false. O novo game é o único com released = true."
}
```

**Campos da Resposta:**
- `id` (UUID): Identificador único da partida
- `gameDate` (OffsetDateTime): Data e hora do jogo em formato ISO 8601 com timezone
- `released` (Boolean): Indica se a lista está liberada para confirmações (sempre `true` ao criar)
- `createdAt` (OffsetDateTime): Data e hora de criação
- `updatedAt` (OffsetDateTime): Data e hora da última atualização
- `message` (String, opcional): Mensagem informativa quando outras partidas foram alteradas

**Erros:**

- **400 Bad Request:** Dados inválidos
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Data de início é obrigatória"
  }
  ```
  Ou:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Data ou hora inválida: Text '2024-01-32' could not be parsed"
  }
  ```

- **401 Unauthorized:** Token ausente ou inválido
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Acesso não autorizado"
  }
  ```

- **403 Forbidden:** Usuário não possui permissão (não é ADMIN ou SUPER_ADMIN)
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Apenas ADMIN ou SUPER_ADMIN podem criar jogos"
  }
  ```

---

### 2. GET /api/games/{gameId}/confirmations
Retorna a lista completa de nomes confirmados para uma partida específica. Inclui informações sobre todos os jogadores confirmados, incluindo convidados.

**Permissões:** ADMIN, SUPER_ADMIN

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Parâmetros de URL:**
- `gameId` (UUID, obrigatório): Identificador único da partida

**Exemplo de URL:**
```
GET /api/games/550e8400-e29b-41d4-a716-446655440000/confirmations
```

**Resposta de Sucesso (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "confirmations": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440000",
      "gameId": "550e8400-e29b-41d4-a716-446655440000",
      "userId": "770e8400-e29b-41d4-a716-446655440000",
      "confirmedName": "João Silva",
      "isGuest": false,
      "confirmedByUserId": null,
      "confirmedAt": "2024-01-15T10:30:00Z",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "880e8400-e29b-41d4-a716-446655440000",
      "gameId": "550e8400-e29b-41d4-a716-446655440000",
      "userId": "990e8400-e29b-41d4-a716-446655440000",
      "confirmedName": "Pedro Convidado",
      "isGuest": true,
      "confirmedByUserId": "770e8400-e29b-41d4-a716-446655440000",
      "confirmedAt": "2024-01-15T10:35:00Z",
      "createdAt": "2024-01-15T10:35:00Z",
      "updatedAt": "2024-01-15T10:35:00Z"
    }
  ],
  "total": 2
}
```

**Campos da Resposta:**
- `gameId` (UUID): Identificador único da partida
- `confirmations` (Array): Lista de confirmações
  - `id` (UUID): Identificador único da confirmação
  - `gameId` (UUID): Identificador da partida
  - `userId` (UUID): Identificador do usuário (pode ser UUID de convidado se `isGuest = true`)
  - `confirmedName` (String): Nome confirmado pelo jogador
  - `isGuest` (Boolean): Indica se é um convidado
  - `confirmedByUserId` (UUID, opcional): ID do usuário que confirmou o convidado (apenas se `isGuest = true`)
  - `confirmedAt` (OffsetDateTime): Data e hora da confirmação
  - `createdAt` (OffsetDateTime): Data e hora de criação
  - `updatedAt` (OffsetDateTime): Data e hora da última atualização
- `total` (Integer): Número total de confirmações

**Resposta quando não há confirmações (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "confirmations": [],
  "total": 0
}
```

**Erros:**

- **401 Unauthorized:** Token ausente ou inválido
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Acesso não autorizado"
  }
  ```

- **403 Forbidden:** Usuário não possui permissão (não é ADMIN ou SUPER_ADMIN)
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Apenas ADMIN ou SUPER_ADMIN podem consultar a lista completa de confirmações"
  }
  ```

- **404 Not Found:** Partida não encontrada
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Jogo não encontrado com id: 550e8400-e29b-41d4-a716-446655440000"
  }
  ```

---

### 3. PUT /api/games/{gameId}/statistics/bulk-update
Atualiza as estatísticas de todos os jogadores confirmados em uma partida específica. Este endpoint permite atualizar múltiplas estatísticas de uma vez para todos os jogadores que confirmaram presença no jogo.

**Permissões:** ADMIN, SUPER_ADMIN

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)
- `Content-Type: application/json` (obrigatório)

**Parâmetros de URL:**
- `gameId` (UUID, obrigatório): Identificador único da partida

**Request Body:**
```json
{
  "statistics": [
    {
      "userId": "770e8400-e29b-41d4-a716-446655440000",
      "minutesPlayed": "1:30:00",
      "goals": 2,
      "complaints": 0,
      "victories": 1,
      "draws": 0,
      "defeats": 0
    },
    {
      "userId": "990e8400-e29b-41d4-a716-446655440000",
      "minutesPlayed": "0:45:00",
      "goals": 1,
      "complaints": 0,
      "victories": 1,
      "draws": 0,
      "defeats": 0
    }
  ]
}
```

**Campos do Request:**
- `statistics` (Array, obrigatório): Lista de estatísticas a serem atualizadas
  - `userId` (UUID, obrigatório): Identificador do usuário (pode ser de convidado)
  - `minutesPlayed` (String, opcional): Minutos jogados no formato `HH:mm:ss` (ex: "1:30:00" para 1 hora e 30 minutos)
  - `goals` (Integer, opcional): Número de gols (não pode ser negativo)
  - `complaints` (Integer, opcional): Número de reclamações (não pode ser negativo)
  - `victories` (Integer, opcional): Número de vitórias (não pode ser negativo)
  - `draws` (Integer, opcional): Número de empates (não pode ser negativo)
  - `defeats` (Integer, opcional): Número de derrotas (não pode ser negativo)

**Validações:**
- `userId` é obrigatório em cada item do array
- `minutesPlayed` deve estar no formato `HH:mm:ss` se fornecido
- Todos os valores numéricos devem ser não negativos (>= 0)
- Todos os `userId` devem corresponder a jogadores confirmados na partida

**Resposta de Sucesso (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "updatedCount": 2,
  "statistics": [
    {
      "id": "aa0e8400-e29b-41d4-a716-446655440000",
      "userId": "770e8400-e29b-41d4-a716-446655440000",
      "minutesPlayed": "1:30:00",
      "goals": 2,
      "complaints": 0,
      "victories": 1,
      "draws": 0,
      "defeats": 0,
      "createdAt": "2024-01-10T10:00:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "bb0e8400-e29b-41d4-a716-446655440000",
      "userId": "990e8400-e29b-41d4-a716-446655440000",
      "minutesPlayed": "0:45:00",
      "goals": 1,
      "complaints": 0,
      "victories": 1,
      "draws": 0,
      "defeats": 0,
      "createdAt": "2024-01-10T10:00:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

**Campos da Resposta:**
- `gameId` (UUID): Identificador único da partida
- `updatedCount` (Integer): Número de estatísticas atualizadas com sucesso
- `statistics` (Array): Lista de estatísticas atualizadas
  - `id` (UUID): Identificador único da estatística
  - `userId` (UUID): Identificador do usuário
  - `minutesPlayed` (String): Minutos jogados no formato `HH:mm:ss`
  - `goals` (Integer): Número de gols
  - `complaints` (Integer): Número de reclamações
  - `victories` (Integer): Número de vitórias
  - `draws` (Integer): Número de empates
  - `defeats` (Integer): Número de derrotas
  - `createdAt` (OffsetDateTime): Data e hora de criação
  - `updatedAt` (OffsetDateTime): Data e hora da última atualização

**Erros:**

- **400 Bad Request:** Dados inválidos
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Formato de minutos inválido. Use HH:mm:ss"
  }
  ```
  Ou:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Gols não pode ser negativo"
  }
  ```
  Ou:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Usuário 770e8400-e29b-41d4-a716-446655440000 não está confirmado nesta partida"
  }
  ```

- **401 Unauthorized:** Token ausente ou inválido
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Acesso não autorizado"
  }
  ```

- **403 Forbidden:** Usuário não possui permissão (não é ADMIN ou SUPER_ADMIN)
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Apenas ADMIN ou SUPER_ADMIN podem atualizar estatísticas"
  }
  ```

- **404 Not Found:** Partida não encontrada
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Jogo não encontrado com id: 550e8400-e29b-41d4-a716-446655440000"
  }
  ```

---

### 4. PUT /api/games/{gameId}/release
Fecha a lista de confirmação de uma partida, bloqueando novas confirmações. Quando a lista é fechada, o campo `released` é alterado para `false`, impedindo que novos jogadores confirmem presença.

**Permissões:** ADMIN, SUPER_ADMIN

**Headers:**
- `Authorization: Bearer {token}` (obrigatório)

**Parâmetros de URL:**
- `gameId` (UUID, obrigatório): Identificador único da partida

**Exemplo de URL:**
```
PUT /api/games/550e8400-e29b-41d4-a716-446655440000/release
```

**Request Body:**
Nenhum (endpoint não requer body)

**Resposta de Sucesso (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "gameDate": "2024-01-20T19:00:00Z",
  "released": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:40:00Z"
}
```

**Campos da Resposta:**
- `id` (UUID): Identificador único da partida
- `gameDate` (OffsetDateTime): Data e hora do jogo em formato ISO 8601 com timezone
- `released` (Boolean): Indica se a lista está liberada para confirmações (sempre `false` após fechar)
- `createdAt` (OffsetDateTime): Data e hora de criação
- `updatedAt` (OffsetDateTime): Data e hora da última atualização

**Erros:**

- **401 Unauthorized:** Token ausente ou inválido
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Acesso não autorizado"
  }
  ```

- **403 Forbidden:** Usuário não possui permissão (não é ADMIN ou SUPER_ADMIN)
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Apenas ADMIN ou SUPER_ADMIN podem iniciar jogos"
  }
  ```

- **404 Not Found:** Partida não encontrada
  ```json
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Jogo não encontrado com id: 550e8400-e29b-41d4-a716-446655440000"
  }
  ```

---

## 📝 NOTAS IMPORTANTES

### Sobre Estatísticas
- As estatísticas são criadas automaticamente quando um usuário é atualizado pela primeira vez
- O campo `minutesPlayed` aceita valores no formato `HH:mm:ss` (ex: "1:30:00" para 1 hora e 30 minutos)
- Valores negativos não são permitidos para campos numéricos (goals, complaints, victories, draws, defeats)
- Se um valor negativo for fornecido para `minutesPlayed`, ele será tratado como subtração, mas o resultado final nunca será negativo (será ajustado para zero)

### Sobre Confirmações
- Um mesmo usuário pode confirmar múltiplos nomes (útil para casos de convidados)
- Nomes confirmados devem ser únicos por partida
- Convidados recebem um UUID único, permitindo estatísticas separadas
- Apenas jogadores confirmados podem ter suas estatísticas atualizadas via bulk-update

### Sobre Partidas
- Apenas uma partida pode ter `released = true` por vez
- Quando uma nova partida é criada, outras partidas com `released = true` são automaticamente alteradas para `false`
- Quando a lista é fechada (`released = false`), nenhum jogador pode mais confirmar presença

### Sobre Validações
- Todos os UUIDs devem estar no formato válido (ex: "550e8400-e29b-41d4-a716-446655440000")
- Datas devem estar no formato `yyyy-MM-dd`
- Horas devem estar no formato `HH:mm`
- Minutos jogados devem estar no formato `HH:mm:ss`

---

## 🔄 FLUXO DE TRABALHO TÍPICO

1. **Criar Partida:** ADMIN cria uma nova partida via `POST /api/games`
   - A partida é criada com `released = true`, permitindo confirmações

2. **Aguardar Confirmações:** Jogadores confirmam presença na partida

3. **Visualizar Confirmações:** ADMIN visualiza lista de confirmados via `GET /api/games/{gameId}/confirmations`

4. **Fechar Lista:** ADMIN fecha a lista via `PUT /api/games/{gameId}/release`
   - A partida recebe `released = false`, bloqueando novas confirmações

5. **Atualizar Estatísticas:** Após o jogo, ADMIN atualiza estatísticas de todos os confirmados via `PUT /api/games/{gameId}/statistics/bulk-update`

---

## 🧪 EXEMPLOS DE REQUISIÇÕES

### Exemplo 1: Criar Partida
```bash
curl -X POST "https://api.futebol.com/api/games" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-01-20",
    "startHour": "19:00"
  }'
```

### Exemplo 2: Listar Confirmações
```bash
curl -X GET "https://api.futebol.com/api/games/550e8400-e29b-41d4-a716-446655440000/confirmations" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Exemplo 3: Atualizar Estatísticas em Lote
```bash
curl -X PUT "https://api.futebol.com/api/games/550e8400-e29b-41d4-a716-446655440000/statistics/bulk-update" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "statistics": [
      {
        "userId": "770e8400-e29b-41d4-a716-446655440000",
        "minutesPlayed": "1:30:00",
        "goals": 2,
        "victories": 1
      },
      {
        "userId": "990e8400-e29b-41d4-a716-446655440000",
        "minutesPlayed": "0:45:00",
        "goals": 1,
        "victories": 1
      }
    ]
  }'
```

### Exemplo 4: Fechar Lista de Confirmação
```bash
curl -X PUT "https://api.futebol.com/api/games/550e8400-e29b-41d4-a716-446655440000/release" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## ⚠️ CÓDIGOS DE STATUS HTTP

| Código | Significado | Quando Ocorre |
|--------|-------------|---------------|
| 200 | OK | Operação realizada com sucesso |
| 201 | Created | Recurso criado com sucesso |
| 400 | Bad Request | Dados inválidos na requisição |
| 401 | Unauthorized | Token ausente ou inválido |
| 403 | Forbidden | Usuário não possui permissão |
| 404 | Not Found | Recurso não encontrado |
| 409 | Conflict | Conflito (ex: nome já confirmado) |
| 500 | Internal Server Error | Erro interno do servidor |

---

## 📚 REFERÊNCIAS

- **Base URL:** Verificar no arquivo `application.properties` ou configuração do ambiente
- **Formato de Data/Hora:** ISO 8601 com timezone (ex: "2024-01-20T19:00:00Z")
- **Formato de UUID:** RFC 4122 (ex: "550e8400-e29b-41d4-a716-446655440000")
- **Formato de Duração:** `HH:mm:ss` (ex: "1:30:00" para 1 hora e 30 minutos)

---

**Documento gerado em:** 2024-01-15  
**Versão da API:** 1.0.0

