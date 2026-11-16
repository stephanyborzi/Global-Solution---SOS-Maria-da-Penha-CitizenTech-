# SOS Mulher — API (CitizenTech · Global Solution 2026/2)

API REST do aplicativo de segurança pública e proteção à mulher. A usuária aciona um **SOS**, cadastra **contatos de confiança**, reporta **locais perigosos** e consulta o **risco de uma rota**, enquanto **sensores de IoT** (simulados) indicam falhas na infraestrutura urbana, como câmeras offline e iluminação fraca.

O aplicativo mobile consome esta API. Todas as rotas (exceto `/auth/**`) exigem autenticação por **token JWT** e respeitam o **perfil** de quem chama.

## Tecnologias

- Java 17+ e Maven 3.9+
- Spring Boot (Web, Data JPA, Validation, Security)
- JWT (jjwt) para autenticação, BCrypt para senhas
- Banco H2 em memória (desenvolvimento)
- Swagger/OpenAPI (springdoc) para documentação e testes

## Como executar

```bash
mvn spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html` (botão **Authorize** para colar o token)

Na primeira execução, **sem configurar nada**, a aplicação sobe normalmente e:

- cria o usuário **ADMIN** inicial (`admin@sosmaria.local`) com uma senha aleatória, **mostrada uma única vez no log** (procure por `Initial ADMIN created`);
- gera um segredo JWT aleatório (os tokens deixam de valer ao reiniciar);
- gera uma chave de API para o simulador de IoT, também mostrada no log.

### Variáveis de ambiente (opcionais)

| Variável | Para que serve |
|---|---|
| `JWT_SECRET` | Segredo de assinatura do JWT (mínimo 32 caracteres). Ex.: `export JWT_SECRET=$(openssl rand -base64 48)` |
| `JWT_EXPIRATION_MS` | Validade do token em milissegundos (padrão: 3600000 = 1 hora) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Conta do administrador inicial |
| `IOT_API_KEY` | Chave do simulador de IoT (header `X-API-Key`) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas, separadas por vírgula |

Nenhum segredo fica no código nem no repositório.

### Console do banco (somente desenvolvimento)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Abre o console do H2 em `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:sosdb`, usuário `sa`, senha vazia). Útil para conferir que a coluna `password_hash` guarda um hash BCrypt (`$2a$...`). Sem o perfil `dev`, o console fica desligado.

O banco H2 é **em memória**: as tabelas são criadas na inicialização e os dados somem quando a aplicação para. Para Oracle ou PostgreSQL, altere `url`, `driver` e `dialect` em `application.properties`.

## Arquitetura

| Camada | Pacote | Responsabilidade |
|---|---|---|
| Controller | `controller` | Recebe a requisição, valida a entrada (`@Valid`), aplica o perfil exigido (`@PreAuthorize`) e devolve o status HTTP |
| Service | `service` | Regras de negócio e verificação de posse dos dados |
| Repository | `repository` | Acesso ao banco (Spring Data JPA, consultas parametrizadas) |
| Model | `model` | Entidades JPA e enums |
| DTO | `dto` | Formatos de entrada e saída (as entidades nunca são expostas) |
| Security | `security` | JWT, filtros de autenticação, regras de acesso, sanitização, limite de tentativas |
| Config | `config` | Spring Security, CORS, Swagger e criação do admin inicial |
| Exception | `exception` | Tratamento centralizado de erros |

## Modelo de dados

| Entidade | Tabela | Descrição |
|---|---|---|
| `User` | `users` | Usuária do app (nome, e-mail único, hash da senha, perfil `ADMIN` ou `CITIZEN`) |
| `Contact` | `contacts` | Contato de confiança de uma usuária |
| `SosAlert` | `sos_alerts` | Pedido de socorro com localização e status (`OPEN`, `IN_PROGRESS`, `CLOSED`) |
| `DangerousLocation` | `dangerous_locations` | Local reportado como perigoso |
| `Sensor` | `sensors` | Sensor urbano (`CAMERA` ou `LIGHTING`) com posição e estado ativo/inativo |
| `SensorReading` | `sensor_readings` | Leituras enviadas por um sensor |

Um `User` tem muitos `Contact`, `SosAlert` e `DangerousLocation`; um `Sensor` tem muitas `SensorReading`.

## Autenticação e perfis de acesso

1. A usuária se cadastra em `POST /auth/register` (sempre como `CITIZEN`) e faz login em `POST /auth/login`, que devolve um **token JWT**.
2. Nas demais chamadas, envia o header `Authorization: Bearer <token>`.
3. O **identificador da usuária vem do token**, nunca do corpo da requisição: ninguém consegue agir em nome de outra pessoa.
4. O perfil é relido do banco a cada requisição; se a conta for apagada, o token deixa de funcionar.

