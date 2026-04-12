# EVALUACION 2 — Construccion de Software II
**Repositorio:** Gestion-de-Informacion-de-un-Banco | **Estudiante(s):** Integrantes no informados (git: andfsanchezag) | **Rama:** develop | **Commit:** 2cafbd19 | **Fecha:** 2026-04-11

## Puntuaciones

| Criterio | Puntaje | Peso | Aporte |
|---|---|---|---|
| 1. Modelado de dominio | 2 | 20% | 8.0% |
| 2. Modelado de puertos | 1 | 20% | 4.0% |
| 3. Servicios de dominio | 1 | 20% | 4.0% |
| 4. Enums y estados | 5 | 10% | 10.0% |
| 5. Reglas de negocio | 1 | 10% | 2.0% |
| 6. Bitacora | 1 | 5% | 1.0% |
| 7. Estructura interna | 3 | 10% | 6.0% |
| 8. Calidad tecnica | 3 | 5% | 3.0% |
| **Total** | | **100%** | **38.0%** |

**Nota base:** (38/100) * 5.0 = 1.90  
**Penalizaciones:** ninguna (codigo en ingles, enums correctos, sin acoplamiento a framework)  
**Nota final:** **1.90**

## Hallazgos

### Fortalezas
- **7 enums correctamente definidos:** AccountStatus, AccountType, Currency, LoanStatus, LoanType, SystemRole, UserStatus — cubre los catalogos criticos del enunciado.
- **Jerarquia de clientes:** `User` (abstracta), `IndividualCustomer`, `CorporateCustomer` con businessName y legalRepresentative.
- **`Loan`** incluye campos para el ciclo completo: loanStatus, approvalDate, disbursementDate, destinationAccount.
- Estructura domain/enums y domain/models separados.
- Codigo en ingles — sin penalizacion.

### Debilidades
- **Sin `Transfer`/Transferencia:** entidad critica ausente — no se puede modelar el flujo de transferencias ni la regla de aprobacion para alto monto.
- **Sin `BitacoraOperacion`:** sin auditoria ni trazabilidad de operaciones.
- **Sin puertos ni servicios:** no hay interfaces de repositorio ni clases de servicio de dominio.
- **Entidades son POJOs:** BankAccount, Loan, User no tienen metodos de negocio (deposit(), withdraw(), approve(), reject()).
- **`balance` en `BankAccount` como `double`:** deberia ser BigDecimal para precision monetaria.
- **Sin `ProductoBancario`:** no existe catalogo de productos bancarios.
- **Solo 12 archivos Java** — implementacion muy incompleta para el alcance del enunciado.

## Recomendaciones
1. Agregar la entidad Transfer con su maquina de estados (PENDING, AWAITING_APPROVAL, EXECUTED, REJECTED, EXPIRED) y enum TransferStatus.
2. Agregar BitacoraOperacion con campos semanticos y puerto BitacoraPort.
3. Agregar ProductoBancario con enum CategoriaProducto.
4. Crear puertos para cada agregado: BankAccountPort, LoanPort, TransferPort, BitacoraPort.
5. Agregar metodos de negocio en BankAccount (deposit, withdraw con validacion de estado y saldo) y en Loan (approve, reject, disburse con maquina de estados).
6. Reemplazar double por BigDecimal en BankAccount.balance.
