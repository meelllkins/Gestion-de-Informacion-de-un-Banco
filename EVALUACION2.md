# EVALUACION 2 - Gestion-de-Informacion-de-un-Banco

## Informacion general
- Estudiante(s): Integrantes no informados en README.md
- Rama evaluada: develop
- Commit evaluado: 2cafbd19ab0a5fe73792597c0b72cdadcebe189d
- Fecha: 2026-04-11
- Nota: Proyecto Spring Boot. Dominio en `Banco/src/main/java/app/domain/`. Implementacion muy inicial: solo enums y modelos basicos. Sin puertos, servicios, ni entidades de transferencia/bitacora.

---

## Tabla de calificacion

| # | Criterio | Peso | Puntaje (1-5) | Contribucion |
|---|----------|------|---------------|--------------|
| 1 | Modelado de dominio | 20% | 2 | 0.40 |
| 2 | Modelado de puertos | 20% | 1 | 0.20 |
| 3 | Modelado de servicios de dominio | 20% | 1 | 0.20 |
| 4 | Enums y estados | 10% | 3 | 0.30 |
| 5 | Reglas de negocio criticas | 10% | 1 | 0.10 |
| 6 | Bitacora y trazabilidad | 5% | 1 | 0.05 |
| 7 | Estructura interna de dominio | 10% | 3 | 0.30 |
| 8 | Calidad tecnica base en domain | 5% | 2 | 0.10 |
| | **Total base** | **100%** | | **1.65** |

**Formula:** Nota = sum(puntaje_i × peso_i) / 100

---

## Penalizaciones
Ninguna.

---

## Nota final
**1.65 / 5.0**

---

## Hallazgos

### Fortalezas
- **Enums definidos correctamente:** `AccountStatus`, `AccountType`, `Currency`, `LoanStatus`, `LoanType`, `SystemRole`, `UserStatus` — 7 enums que cubren las entidades presentes.
- **Jerarquia de clientes:** `User` (abstracta con Lombok), `IndividualCustomer`, `CorporateCustomer` con `businessName` y `legalRepresentative`.
- **`Loan`** incluye campos para el ciclo completo del prestamo: `loanStatus`, `approvalDate`, `disbursementDate`, `destinationAccount`.
- Estructura `domain/enums/` y `domain/models/` separados.

### Debilidades
- **Entidades criticas faltantes:** No existe `Transfer`/`Transferencia`. Sin esta entidad no se puede modelar el flujo de transferencias bancarias ni la regla de aprobacion para alto monto. No existe `ProductoBancario`. No existe `BitacoraOperacion`.
- **`BankAccount` con tipos primitivos:** `balance` es `double` en lugar de `BigDecimal`. Fecha de apertura como `String` en lugar de `LocalDate` o `LocalDateTime`.
- **Sin comportamiento en entidades:** Todas las clases parecen ser POJOs con Lombok sin metodos de negocio (`deposit()`, `withdraw()`, `approve()`, `reject()`).
- **Sin puertos:** No hay ninguna interfaz de repositorio o puerto de salida.
- **Sin servicios de dominio:** No hay ninguna clase de servicio.
- **`TransferStatus` faltante:** Solo hay 7 enums, no se modela el estado de transferencia (PENDING, AWAITING_APPROVAL, EXECUTED, REJECTED, EXPIRED).
- **Solo 12 archivos Java en total** — implementacion extremadamente incompleta para el alcance del enunciado.
- **`User` usa Lombok** sin restricciones de acceso ni validaciones de negocio.

---

## Recomendaciones
1. Agregar la entidad `Transfer` con su maquina de estados (PENDING, AWAITING_APPROVAL, EXECUTED, REJECTED, EXPIRED) y su enum `TransferStatus`.
2. Agregar `BitacoraOperacion` con campos semanticos y su puerto `BitacoraPort`.
3. Agregar `ProductoBancario` con `CategoriaProducto`.
4. Reemplazar `double` por `BigDecimal` y `String` de fecha por `LocalDate`/`LocalDateTime`.
5. Agregar comportamiento de negocio en `BankAccount` (`deposit()`, `withdraw()` con validacion de estado y saldo) y en `Loan` (`approve()`, `reject()`, `disburse()`).
6. Definir interfaces de puerto para cada agregado.
7. Completar los enums que faltan: `TransferStatus`, `CategoriaProducto`.
