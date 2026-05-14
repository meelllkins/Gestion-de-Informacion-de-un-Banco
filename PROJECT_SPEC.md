# PROJECT_SPEC.md — Aplicación de Gestión de Información Bancaria

> **Propósito:** Fuente de verdad técnica para auditorías, generación de pruebas automatizadas y validación de requisitos. Este documento permite que cualquier agente de IA o revisor humano evalúe el proyecto sin necesidad de leer el enunciado original.
>
> **Versión del análisis:** 2026-05-14  
> **Evaluaciones previas:** EVALUACION.md / EVALUACION2.md (1.90/5.0)

---

## Índice

1. [Objetivos del Proyecto](#1-objetivos-del-proyecto)
2. [Tecnologías y Arquitectura Detectadas](#2-tecnologías-y-arquitectura-detectadas)
3. [Estructura de Carpetas](#3-estructura-de-carpetas)
4. [Módulos y Clases Existentes](#4-módulos-y-clases-existentes)
5. [Requisitos Funcionales](#5-requisitos-funcionales)
6. [Reglas de Negocio](#6-reglas-de-negocio)
7. [Validaciones Requeridas](#7-validaciones-requeridas)
8. [Restricciones por Rol](#8-restricciones-por-rol)
9. [Clases Obligatorias (Especificación)](#9-clases-obligatorias-especificación)
10. [Métodos Obligatorios (Especificación)](#10-métodos-obligatorios-especificación)
11. [Manejo de Errores](#11-manejo-de-errores)
12. [Requisitos de Base de Datos](#12-requisitos-de-base-de-datos)
13. [Requisitos de Interfaz](#13-requisitos-de-interfaz)
14. [Casos Límite (Edge Cases)](#14-casos-límite-edge-cases)
15. [Criterios de Evaluación](#15-criterios-de-evaluación)
16. [Checklist de Cumplimiento](#16-checklist-de-cumplimiento)
17. [Posibles Puntos de Fallo](#17-posibles-puntos-de-fallo)
18. [Casos de Prueba Sugeridos](#18-casos-de-prueba-sugeridos)
19. [Módulos Incompletos o en Riesgo](#19-módulos-incompletos-o-en-riesgo)
20. [Ambigüedades del Enunciado](#20-ambigüedades-del-enunciado)

---

## 1. Objetivos del Proyecto

| # | Objetivo | Descripción |
|---|----------|-------------|
| O-01 | Core Transaccional | Sistema para gestión de clientes, productos y operaciones bancarias |
| O-02 | Seguridad y Escalabilidad | Diseño robusto bajo normativas de negocio |
| O-03 | Persistencia Híbrida | Manejo de modelos de datos relacionales (SQL) y no relacionales (NoSQL) |
| O-04 | Segregación de Funciones | Control de acceso basado en roles (RBAC) |

---

## 2. Tecnologías y Arquitectura Detectadas

### Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje principal |
| Spring Boot | 4.0.3 | Framework principal |
| Spring Data JPA | Managed | Persistencia SQL (ORM) |
| Spring Data MongoDB | Managed | Persistencia NoSQL (Bitácora) |
| Spring Security | Managed | Autenticación y autorización |
| JJWT | 0.12.6 | Generación y validación de tokens JWT |
| MySQL | Latest | Base de datos relacional (datos transaccionales) |
| MongoDB | Latest | Base de datos documental (bitácora inmutable) |
| Lombok | Latest | Generación de código boilerplate |
| Maven | 3.8+ | Herramienta de build |

### Patrón Arquitectónico

El proyecto implementa **Arquitectura Hexagonal (Ports & Adapters)**:

```
┌─────────────────────────────────────────────────────────┐
│                    API Layer (REST)                      │
│  Controllers → Request DTOs → Response DTOs             │
├─────────────────────────────────────────────────────────┤
│                  Application Layer                       │
│  Use Cases → Register* (casos de uso por rol)           │
├─────────────────────────────────────────────────────────┤
│                   Domain Layer                           │
│  Models → Service Interfaces → Service Implementations  │
│  Ports (interfaces de repositorio)                      │
├─────────────────────────────────────────────────────────┤
│               Infrastructure Layer                       │
│  JPA Entities → Persistence Adapters → Repositories     │
│  MongoDB Documents → Mongo Adapters → Mongo Repositories│
│  Security (JWT, SecurityConfig, UserDetailsService)      │
└─────────────────────────────────────────────────────────┘
```

### Configuración de Base de Datos (`application.properties`)

```properties
# MySQL (datos transaccionales)
spring.datasource.url=jdbc:mysql://localhost:3306/banco
spring.jpa.hibernate.ddl-auto=update

# MongoDB (bitácora de auditoría)
spring.mongodb.uri=mongodb://localhost:27017/banco

# JWT
app.jwt.secret=bancoAppSecretKeyForJWTTokenGeneration2026012345
app.jwt.expiration=30000
```

---

## 3. Estructura de Carpetas

```
Gestion-de-Informacion-de-un-Banco/
├── Banco/
│   ├── pom.xml                                         ← Dependencias Maven
│   └── src/
│       ├── main/java/app/
│       │   ├── BancoApplication.java                   ← Punto de entrada Spring Boot
│       │   ├── EndpointLogger.java                     ← Logger de endpoints
│       │   ├── application/
│       │   │   ├── Register*.java (×7)                 ← Registro por rol
│       │   │   ├── adapters/
│       │   │   │   ├── api/
│       │   │   │   │   ├── controllers/ (×8 + handler) ← Controladores REST
│       │   │   │   │   ├── request/    (×8)            ← DTOs de entrada
│       │   │   │   │   └── response/   (×8)            ← DTOs de salida
│       │   │   │   └── persistance/
│       │   │   │       ├── sql/
│       │   │   │       │   ├── entities/      (×4)     ← JPA Entities
│       │   │   │       │   ├── repositories/  (×4)     ← JPA Repositories
│       │   │   │       │   └── *PersistenceAdapter (×4)← SQL Adapters
│       │   │   │       └── mongodb/
│       │   │   │           ├── documents/              ← LogRecordDocument
│       │   │   │           ├── repositories/           ← LogRecordMongoRepository
│       │   │   │           └── LogRecordPersistenceAdapter.java
│       │   │   └── usecases/ (×8)                      ← Casos de uso por rol
│       │   ├── domain/
│       │   │   ├── Exceptions/        (×2)             ← Excepciones de dominio
│       │   │   ├── models/
│       │   │   │   ├── *.java         (×9)             ← Modelos de dominio
│       │   │   │   └── enums/         (×9)             ← Enumeraciones
│       │   │   ├── ports/             (×5)             ← Interfaces de repositorio
│       │   │   └── services/
│       │   │       ├── interfaces/    (×6)             ← Interfaces de servicio
│       │   │       └── implementations/ (×10)          ← Implementaciones de servicio
│       │   └── infrastructure/
│       │       ├── DataSeeder.java                     ← Datos iniciales
│       │       ├── PersistenceConfig.java
│       │       └── security/
│       │           ├── JwtAuthenticationFilter.java
│       │           ├── JwtUtil.java
│       │           ├── SecurityConfig.java
│       │           └── UserDetailsServiceImpl.java
│       └── test/java/app/
│           └── BancoApplicationTests.java
├── EVALUACION.md                                       ← Primera evaluación
├── EVALUACION2.md                                      ← Segunda evaluación (1.90/5.0)
├── README.md
├── POSTMAN_COLLECTION_README.md
└── banco-controllers-validation.postman_collection.json
```

---

## 4. Módulos y Clases Existentes

### 4.1 Jerarquía de Modelos de Dominio

```
Person (abstract)
  ├── User            → UsuarioSistema del enunciado
  └── Customer (abstract)
      ├── IndividualCustomer   → ClientePersonaNatural
      └── CorporateCustomer    → ClienteEmpresa

GeneralBankProduct (abstract)
  ├── BankAccount     → CuentaBancaria
  └── Loan            → Prestamo

Transfer             → Transferencia (standalone)
LogRecord            → BitacoraOperaciones (standalone)
```

### 4.2 Enumeraciones

| Enum | Valores conocidos |
|------|-------------------|
| `SystemRole` | INDIVIDUAL_CUSTOMER, CORPORATE_CUSTOMER, TELLER_EMPLOYEE, COMMERCIAL_EMPLOYEE, CORPORATE_EMPLOYEE, CORPORATE_SUPERVISOR, INTERNAL_ANALYST |
| `UserStatus` | ACTIVE, INACTIVE, BLOCKED |
| `AccountStatus` | ACTIVE, BLOCKED, CANCELLED |
| `AccountType` | SAVINGS, CHECKING, PERSONAL, CORPORATE |
| `Currency` | (multimoneda mencionada, valores exactos a verificar) |
| `LoanStatus` | PENDING (En estudio), APPROVED, REJECTED, DISBURSED |
| `LoanType` | (a verificar en código) |
| `TransferStatus` | PENDING, WAITING_APPROVAL, EXECUTED, APPROVED, REJECTED, EXPIRED |
| `Category` | (a verificar en código) |

### 4.3 Puertos de Dominio (Interfaces de Repositorio)

| Puerto | Responsabilidad |
|--------|----------------|
| `IUserPort` | CRUD de usuarios/clientes |
| `IAccountPort` | CRUD de cuentas bancarias |
| `ILoanPort` | CRUD de préstamos |
| `ITransferPort` | CRUD de transferencias |
| `ILogPort` | Escritura en bitácora NoSQL |

### 4.4 Interfaces de Servicio de Dominio

| Interfaz | Responsabilidad |
|----------|----------------|
| `IAuthService` | Autenticación y generación de token |
| `IUserService` | Gestión de usuarios |
| `IAccountService` | Operaciones sobre cuentas |
| `ILoanService` | Flujo de préstamos |
| `ITransferService` | Flujo de transferencias |
| `ILogService` | Escritura en bitácora |

### 4.5 Implementaciones de Servicio

| Clase | Responsabilidad |
|-------|----------------|
| `AuthService` | Login, generación de JWT |
| `UserService` | Registro, búsqueda de usuarios |
| `LoanService` | Solicitar, aprobar, rechazar, desembolsar |
| `TransferService` | Crear, aprobar, expirar transferencias |
| `LogService` | Registrar en MongoDB |
| `LogOperation` | Helper para construir entradas de bitácora |
| `OpenAccount` | Apertura de cuenta |
| `FindAccountByNumber` | Búsqueda por número de cuenta |
| `GetAccountsByHolder` | Cuentas de un titular |
| `GetActiveAccount` | Filtrar cuentas activas |
| `Deposit` | Depósito en cuenta |
| `Withdraw` | Retiro de cuenta |

### 4.6 Casos de Uso por Rol (Application Layer)

| Caso de Uso | Rol Asociado |
|-------------|-------------|
| `AuthUseCase` | Todos (autenticación) |
| `IndividualCustomerUseCase` | INDIVIDUAL_CUSTOMER |
| `CorporateCustomerUseCase` | CORPORATE_CUSTOMER |
| `TellerEmployeeUseCase` | TELLER_EMPLOYEE |
| `CommercialEmployeeUseCase` | COMMERCIAL_EMPLOYEE |
| `CorporateEmployeeUseCase` | CORPORATE_EMPLOYEE |
| `CorporateSupervisorUseCase` | CORPORATE_SUPERVISOR |
| `InternalAnalystUseCase` | INTERNAL_ANALYST |

### 4.7 Controladores REST

| Controlador | Ruta Base (estimada) | Rol |
|-------------|---------------------|-----|
| `AuthController` | `/api/auth` | Público |
| `IndividualCustomerController` | `/api/individual-customer` | INDIVIDUAL_CUSTOMER |
| `CorporateCustomerController` | `/api/corporate-customer` | CORPORATE_CUSTOMER |
| `TellerEmployeeController` | `/api/teller` | TELLER_EMPLOYEE |
| `CommercialEmployeeController` | `/api/commercial` | COMMERCIAL_EMPLOYEE |
| `CorporateEmployeeController` | `/api/corporate-employee` | CORPORATE_EMPLOYEE |
| `CorporateSupervisorController` | `/api/supervisor` | CORPORATE_SUPERVISOR |
| `InternalAnalystController` | `/api/analyst` | INTERNAL_ANALYST |
| `GlobalExceptionHandler` | — | Manejo global de errores |

### 4.8 Esquema de Entidades SQL (JPA)

**`UserEntity` → tabla `users`**
```
id (PK), name, identification_id (UNIQUE), email, phone,
birth_date, address, system_role (enum), username (UNIQUE),
password, user_status (enum), related_id
```

**`BankAccountEntity` → tabla `bank_accounts`**
```
id (PK), account_number (UNIQUE), account_type (enum),
account_holder_id (FK → users), balance (double ⚠️),
currency (enum), account_status (enum), opening_date,
product_code, product_name, category (enum), requires_approval
```

**`LoanEntity` → tabla `loans`**
```
id (PK), loan_id (UNIQUE), loan_type (enum),
applicant_client_id (FK → users), requested_amount,
approved_amount, interest_rate, term_months, approval_date,
disbursement_date, destination_account (FK → bank_accounts),
loan_status (enum), product_code, product_name,
category (enum), requires_approval
```

**`TransferEntity` → tabla `transfers`**
```
id (PK), transfer_id (UNIQUE), source_account (FK → bank_accounts),
destination_account (FK → bank_accounts), amount, creation_date,
approval_date, transfer_status (enum),
creator_user_id (FK → users), approver_user_id (FK → users)
```

### 4.9 Documento MongoDB

**`LogRecordDocument` → colección `log_records`**
```json
{
  "_id": "ObjectId",
  "operationType": "string",
  "operationDateTime": "datetime",
  "userId": "string",
  "userRole": "string",
  "affectedProductId": "string",
  "detailData": { "campo_flexible": "valor" }
}
```

---

## 5. Requisitos Funcionales

### RF-01 — Gestión de Clientes

| ID | Descripción |
|----|-------------|
| RF-01-A | Registro de **ClientePersonaNatural** con validación de unicidad de número de identificación (DNI) |
| RF-01-B | Registro de **ClienteEmpresa** con validación de unicidad de NIT y datos de representante legal |
| RF-01-C | Un Empleado de Ventanilla puede registrar clientes nuevos durante la apertura de cuenta |
| RF-01-D | Los clientes no pueden ver información de otros clientes |

### RF-02 — Gestión de Cuentas

| ID | Descripción |
|----|-------------|
| RF-02-A | Apertura de cuentas tipo: ahorro, corriente, personal, empresarial |
| RF-02-B | No permitir apertura a usuarios con estado `INACTIVE` o `BLOCKED` |
| RF-02-C | Consulta de saldo disponible para el titular (cliente) y para Empleado de Ventanilla con identificación del cliente |
| RF-02-D | Las cuentas con estado `BLOCKED` o `CANCELLED` no pueden ser origen de transferencias |

### RF-03 — Gestión de Préstamos

| ID | Descripción |
|----|-------------|
| RF-03-A | Solicitud de préstamo iniciada por cliente o Empleado Comercial → estado inicial: `En estudio` (PENDING) |
| RF-03-B | Revisión y aprobación/rechazo exclusivamente por Analista Interno |
| RF-03-C | Desembolso de fondos tras aprobación: actualiza `Saldo_Actual` de la cuenta destino |
| RF-03-D | El desembolso solo es posible si la `Cuenta_Destino_Desembolso` existe y está en estado `ACTIVE` |
| RF-03-E | El Empleado Comercial puede ver el estado de las solicitudes que ha enviado |

### RF-04 — Gestión de Transferencias

| ID | Descripción |
|----|-------------|
| RF-04-A | Transferencias entre cuentas propias |
| RF-04-B | Transferencias a cuentas de terceros |
| RF-04-C | Pagos masivos/nómina para empresas (`crearTransferenciaEmpresa`) |
| RF-04-D | Las transferencias de empresa que superen un umbral predefinido pasan a estado `WAITING_APPROVAL` |
| RF-04-E | Aprobación de transferencias en espera exclusivamente por Supervisor de Empresa |
| RF-04-F | Las transferencias en `WAITING_APPROVAL` vencen automáticamente a los **60 minutos** sin aprobación |
| RF-04-G | Una transferencia vencida (`EXPIRED`) **no debe mover fondos** |
| RF-04-H | El Supervisor de Empresa no puede aprobar sus propias transferencias personales |

### RF-05 — Bitácora de Auditoría

| ID | Descripción |
|----|-------------|
| RF-05-A | Registro de toda operación significativa en MongoDB (NoSQL) |
| RF-05-B | La bitácora es **inmutable**: no puede ser modificada ni eliminada |
| RF-05-C | La bitácora **no se usa** para calcular saldos; solo para auditoría |
| RF-05-D | Cada entrada debe incluir: tipo de operación, fecha/hora, usuario, rol, producto afectado, detalle flexible (JSON) |
| RF-05-E | Los cambios de estado deben registrar tanto el estado anterior como el nuevo en `detailData` |
| RF-05-F | El vencimiento automático de una transferencia debe registrar el motivo del vencimiento en la bitácora |

---

## 6. Reglas de Negocio

| ID | Regla | Módulo Afectado |
|----|-------|----------------|
| RN-01 | Un préstamo solo puede transicionar de `PENDING` → `APPROVED` o `PENDING` → `REJECTED`. No existe transición `REJECTED` → `APPROVED`. | Préstamos |
| RN-02 | Las transferencias empresariales superiores al umbral predefinido entran en `WAITING_APPROVAL` automáticamente. | Transferencias |
| RN-03 | Una transferencia en `WAITING_APPROVAL` que no sea aprobada en 60 minutos pasa a `EXPIRED`. | Transferencias |
| RN-04 | Una transferencia `EXPIRED` no ejecuta ningún movimiento de fondos. | Transferencias |
| RN-05 | El saldo actual (`balance`) de una cuenta se gestiona exclusivamente en la base SQL. | Cuentas |
| RN-06 | Al ejecutar un desembolso de préstamo, el saldo de la cuenta destino debe incrementarse en `Monto_Aprobado`. | Préstamos → Cuentas |
| RN-07 | Antes de ejecutar cualquier transferencia, se debe validar que `balance >= monto` en la cuenta origen. | Transferencias |
| RN-08 | La apertura de cuentas requiere que el titular tenga estado `ACTIVE`. | Cuentas → Usuarios |
| RN-09 | El número de identificación (DNI/NIT) debe ser único en todo el sistema. | Clientes |
| RN-10 | Al aprobar una transferencia, si el saldo de la cuenta origen ya no es suficiente, la operación debe fallar. | Transferencias |
| RN-11 | Un Supervisor de Empresa no puede aprobar transferencias que él mismo creó. | Transferencias |

---

## 7. Validaciones Requeridas

| ID | Campo | Regla de Validación | Error si falla |
|----|-------|---------------------|----------------|
| VAL-01 | `birth_date` (ClientePersonaNatural) | Debe ser mayor de 18 años al momento del registro | Rechazar registro |
| VAL-02 | `identification_id` | Único en todo el sistema (DNI y NIT en mismo espacio) | Rechazar registro |
| VAL-03 | `email` | Debe contener `@` y un dominio válido | Rechazar registro |
| VAL-04 | `phone` | Debe tener entre 7 y 15 dígitos numéricos | Rechazar registro |
| VAL-05 | `user_status` | No permitir apertura de cuenta si es `INACTIVE` o `BLOCKED` | Lanzar excepción de negocio |
| VAL-06 | `balance` (cuenta origen) | Debe ser `>= amount` antes de ejecutar transferencia | Lanzar excepción de negocio |
| VAL-07 | `account_status` (origen) | No permitir transferencias desde cuentas `BLOCKED` o `CANCELLED` | Lanzar excepción de negocio |
| VAL-08 | `destination_account` (desembolso) | Debe existir y estar en estado `ACTIVE` | Lanzar excepción de negocio |
| VAL-09 | `loan_status` | Solo transición `PENDING` → `APPROVED` o `PENDING` → `REJECTED` | Lanzar excepción de negocio |
| VAL-10 | Rol del usuario | Cada endpoint/método debe verificar que el rol coincide con el permitido | HTTP 403 Forbidden |

---

## 8. Restricciones por Rol

### Matriz de Permisos

| Operación | IND_CUSTOMER | CORP_CUSTOMER | TELLER_EMP | COMMERCIAL_EMP | CORP_EMP | CORP_SUPERVISOR | INT_ANALYST |
|-----------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Ver saldo propio | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Ver saldo de clientes | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Abrir cuenta | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Solicitar préstamo | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Revisar/aprobar préstamo | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Desembolsar préstamo | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Crear transferencia individual | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Crear transferencia empresa | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Aprobar transferencia empresa | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Ver estado de solicitudes enviadas | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Registrar nuevo cliente | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Ver riesgos crediticios | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Modificar saldos directamente | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

### Restricciones Explícitas

| ID | Actor | Restricción |
|----|-------|-------------|
| REST-01 | Empleado de Ventanilla | NO puede ver riesgos crediticios |
| REST-02 | Empleado de Ventanilla | NO puede aprobar préstamos |
| REST-03 | Analista Interno | NO puede realizar operaciones de ventanilla |
| REST-04 | Analista Interno | NO puede crear transferencias |
| REST-05 | Analista Interno | NO puede modificar saldos arbitrariamente (solo por flujo de desembolso) |
| REST-06 | Clientes | NO pueden ver información de otros clientes |
| REST-07 | Clientes individuales | NO pueden ver cuentas empresariales ajenas |
| REST-08 | Supervisor de Empresa | NO puede aprobar sus propias transferencias personales |

---

## 9. Clases Obligatorias (Especificación)

| Clase (Enunciado) | Clase Implementada | Estado |
|-------------------|--------------------|--------|
| `UsuarioSistema` | `User` | ✅ Implementada |
| `ClientePersonaNatural` | `IndividualCustomer` | ✅ Implementada |
| `ClienteEmpresa` | `CorporateCustomer` | ✅ Implementada |
| `CuentaBancaria` | `BankAccount` | ✅ Implementada |
| `Prestamo` | `Loan` | ✅ Implementada |
| `Transferencia` | `Transfer` | ✅ Implementada |
| `BitacoraOperaciones` | `LogRecord` / `LogRecordDocument` | ✅ Implementada |

---

## 10. Métodos Obligatorios (Especificación)

| Método (Enunciado) | Clase/Servicio Esperado | Rol Permitido | Estado |
|--------------------|-----------------------|---------------|--------|
| `solicitarPrestamo()` | `LoanService` | INDIVIDUAL_CUSTOMER, CORPORATE_CUSTOMER, COMMERCIAL_EMPLOYEE | Verificar |
| `revisarSolicitudPrestamo()` | `LoanService` | INTERNAL_ANALYST | Verificar |
| `desembolsarFondos()` | `LoanService` + `AccountService` | INTERNAL_ANALYST | Verificar |
| `crearTransferenciaEmpresa()` | `TransferService` | CORPORATE_EMPLOYEE | Verificar |
| `aprobarTransferencia()` | `TransferService` | CORPORATE_SUPERVISOR | Verificar |
| `registrarEnBitacora()` | `LogService` | Sistema (transversal) | Verificar |
| `verificarVencimientoTransferencia()` | Worker/Scheduler | Sistema automático | ⚠️ Verificar si existe scheduler |

---

## 11. Manejo de Errores

| ID | Escenario de Error | Excepción Esperada | HTTP Status |
|----|-------------------|-------------------|-------------|
| ERR-01 | Acceso a endpoint sin el rol requerido | `BusinessException` / Spring Security | 403 Forbidden |
| ERR-02 | Transferencia desde cuenta `BLOCKED` o `CANCELLED` | `BusinessException` | 400 Bad Request |
| ERR-03 | Desembolso con cuenta destino inactiva o cancelada | `BusinessException` | 400 Bad Request |
| ERR-04 | Transferencia con saldo insuficiente | `BusinessException` | 400 Bad Request |
| ERR-05 | Registro de cliente con identificación duplicada | `BusinessException` | 409 Conflict |
| ERR-06 | Apertura de cuenta para usuario `INACTIVE` o `BLOCKED` | `BusinessException` | 400 Bad Request |
| ERR-07 | Aprobación de préstamo en estado diferente a `PENDING` | `BusinessException` | 400 Bad Request |
| ERR-08 | Recurso no encontrado (cuenta, préstamo, usuario) | `NotFoundException` | 404 Not Found |
| ERR-09 | Token JWT inválido o expirado | Spring Security | 401 Unauthorized |
| ERR-10 | Registro de persona menor de 18 años | `BusinessException` | 400 Bad Request |

### Clases de Excepción Existentes

```java
// domain/Exceptions/BusinessException.java
// domain/Exceptions/NotFoundException.java
```

---

## 12. Requisitos de Base de Datos

### 12.1 Base de Datos Relacional (SQL — MySQL)

**Propósito:** Datos transaccionales estructurados (clientes, cuentas, préstamos, transferencias).

**Tabla `users`**
```sql
- id              BIGINT PK AUTO_INCREMENT
- name            VARCHAR
- identification_id VARCHAR UNIQUE NOT NULL   -- DNI o NIT
- email           VARCHAR
- phone           VARCHAR
- birth_date      DATE
- address         VARCHAR
- system_role     ENUM(INDIVIDUAL_CUSTOMER, CORPORATE_CUSTOMER, ...)
- username        VARCHAR UNIQUE
- password        VARCHAR (bcrypt)
- user_status     ENUM(ACTIVE, INACTIVE, BLOCKED)
- related_id      BIGINT                       -- FK a Customer
```

**Tabla `bank_accounts`**
```sql
- id              BIGINT PK AUTO_INCREMENT
- account_number  VARCHAR UNIQUE NOT NULL
- account_type    ENUM(SAVINGS, CHECKING, PERSONAL, CORPORATE)
- account_holder_id BIGINT FK → users(id)
- balance         DECIMAL(19,4)               -- ⚠️ en código es double, debe ser DECIMAL
- currency        ENUM(...)
- account_status  ENUM(ACTIVE, BLOCKED, CANCELLED)
- opening_date    DATE
- product_code    VARCHAR
- product_name    VARCHAR
- category        ENUM(...)
- requires_approval BOOLEAN
```

**Tabla `loans`**
```sql
- id              BIGINT PK AUTO_INCREMENT
- loan_id         VARCHAR UNIQUE NOT NULL
- loan_type       ENUM(...)
- applicant_client_id BIGINT FK → users(id)
- requested_amount DECIMAL(19,4)
- approved_amount  DECIMAL(19,4)              -- Monto_Aprobado (obligatorio)
- interest_rate    DECIMAL(5,2)               -- Tasa_Interes (obligatorio)
- term_months      INT                        -- Plazo_Meses (obligatorio)
- approval_date    DATETIME
- disbursement_date DATETIME
- destination_account VARCHAR FK → bank_accounts(account_number)
- loan_status      ENUM(PENDING, APPROVED, REJECTED, DISBURSED)
- product_code     VARCHAR
- product_name     VARCHAR
- category         ENUM(...)
- requires_approval BOOLEAN
```

**Tabla `transfers`**
```sql
- id              BIGINT PK AUTO_INCREMENT
- transfer_id     VARCHAR UNIQUE NOT NULL
- source_account  VARCHAR FK → bank_accounts(account_number)
- destination_account VARCHAR FK → bank_accounts(account_number)
- amount          DECIMAL(19,4)
- creation_date   DATETIME NOT NULL
- approval_date   DATETIME
- transfer_status ENUM(PENDING, WAITING_APPROVAL, EXECUTED, APPROVED, REJECTED, EXPIRED)
- creator_user_id BIGINT FK → users(id)
- approver_user_id BIGINT FK → users(id)
```

### 12.2 Base de Datos No Relacional (NoSQL — MongoDB)

**Propósito:** Bitácora inmutable de auditoría.

**Colección `log_records`**
```json
{
  "_id": "ObjectId",
  "operationType": "LOAN_DISBURSEMENT | TRANSFER_EXECUTED | ...",
  "operationDateTime": "ISODate",
  "userId": "string",
  "userRole": "INTERNAL_ANALYST | ...",
  "affectedProductId": "string (loan_id / transfer_id / account_number)",
  "detailData": {
    "previousStatus": "PENDING",
    "newStatus": "APPROVED",
    "amount": 5000.00,
    "reason": "Aprobado por análisis crediticio"
  }
}
```

**Índices recomendados:**
- `affectedProductId` (búsqueda por producto)
- `userId` (búsqueda por actor)
- `operationType` (búsqueda por tipo de operación)
- `operationDateTime` (ordenamiento temporal)

### 12.3 Relaciones entre Tablas (Foreign Keys Implícitas)

```
users (1) ─────────────────────── (N) bank_accounts      [account_holder_id → users.id]
users (1) ─────────────────────── (N) loans              [applicant_client_id → users.id]
users (1) ─────────────────────── (N) transfers          [creator_user_id → users.id]
users (1) ─────────────────────── (N) transfers          [approver_user_id → users.id]
bank_accounts (1) ──────────────── (N) loans             [destination_account → bank_accounts.account_number]
bank_accounts (1) ──────────────── (N) transfers         [source_account → bank_accounts.account_number]
bank_accounts (1) ──────────────── (N) transfers         [destination_account → bank_accounts.account_number]
```

---

## 13. Requisitos de Interfaz

| ID | Vista / Endpoint | Roles con Acceso | Descripción |
|----|-----------------|------------------|-------------|
| UI-01 | Consulta de saldo propio | INDIVIDUAL_CUSTOMER, CORPORATE_CUSTOMER | Solo cuentas del usuario autenticado |
| UI-02 | Consulta de saldo por identificación | TELLER_EMPLOYEE | Requiere DNI/NIT del cliente |
| UI-03 | Apertura de cuenta | TELLER_EMPLOYEE | Puede crear el cliente si no existe |
| UI-04 | Estado de solicitudes de préstamo | COMMERCIAL_EMPLOYEE | Solo las que él inició |
| UI-05 | Lista de préstamos en revisión | INTERNAL_ANALYST | Todos los préstamos en `PENDING` |
| UI-06 | Aprobación/rechazo de préstamo | INTERNAL_ANALYST | Cambia estado y registra en bitácora |
| UI-07 | Desembolso de préstamo | INTERNAL_ANALYST | Solo préstamos en `APPROVED` |
| UI-08 | Crear transferencia empresa | CORPORATE_EMPLOYEE | Incluye pagos masivos/nómina |
| UI-09 | Aprobar transferencia en espera | CORPORATE_SUPERVISOR | Solo transferencias en `WAITING_APPROVAL` |
| UI-10 | Login | Público | Retorna JWT |

**Reglas de Visibilidad:**
- Cada respuesta de API debe filtrar datos según el rol del usuario autenticado.
- Los clientes individuales no deben recibir datos de cuentas empresariales de terceros.
- Los empleados de ventanilla no deben recibir información de riesgos crediticios.

---

## 14. Casos Límite (Edge Cases)

| ID | Escenario | Comportamiento Esperado |
|----|-----------|------------------------|
| EC-01 | Transferencia de empresa con monto **exactamente igual** al umbral predefinido | Debe pasar a `WAITING_APPROVAL` (umbral es inclusivo por defecto) |
| EC-02 | Aprobación de transferencia en el **minuto 59** desde su creación | Debe ejecutarse correctamente |
| EC-03 | Aprobación de transferencia en el **minuto 61** (ya expirada) | Debe rechazar la aprobación, estado `EXPIRED` |
| EC-04 | Cuenta destino de préstamo **cancelada después** de aprobación pero **antes** de desembolso | Desembolso debe fallar con error descriptivo |
| EC-05 | Saldo exactamente igual al monto de la transferencia (`balance == amount`) | Transferencia debe ejecutarse (saldo queda en 0) |
| EC-06 | Registro de cliente cuyo DNI ya existe en el sistema | Rechazar con error de unicidad |
| EC-07 | Persona que cumple 18 años exactamente hoy | Debe ser aceptada (`>=` 18 años) |
| EC-08 | Préstamo aprobado pero `destination_account` no existe en el sistema | Fallo en desembolso con `NotFoundException` |
| EC-09 | Supervisor intenta aprobar transferencia que él mismo creó | Rechazar con error de restricción de negocio |
| EC-10 | Analista Interno intenta crear una transferencia | HTTP 403 Forbidden |
| EC-11 | Teléfono con exactamente **7 dígitos** | Válido (límite inferior) |
| EC-12 | Teléfono con exactamente **15 dígitos** | Válido (límite superior) |
| EC-13 | Teléfono con **6 dígitos** | Inválido |
| EC-14 | Transferencia desde cuenta en estado `BLOCKED` | Rechazar inmediatamente |
| EC-15 | Apertura de cuenta para cliente `INACTIVE` | Rechazar con error de estado |

---

## 15. Criterios de Evaluación

| ID | Criterio | Ponderación (estimada) | Descripción |
|----|----------|----------------------|-------------|
| CE-01 | Persistencia Híbrida (SQL/NoSQL) | Alta | Datos transaccionales en MySQL, bitácora en MongoDB |
| CE-02 | Flujos de Aprobación | Alta | Préstamos y transferencias siguen su máquina de estados correctamente |
| CE-03 | Control de Acceso por Roles (RBAC) | Alta | Ningún rol puede ejecutar operaciones que no le corresponden |
| CE-04 | Precisión en Actualización de Saldos | Alta | Saldo incrementado tras desembolso, decrementado tras transferencia |
| CE-05 | Registro de Cambios de Estado en Bitácora | Alta | Estado anterior y nuevo deben constar en `detailData` |
| CE-06 | Vencimiento Automático a los 60 min | Media | Proceso en segundo plano o verificación lazy |
| CE-07 | Validaciones de Datos de Entrada | Media | Edad, unicidad, formato de contacto |
| CE-08 | Manejo de Errores | Media | Excepciones claras con mensajes descriptivos |

---

## 16. Checklist de Cumplimiento

| ID | Requisito | Obligatorio | Estado | Evidencia en Código |
|----|-----------|:-----------:|--------|---------------------|
| CK-01 | Unicidad de Identificación (DNI/NIT) | ✅ Sí | ⬜ Verificar | `UserJpaRepository.existsByIdentificationId()` |
| CK-02 | Validación de mayoría de edad (18+) | ✅ Sí | ⬜ Verificar | Lógica en `UserService` o `RegisterIndividualCustomer` |
| CK-03 | Persistencia de Bitácora en NoSQL (MongoDB) | ✅ Sí | ⬜ Verificar | `LogRecordDocument`, `LogRecordMongoRepository` |
| CK-04 | Vencimiento de transferencia a los 60 min | ✅ Sí | ⬜ Verificar | Buscar `@Scheduled` o `findByTransferStatusAndCreationDateBefore()` |
| CK-05 | Aprobación de préstamo solo por Analista Interno | ✅ Sí | ⬜ Verificar | Control de rol en `LoanService.revisarSolicitudPrestamo()` |
| CK-06 | Registro de estado anterior y nuevo en Bitácora | ✅ Sí | ⬜ Verificar | `detailData.previousStatus` y `detailData.newStatus` en logs |
| CK-07 | No permitir apertura de cuenta a usuarios Inactivos/Bloqueados | ✅ Sí | ⬜ Verificar | Validación en `OpenAccount` service |
| CK-08 | Validación de fondos antes de transferencia | ✅ Sí | ⬜ Verificar | Comparación `balance >= amount` en `TransferService` |
| CK-09 | No permitir transferencia desde cuenta Bloqueada/Cancelada | ✅ Sí | ⬜ Verificar | Validación de `AccountStatus` en `TransferService` |
| CK-10 | Flujo de estado de préstamo correcto (no REJECTED → APPROVED) | ✅ Sí | ⬜ Verificar | Validación de `LoanStatus` en `LoanService` |
| CK-11 | Motivo de vencimiento registrado en bitácora | ✅ Sí | ⬜ Verificar | `detailData.reason = "Expirado por tiempo"` en log |
| CK-12 | Validación de teléfono (7-15 dígitos) | ✅ Sí | ⬜ Verificar | Validación en DTO o servicio |
| CK-13 | Validación de formato de correo | ✅ Sí | ⬜ Verificar | Validación `@` y dominio |
| CK-14 | Analista Interno no puede crear transferencias | ✅ Sí | ⬜ Verificar | Verificación de rol en `TransferService` |
| CK-15 | Saldo calculado desde SQL, no desde bitácora | ✅ Sí | ⬜ Verificar | `balance` viene de `BankAccountEntity`, no de MongoDB |
| CK-16 | Desembolso incrementa saldo en cuenta destino | ✅ Sí | ⬜ Verificar | `Deposit` service llamado tras aprobación de préstamo |
| CK-17 | `approved_amount`, `interest_rate`, `term_months` en tabla loans | ✅ Sí | ⬜ Verificar | Campos presentes en `LoanEntity` |
| CK-18 | Supervisor no aprueba sus propias transferencias | ✅ Sí | ⬜ Verificar | `creator_user_id != approver_user_id` en `TransferService` |

---

## 17. Posibles Puntos de Fallo

### P1 — Críticos (Descuento Directo en Evaluación)

| ID | Punto de Fallo | Descripción | Dónde Revisar |
|----|---------------|-------------|---------------|
| PF-01 | **No registrar estado anterior/nuevo en bitácora** | Al aprobar/rechazar préstamo o transferencia, `detailData` debe incluir `previousStatus` y `newStatus`. Omitirlo es descuento explícito. | `LogService`, `LoanService`, `TransferService` |
| PF-02 | **Analista Interno puede crear transferencias** | Violación de segregación de funciones. Verificar que todos los endpoints de transferencia rechazan este rol. | `TransferService`, `CorporateEmployeeController` |
| PF-03 | **Motivo de vencimiento ausente en bitácora** | Cuando una transferencia expira, el log debe incluir el motivo. | `verificarVencimientoTransferencia()`, `LogService` |
| PF-04 | **Saldo calculado desde MongoDB** | Error arquitectónico grave. El saldo debe venir exclusivamente de `bank_accounts.balance` en MySQL. | Cualquier método que consulte saldo |
| PF-05 | **No actualizar `Saldo_Actual` tras desembolso** | Bug lógico crítico. El saldo de la cuenta destino debe incrementarse. | `desembolsarFondos()`, `Deposit` service |

### P2 — Altos (Impactan Funcionalidad)

| ID | Punto de Fallo | Descripción |
|----|---------------|-------------|
| PF-06 | **`balance` como `double` en vez de `Decimal`** | Imprecisión de punto flotante en operaciones monetarias. Debe ser `BigDecimal` en dominio y `DECIMAL(19,4)` en SQL. |
| PF-07 | **Sin scheduler para vencimiento de transferencias** | El proceso automático de 60 min no existe si no hay `@Scheduled` o job equivalente. |
| PF-08 | **Validaciones ausentes en DTOs de entrada** | Si los DTOs no validan teléfono, email, edad, el servicio puede recibir datos inválidos. |
| PF-09 | **Falta de pruebas unitarias** | `BancoApplicationTests.java` parece el único test. Sin tests, el comportamiento real es incierto. |
| PF-10 | **No validar que cuenta destino del préstamo está activa al desembolsar** | El estado puede cambiar entre aprobación y desembolso. |

### P3 — Medios (Mejoras Recomendadas)

| ID | Punto de Fallo | Descripción |
|----|---------------|-------------|
| PF-11 | **JWT expiration muy corta o inconsistente** | `app.jwt.expiration=30000` milisegundos = 30 segundos. Posiblemente debe ser en segundos (500 min) o el valor es incorrecto. |
| PF-12 | **DataSeeder sin roles predefinidos documentados** | Si `DataSeeder.java` no crea usuarios de prueba para todos los roles, las pruebas manuales son difíciles. |
| PF-13 | **Falta de paginación en listas** | Endpoints que retornan muchos registros sin paginación pueden afectar rendimiento. |
| PF-14 | **No hay soft delete** | Cuentas y clientes "eliminados" podrían necesitar estado `CANCELLED` en vez de borrado físico. |

---

## 18. Casos de Prueba Sugeridos

### 18.1 Casos Normales (Happy Path)

```
TEST-N-01: Registro de cliente persona natural
  DADO: datos válidos (mayor de 18, DNI único, email y teléfono válidos)
  CUANDO: POST /api/teller/register-individual-customer
  ENTONCES: HTTP 201, usuario creado con status ACTIVE

TEST-N-02: Apertura de cuenta de ahorros
  DADO: cliente existente con status ACTIVE
  CUANDO: POST /api/teller/open-account (tipo: SAVINGS)
  ENTONCES: HTTP 201, cuenta creada con balance 0 y status ACTIVE

TEST-N-03: Solicitud de préstamo por cliente individual
  DADO: cliente autenticado con JWT válido
  CUANDO: POST /api/individual-customer/request-loan
  ENTONCES: HTTP 201, préstamo creado con status PENDING

TEST-N-04: Aprobación de préstamo por analista interno
  DADO: préstamo en estado PENDING, usuario con rol INTERNAL_ANALYST
  CUANDO: PUT /api/analyst/approve-loan/{loanId}
  ENTONCES: HTTP 200, préstamo en estado APPROVED, bitácora registrada con previousStatus=PENDING, newStatus=APPROVED

TEST-N-05: Desembolso de préstamo
  DADO: préstamo en estado APPROVED, cuenta destino ACTIVE
  CUANDO: POST /api/analyst/disburse-loan/{loanId}
  ENTONCES: HTTP 200, saldo de cuenta destino incrementado en approved_amount, préstamo en DISBURSED

TEST-N-06: Transferencia individual entre cuentas propias
  DADO: cliente con 2 cuentas activas, saldo suficiente
  CUANDO: POST /api/individual-customer/transfer
  ENTONCES: HTTP 200, saldo origen decrementado, saldo destino incrementado, log en MongoDB

TEST-N-07: Transferencia empresa por debajo del umbral
  DADO: empleado corporativo, monto < umbral
  CUANDO: POST /api/corporate-employee/transfer
  ENTONCES: HTTP 200, estado EXECUTED, fondos movidos, log en MongoDB

TEST-N-08: Transferencia empresa por encima del umbral
  DADO: empleado corporativo, monto > umbral
  CUANDO: POST /api/corporate-employee/transfer
  ENTONCES: HTTP 201, estado WAITING_APPROVAL, fondos NO movidos aún

TEST-N-09: Aprobación de transferencia en espera
  DADO: transferencia en WAITING_APPROVAL, supervisor diferente al creador
  CUANDO: PUT /api/supervisor/approve-transfer/{transferId}
  ENTONCES: HTTP 200, fondos movidos, estado EXECUTED, log en MongoDB
```

### 18.2 Casos de Error Esperados

```
TEST-E-01: Registro de menor de edad
  DADO: fecha de nacimiento que resulta en 17 años
  CUANDO: POST /api/teller/register-individual-customer
  ENTONCES: HTTP 400, mensaje "El cliente debe ser mayor de 18 años"

TEST-E-02: Registro con DNI duplicado
  DADO: DNI ya existente en el sistema
  CUANDO: POST (cualquier registro de cliente)
  ENTONCES: HTTP 409, mensaje de unicidad

TEST-E-03: Apertura de cuenta para usuario bloqueado
  DADO: usuario con status BLOCKED
  CUANDO: POST /api/teller/open-account
  ENTONCES: HTTP 400, mensaje de estado inválido

TEST-E-04: Transferencia con saldo insuficiente
  DADO: cuenta origen con balance < monto de transferencia
  CUANDO: POST (cualquier transferencia)
  ENTONCES: HTTP 400, mensaje "Saldo insuficiente"

TEST-E-05: Transferencia desde cuenta cancelada
  DADO: cuenta origen con status CANCELLED
  CUANDO: POST (cualquier transferencia)
  ENTONCES: HTTP 400, mensaje de estado de cuenta

TEST-E-06: Acceso de analista a endpoint de transferencia
  DADO: JWT de usuario con rol INTERNAL_ANALYST
  CUANDO: POST /api/corporate-employee/transfer (o cualquier endpoint de transferencia)
  ENTONCES: HTTP 403 Forbidden

TEST-E-07: Desembolso con cuenta destino inactiva
  DADO: préstamo en APPROVED, cuenta destino en CANCELLED
  CUANDO: POST /api/analyst/disburse-loan/{loanId}
  ENTONCES: HTTP 400, mensaje de cuenta destino inactiva

TEST-E-08: Supervisor aprueba su propia transferencia
  DADO: transferencia creada por el mismo supervisor
  CUANDO: PUT /api/supervisor/approve-transfer/{transferId}
  ENTONCES: HTTP 400, mensaje de restricción de negocio
```

### 18.3 Casos Límite (Edge Cases)

```
TEST-L-01: Transferencia exactamente en el umbral de monto
  DADO: monto == umbral predefinido
  CUANDO: POST /api/corporate-employee/transfer
  ENTONCES: HTTP 201, estado WAITING_APPROVAL (umbral es inclusivo)

TEST-L-02: Aprobación de transferencia a los 59 minutos
  DADO: transferencia en WAITING_APPROVAL creada hace 59 min
  CUANDO: PUT /api/supervisor/approve-transfer/{transferId}
  ENTONCES: HTTP 200, ejecutada exitosamente

TEST-L-03: Intento de aprobación a los 61 minutos (expirada)
  DADO: transferencia debería haber expirado
  CUANDO: PUT /api/supervisor/approve-transfer/{transferId}
  ENTONCES: HTTP 400, transferencia en estado EXPIRED, fondos NO movidos

TEST-L-04: Saldo exactamente igual al monto
  DADO: balance == amount en transferencia
  CUANDO: POST (transferencia)
  ENTONCES: HTTP 200, ejecutada, saldo queda en 0

TEST-L-05: Registro de persona que cumple 18 hoy
  DADO: fecha de nacimiento = hace exactamente 18 años hoy
  CUANDO: POST /api/teller/register-individual-customer
  ENTONCES: HTTP 201, aceptado (>= 18 años)

TEST-L-06: Teléfono con 7 dígitos (límite mínimo)
  ENTONCES: HTTP 201, válido

TEST-L-07: Teléfono con 6 dígitos (fuera del límite)
  ENTONCES: HTTP 400, inválido

TEST-L-08: Teléfono con 15 dígitos (límite máximo)
  ENTONCES: HTTP 201, válido
```

### 18.4 Pruebas de Bitácora

```
TEST-B-01: Toda aprobación de préstamo genera log con previousStatus y newStatus
  VERIFICAR: MongoDB contiene documento con detailData.previousStatus = "PENDING"
             y detailData.newStatus = "APPROVED"

TEST-B-02: Vencimiento automático genera log con motivo
  VERIFICAR: MongoDB contiene documento con detailData.reason = "EXPIRED" o similar

TEST-B-03: Transferencia ejecutada genera log
  VERIFICAR: MongoDB contiene documento con operationType = "TRANSFER_EXECUTED"

TEST-B-04: Saldo no se calcula desde MongoDB
  VERIFICAR: El campo balance de BankAccountEntity no se modifica al leer logs
```

---

## 19. Módulos Incompletos o en Riesgo

| Módulo | Riesgo | Descripción | Archivos a Verificar |
|--------|--------|-------------|---------------------|
| **Scheduler de Vencimiento** | 🔴 Alto | No se detectó `@Scheduled` o job automático para expirar transferencias a los 60 min. Es un requisito explícito. | Buscar `@Scheduled`, `@EnableScheduling` en todo el proyecto |
| **Precisión Monetaria** | 🔴 Alto | `balance` declarado como `double` en `BankAccountEntity`. En operaciones bancarias esto es un error de arquitectura; debe ser `BigDecimal`. | `BankAccountEntity.balance`, `Withdraw.java`, `Deposit.java` |
| **Implementación de Use Cases** | 🟡 Medio | Las 8 clases `*UseCase` existen pero su completitud de métodos no fue verificada. | `application/usecases/*.java` |
| **Validaciones en DTOs** | 🟡 Medio | No se confirmó que los DTOs de registro usen `@Valid`, `@NotNull`, `@Pattern` de Bean Validation. | `application/adapters/api/request/*.java` |
| **Pruebas Unitarias** | 🟡 Medio | Solo existe `BancoApplicationTests.java` (test de carga de contexto). Sin pruebas de lógica de negocio. | `src/test/` |
| **Controladores — Cobertura de Endpoints** | 🟡 Medio | Los 8 controladores existen pero sus métodos específicos no fueron auditados. | `application/adapters/api/controllers/*.java` |
| **Registro en Bitácora — Cobertura** | 🟡 Medio | No confirmado que TODAS las operaciones significativas llamen a `registrarEnBitacora()`. | `LoanService`, `TransferService`, `OpenAccount` |
| **Datos Semilla** | 🟢 Bajo | `DataSeeder.java` existe pero su contenido no fue verificado. | `infrastructure/DataSeeder.java` |

---

## 20. Ambigüedades del Enunciado

| ID | Ambigüedad | Impacto | Decisión Recomendada |
|----|-----------|---------|---------------------|
| AMB-01 | **Valor del Umbral de Transferencia**: no se especifica monto exacto | Alto | Definir constante configurable, p.ej. `TRANSFER_APPROVAL_THRESHOLD = 10000.00` en `application.properties` |
| AMB-02 | **Multimoneda**: campo `Moneda` existe pero no se especifica si hay conversión de tasas entre monedas | Medio | Implementar como campo informativo sin conversión automática; documentar limitación |
| AMB-03 | **Representante Legal**: no queda claro si siempre es un `ClientePersonaNatural` registrado previamente o un registro independiente | Medio | Implementar como campo en `CorporateCustomer` que referencia a un `User` existente o como datos embebidos |
| AMB-04 | **Proceso de Cierre**: se menciona que "procesos internos de cierre" pueden operar en cuentas bloqueadas pero no se definen | Bajo | No implementar hasta que sea especificado; documentar como pendiente |
| AMB-05 | **Umbral inclusivo o exclusivo**: "superen un umbral" implica `> umbral` (exclusivo superior) o `>= umbral` (inclusivo) | Medio | Usar `>= umbral` (inclusivo) por ser la interpretación más conservadora en banca |

---

*Documento generado automáticamente a partir del enunciado oficial y el análisis del código fuente existente.*  
*Para regenerar o actualizar este documento, ejecutar el análisis sobre la rama `claude/banking-management-app-ff6Ih`.*
