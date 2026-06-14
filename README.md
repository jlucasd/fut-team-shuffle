# ⚽ Gaviões FC Sorteio

Sistema web completo para gerenciamento de sorteio de times equilibrados do Gaviões FC.

## Visão Geral

O Gaviões FC Sorteio é uma aplicação full-stack que permite:
- Cadastro e gerenciamento de jogadores (nome, posição, nível 1-5, status ativo/inativo)
- Sorteio balanceado dividindo jogadores em Time Amarelo e Time Preto
- Edição manual pós-sorteio com recálculo de médias em tempo real
- Geração de imagem PNG para compartilhamento via WhatsApp
- Histórico de sorteios anteriores

## Stack Tecnológica

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA |
| Frontend | React 18, TypeScript, Vite, TailwindCSS, Zustand, React Query |
| Banco de Dados | H2 (desenvolvimento), PostgreSQL 16 (produção) |
| Infraestrutura | Docker, Docker Compose, Nginx |
| Testes | JUnit 5 + jqwik (backend), Vitest + fast-check (frontend) |

## Pré-requisitos

- **Java 21** (JDK)
- **Node.js 20+** (com npm)
- **Docker** e **Docker Compose** (para ambiente completo)
- **Maven 3.9+** (wrapper incluído no projeto)

## Estrutura do Projeto

```
gavioes-fc-sorteio/
├── backend/                  # API Spring Boot
│   ├── src/main/java/        # Código fonte Java
│   ├── src/main/resources/   # Configurações (application.yml)
│   ├── src/test/             # Testes unitários e property-based
│   ├── Dockerfile            # Imagem Docker do backend
│   └── pom.xml               # Dependências Maven
├── frontend/                 # SPA React
│   ├── src/                  # Código fonte TypeScript/React
│   │   ├── components/       # Componentes reutilizáveis
│   │   ├── pages/            # Páginas (Home, Players, Draw, History)
│   │   ├── stores/           # Zustand stores
│   │   ├── services/         # Camada de API
│   │   ├── hooks/            # React Query hooks
│   │   ├── types/            # Tipos TypeScript
│   │   └── __tests__/        # Testes property-based
│   ├── Dockerfile            # Imagem Docker do frontend
│   ├── vercel.json           # Config de deploy Vercel
│   └── package.json          # Dependências npm
└── docker/
    └── docker-compose.yml    # Orquestração local completa
```

## Primeiros Passos

### Backend (Desenvolvimento)

```bash
cd backend
./mvnw spring-boot:run
```

O backend inicia na porta `8080` com perfil `dev` (H2 em memória).

Endpoints úteis:
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

Credenciais padrão (v1):
- Usuário: `admin`
- Senha: `admin123`

### Frontend (Desenvolvimento)

```bash
cd frontend
npm install
npm run dev
```

O frontend inicia na porta `5173` com proxy para o backend.

Acesse: http://localhost:5173

### Executar Testes

**Backend:**
```bash
cd backend
./mvnw test
```

**Frontend:**
```bash
cd frontend
npm test
```

## Docker Compose (Ambiente Completo)

Para rodar a aplicação completa com PostgreSQL:

```bash
cd docker
docker compose up --build
```

Serviços:
- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8080
- **PostgreSQL**: localhost:5432

Para parar:
```bash
docker compose down
```

Para limpar dados persistidos:
```bash
docker compose down -v
```

## Deploy

### Frontend (Vercel)

1. Conecte o repositório ao Vercel
2. Configure:
   - **Framework Preset**: Vite
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
3. Defina variável de ambiente:
   - `VITE_API_BASE_URL`: URL do backend em produção (ex: `https://api.gavioes-fc.com`)
4. O arquivo `vercel.json` já configura rewrites para SPA

### Backend (Qualquer host Java)

1. Build do JAR:
   ```bash
   cd backend
   ./mvnw clean package -DskipTests
   ```

2. Execute com variáveis de ambiente:
   ```bash
   java -jar target/sorteio-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=prod \
     --DB_HOST=seu-host-postgres \
     --DB_PORT=5432 \
     --DB_NAME=gavioesfc \
     --DB_USERNAME=seu-usuario \
     --DB_PASSWORD=sua-senha \
     --JWT_SECRET=sua-chave-secreta-256-bits \
     --CORS_ORIGINS=https://seu-frontend.vercel.app
   ```

3. Ou use a imagem Docker:
   ```bash
   docker build -t gavioes-fc-backend ./backend
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e DB_HOST=seu-host-postgres \
     -e DB_USERNAME=seu-usuario \
     -e DB_PASSWORD=sua-senha \
     -e JWT_SECRET=sua-chave-secreta \
     -e CORS_ORIGINS=https://seu-frontend.vercel.app \
     gavioes-fc-backend
   ```

## Algoritmo de Sorteio

O algoritmo de balanceamento segue estes passos:

1. Separa goleiros dos jogadores de linha
2. Embaralha todos os jogadores (shuffle)
3. Ordena jogadores de linha por nível (decrescente)
4. Distribui alternadamente entre os times
5. Calcula diferença de médias
6. Se diferença > 0.5: realiza trocas binárias entre jogadores de nível próximo
7. Atribui 1 goleiro por time (quando 2+ disponíveis)
8. Número ímpar → último jogador vai para Reserva
9. Retorna resultado com flag "equilibrado" (diferença ≤ 0.5)

## Variáveis de Ambiente

### Backend

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SPRING_PROFILES_ACTIVE` | Perfil ativo (dev/prod) | `dev` |
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco | `gavioesfc` |
| `DB_USERNAME` | Usuário do banco | `gavioesfc` |
| `DB_PASSWORD` | Senha do banco | `gavioesfc` |
| `JWT_SECRET` | Chave secreta para JWT (mín. 256 bits) | — |
| `JWT_EXPIRATION` | Tempo de expiração do token (ms) | `86400000` |
| `CORS_ORIGINS` | Origens permitidas (CORS) | `http://localhost:5173` |
| `SERVER_PORT` | Porta do servidor | `8080` |

### Frontend

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `VITE_API_BASE_URL` | URL base da API | `http://localhost:8080` |

## Licença

Projeto privado — Gaviões FC.
