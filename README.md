# Desafio Stefanini — Consulta CEP

> **Resumo:** Aplicação Spring Boot que consulta endereços por CEP (via API externa mockada com WireMock) e persiste logs das consultas em um banco simulado da AWS (DynamoDB via Localstack). Projeto demonstrativo para processo seletivo — foco em clareza arquitetural, testes locais reprodutíveis e aplicação de princípios SOLID.

---

## Índice
- [1. O que a aplicação faz](#1-o-que-a-aplicação-faz)
- [2. Tecnologias e ferramentas](#2-tecnologias-e-ferramentas)
- [3. Estrutura do projeto (arquivos principais)](#3-estrutura-do-projeto)
- [4. Arquitetura e responsabilidades](#4-arquitetura-e-responsabilidades)
- [5. Modelo de dados](#5-modelo-de-dados)
- [6. Configurações importantes](#6-configurações-importantes)
- [7. Docker / Infra local (WireMock + Localstack)](#7-docker--infra-local)
- [8. DynamoDB / criação de tabela (Localstack)](#8-criar-tabela-dynamodb-localstack)
- [9. Como executar localmente (passo a passo)](#9-como-executar-localmente-passo-a-passo)
- [10. Exceções e tratamento de erros](#10-exceções-e-tratamento-de-erros)
- [11. Estratégias e decisões técnicas](#11-estratégias-e-decisões-técnicas)
- [12. Boas práticas para a apresentação](#12-próximos-passos--melhorias)

---

## 1. O que a aplicação faz

* Expõe um endpoint REST: `GET /api/v1/ceps/{cep}`.
* Valida o CEP (formato: 8 dígitos numéricos).
* Consulta uma API externa de CEP (mockada localmente com WireMock ou real ViaCEP em produção) usando **RestClient**.
* Persiste um log da consulta no **DynamoDB** (simulado via Localstack): inclui id, cep, response JSON, dataHora.
* Retorna para o cliente o endereço ou um erro padronizado via JSON.

---

## 2. Tecnologias e ferramentas

* Java 17+ (compatível com 11+)
* Spring Boot 3.x
* Spring RestClient (API síncrona)
* Docker (WireMock + Localstack)
* WireMock (mock da API de CEP)
* Localstack (simula serviços AWS — DynamoDB)
* AWS SDK v2 (DynamoDbClient com `endpointOverride` apontando para Localstack)
* Maven
* Logback (logging)
* Lombok
* cURL / Postman para testes

---

## 3. Estrutura do projeto

```
consulta-cep/
 ├── docker-compose.yml
 ├── wiremock/
 │    └── mappings/cep.json
 ├── src/
 │   └── main/
 │       ├── java/br/com/stefanini/consultacep/
 │       │   ├── StefaniniConsultaCepApplication.java
 │       │   │── client /
 │       │   │    └── CepClient.java
 │       │   ├── config/
 │       │   │    └── DynamoDBConfig.java
 │       │   ├── controller/
 │       │   │    └── CepController.java
 │       │   ├── dto/
 │       │   │    └── EnderecoResponseDTO.java
 │       │   ├── exception/
 │       │   │    ├── response/
 │       │   │    │     ├── BaseExceptionResponse.java
 │       │   │    │     └── GlobalExceptionHandler.java
 │       │   │    ├── CepNotFoundException.java
 │       │   ├── service/
 │       │   │    ├── interfaces/ 
 │       │   │    │     └── CepService.java
 │       │   │    └── CepServiceImpl.java
 │       └── resources/
 │            ├── application.yml
 └── README.md
```

---

## 4. Arquitetura e responsabilidades

**Controller (CepController)**

* Recebe a requisição HTTP, valida o `cep` (path variable) e delega ao Service.
* Não contém lógica de negócio.

**Service (CepService / CepServiceImpl)**

* Orquestra a validação, chamada ao client externo, transformação, criação do `LogConsulta` e persistência em DynamoDB.
* Lança exceções de negócio quando necessário (ex.: CEP inválido, não encontrado).

**Client (CepClient)**

* Abstrai a integração HTTP com o serviço de CEP (usa RestClient configurado).
* Torna simples trocar a implementação sem impactar a lógica do Service.

**Repository / AWS SDK**

* Opera a persistência no DynamoDB via AWS SDK v2 (DynamoDbClient)

**GlobalExceptionHandler**

* Captura `ApiException` e outras exceções, retornando um `ErrorResponse` padronizado (id, cep, response JSON, dataHora).

---

## 5. Modelo de dados

### LogConsulta (armazenado no DynamoDB)

* `id` (PK) — UUID
* `cep` — string (8 dígitos)
* `resposta` — string (JSON serializado da resposta do CEP)
* `dataHora` — ISO string

### EnderecoResponseDTO (DTO)

* `cep`, `logradouro`, `bairro`, `localidade`, `uf`, etc.

---

## 6. Configurações importantes

### application.yml (exemplo)

```yaml
cep:
  api:
    base-url: http://localhost:8081  
aws:
  dynamodb:
    endpoint: http://localhost:4566
    region: sa-east-1
    accessKey: test
    secretKey: test

server:
  port: 8080
```

* `region` é livre para testes (Localstack aceita a maioria das regiões); usei `sa-east-1` por convenção. Para demonstrar, você pode trocar para `us-east-1`.

---

## 7. Docker / Infra local

### docker-compose.yml (resumo)

```yaml
version: "3.8"
services:
  wiremock:
    image: wiremock/wiremock:latest
    container_name: wiremock-cep
    ports:
      - "8081:8080"
    volumes:
      - ./wiremock:/home/wiremock

  localstack:
    image: localstack/localstack:latest
    container_name: localstack
    ports:
      - "4566:4566"
    environment:
      - SERVICES=dynamodb
      - DATA_DIR=/tmp/localstack/data
    volumes:
      - ./localstack:/tmp/localstack
```

**Observações:**

* `./wiremock:/home/wiremock` monta os stubs locais no container WireMock para carga automática.

---

## 8. Criar tabela DynamoDB (Localstack)

Com Localstack rodando, execute o comando abaixo (AWS CLI):

```bash
aws --endpoint-url=http://localhost:4566 dynamodb create-table --table-name log_consulta --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --billing-mode PAY_PER_REQUEST
```

---

## 9. Como executar localmente (passo a passo)

1. Subir infra local:

   ```bash
   docker-compose up -d
   ```
2. Criar tabela DynamoDB (via AWS CLI apontando para Localstack) — conforme comando acima.
3. Rodar a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   ou

   ```bash
   ./mvnw clean package
   java -jar target/consulta-cep-0.0.1-SNAPSHOT.jar
   ```
4. Testar endpoint (exemplo):
   ```bash
   curl -v http://localhost:8079/api/ceps/01001000
   ```
   ```bash
   curl -v http://localhost:8079/api/ceps/30140071
   ```
   (Chamada 404 NotFound)
   ```bash
   curl -v http://localhost:8079/api/ceps/20040002
   ```
5. Conferir itens no Localstack:

   ```bash
   aws --endpoint-url=http://localhost:4566 dynamodb scan --table-name log_consulta
   ```

---

## 10. Exceções e tratamento de erros

### Exceção personalizada (exemplos)

* `GlobalExceptionHandler` (tratando a exceção)
* `CepNotFoundException` (extends `GlobalExceptionHandler` com `NOT_FOUND`)

### Handler global

* `@RestControllerAdvice` que mapeia exceções para `ErrorResponse` (id, cep, response JSON, dataHora).

---

## 11. Estratégias e decisões técnicas

* **RestClient**: simples, síncrono, fluente — adequado para esse fluxo bloqueante.
* **Localstack + DynamoDB**: permite demonstrar integração com AWS sem custos reais.
* **WireMock em Docker**: garante mocks reprodutíveis; diretório montado facilita edição local.
* **Logs no DB**: salva resposta completa e tempo de execução para auditoria e diagnóstico.
* **SOLID**: separação clara entre controller, service, client e persistence; service depende de abstrações.


---

## 12. Próximos passos / melhorias

* Criar testes de integração que usem Localstack e WireMock no pipeline CI.

