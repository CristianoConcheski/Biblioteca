# Sistema de Biblioteca (Java + JavaFX)

Aplicação desktop com quatro telas:

| Tela | O que faz |
|------|-----------|
| **Login** | Autenticação de usuário (senha guardada como hash SHA-256) |
| **Empréstimos e devoluções** | Registra empréstimos com prazo, lista em aberto/atrasados/devolvidos, registra devolução e calcula multa |
| **Clientes** | Cadastro, edição, exclusão e busca por nome/CPF |
| **Acervo** | Cadastro, edição, exclusão e busca de livros, com controle de exemplares |

**Primeiro acesso:** usuário `admin`, senha `admin123`.

## Requisitos

- JDK 25 (o projeto compila com `release 25` e usa JavaFX 25)
- Maven 3.9+

## Como executar

```bash
mvn javafx:run
```

Para gerar o pacote: `mvn package` (o JavaFX é baixado automaticamente pelo Maven).

## Regras de negócio

- Prazo padrão de empréstimo: 14 dias (ajustável por empréstimo)
- Máximo de 3 empréstimos simultâneos por cliente
- Cliente com livro em atraso não pode pegar novo empréstimo
- Multa de R$ 1,00 por dia de atraso (`Emprestimo.MULTA_POR_DIA`)
- Não é possível excluir cliente ou livro com empréstimo em aberto
- CPF validado (11 dígitos) e único; ISBN único quando informado

## Dados

Tudo é salvo automaticamente em `~/.biblioteca/dados.bin` (pasta do usuário). Para zerar o sistema, basta apagar esse arquivo.

## Estrutura

```
src/main/java/com/biblioteca/
  Main.java                  ponto de entrada e navegação entre telas
  model/                     Usuario, Cliente, Livro, Emprestimo
  data/Biblioteca.java       regras de negócio e persistência
  ui/                        LoginView, MainView, EmprestimosView, ClientesView, AcervoView
src/main/resources/estilo.css
```
