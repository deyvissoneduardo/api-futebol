## 🎯 Prompt: Planejamento Completo de Dockerização para Produção da `api-futebol`

---

## 1. 🌍 Contexto

**Stack atual do projeto:**
- Java 21 com Quarkus 3.30.6
- PostgreSQL como banco de dados
- Maven para gerenciamento de dependências

**Arquitetura/Padrão:**
- Clean Architecture conforme `.cursor/rules/01-architecture.mdc`
- Estrutura de camadas: `config/`, `core/`, `domain/`, `application/`, `infrastructure/`, `interfaces/`

**Foco deste prompt:**
- Planejar e implementar **apenas o ambiente de PRODUÇÃO** usando Docker e, Dockerfile para criar imagem.
- Tratar aspectos de **build**, **runtime**, **segurança**, **observabilidade** e **deploy** em produção

**Persona:**
- Especialista em DevOps / SRE com experiência em:
  - Quarkus em produção
  - Containers Docker
  - Observabilidade (logs, métricas, health checks)
  - Segurança (segredos, rede, imagens, hardening)

---

## 2. 🎯 Objetivo

**Objetivo principal:**
- Entregar um **plano completo e detalhado** para dockerizar a `api-futebol` para PRODUÇÃO, incluindo:
  1. Estratégia de build da imagem (JVM ou native image, multi-stage build)
  2. Estrutura de `Dockerfile` otimizada para produção
  3. Definição de variáveis de ambiente e segredos (sem vazar valores reais)
  4. Configuração de rede, portas e comunicação com PostgreSQL em produção
  5. Estratégia de logs e health checks (liveness/readiness)
  6. Estratégia de escalabilidade (réplicas, horizontal scaling) – em nível conceitual
  7. Estratégia de deploy (Docker puro, Docker Compose em PROD, ou orquestrador como Kubernetes) – explicar prós e contras
  8. Checklist de boas práticas de segurança para a imagem e o container

**O que NÃO é foco:**
- Não tratar ambiente de desenvolvimento ou homologação (somente PRODUÇÃO)
- Não escrever código Java de negócio; apenas o que for necessário para configuração de build/run

---

## 3. 🧱 Planejamento de Dockerização para Produção

### 3.1 Estratégia de Build da Imagem

- **DEVE** propor e justificar:
  - Se a imagem será **JVM** (JAR/runner) ou **nativa** (GraalVM/Quarkus native image)
  - Se o `Dockerfile` usará **multi-stage build** (ex.: builder + runtime)
  - A imagem base de build (por exemplo, `maven:3.9-eclipse-temurin-21` ou equivalente)
  - A imagem base de runtime (por exemplo, `eclipse-temurin:21-jre-alpine` ou `ubi-minimal`)
- **DEVE** considerar:
  - Tamanho da imagem
  - Tempo de build
  - Performance e consumo de memória em produção
  - Compatibilidade com Quarkus 3.30.6

### 3.2 Estrutura do `Dockerfile` de Produção

- **DEVE** propor um `Dockerfile` de produção contendo:
  1. Estágio de build (download de dependências, build com Maven, testes opcionais)
  2. Estágio de runtime minimalista
  3. Copia apenas o artefato necessário para execução (JAR/runner ou binário nativo)
  4. Definição de `WORKDIR`, `USER` não-root e permissões
  5. Definição de `EXPOSE` apenas da porta necessária (ex.: 8080)
  6. Configuração de variáveis de ambiente mínimas para apontar para o profile `prod`
  7. Configuração de `ENTRYPOINT`/`CMD` enxuta e clara

### 3.3 Configuração de Variáveis de Ambiente e Segredos

- **DEVE**:
  - Descrever quais variáveis de ambiente serão usadas em produção, por exemplo:
    - `QUARKUS_PROFILE=prod`
    - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
    - Segredos para JWT, chaves RSA, etc.
  - Explicar **como** essas variáveis devem ser injetadas:
    - Via `docker run -e` / compose / secrets manager (sem definir valores reais)
  - Destacar que **NÃO DEVE**:
    - Versionar segredos no repositório
    - Hardcodar credenciais no `Dockerfile`

### 3.4 Comunicação com PostgreSQL em Produção

