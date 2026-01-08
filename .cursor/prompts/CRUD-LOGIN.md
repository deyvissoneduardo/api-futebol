# 🎯 Prompt: Implementação de CRUD de Usuário e Autenticação JWT

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
- quarkus-flyway (migrations)
- quarkus-hibernate-validator (validações)
- quarkus-smallrye-openapi (Swagger/OpenAPI) ✅
- quarkus-smallrye-fault-tolerance
- quarkus-hibernate-orm-panache (ORM)
- quarkus-smallrye-jwt (autenticação JWT) ✅
- quarkus-smallrye-jwt-build (gerar tokens JWT) ✅
- quarkus-jdbc-postgresql (conexão PostgreSQL) ✅
- quarkus-arc (CDI)
- quarkus-hibernate-orm
- quarkus-junit5 (testes) ✅
- rest-assured (testes de integração) ✅
```

**⚠️ Dependências Faltantes (PERGUNTE ANTES DE INSTALAR):**
```xml
<!-- Lombok - para @Data, @Builder, @Getter, @Setter conforme padrões -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>

<!-- Serialização JSON para REST -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jackson</artifactId>
</dependency>

<!-- BCrypt para hash de senhas -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-elytron-security-common</artifactId>
</dependency>

<!-- Mockito para testes unitários -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5-mockito</artifactId>
    <scope>test</scope>
</dependency>

<!-- Health checks -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
```

---

## 2. 🎯 Objetivo

**O que precisa ser entregue:**
1. CRUD completo de usuário (Create, Read, Update, Delete)
2. Rota de Health Check
3. Autenticação com login (e-mail + senha) retornando JWT
4. Configuração de rotas públicas (skip de autenticação)
5. Testes unitários e de integração para todas as rotas
6. Configuração do Swagger/OpenAPI
7. Ambientes de HML e PROD separados
8. Docker Compose para PostgreSQL + Admin (pgAdmin)

**Propósito da tarefa:**
- Base de autenticação e gestão de usuários para a API de futebol

**Resultado esperado:**
- Código funcional seguindo Clean Architecture
- Migrations do Flyway criadas
- Testes passando
- Documentação OpenAPI funcionando

---

## 3. ⚙️ Instruções Específicas

### 3.1 Entidade User

**Campos obrigatórios:**
- `id`: UUID (chave primária)
- `fullName`: String (nome completo, não nulo)
- `email`: String (único, não nulo, validado)
- `password`: String (hash BCrypt, não nulo)
- `photo`: String (URL da foto, opcional)
- `profile`: Enum (SUPER_ADMIN, ADMIN, JOGADOR)
- `active`: Boolean (default true)
- `createdAt`: OffsetDateTime
- `updatedAt`: OffsetDateTime

### 3.2 Estrutura de Arquivos a Criar

```
src/main/java/br/com/futebol/
├── config/
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── core/
│   ├── security/
│   │   ├── JwtService.java
│   │   ├── PasswordService.java
│   │   └── PublicRoutes.java
│   └── exceptions/
│       ├── BusinessException.java
│       ├── ResourceNotFoundException.java
│       ├── UnauthorizedException.java
│       └── GlobalExceptionHandler.java
├── domain/
│   └── user/
│       ├── User.java (Entidade)
│       └── UserProfile.java (Enum)
├── application/
│   └── user/
│       ├── UserService.java
│       └── AuthService.java
├── infrastructure/
│   └── user/
│       └── UserRepository.java
└── interfaces/
    ├── health/
    │   └── HealthResource.java
    ├── auth/
    │   ├── AuthResource.java
    │   ├── LoginRequest.java
    │   └── LoginResponse.java
    └── user/
        ├── UserResource.java
        ├── CreateUserRequest.java
        ├── UpdateUserRequest.java
        └── UserResponse.java

src/main/resources/
├── application.properties (base)
├── application-hml.properties
├── application-prod.properties
└── db/migration/
    └── V1__create_users_table.sql

src/test/java/br/com/futebol/
├── unit/
│   ├── user/
│   │   └── UserServiceTest.java
│   └── auth/
│       └── AuthServiceTest.java
└── integration/
    ├── user/
    │   └── UserResourceIT.java
    ├── auth/
    │   └── AuthResourceIT.java
    └── health/
        └── HealthResourceIT.java

