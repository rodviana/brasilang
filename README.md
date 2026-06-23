# Brasilang

**Compilador** da linguagem **Brasilang** — sintaxe em português com pipeline completo de compilação, desenvolvido na **PUC Goiás** (disciplina de Compiladores).

| | |
|---|---|
| **Stack** | Java 21 · Spring Boot 3.5 · Maven |
| **Autor** | Rodrigo Viana Quirino |
| **Repositório** | [github.com/rodviana/brasilang](https://github.com/rodviana/brasilang) |

## O que é

Brasilang é uma linguagem de programação com palavras-chave em português (`inteiro`, `funcao`, `se`, `enquanto`, `imprima`, etc.). Este projeto implementa:

| Fase | Componentes |
|------|-------------|
| **Léxica** | `Lexer`, `Token`, `EnumTokenType` |
| **Sintática** | AST — `Program`, `FunctionDeclaration`, `ConditionalStruct`, `RepetitionStruct`, expressões |
| **Semântica** | `SemanticAnalyzer` — tipos, escopos, validações |
| **Tradução** | `TranslateService`, `CodeBuilder` — geração de código C |
| **Otimização** | `DeadCodeEliminationService`, `InlineOptimizerService`, `PrintOptimizerService` |

## Pipeline

```
Código Brasilang (.br / string)
    → LexerService (tokens)
    → SintaxeService (AST)
    → SemanticAnalyzer
    → OptimizerService (opcional)
    → TranslateService (código C)
```

## Exemplo

```brasilang
inteiro n = 5;
funcao inteiro fatorial(inteiro n) {
    se (n <= 1) { retorne 1; }
    senao { retorne n * fatorial(n - 1); }
}
imprima(fatorial(n));
```

O `BrasilangApplication` executa um programa de demonstração no `@PostConstruct` com tipos, arrays, funções recursivas e estruturas de controle.

## Estrutura do projeto

```
src/main/java/br/edu/pucgoias/brasilang/
├── model/lexico/       # Análise léxica
├── model/sintaxe/      # AST (statements + expressions)
├── model/optimizer/    # Parâmetros de otimização
├── service/            # Lexer, Sintaxe, Semântica, Tradução, Otimizadores
└── exception/          # LexicalException, SyntaxException, SemanticException
```

## Como executar

```bash
./mvnw spring-boot:run
```

A saída exibe tokens, AST, análise semântica e código C gerado no console.

## Relacionado

- [puc-compiladores](https://github.com/rodviana/puc-compiladores) — mini-compilador em Python (atividade acadêmica)
- [puc-linguagens-formais-automatos](https://github.com/rodviana/puc-linguagens-formais-automatos) — autômatos e gramáticas (LFA)