- **DEVE**:
  - Descrever a topologia em produção:
    - Banco gerenciado (RDS, Cloud SQL, etc.) ou container PostgreSQL dedicado
  - Explicar a configuração de conexão:
    - URL, pool de conexões, timeouts
  - Se utilizar Docker Compose em produção:
    - Definir um serviço `api-futebol` e um serviço `postgres` com redes isoladas
  - Se utilizar banco externo:
    - Garantir que apenas o serviço da API tenha acesso ao banco via rede (security group/firewall)

### 3.5 Observabilidade: Logs, Health Checks e Métricas

- **DEVE**:
  - Explicar a estratégia de logs:
    - Logs no `stdout/stderr` para coleta por ferramentas externas (ELK, Loki, etc.)
    - Formato estruturado (JSON) se aplicável
  - Configurar endpoints de health:
    - Liveness/Readiness (`/q/health/live`, `/q/health/ready`) para uso pelo orquestrador
  - Sugerir como integrar métricas:
    - Micrometer/Prometheus, se já estiver no projeto ou for viável

### 3.6 Escalabilidade e Deploy em Produção

- **DEVE**:
  - Comparar rapidamente:
    - Deploy com **Docker puro**
    - Deploy com **Docker Compose** em produção
    - Deploy em **Kubernetes** ou outro orquestrador
  - Sugerir a abordagem mais adequada para um backend Quarkus com PostgreSQL
  - Descrever:
    - Como escalar horizontalmente a API (múltiplas réplicas)
    - Considerações sobre sticky sessions (se houver) e statelessness
    - Uso de um load balancer (Nginx, ingress, gateway de nuvem, etc.)

### 3.7 Segurança da Imagem e do Container

- **DEVE** listar práticas de hardening:
  - Usar imagem base mínima (Alpine/UBI minimal, etc.)
  - Rodar o processo com **usuário não-root**
  - Remover ferramentas desnecessárias da imagem (compiladores, shells, etc.)
  - Fixar tags de imagem (evitar `latest`)
  - Escanear imagens em busca de vulnerabilidades (Trivy, etc.)
  - Configurar:
    - Limites de memória/CPU
    - Read-only root filesystem (quando possível)
    - Montagens de volume mínimas

### 3.8 Checklist Final de Produção

- **DEVE** entregar um checklist marcado com itens como:
  - [ ] `Dockerfile` de produção definido e revisado
  - [ ] Estratégia de build (JVM vs nativo) decidida e documentada
  - [ ] Variáveis de ambiente necessárias listadas
  - [ ] Estratégia de segredos definida (sem segredos no repositório)
  - [ ] Comunicação segura com PostgreSQL definida
  - [ ] Health checks expostos e documentados
  - [ ] Estratégia de logs definida (stdout, formato, rotação externa)
  - [ ] Estratégia de escalabilidade e deploy em produção descrita
  - [ ] Checklist de segurança aplicado à imagem e ao container

---

## 4. 📋 Formato da Resposta Esperada

- **Passo 1:** Apresentar uma visão geral do plano de dockerização para produção em 5–10 bullets.
- **Passo 2:** Detalhar cada uma das seções (3.1 a 3.8) com explicações claras e objetivas.
- **Passo 3:** Sugerir um esqueleto de `Dockerfile` de produção comentado.
- **Passo 4:** Se fizer sentido, sugerir um `docker-compose.yml` **exclusivamente voltado para produção** (sem ferramentas de admin como pgAdmin).
- **Passo 5:** Finalizar com o checklist de produção preenchível.

**Limites:**
- Não gerar valores reais de senhas, chaves ou URLs sensíveis.
- Não alterar arquivos do projeto sem instrução explícita.

---

## 5. 👤 Persona / Tom

- Falar sempre como **especialista em DevOps/SRE sênior**.
- Tom técnico, objetivo e pragmático.
- Responder sempre em **português (pt-BR)**.
- Explicar brevemente as decisões importantes (trade-offs), mas evitar texto prolixo.

---

## 6. ✅ Critério de Sucesso

- Ao final, deve existir:
  - Um plano claramente utilizável para implementar a dockerização de PRODUÇÃO da `api-futebol`.
  - Um `Dockerfile` de produção proposto, pronto para ser adaptado e aplicado.
  - Uma visão clara de como rodar e escalar a aplicação em produção usando containers.