| Perfil | Quem é | O que pode |
|---|---|---|
| `CITIZEN` | Usuária do app | Gerencia a própria conta, contatos e locais; aciona e consulta os **próprios** alertas SOS; consulta mapa, sensores e rota segura |
| `ADMIN` | Central de atendimento | Vê **todos** os alertas e altera o status; lista usuárias; cadastra sensores; remove qualquer local |
| `SENSOR` | Simulador de IoT (chave de API) | Apenas envia leituras em `POST /sensors/{id}/readings` |

Resposta **401** = sem token, token inválido ou expirado. Resposta **403** = autenticada, mas sem permissão para aquele recurso.

## Endpoints

| Recurso | Método e rota | Quem acessa | Resposta |
|---|---|---|---|
| Autenticação | `POST /auth/register` | público | 201 / 400 / 409 |
| | `POST /auth/login` | público | 200 / 401 / 429 |
| Usuários | `GET /users` | ADMIN | 200 |
| | `GET /users/me` | autenticada | 200 |
| | `GET /users/{id}` | a própria ou ADMIN | 200 / 403 / 404 |
| | `PUT /users/{id}` | a própria ou ADMIN | 200 / 403 / 409 |
| | `PATCH /users/{id}/password` | somente a própria | 204 / 400 / 403 |
| | `DELETE /users/{id}` | a própria ou ADMIN | 204 / 403 / 409 |
| Contatos | `POST /contacts` | CITIZEN | 201 |
| | `GET /contacts` | CITIZEN (os seus) | 200 |
| | `PUT /contacts/{id}` · `DELETE /contacts/{id}` | CITIZEN (dona) | 200 · 204 / 403 |
| SOS | `POST /sos-alerts` | CITIZEN | 201 |
| | `GET /sos-alerts` | CITIZEN (os seus) / ADMIN (todos) | 200 |
| | `GET /sos-alerts/{id}` | dona ou ADMIN | 200 / 403 |
| | `PUT /sos-alerts/{id}` | ADMIN | 200 / 403 |
| Locais perigosos | `POST /dangerous-locations` | CITIZEN | 201 |
| | `GET /dangerous-locations` | autenticada | 200 |
| | `DELETE /dangerous-locations/{id}` | dona ou ADMIN | 204 / 403 |
| Sensores (IoT) | `POST /sensors` | ADMIN | 201 |
| | `GET /sensors` · `GET /sensors/{id}/readings` | autenticada | 200 |
| | `POST /sensors/{id}/readings` | SENSOR (X-API-Key) ou ADMIN | 201 |
| Rota | `GET /safe-route?originLat=&originLng=&destLat=&destLng=` | autenticada | 200 |

## Regras de negócio

**SOS.** `POST /sos-alerts` grava o pedido de socorro da usuária logada com a localização e o status `OPEN`. Isso **simula** o acionamento da central, sem integração com serviços reais (190, 180). A central (ADMIN) acompanha o atendimento: `OPEN` → `IN_PROGRESS` → `CLOSED`.

**Simulação de IoT.** Cada leitura enviada é avaliada: câmera com valor `0` está offline (`1` = online); iluminação abaixo de **20 lux** é falha. Em caso de problema, o sensor passa a `active = false` e a resposta traz `alert: true`. Quando a leitura volta ao normal, o sensor é reativado.

**Rota segura.** Amostra 20 pontos entre origem e destino e, em cada um, procura locais perigosos e sensores em falha num raio de **150 m** (Haversine). Cada local soma **+1** e cada sensor em falha soma **+2**. Resultado: `LOW` (0), `MEDIUM` (até 10) ou `HIGH` (acima de 10). Regra simples, sem machine learning.

## Segurança — o que foi implementado

| Prática | Onde está no código |
|---|---|
| **Senha com hash BCrypt**, nunca em texto puro | `PasswordEncoderConfig`, `UserService`, `AuthService` |
| **Login seguro**: mensagem genérica ("Invalid email or password") e tempo de resposta igual para e-mail inexistente | `AuthService` |
| **Autenticação stateless com JWT** assinado e com expiração | `JwtService`, `JwtAuthFilter` |
| **Controle de acesso por perfil** (ADMIN, CITIZEN, SENSOR) | `@PreAuthorize` nos controllers, `SecurityConfig` |
| **Controle de posse**: cada usuária só acessa os próprios dados (403 caso contrário) | `AccessRules`, services |
| **Identidade vem do token**, não do corpo da requisição | `AuthUser` (`@AuthenticationPrincipal`) |
| **Validação de entrada** em todos os endpoints | Bean Validation nos DTOs, `GlobalExceptionHandler` |
| **Proteção contra SQL Injection** | Spring Data JPA com consultas parametrizadas |
| **Proteção contra XSS**: textos livres são escapados antes de salvar | `InputSanitizer` |
| **Segredos em variáveis de ambiente** (JWT, admin, chave de IoT) | `application.properties` |
| **Limite de tentativas de login** (5 falhas = bloqueio de 15 min, resposta 429) | `LoginAttemptService` |
| **CORS restrito** às origens configuradas | `SecurityConfig` |
| **Chave de API** comparada em tempo constante, aceita só para enviar leituras | `ApiKeyAuthFilter`, `IotKeyService` |
| **Respostas sem dados sensíveis** (DTOs, sem hash) | pacote `dto` |
| **Cabeçalhos de segurança** (X-Content-Type-Options, X-Frame-Options, etc.) | padrão do Spring Security |

