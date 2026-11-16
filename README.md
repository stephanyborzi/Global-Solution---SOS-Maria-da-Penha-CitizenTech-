# SOS Maria da Penha — API (CitizenTech · Global Solution 2026/2)

API REST do aplicativo de segurança pública e proteção à mulher. A usuária aciona um **SOS**, cadastra **contatos de confiança**, reporta **locais perigosos** e consulta o **risco de uma rota**. **Sensores de IoT** (simulados) enviam leituras à API e indicam falhas na infraestrutura urbana, como câmera offline e iluminação fraca. O aplicativo mobile consome esta API.

Todas as rotas, exceto `/auth/**` e o Swagger, exigem **token JWT** e respeitam o **perfil** de quem chama.

## Tecnologias

- Java 17+ e Maven 3.9+
- Spring Boot 3 (Web, Data JPA, Validation, Security)
- JWT (jjwt) para autenticação e BCrypt para senhas
- Banco H2 em memória (desenvolvimento)
- Swagger/OpenAPI (springdoc) para documentação e testes

## Como executar

```bash
mvn spring-boot:run
```
- API: `http://localhost:8086`
- Swagger UI: `http://localhost:8086/swagger-ui/index.html`
- Documentação em JSON: `http://localhost:8086/v3/api-docs`

Na primeira execução, **sem configurar nada**, a aplicação sobe normalmente e:
- cria o usuário **ADMIN** `admin@sosmaria.local`, com uma senha aleatória **mostrada uma única vez no log** (procure por `Initial ADMIN created`);
- gera um segredo JWT aleatório (os tokens deixam de valer ao reiniciar);
- gera uma chave de API para o simulador de IoT, também mostrada no log (procure por `IOT_API_KEY is not set`).
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

Abre o console do H2 em `http://localhost:8086/h2-console` (usuário `sa`, senha vazia). O endereço JDBC aparece no log de inicialização. Serve para conferir que a coluna `password_hash` guarda um hash BCrypt (`$2a$...`). Sem o perfil `dev`, o console fica desligado.

## Como usar a API (passo a passo no Swagger)

1. `POST /auth/register` cria uma conta de cidadã (ou use o ADMIN criado na inicialização).
2. `POST /auth/login` com e-mail e senha devolve o `token`.
3. Clique em **Authorize** no Swagger e cole o token (só o texto do token, sem a palavra `Bearer`).
4. Chame os demais endpoints. Para trocar de usuária, clique em *Logout* e autorize com o novo token.

Fora do Swagger, envie o header `Authorization: Bearer <token>` em toda requisição.

## Arquitetura
```
com.citizentech.sos
├── controller    recebe a requisição, valida a entrada, exige o perfil e devolve o status HTTP
├── service       regras de negócio e verificação de posse dos dados
├── repository    acesso ao banco (Spring Data JPA)
├── model         entidades JPA e enums
├── dto           formatos de entrada e saída (a entidade nunca é exposta)
├── security      JWT, filtros de autenticação, regras de acesso, sanitização, limite de tentativas
├── config        Spring Security, CORS, Swagger e criação do admin inicial
└── exception     tratamento centralizado de erros
```

O fluxo de uma chamada é sempre **Controller → Service → Repository**: o controller só recebe e responde, o service aplica as regras e o repository fala com o banco.

## Entidades usadas pela API

| Entidade | Tabela | Descrição |
|---|---|---|
| `User` | `users` | Usuária (nome, e-mail único, hash da senha, perfil `ADMIN` ou `CITIZEN`) |
| `Contact` | `contacts` | Contato de confiança de uma usuária (nome e telefone) |
| `SosAlert` | `sos_alerts` | Pedido de socorro com localização e status (`OPEN`, `IN_PROGRESS`, `CLOSED`) |
| `DangerousLocation` | `dangerous_locations` | Local reportado como perigoso (tipo, descrição, coordenadas) |
| `Sensor` | `sensors` | Sensor urbano (`CAMERA`, `LIGHTING` ou `MOTION`) com posição e estado ativo/inativo |
| `SensorReading` | `sensor_readings` | Leituras enviadas por um sensor |

Um `User` tem muitos `Contact`, `SosAlert` e `DangerousLocation`; um `Sensor` tem muitas `SensorReading`. O diagrama ER e o script SQL fazem parte da entrega de Banco de Dados.

## Autenticação e perfis

O login devolve um **token JWT** assinado e com validade. O token carrega apenas o id da usuária. A cada requisição, a API busca a usuária no banco e usa o **perfil guardado lá**: se a conta for apagada, o token deixa de funcionar na hora.

