# Atalhos para rodar a Baozi Store. Use "make" ou "make help" para ver os comandos.

ifeq ($(OS),Windows_NT)
SHELL := cmd.exe
MVNW := mvnw.cmd
RM_DATA := if exist data rmdir /s /q data
else
MVNW := ./mvnw
RM_DATA := rm -rf data
endif

JAR := target/baozi-store-0.0.1-SNAPSHOT.jar

.DEFAULT_GOAL := help
.PHONY: help run test build run-jar clean reset-db

help:
	@echo make run       - sobe a API em http://localhost:8080
	@echo make test      - roda todos os testes
	@echo make build     - gera o jar em target/
	@echo make run-jar   - gera o jar e sobe a API a partir dele
	@echo make clean     - apaga a pasta target/
	@echo make reset-db  - apaga o banco H2 local (pasta data/)

run:
	$(MVNW) spring-boot:run

test:
	$(MVNW) verify

build:
	$(MVNW) -DskipTests package

run-jar: build
	java -jar $(JAR)

clean:
	$(MVNW) clean

# Pare a aplicação antes: o H2 mantém o arquivo aberto enquanto ela roda
reset-db:
	$(RM_DATA)
