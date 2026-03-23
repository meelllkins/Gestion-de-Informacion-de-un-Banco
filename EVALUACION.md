# EVALUACIÓN - Gestion-de-Informacion-de-un-Banco

## Información General
- Estudiante(s): meelllkins (usuario GitHub)
- Rama evaluada: develop
- Fecha de evaluación: 2026-03-23

## Tabla de Calificación

| # | Criterio | Peso | Puntaje (1-5) | Nota ponderada |
|---|---|---|---|---|
| 1 | Modelado de dominio | 25% | 2 | 0.50 |
| 2 | Relaciones entre entidades | 15% | 2 | 0.30 |
| 3 | Uso de Enums | 15% | 3 | 0.45 |
| 4 | Manejo de estados | 5% | 3 | 0.15 |
| 5 | Tipos de datos | 5% | 2 | 0.10 |
| 6 | Separación Usuario vs Cliente | 10% | 1 | 0.10 |
| 7 | Bitácora | 5% | 1 | 0.05 |
| 8 | Reglas básicas de negocio | 5% | 1 | 0.05 |
| 9 | Estructura del proyecto | 10% | 4 | 0.40 |
| 10 | Repositorio | 10% | 3 | 0.30 |
| **TOTAL** | | **100%** | | **2.40** |

## Penalizaciones
- Ninguna

## Bonus
- Código limpio (Lombok, nomenclatura en inglés): +0.1

## Nota Final: 2.5 / 5.0

> Cálculo: 2.40 + 0.1 = 2.5

---

## Análisis por Criterio

### 1. Modelado de dominio (Puntaje: 2)
Entidades presentes: `User` (abstracta), `IndividualCustomer` (vacía), `CorporateCustomer`, `BankAccount`, `Loan`. **Faltan entidades críticas:**
- `Transfer` — completamente ausente.
- `BankingProduct` — completamente ausente.
- `BitacoraDeOperaciones`/AuditLog — completamente ausente.
- `IndividualCustomer` solo tiene la declaración `extends User` sin ningún campo.

### 2. Relaciones entre entidades (Puntaje: 2)
`BankAccount` usa `String accountHolderId` en lugar de referencia a `User`/`Client`. `Loan` usa `String applicantClientId`. Sin `Transfer` ni `BankingProduct`, las relaciones más complejas del dominio están ausentes.

### 3. Uso de Enums (Puntaje: 3)
7 enums implementados: `SystemRole` ✓, `UserStatus` ✓, `AccountType` ✓, `AccountStatus` ✓, `Currency` ✓, `LoanStatus` ✓, `LoanType` ✓. Se usan correctamente en `BankAccount`, `Loan` y `User`. Sin embargo, falta `TransferStatus` (no hay clase Transfer), los estados del préstamo no incluyen `DISBURSED`/`IN_STUDY` (tiene APPROVED, REJECTED, PENDING), y `Currency` solo tiene `COLOMBIAN_PESO` con nombre largo en lugar de `COP`. `openingDate` en `BankAccount` es `String` en lugar de `LocalDate`.

### 4. Manejo de estados (Puntaje: 3)
Los enums de estado se usan en las entidades que existen. `AccountStatus`, `LoanStatus`, `UserStatus` presentes y referenciados. No hay métodos de transición. Falta `TransferStatus` y estados de préstamo incompletos.

### 5. Tipos de datos (Puntaje: 2)
- `balance` en `BankAccount` es `double` — debería ser `BigDecimal`.
- `requestedAmount`, `approvedAmount`, `interestRate` en `Loan` son `double` — deberían ser `BigDecimal`.
- `openingDate` en `BankAccount` es `String` — debería ser `LocalDate`.
- `loanId` es `int` en lugar de `Long`.
- `approvalDate`, `disbursementDate` en `Loan` son `LocalDate` ✓.

### 6. Separación Usuario vs Cliente (Puntaje: 1)
`IndividualCustomer extends User` y `CorporateCustomer extends User`. La clase `User` sirve como base para todo tipo de actor del sistema, mezclando el acceso al sistema con la titularidad de productos bancarios. No hay una jerarquía de `Cliente` independiente.

### 7. Bitácora (Puntaje: 1)
No existe ninguna clase de bitácora ni log de auditoría en el proyecto.

### 8. Reglas básicas de negocio (Puntaje: 1)
Todo usa `@NoArgsConstructor` de Lombok. No hay validaciones en constructores, ni métodos de negocio en ninguna entidad.

### 9. Estructura del proyecto (Puntaje: 4)
Proyecto Spring Boot con Maven (Banco/pom.xml) ✓. Bien organizado: `src/main/java/app/domain/` con subpaquetes `models/` y `enums/`. Rama `develop` presente ✓. La estructura es adecuada para un proyecto DDD, aunque podría mejorarse con subpaquetes por agregado.

### 10. Repositorio (Puntaje: 3)
- **Nombre:** `Gestion-de-Informacion-de-un-Banco` — descriptivo y en español, coherente con el enunciado.
- **README:** Solo el título del repositorio. Sin descripción, materia ni contenido.
- **Commits:** 8 commits — varios con mensajes en español (bien detallados como "Eliminar clases obsoletas y agregar nuevas...") pero uno dice "Nothing." — inconsistente.
- **Ramas:** Tiene `develop` ✓ — único repositorio del grupo con rama develop.
- **Tag:** Ninguno.

---

## Fortalezas
- 7 enums correctamente definidos y usados en las entidades.
- Proyecto Spring Boot con Maven — estructura estándar.
- Única entrega con rama `develop` activa.
- Código limpio con Lombok y totalmente en inglés.
- Separación en paquetes `models/` y `enums/`.

## Oportunidades de mejora
- **Crítico:** Implementar `Transfer`, `BankingProduct` y `BitacoraDeOperaciones`.
- Completar `IndividualCustomer` con atributos propios.
- Separar `User` (acceso al sistema) de `Customer`/`Client` (titular de productos).
- Usar `BigDecimal` para todos los campos monetarios.
- Cambiar `openingDate: String` por `LocalDate` en `BankAccount`.
- Corregir estados del préstamo: agregar `IN_STUDY` y `DISBURSED`.
- Agregar validaciones en constructores y métodos de negocio.
- Agregar contenido al README: materia, integrantes, tecnología, instrucciones.
- Crear tag de entrega en la rama develop.