Observações: em produção, o tráfego deve passar por **HTTPS** (certificado no servidor ou em proxy reverso) e o Swagger deve ser desativado ou protegido. O console do H2 só existe no perfil `dev`.

## Roteiro de testes de segurança

Execute pelo Swagger ou pelo Postman e guarde os prints como evidência. Antes, copie do log a senha do ADMIN.

| # | Cenário | Esperado |
|---|---|---|
| 1 | `POST /auth/register` com dados válidos | 201, resposta sem senha |
| 2 | `POST /auth/register` com e-mail inválido ou senha curta | 400 com a lista de campos |
| 3 | `POST /auth/register` repetindo o e-mail | 409 |
| 4 | `POST /auth/login` correto | 200 com `token` |
| 5 | `POST /auth/login` com senha errada | 401 "Invalid email or password" |
| 6 | `POST /auth/login` com e-mail inexistente | 401 com a **mesma** mensagem do teste 5 |
| 7 | 6 logins errados seguidos para o mesmo e-mail | 429 |
| 8 | `POST /auth/login` com `' OR '1'='1` no e-mail | 400 (e-mail inválido) |
| 9 | `GET /users/me` sem token | 401 |
| 10 | `GET /users/me` com token adulterado (altere uma letra) | 401 |
| 11 | CITIZEN chama `GET /users` | 403 |
| 12 | CITIZEN A abre `GET /sos-alerts/{id}` de um alerta da CITIZEN B | 403 |
| 13 | CITIZEN chama `PUT /sos-alerts/{id}` | 403 |
| 14 | ADMIN chama `GET /sos-alerts` | 200 com alertas de todas |
| 15 | ADMIN chama `PUT /sos-alerts/{id}` com `{"status":"IN_PROGRESS"}` | 200 |
| 16 | `POST /dangerous-locations` com `description` = `<script>alert(1)</script>` | 201 e o texto volta escapado (`&lt;script&gt;...`) |
| 17 | `POST /sensors/1/readings` com `X-API-Key` correta | 201 |
| 18 | O mesmo, sem a chave ou com chave errada | 401 |
| 19 | Perfil `dev`: tabela `users` no console do H2 | coluna `password_hash` começa com `$2a$` |

## Exemplos de requisição

Cadastro (`POST /auth/register`):

```json
{ "name": "Maria Silva", "email": "maria@email.com", "password": "senhaSegura123" }
```

Login (`POST /auth/login`) e resposta:

```json
{ "email": "maria@email.com", "password": "senhaSegura123" }
```
```json
{ "token": "eyJhbGciOi...", "tokenType": "Bearer", "expiresInMs": 3600000,
  "user": { "id": 2, "name": "Maria Silva", "email": "maria@email.com", "profile": "CITIZEN" } }
```

Acionar SOS (`POST /sos-alerts`, com o token no header):

```json
{ "latitude": -23.5505, "longitude": -46.6333 }
```

Enviar leitura de sensor (`POST /sensors/1/readings`, header `X-API-Key`):

```json
{ "value": 0 }
```

## Erros

Todos os erros seguem o mesmo formato:

```json
{ "timestamp": "2026-11-14T10:00:00", "status": 403, "error": "Access denied" }
```

| Status | Quando |
|---|---|
| 400 | Dados inválidos, requisição mal formada ou regra violada (ex.: senha atual incorreta) |
| 401 | Sem token, token inválido/expirado, ou e-mail/senha incorretos |
| 403 | Autenticada, mas sem permissão para o recurso |
| 404 | Recurso não encontrado |
| 409 | E-mail já cadastrado, ou exclusão de usuária com alertas/locais vinculados |
| 429 | Muitas tentativas de login |

## Uso de IA generativa

(Declarar aqui as ferramentas utilizadas e para que foram usadas.)

## Integrantes

| RM | Nome |
|---|---|
| | |
