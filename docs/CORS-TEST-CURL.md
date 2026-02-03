# Testes CORS com curl (Profile Prod)

Execute estes comandos após iniciar a API com `-Dquarkus.profile=prod` ou contra a API em produção no Heroku.

## Pré-requisitos

- API rodando (localmente com profile prod ou Heroku)
- Base URL: `http://localhost:8080` (local) ou `https://api-futebol-06387567b79a.herokuapp.com` (Heroku)

---

## 1. Teste GET com Origin Vercel

```bash
curl -s -i -X GET "http://localhost:8080/q/health/ready" \
  -H "Origin: https://confirm-name-today.vercel.app"
```

**Esperado:** Resposta deve conter `Access-Control-Allow-Origin: https://confirm-name-today.vercel.app`

---

## 2. Teste GET com Origin localhost

```bash
curl -s -i -X GET "http://localhost:8080/q/health/ready" \
  -H "Origin: http://localhost:3000"
```

**Esperado:** Resposta deve conter `Access-Control-Allow-Origin: http://localhost:3000`

---

## 3. Teste OPTIONS (preflight)

```bash
curl -s -i -X OPTIONS "http://localhost:8080/q/health/ready" \
  -H "Origin: https://confirm-name-today.vercel.app" \
  -H "Access-Control-Request-Method: GET"
```

**Esperado:** Status 200 e headers CORS:

- `Access-Control-Allow-Origin`
- `Access-Control-Allow-Methods` (GET, POST, PUT, PATCH, OPTIONS, DELETE)
- `Access-Control-Allow-Headers`
- `Access-Control-Allow-Credentials: true`

---

## Para testar contra Heroku (após deploy)

Substitua `http://localhost:8080` por `https://api-futebol-06387567b79a.herokuapp.com` nos comandos acima.