| Perfil | Quem é | O que pode |
|---|---|---|
| `CITIZEN` | Usuária do app | Gerencia a própria conta, contatos e locais; aciona e consulta os **próprios** alertas SOS; consulta sensores e rota segura |
| `ADMIN` | Central de atendimento | Vê **todos** os alertas e altera o status; lista usuárias; cadastra sensores; remove qualquer local |
| `SENSOR` | Simulador de IoT | Somente envia leituras em `POST /sensors/{id}/readings`, usando o header `X-API-Key` |

O cadastro público **sempre** cria `CITIZEN`. O `ADMIN` só existe pela carga inicial. O identificador da usuária vem do token, **nunca do corpo da requisição**, então ninguém age em nome de outra pessoa.

**401** = sem token, token inválido ou expirado. **403** = autenticada, mas sem permissão para aquele recurso.

## Endpoints (24)

| Recurso | Método e rota | Quem acessa | Respostas |
|---|---|---|---|
| Autenticação | `POST /auth/register` | público | 201 / 400 / 409 |
| | `POST /auth/login` | público | 200 / 400 / 401 / 429 |
| Usuários | `GET /users` | ADMIN | 200 / 403 |
| | `GET /users/me` | autenticada | 200 |
| | `GET /users/{id}` | a própria ou ADMIN | 200 / 403 / 404 |
| | `PUT /users/{id}` | a própria ou ADMIN | 200 / 400 / 403 / 404 / 409 |
| | `PATCH /users/{id}/password` | somente a própria | 204 / 400 / 403 |
| | `DELETE /users/{id}` | a própria ou ADMIN | 204 / 403 / 404 / 409 |
| Contatos | `POST /contacts` | CITIZEN | 201 / 400 |
| | `GET /contacts` | CITIZEN (os seus) | 200 |
| | `PUT /contacts/{id}` | CITIZEN (dona) | 200 / 400 / 403 / 404 |
| | `DELETE /contacts/{id}` | CITIZEN (dona) | 204 / 403 / 404 |
| SOS | `POST /sos-alerts` | CITIZEN | 201 / 400 |
| | `GET /sos-alerts` | CITIZEN (os seus) / ADMIN (todos) | 200 |
| | `GET /sos-alerts/{id}` | dona ou ADMIN | 200 / 403 / 404 |
| | `PUT /sos-alerts/{id}` | ADMIN | 200 / 400 / 403 / 404 |
| Locais perigosos | `POST /dangerous-locations` | CITIZEN | 201 / 400 |
| | `GET /dangerous-locations` | autenticada | 200 |
| | `DELETE /dangerous-locations/{id}` | dona ou ADMIN | 204 / 403 / 404 |
| Sensores (IoT) | `POST /sensors` | ADMIN | 201 / 400 / 403 |
| | `GET /sensors` | autenticada | 200 |
| | `GET /sensors/{id}/readings` | autenticada | 200 / 404 |
| | `POST /sensors/{id}/readings` | SENSOR (X-API-Key) ou ADMIN | 201 / 400 / 404 |
| Rota | `GET /safe-route?originLat=&originLng=&destLat=&destLng=` | autenticada | 200 / 400 |

## Regras de negócio

### Cadastro e usuárias
- O e-mail é normalizado (minúsculas, sem espaços) e **não pode se repetir** (409).
- Na troca de senha, a senha atual é conferida contra o hash antes de gravar a nova.
- Ao apagar uma usuária, os contatos dela são removidos junto. Se ela tiver alertas SOS ou locais vinculados, o banco recusa e a API devolve **409**, porque alertas são registros históricos.

### SOS
`POST /sos-alerts` grava o pedido de socorro da usuária logada com a localização e o status `OPEN`. Isso **simula** o acionamento da central, sem integração com serviços reais (190, 180). A central (ADMIN) acompanha o atendimento alterando o status: `OPEN` → `IN_PROGRESS` → `CLOSED`.

### Locais perigosos
Qualquer cidadã reporta um local (tipo de até 30 e descrição de até 300 caracteres, mais as coordenadas). Os locais ativos alimentam o cálculo de risco das rotas. Só a dona ou um ADMIN remove um local.

### Simulação de IoT
Cada leitura enviada para `POST /sensors/{id}/readings` é validada e avaliada conforme o tipo do sensor:

