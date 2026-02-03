# Prompt de Implementação: CORS

## Contexto

Este prompt orienta a implementação da configuração de CORS na API, liberando as origens e métodos necessários. **Não execute alterações no código antes da autorização explícita do responsável pelo projeto.**

---

## Objetivo

Resolver o problema de CORS e liberar todas as requisições dos métodos **GET, POST, PUT, PATCH, OPTIONS e DELETE** para as seguintes origens:

- `https://confirm-name-today.vercel.app`
- `http://localhost:3000`

---

## Requisitos

1. **Origens permitidas (Allow-Origin)**  
   Apenas estas duas origens devem ser aceitas (sem uso de `*` se for necessário suportar credenciais):
   - `https://confirm-name-today.vercel.app`
   - `http://localhost:3000`

2. **Métodos permitidos (Allow-Methods)**  
   GET, POST, PUT, PATCH, OPTIONS, DELETE.

3. **Comportamento**
   - Headers CORS devem ser aplicados em todas as respostas relevantes.
   - Requisições OPTIONS (preflight) devem retornar status 200 e os headers CORS corretos.
   - Manter suporte a credenciais (`Access-Control-Allow-Credentials`) se já existir no projeto.

4. **Padrão do projeto**
   - Seguir a estrutura e o padrão do projeto atual.
   - O projeto já possui uma classe `CorsConfig` em `src/main/java/br/com/futebol/config/CorsConfig.java` (atualmente comentada), usando Quarkus, `ContainerResponseFilter` e configuração via `@ConfigProperty`. Reutilizar/adaptar essa abordagem.

5. **Fonte única de CORS**
   - **Remover** de todos os arquivos `application-*.properties` (ex.: `application.properties`, `application-dev.properties`, etc.) qualquer propriedade ou bloco relacionado a CORS (ex.: propriedades do Quarkus ou de outro framework para CORS).
   - CORS deve ser tratado **somente** pela classe `src/main/java/br/com/futebol/config/CorsConfig.java` (atualmente comentada). Nenhuma outra configuração de CORS deve permanecer em `application-*.properties`.

6. **Configuração**
   - Manter origens e métodos configuráveis via propriedades (ex.: `cors.allowed-origins`, `cors.allowed-methods`) **no código da CorsConfig** (valores default), já incluindo `https://confirm-name-today.vercel.app` e `http://localhost:3000` e os métodos listados acima. Opcionalmente, permitir override por variáveis de ambiente ou por propriedades em `application-*.properties` apenas se forem lidas pela própria `CorsConfig`; não usar configuração nativa de CORS do framework nos properties.

---

## Tarefas de Implementação

1. **Estudar e analisar o projeto atual**
   - Revisar `CorsConfig.java` e onde CORS é (ou será) aplicado.
   - Identificar em todos os `application-*.properties` (e, se existir, `application.yaml`) qualquer configuração relacionada a CORS.

2. **Remover CORS dos application-\*.properties**
   - Remover de **todos** os arquivos `application-*.properties` qualquer propriedade ou trecho relacionado a CORS (ex.: `quarkus.http.cors.*` ou equivalente). CORS deve ser controlado apenas por `src/main/java/br/com/futebol/config/CorsConfig.java`.

3. **Implementar/reativar a configuração CORS**
   - Descomentar e ajustar `CorsConfig.java` para:
     - Incluir nas origens permitidas: `https://confirm-name-today.vercel.app` e `http://localhost:3000`.
     - Incluir nos métodos: GET, POST, PUT, PATCH, OPTIONS, DELETE.
   - Garantir que o filtro seja registrado (ex.: `@Provider` + `@ApplicationScoped`) e que OPTIONS responda com 200 e headers CORS.
   - Valores default das propriedades (ex.: `cors.allowed-origins`, `cors.allowed-methods`) devem estar na própria `CorsConfig`; se desejar override por ambiente, usar apenas propriedades lidas pela `CorsConfig`, sem duplicar configuração nativa de CORS nos properties.

4. **Testes com curl**
   - Após a implementação (e somente após autorização para alterar código), executar testes com `curl` para:
     - Requisição GET a um endpoint da API com header `Origin: https://confirm-name-today.vercel.app` e verificar se a resposta contém `Access-Control-Allow-Origin: https://confirm-name-today.vercel.app`.
     - Repetir com `Origin: http://localhost:3000`.
     - Enviar requisição OPTIONS (preflight) e garantir status 200 e headers CORS corretos.
   - Documentar os comandos `curl` utilizados para reprodutibilidade.

---

## Checklist antes de executar

- [ ] Autorização do responsável pelo projeto para alterar código e configuração.
- [ ] Backup ou commit atual do repositório (recomendado).
- [ ] Execução dos testes com curl apenas após a implementação estar aplicada.

---

## Observação

Nenhuma alteração de código ou execução de comandos deve ser feita antes da autorização explícita do responsável pelo projeto.