docker-compose.yaml (na raiz do projeto)
```

### 3.3 Endpoints da API

| Método | Rota | Descrição | Auth | Roles |
|--------|------|-----------|------|-------|
| GET | `/q/health` | Health check | ❌ Público | - |
| GET | `/q/health/live` | Liveness | ❌ Público | - |
| GET | `/q/health/ready` | Readiness | ❌ Público | - |
| POST | `/api/auth/login` | Login | ❌ Público | - |
| GET | `/api/users` | Listar usuários | ✅ | ADMIN, SUPER_ADMIN |
| GET | `/api/users/{id}` | Buscar usuário | ✅ | ADMIN, SUPER_ADMIN |
| POST | `/api/users` | Criar usuário | ✅ | SUPER_ADMIN |
| PUT | `/api/users/{id}` | Atualizar usuário | ✅ | ADMIN, SUPER_ADMIN |
| DELETE | `/api/users/{id}` | Deletar usuário | ✅ | SUPER_ADMIN |
| GET | `/api/users/me` | Perfil logado | ✅ | Qualquer |

### 3.4 Configuração de Ambientes

**application.properties (base):**
```properties
# Configurações comuns
quarkus.application.name=api-futebol
quarkus.http.port=8080

# Flyway
quarkus.flyway.migrate-at-start=true

# JWT
mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem
mp.jwt.verify.issuer=https://futebol.com
smallrye.jwt.sign.key.location=META-INF/resources/privateKey.pem
smallrye.jwt.new-token.lifespan=86400

# OpenAPI
quarkus.smallrye-openapi.path=/q/openapi
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/q/swagger-ui
```

**application-hml.properties:**
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/futebol_hml
quarkus.datasource.username=futebol
quarkus.datasource.password=futebol123
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=true
```

**application-prod.properties:**
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=${DB_URL}
quarkus.datasource.username=${DB_USERNAME}
quarkus.datasource.password=${DB_PASSWORD}
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=false
quarkus.swagger-ui.always-include=false
```

### 3.5 Docker Compose

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    container_name: futebol-postgres
    environment:
      POSTGRES_USER: futebol
      POSTGRES_PASSWORD: futebol123
      POSTGRES_DB: futebol_hml
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - futebol-network

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: futebol-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@futebol.com
      PGADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "5050:80"
    depends_on:
      - postgres
    networks:
      - futebol-network

volumes:
  postgres_data:

networks:
  futebol-network:
    driver: bridge
```

---

## 4. ✓ Regras: DEVE / NÃO DEVE

### ✅ DEVE:

- **DEVE** seguir a estrutura de Clean Architecture definida em `.cursor/rules/01-architecture.mdc`
- **DEVE** seguir os padrões de código definidos em `.cursor/rules/02-coding-standards.mdc`
- **DEVE** usar Lombok (@Builder, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)
- **DEVE** usar BCrypt para hash de senhas
- **DEVE** documentar todos os endpoints com OpenAPI (@Tag, @Operation, @APIResponses)
- **DEVE** usar DTOs para Request e Response (nunca expor entidades diretamente)
- **DEVE** validar inputs com Bean Validation (@NotBlank, @Email, @Size, etc)
- **DEVE** usar UUID para chaves primárias
- **DEVE** criar migrations Flyway para o banco
- **DEVE** implementar tratamento de exceções global
- **DEVE** criar testes unitários para Services
- **DEVE** criar testes de integração para Resources
- **DEVE** usar o padrão AAA (Arrange, Act, Assert) nos testes
- **DEVE** usar @DisplayName em português nos testes
- **DEVE** gerar chaves RSA para JWT (privateKey.pem e publicKey.pem)
- **DEVE** configurar rotas públicas sem necessidade de token
- **DEVE** usar perfis (profiles) para separar ambientes
- **DEVE** PERGUNTAR antes de instalar novas dependências

### ❌ NÃO DEVE:

