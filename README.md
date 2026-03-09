<div align="center">

<img src="https://raw.githubusercontent.com/mamadu-sama/Plataforma-de-Pagamentos/main/assets/logo.png" alt="FinTouch Ledger" width="120" />

# 💰 FinTouch Ledger

### API de Pagamentos Simplificada,Robusta, Confiável, Financeiramente Precisa.

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)
[![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

[Sobre o Projeto](#-sobre-o-projeto) •
[Arquitetura](#-arquitetura-e-decisões-técnicas) •
[Tecnologias](#-tecnologias) •
[Como Rodar](#-como-rodar) •
[Endpoints](#-endpoints) •
[Testes](#-testes) •
[Contato](#-autor)

</div>

---

## 📌 Sobre o Projeto

O **FinTouch Ledger** é uma implementação de um **Livro Razão (Ledger)** para uma plataforma de pagamentos simplificada. O projeto expõe uma API RESTful que permite transferências de valores entre usuários **comuns** e **lojistas**, com foco total em:

- ✅ **Consistência de dados** em cenários de alta concorrência
- ✅ **Atomicidade** nas operações financeiras (tudo ou nada)
- ✅ **Precisão numérica** para evitar perda de centavos
- ✅ **Resiliência** frente a falhas de serviços externos

> "Em sistemas financeiros, a lógica errada não gera um bug, gera prejuízo real. Este projeto foi desenhado com esse nível de responsabilidade em mente."

---

## 🏗️ Arquitetura e Decisões Técnicas

### 1. 🔒 Controle de Concorrência : Pessimistic Locking

O maior risco em qualquer sistema de pagamentos é a **race condition**: dois saques simultâneos que juntos ultrapassam o saldo disponível (o clássico problema do *double spending*).

**Solução adotada:** `SELECT FOR UPDATE` (Pessimistic Lock via JPA)

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM Wallet w WHERE w.id = :id")
Optional<Wallet> findByIdWithLock(@Param("id") Long id);
```

**Por quê bloqueio pessimista?**
Em cenários financeiros, o custo de uma inconsistência é muito maior do que o custo do bloqueio. Enquanto uma transação lê e atualiza um saldo, nenhuma outra operação consegue interferir naquele registro até o `COMMIT`, eliminando a possibilidade de saldo negativo por concorrência.

---

### 2. ⚛️ Atomicidade: Transações ACID

Todo o fluxo de transferência é encapsulado em uma única transação com `@Transactional`. Isso garante que, se qualquer etapa do processo falhar (ex: timeout na chamada ao autorizador externo), o Spring Data JPA executa o **rollback automático**, deixando o Ledger exatamente no estado anterior.

```
Débito do Pagador ──┐
                    ├──▶ @Transactional ──▶ COMMIT ✅
Crédito do Recebedor┘                  └──▶ ROLLBACK ↩️ (em caso de falha)
```

---

### 3. 🔢 Precisão Numérica: `BigDecimal`

Valores monetários **nunca** são manipulados com `double` ou `float`. A razão é simples:

```java
// ❌ Imprecisão com float/double
double a = 0.1 + 0.2; // Resultado: 0.30000000000000004

// ✅ Precisão garantida com BigDecimal
BigDecimal a = new BigDecimal("0.10").add(new BigDecimal("0.20")); // Resultado: 0.30
```

Cada centavo é contabilizado corretamente, usando `BigDecimal` com escala e arredondamento explícitos em todas as operações.

---

### 4. 🔔 Resiliência em Notificações

O serviço de notificação (ex: envio de e-mail/SMS) é tratado como um componente **não-crítico**. Sua falha **não cancela** a transação financeira, que já foi concluída com sucesso.

```java
try {
    notificationService.notify(transaction);
} catch (NotificationException e) {
    log.warn("Falha ao enviar notificação para transação {}: {}", transaction.getId(), e.getMessage());
    // A transação permanece válida, apenas o aviso é logado.
}
```

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.x |
| Persistência | Spring Data JPA / Hibernate |
| Banco de Dados | PostgreSQL |
| Documentação | Swagger / OpenAPI 3 |
| Containerização | Docker & Docker Compose |
| Testes | JUnit 5, Mockito, RestAssured |

---

## 🚀 Como Rodar

> **Pré-requisito:** Apenas o [Docker](https://docs.docker.com/get-docker/) precisa estar instalado.

**1. Clone o repositório**
```bash
git clone https://github.com/mamadu-sama/Plataforma-de-Pagamentos.git
cd Plataforma-de-Pagamentos
```

**2. Suba o ambiente completo (App + Banco de Dados)**
```bash
docker-compose up -d
```

A aplicação estará disponível em → `http://localhost:8080`

**3. Explore a documentação interativa (Swagger UI)**

Acesse `http://localhost:8080/swagger-ui.html` para visualizar e testar todos os endpoints diretamente pelo browser.

---

## 📡 Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/transaction` | Realiza uma transferência entre usuários |
| `GET` | `/wallet/{id}` | Consulta saldo e dados de uma carteira |
| `POST` | `/users` | Cadastra um novo usuário (Comum ou Lojista) |

### Exemplo de Requisição — Transferência

```json
POST /transaction
{
  "value": 150.00,
  "payer": 4,
  "payee": 15
}
```

### Regras de Negócio
- 🚫 **Lojistas não podem ser pagadores** , apenas recebem transferências.
- 🚫 **Saldo insuficiente** bloqueia a transação antes de qualquer operação no banco.
- ✅ A transferência é **validada por um serviço autorizador externo** antes de ser concluída.

---

## 🧪 Testes

A estratégia de testes cobre tanto o caminho feliz quanto, principalmente, os **cenários de exceção** ,  que são onde os bugs financeiros realmente moram.

| Tipo | Cobertura |
|---|---|
| **Unitários** | Regras de negócio isoladas (ex: validação de tipo de usuário, saldo insuficiente) |
| **Integração** | Fluxo completo de transferência com banco de dados real via RestAssured |

**Para rodar os testes:**
```bash
./mvnw test
```

---

## 👤 Autor

<div align="center">

**Mamadú Sama**

Desenvolvedor Backend com foco em **Engenharia de Software** e **Sistemas Financeiros Robustos**.

[![Email](https://img.shields.io/badge/Email-mamadusama19%40gmail.com-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:mamadusama19@gmail.com)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/mamadusama)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mamadu-sama)

*Este projeto faz parte do meu portfólio pessoal focado em backend e sistemas de alta confiabilidade.*

</div>

---

<div align="center">
<sub>Feito com ☕ e muita atenção aos detalhes por Mamadú Sama</sub>
</div>
