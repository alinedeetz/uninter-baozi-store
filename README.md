# Baozi Store

API REST para gerenciar clientes, produtos e pedidos de uma loja de pãezinhos chineses (*baozi*). Projeto acadêmico da disciplina Desenvolvimento Web Back-End.

Feita em Java 25 com Spring Boot 4.1.1, Spring Data JPA e banco H2.

## Requisitos

- JDK 25 instalado (`java -version`).

Não é preciso instalar Maven nem banco de dados: o projeto traz o Maven Wrapper e usa o H2 embutido.

## Como rodar

No Windows (PowerShell ou Prompt):

```
.\mvnw.cmd spring-boot:run
```

No Linux, macOS ou Git Bash:

```
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. A primeira execução baixa as dependências e demora mais.

### Com make (opcional)

Quem tem o `make` instalado pode usar os atalhos do `Makefile`:

| Comando | O que faz |
| ------- | --------- |
| `make run` | Sobe a API |
| `make test` | Roda todos os testes |
| `make build` | Gera o jar em `target/` |
| `make run-jar` | Gera o jar e sobe a API a partir dele |
| `make clean` | Apaga a pasta `target/` |
| `make reset-db` | Apaga o banco H2 local (pare a aplicação antes) |

## Banco de dados

Os dados ficam no arquivo `./data/baozistore.mv.db`, criado na primeira execução, e continuam lá depois de reiniciar a aplicação. Para começar do zero, pare a aplicação e apague a pasta `data`.

O console web do H2 fica em `http://localhost:8080/h2-console`:

| Campo | Valor |
| ----- | ----- |
| JDBC URL | `jdbc:h2:file:./data/baozistore` |
| User Name | `sa` |
| Password | (vazio) |

As tabelas são `CLIENTE`, `PRODUTO` e `PEDIDO`. A tabela `PEDIDO` tem chaves estrangeiras para as outras duas.

O arquivo do H2 aceita uma aplicação por vez: não rode duas instâncias ao mesmo tempo.

## Endpoints

| Método | Endpoint | Descrição | Sucesso |
| ------ | -------- | --------- | ------- |
| POST | `/clientes` | Cadastrar cliente | 201 |
| GET | `/clientes` | Listar clientes | 200 |
| GET | `/clientes/{id}` | Consultar cliente | 200 |
| PUT | `/clientes/{id}` | Atualizar cliente | 200 |
| DELETE | `/clientes/{id}` | Excluir cliente | 204 |
| POST | `/produtos` | Cadastrar produto | 201 |
| GET | `/produtos` | Listar produtos | 200 |
| GET | `/produtos/{id}` | Consultar produto | 200 |
| PUT | `/produtos/{id}` | Atualizar produto | 200 |
| DELETE | `/produtos/{id}` | Excluir produto | 204 |
| POST | `/pedidos` | Registrar pedido | 201 |
| GET | `/pedidos` | Listar pedidos | 200 |
| GET | `/pedidos/{id}` | Consultar pedido | 200 |
| PUT | `/pedidos/{id}` | Atualizar pedido | 200 |
| DELETE | `/pedidos/{id}` | Excluir pedido | 204 |

### Corpo das requisições

Cliente (`clienteDesde` é opcional; sem ele, vale a data de hoje):

```json
{
  "nome": "Aline dos Santos Mattos 5497233",
  "clienteDesde": "2026-10-12"
}
```

Produto (`estoque` indica disponibilidade, não quantidade):

```json
{
  "nome": "Baozi Tradicional",
  "preco": 12.50,
  "estoque": true
}
```

Pedido:

```json
{
  "clienteId": 1,
  "produtoId": 1,
  "quantidade": 3
}
```

### Regras e erros

| Situação | Status |
| -------- | ------ |
| Nome vazio, preço ausente ou negativo, `estoque` ausente, quantidade ausente ou menor que 1, `clienteId` ou `produtoId` ausente | 400 |
| JSON malformado ou `{id}` não numérico | 400 |
| ID inexistente, inclusive o `clienteId` ou `produtoId` de um pedido | 404 |
| Excluir cliente ou produto que tem pedidos | 409 |
| Pedido de produto com `estoque: false` | 409 |

Os erros seguem o formato `ProblemDetail` (RFC 9457):

```json
{
  "detail": "Um ou mais campos estão inválidos",
  "instance": "/clientes",
  "status": 400,
  "title": "Dados inválidos",
  "erros": [
    { "campo": "nome", "mensagem": "não pode ser vazio" }
  ]
}
```

## Postman

A coleção está no formato YAML v3 do Postman, em `postman/postman/collections/Baozi Store/`, com um arquivo por requisição. Para usá-la, abra a pasta `postman/` deste repositório como workspace local no Postman: a coleção "Baozi Store" aparece no painel de coleções.

São 15 requisições em quatro pastas, que devem ser executadas na ordem: os POST guardam os IDs criados em variáveis, e as exclusões ficam por último (pedido, produto, cliente) porque cliente e produto com pedido não podem ser excluídos.

As variáveis da coleção já vêm preenchidas: `baseUrl` aponta para `http://localhost:8080` e os três IDs começam em 1.

## Testes

```
.\mvnw.cmd verify
```

São 116 testes: unidade nos services (Mockito) e integração nos endpoints e no repository (MockMvc e H2 em memória). Os testes não tocam no arquivo `./data`.

## Estrutura

```
src/main/java/com/baozistore/
├── BaoziStoreApplication.java
├── model/        entidades JPA: Cliente, Produto, Pedido
├── repository/   interfaces Spring Data JPA
├── service/      regras de negócio
├── controller/   endpoints REST
├── dto/          entidades de apresentação (Request e Response)
└── exception/    exceções e tratamento global de erros
```

As entidades JPA não aparecem no JSON: cada recurso tem um record `Request` e um `Response`, e a conversão é feita no controller.