- **NÃO DEVE** expor informações sensíveis em logs (senhas, tokens)
- **NÃO DEVE** armazenar senhas em texto plano
- **NÃO DEVE** expor entidades JPA diretamente nos endpoints
- **NÃO DEVE** hardcodar credenciais em ambiente de produção
- **NÃO DEVE** ignorar erros silenciosamente
- **NÃO DEVE** criar código duplicado (DRY)
- **NÃO DEVE** modificar arquivos de migration já existentes
- **NÃO DEVE** pular validações de segurança
- **NÃO DEVE** instalar dependências sem perguntar primeiro
- **NÃO DEVE** remover código ou arquivos existentes sem perguntar
- **NÃO DEVE** criar estrutura fora do padrão Clean Architecture definido

### ⚠️ ATENÇÃO ESPECIAL:

- Ao manipular senhas, SEMPRE usar BCrypt
- Ao fazer queries SQL, SEMPRE usar prepared statements/named parameters
- Ao lidar com datas, SEMPRE usar OffsetDateTime com timezone
- Ao criar testes, SEMPRE mockar dependências externas
- SEMPRE verificar permissões (roles) nos endpoints protegidos

---

## 5. 📋 Formato da Resposta

**Estrutura desejada:**
1. Primeiro, listar as dependências que precisam ser adicionadas e aguardar confirmação
2. Criar arquivos na ordem: migrations → entidades → repositories → services → resources → testes
3. Código completo com comentários explicativos quando necessário
4. Comandos para gerar chaves RSA

**Limites:**
- Criar um arquivo por vez para facilitar revisão
- Explicar brevemente cada arquivo criado

**Estilo:**
- Responder sempre em português
- Usar markdown para formatação
- Código Java seguindo padrões definidos nas rules

---

## 6. 👤 Persona / Tom

**Perspectiva:**
- Desenvolvedor backend sênior especializado em Java/Quarkus

**Tom da explicação:**
- Técnico e objetivo
- Consultivo quando houver decisões de arquitetura

**Nível de profundidade:**
- Código completo e funcional
- Explicações apenas quando necessário para decisões importantes

---

## 7. ✅ Critérios de Aceite

- [ ] Dependências verificadas e instaladas (com aprovação)
- [ ] Migration V1 criada para tabela users
- [ ] Entidade User com todos os campos
- [ ] Enum UserProfile (SUPER_ADMIN, ADMIN, JOGADOR)
- [ ] UserRepository funcionando
- [ ] UserService com CRUD completo
- [ ] AuthService com login JWT
- [ ] PasswordService com BCrypt
- [ ] JwtService para geração de tokens
- [ ] UserResource com todos endpoints documentados
- [ ] AuthResource com endpoint de login
- [ ] HealthResource funcionando
- [ ] Configuração de rotas públicas
- [ ] application.properties configurado
- [ ] application-hml.properties configurado
- [ ] application-prod.properties configurado
- [ ] docker-compose.yaml funcionando
- [ ] Testes unitários de UserService
- [ ] Testes unitários de AuthService
- [ ] Testes de integração de UserResource
- [ ] Testes de integração de AuthResource
- [ ] Testes de integração de HealthResource
- [ ] Swagger UI acessível em /q/swagger-ui
- [ ] Chaves RSA geradas (privateKey.pem, publicKey.pem)

---

## 8. 💬 Fluxo de Execução

**Passo 1:** Confirmar dependências a serem instaladas

**Passo 2:** Criar docker-compose.yaml

**Passo 3:** Criar estrutura de pastas e arquivos de configuração

**Passo 4:** Criar migrations

**Passo 5:** Criar camada domain (entidades)

**Passo 6:** Criar camada infrastructure (repositories)

**Passo 7:** Criar camada core (security, exceptions)

**Passo 8:** Criar camada application (services)

**Passo 9:** Criar camada interfaces (resources, DTOs)

**Passo 10:** Criar testes unitários

**Passo 11:** Criar testes de integração

**Passo 12:** Gerar chaves RSA e testar

---

## 📝 Observações Finais

Este prompt foi criado com base nas rules existentes no projeto:
- `.cursor/rules/01-architecture.mdc` (estrutura de camadas)
- `.cursor/rules/02-coding-standards.mdc` (padrões de código)
- `.cursor/rules/05-exceptions.mdc` (tratamento de erros)
- `.cursor/rules/06-testing.mdc` (estrutura de testes)
- `.cursor/rules/07-security.mdc` (JWT e segurança)
- `.cursor/rules/08-database.mdc` (migrations e banco)

**Lembre-se:** SEMPRE pergunte antes de instalar dependências ou fazer alterações significativas.