| Tipo | Valor aceito | Quando gera `alert: true` |
|---|---|---|
| `CAMERA` | 0 ou 1 (1 = online) | valor 0 (câmera offline); o sensor passa a `active = false` |
| `LIGHTING` | 0 a 100 (%) | abaixo de 20% |
| `MOTION` | 0 ou 1 | valor 1 (movimento detectado) |

Valores fora da faixa do tipo são rejeitados. A leitura fica gravada e a resposta traz o campo `alert`. O simulador de IoT autentica com o header `X-API-Key`, e o ADMIN também pode enviar leituras.

### Rota segura
`GET /safe-route` avalia o risco do trajeto por uma regra simples, **sem machine learning**:

1. Amostra 20 pontos ao longo da linha entre origem e destino.
2. Em cada ponto, procura locais perigosos e sensores inativos num raio de **150 m** (distância pela fórmula de Haversine).
3. Soma o risco: **+1** por local perigoso e **+2** por sensor inativo.
4. Classifica: `LOW` (risco 0), `MEDIUM` (até 10) ou `HIGH` (acima de 10).

Resposta de exemplo:

```json
{
  "riskPoints": 4,
  "totalRisk": 6,
  "level": "MEDIUM",
  "note": "Estimated risk along the direct path, considering reported locations and failed sensors."
}
```

## Segurança — o que foi implementado

| Prática | Onde está no código |
|---|---|
| **Senha com hash BCrypt**, nunca em texto puro | `PasswordEncoderConfig`, `UserService`, `AdminSeeder` |
| **Login seguro**: mensagem genérica ("Invalid email or password") e tempo de resposta igual para e-mail inexistente | `AuthService` |
| **Autenticação stateless com JWT** assinado e com expiração | `JwtService`, `JwtAuthFilter` |
| **Controle de acesso por perfil** (ADMIN, CITIZEN, SENSOR) | `@PreAuthorize` nos controllers, `SecurityConfig` |
| **Controle de posse**: cada usuária só acessa os próprios dados (403 caso contrário) | `AccessRules`, services |
| **Identidade vem do token**, não do corpo da requisição | `AuthUser` (`@AuthenticationPrincipal`) |
| **Validação de entrada** em todos os endpoints | Bean Validation nos DTOs, `GlobalExceptionHandler` |
| **Proteção contra SQL Injection** | Spring Data JPA (consultas parametrizadas) |
| **Proteção contra XSS**: textos livres são escapados antes de salvar | `InputSanitizer` |
| **Limite de tentativas de login**: 3 falhas bloqueiam o e-mail por 5 minutos (429) | `LoginAttemptService` |
| **Segredos em variáveis de ambiente** (JWT, admin, chave de IoT) | `application.properties` |
| **CORS restrito** às origens configuradas | `SecurityConfig` |
| **Chave de API** do IoT comparada em tempo constante e aceita só para enviar leituras | `ApiKeyAuthFilter`, `IotKeyService` |
| **Respostas sem dados sensíveis** (nunca devolvem a senha nem o hash) | pacote `dto` |
| **Cabeçalhos de segurança** padrão do Spring Security | `SecurityConfig` |

Observações: em produção o tráfego deve passar por **HTTPS** (certificado no servidor ou em proxy reverso) e o Swagger deve ser desativado ou protegido. O console do H2 só existe no perfil `dev`.

## Erros

Todos os erros seguem o mesmo formato:

```json
{ "timestamp": "2026-11-16T10:00:00", "status": 403, "error": "Access denied" }
```

Erros de validação trazem também a lista de campos inválidos (`fields`).

| Status | Quando |
|---|---|
| 400 | Dados inválidos, requisição mal formada ou regra violada (ex.: senha atual incorreta) |
| 401 | Sem token, token inválido/expirado, ou e-mail/senha incorretos |
| 403 | Autenticada, mas sem permissão para o recurso |
| 404 | Recurso não encontrado |
| 409 | E-mail já cadastrado, ou exclusão de usuária com registros vinculados |
| 429 | Muitas tentativas de login |

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

Acionar SOS (`POST /sos-alerts`, com o token):

```json
{ "latitude": -23.5505, "longitude": -46.6333 }
```

Reportar local perigoso (`POST /dangerous-locations`):

```json
{ "type": "ILUMINACAO", "description": "Rua sem iluminação à noite", "latitude": -23.5614, "longitude": -46.6559 }
```

Cadastrar sensor (`POST /sensors`, como ADMIN):

```json
{ "type": "CAMERA", "latitude": -23.5614, "longitude": -46.6559 }
```

Enviar leitura (`POST /sensors/1/readings`, header `X-API-Key` ou token de ADMIN):

```json
{ "value": 0 }
```


