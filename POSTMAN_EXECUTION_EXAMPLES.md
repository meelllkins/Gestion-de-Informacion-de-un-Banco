# 🎯 Ejemplos de Ejecución - Casos de Uso Reales

## Introducción

Este documento muestra ejemplos reales de **request → response** para los casos de uso más importantes del sistema bancario.

---

## 📋 Caso 1: Registro de Cliente Individual + Solicitud de Préstamo

### Paso 1️⃣: Registrar Cliente Individual

**Request:**
```http
POST http://localhost:8080/api/customers/individual/register
Content-Type: application/json

{
  "name": "Juan Pérez García",
  "identificationId": "12345678A",
  "email": "juan.perez@email.com",
  "phone": "+34912345678",
  "birthDate": "1990-05-15",
  "address": "Calle Principal 123, Madrid",
  "username": "juan.perez",
  "password": "SecurePass123!"
}
```

**Response (201 CREATED):**
```json
{
  "name": "Juan Pérez García",
  "identificationId": "12345678A",
  "email": "juan.perez@email.com",
  "phone": "+34912345678",
  "birthDate": "1990-05-15",
  "address": "Calle Principal 123, Madrid",
  "systemRole": "CUSTOMER",
  "username": "juan.perez"
}
```

### Paso 2️⃣: Cliente hace Login

**Request:**
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "juan.perez",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "username": "juan.perez",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuLnBlcmV6Iiwicm9sZXMiOlsiQ1VTVE9NRVIiXSwiaWF0IjoxNzE3MDk2MDAwfQ.abc123xyz...",
  "role": "CUSTOMER"
}
```

**Guardar token en variable:** `juan_customer_token`

### Paso 3️⃣: Cliente Solicita Préstamo

**Request:**
```http
POST http://localhost:8080/api/customers/individual/request-loan
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "loanType": "PERSONAL",
  "requestedAmount": 5000,
  "termMonths": 24,
  "productCode": "PRSTM001",
  "productName": "Préstamo Personal",
  "category": "PERSONAL",
  "destinationAccount": "ES1234567890123456789012"
}
```

**Response (201 CREATED):**
```json
{
  "loanId": 1,
  "loanType": "PERSONAL",
  "applicantClientId": "12345678A",
  "requestedAmount": 5000,
  "approvedAmount": null,
  "interestRate": null,
  "termMonths": 24,
  "approvalDate": null,
  "disbursementDate": null,
  "destinationAccount": "ES1234567890123456789012",
  "loanStatus": "PENDING",
  "productCode": "PRSTM001",
  "productName": "Préstamo Personal",
  "category": "PERSONAL"
}
```

---

## 🏦 Caso 2: Flujo Completo de Préstamo (Customer → Analyst → Disburse)

### Paso 1️⃣: Analista Obtiene Préstamos Pendientes

**Request:**
```http
GET http://localhost:8080/api/employees/analyst/pending-loans
Content-Type: application/json
Authorization: Bearer {analyst_token}
```

**Response (200 OK):**
```json
[
  {
    "loanId": 1,
    "loanType": "PERSONAL",
    "applicantClientId": "12345678A",
    "requestedAmount": 5000,
    "approvedAmount": null,
    "interestRate": null,
    "termMonths": 24,
    "approvalDate": null,
    "disbursementDate": null,
    "destinationAccount": "ES1234567890123456789012",
    "loanStatus": "PENDING",
    "productCode": "PRSTM001",
    "productName": "Préstamo Personal",
    "category": "PERSONAL"
  }
]
```

### Paso 2️⃣: Analista Aprueba Préstamo

**Request:**
```http
PUT http://localhost:8080/api/employees/analyst/approve-loan/1
Content-Type: application/json
Authorization: Bearer {analyst_token}

{
  "approvedAmount": 4500,
  "interestRate": 4.5
}
```

**Response (200 OK):**
```json
{
  "loanId": 1,
  "loanType": "PERSONAL",
  "applicantClientId": "12345678A",
  "requestedAmount": 5000,
  "approvedAmount": 4500,
  "interestRate": 4.5,
  "termMonths": 24,
  "approvalDate": "2026-05-25T14:30:00Z",
  "disbursementDate": null,
  "destinationAccount": "ES1234567890123456789012",
  "loanStatus": "APPROVED",
  "productCode": "PRSTM001",
  "productName": "Préstamo Personal",
  "category": "PERSONAL"
}
```

### Paso 3️⃣: Analista Desembolsa Préstamo

**Request:**
```http
POST http://localhost:8080/api/employees/analyst/disburse-loan/1
Content-Type: application/json
Authorization: Bearer {analyst_token}

{
  "destinationAccount": "ES1234567890123456789012"
}
```

**Response (200 OK):**
```json
{
  "loanId": 1,
  "loanType": "PERSONAL",
  "applicantClientId": "12345678A",
  "requestedAmount": 5000,
  "approvedAmount": 4500,
  "interestRate": 4.5,
  "termMonths": 24,
  "approvalDate": "2026-05-25T14:30:00Z",
  "disbursementDate": "2026-05-25T14:31:00Z",
  "destinationAccount": "ES1234567890123456789012",
  "loanStatus": "DISBURSED",
  "productCode": "PRSTM001",
  "productName": "Préstamo Personal",
  "category": "PERSONAL"
}
```

---

## 💳 Caso 3: Transferencia de Cliente Individual

### Paso 1️⃣: Cliente Crea Transferencia

**Request:**
```http
POST http://localhost:8080/api/customers/individual/transfer
Content-Type: application/json
Authorization: Bearer {juan_customer_token}

{
  "sourceAccount": "ES1111111111111111111111",
  "destinationAccount": "ES2222222222222222222222",
  "amount": 1500
}
```

**Response (201 CREATED):**
```json
{
  "transferId": 1,
  "sourceAccount": "ES1111111111111111111111",
  "destinationAccount": "ES2222222222222222222222",
  "amount": 1500,
  "creationDate": "2026-05-25T14:32:00Z",
  "approvalDate": null,
  "transferStatus": "PENDING",
  "creatorUserId": "12345678A"
}
```

**⚠️ Nota:** El estado es PENDING porque requiere aprobación de supervisor

---

## 🏢 Caso 4: Transferencia Corporativa + Aprobación de Supervisor

### Paso 1️⃣: Registrar Corporación, Empleado y Supervisor

**Crear Corporación:**
```http
POST http://localhost:8080/api/customers/corporate/register
Content-Type: application/json

{
  "businessName": "TechCorp España S.L.",
  "identificationId": "A12345678",
  "email": "info@techcorp.es",
  "phone": "+34932345678",
  "address": "Calle Empresarial 456, Barcelona",
  "legalRepresentative": "Roberto García Martínez",
  "username": "techcorp.admin",
  "password": "CorpPass123!"
}
```

**Crear Empleado Corporativo:**
```http
POST http://localhost:8080/api/employees/corporate/register
Content-Type: application/json
Authorization: Bearer {analyst_token}

{
  "name": "Fernando Sanz López",
  "identificationId": "90123456I",
  "email": "fernando.corp@banco.es",
  "phone": "+34912345687",
  "address": "Avenida Corporativa 150, Madrid",
  "birthDate": "1991-11-30",
  "relatedId": "A12345678",
  "username": "fernando.corp",
  "password": "CorpEmpPass123!"
}
```

**Crear Supervisor:**
```http
POST http://localhost:8080/api/employees/supervisor/register
Content-Type: application/json
Authorization: Bearer {analyst_token}

{
  "name": "Victoria Hernández Ruiz",
  "identificationId": "01234567J",
  "email": "victoria.supervisor@banco.es",
  "phone": "+34912345688",
  "address": "Calle de Supervisores 200, Madrid",
  "birthDate": "1988-03-25",
  "relatedId": "A12345678",
  "username": "victoria.supervisor",
  "password": "SupervisorPass123!"
}
```

### Paso 2️⃣: Empleado Corporativo Crea Transferencia

**Request:**
```http
POST http://localhost:8080/api/employees/corporate/transfer
Content-Type: application/json
Authorization: Bearer {fernando_corp_token}

{
  "sourceAccount": "ES3333333333333333333333",
  "destinationAccount": "ES4444444444444444444444",
  "amount": 25000
}
```

**Response (201 CREATED):**
```json
{
  "transferId": 2,
  "sourceAccount": "ES3333333333333333333333",
  "destinationAccount": "ES4444444444444444444444",
  "amount": 25000,
  "creationDate": "2026-05-25T15:00:00Z",
  "approvalDate": null,
  "transferStatus": "PENDING",
  "creatorUserId": "90123456I"
}
```

### Paso 3️⃣: Supervisor Ve Transferencias Pendientes

**Request:**
```http
GET http://localhost:8080/api/employees/supervisor/pending-transfers
Authorization: Bearer {victoria_supervisor_token}
```

**Response (200 OK):**
```json
[
  {
    "transferId": 2,
    "sourceAccount": "ES3333333333333333333333",
    "destinationAccount": "ES4444444444444444444444",
    "amount": 25000,
    "creationDate": "2026-05-25T15:00:00Z",
    "approvalDate": null,
    "transferStatus": "PENDING",
    "creatorUserId": "90123456I"
  }
]
```

### Paso 4️⃣: Supervisor Aprueba Transferencia

**Request:**
```http
PUT http://localhost:8080/api/employees/supervisor/approve-transfer/2
Authorization: Bearer {victoria_supervisor_token}
```

**Response (200 OK):**
```json
{
  "transferId": 2,
  "sourceAccount": "ES3333333333333333333333",
  "destinationAccount": "ES4444444444444444444444",
  "amount": 25000,
  "creationDate": "2026-05-25T15:00:00Z",
  "approvalDate": "2026-05-25T15:02:00Z",
  "transferStatus": "APPROVED",
  "creatorUserId": "90123456I"
}
```

---

## 📊 Caso 5: Transferencias Masivas (NÓMINA)

### Paso 1️⃣: Empleado Corporativo Crea Transferencias Masivas

**Request:**
```http
POST http://localhost:8080/api/employees/corporate/bulk-transfer
Content-Type: application/json
Authorization: Bearer {fernando_corp_token}

{
  "transfers": [
    {
      "sourceAccount": "ES3333333333333333333333",
      "destinationAccount": "ES4444444444444444444444",
      "amount": 3500
    },
    {
      "sourceAccount": "ES3333333333333333333333",
      "destinationAccount": "ES5555555555555555555555",
      "amount": 3500
    },
    {
      "sourceAccount": "ES3333333333333333333333",
      "destinationAccount": "ES6666666666666666666666",
      "amount": 3500
    }
  ]
}
```

**Response (201 CREATED):**
```json
[
  {
    "transferId": 3,
    "sourceAccount": "ES3333333333333333333333",
    "destinationAccount": "ES4444444444444444444444",
    "amount": 3500,
    "creationDate": "2026-05-25T15:10:00Z",
    "approvalDate": null,
    "transferStatus": "PENDING",
    "creatorUserId": "90123456I"
  },
  {
    "transferId": 4,
    "sourceAccount": "ES3333333333333333333333",
    "destinationAccount": "ES5555555555555555555555",
    "amount": 3500,
    "creationDate": "2026-05-25T15:10:00Z",
    "approvalDate": null,
    "transferStatus": "PENDING",
    "creatorUserId": "90123456I"
  },
  {
    "transferId": 5,
    "sourceAccount": "ES3333333333333333333333",
    "destinationAccount": "ES6666666666666666666666",
    "amount": 3500,
    "creationDate": "2026-05-25T15:10:00Z",
    "approvalDate": null,
    "transferStatus": "PENDING",
    "creatorUserId": "90123456I"
  }
]
```

---

## 🏪 Caso 6: Operaciones de Cajero (Apertura, Depósito, Retiro)

### Paso 1️⃣: Registrar Empleado de Caja

**Request:**
```http
POST http://localhost:8080/api/employees/teller/register
Content-Type: application/json
Authorization: Bearer {analyst_token}

{
  "name": "Isabel Rodríguez García",
  "identificationId": "78901234G",
  "email": "isabel.teller@banco.es",
  "phone": "+34912345685",
  "address": "Paseo Central 50, Madrid",
  "birthDate": "1994-09-18",
  "username": "isabel.teller",
  "password": "TellerPass123!"
}
```

### Paso 2️⃣: Cajero Abre Nueva Cuenta

**Request:**
```http
POST http://localhost:8080/api/employees/teller/open-account
Content-Type: application/json
Authorization: Bearer {isabel_teller_token}

{
  "accountNumber": "ES5555555555555555555555",
  "accountType": "SAVINGS",
  "accountHolderId": "12345678A",
  "balance": 10000,
  "currency": "EUR",
  "productCode": "ACC001",
  "productName": "Cuenta de Ahorro Premium",
  "category": "PERSONAL",
  "requiresApproval": false
}
```

**Response (201 CREATED):**
```json
{
  "accountNumber": "ES5555555555555555555555",
  "accountType": "SAVINGS",
  "accountHolderId": "12345678A",
  "balance": 10000,
  "currency": "EUR",
  "accountStatus": "ACTIVE",
  "openingDate": "2026-05-25T15:20:00Z",
  "productCode": "ACC001",
  "productName": "Cuenta de Ahorro Premium",
  "category": "PERSONAL"
}
```

### Paso 3️⃣: Cajero Realiza Depósito

**Request:**
```http
POST http://localhost:8080/api/employees/teller/deposit
Content-Type: application/json
Authorization: Bearer {isabel_teller_token}

{
  "accountNumber": "ES5555555555555555555555",
  "amount": 2500
}
```

**Response (200 OK):**
```
(vacío - solo confirma operación)
```

**Balance después:** 12500

### Paso 4️⃣: Cajero Realiza Retiro

**Request:**
```http
POST http://localhost:8080/api/employees/teller/withdraw
Content-Type: application/json
Authorization: Bearer {isabel_teller_token}

{
  "accountNumber": "ES5555555555555555555555",
  "amount": 1000
}
```

**Response (200 OK):**
```
(vacío - solo confirma operación)
```

**Balance después:** 11500

### Paso 5️⃣: Consultar Balance de Cuenta

**Request:**
```http
GET http://localhost:8080/api/employees/teller/account-balance/12345678A
Authorization: Bearer {isabel_teller_token}
```

**Response (200 OK):**
```json
[
  {
    "accountNumber": "ES5555555555555555555555",
    "accountType": "SAVINGS",
    "accountHolderId": "12345678A",
    "balance": 11500,
    "currency": "EUR",
    "accountStatus": "ACTIVE",
    "openingDate": "2026-05-25T15:20:00Z"
  }
]
```

---

## ❌ Caso 7: Validaciones de Error

### Error 1: Login con credenciales inválidas

**Request:**
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "usuario_inexistente",
  "password": "password_invalida"
}
```

**Response (401 UNAUTHORIZED):**
```json
{
  "error": "Invalid credentials",
  "message": "Username or password is incorrect",
  "timestamp": "2026-05-25T15:30:00Z"
}
```

### Error 2: Crear transferencia sin token

**Request:**
```http
POST http://localhost:8080/api/employees/corporate/transfer
Content-Type: application/json

{
  "sourceAccount": "ES3333333333333333333333",
  "destinationAccount": "ES4444444444444444444444",
  "amount": 5000
}
```

**Response (401 UNAUTHORIZED):**
```json
{
  "error": "Unauthorized",
  "message": "Authorization header is missing",
  "timestamp": "2026-05-25T15:31:00Z"
}
```

### Error 3: Registrar cliente con email inválido

**Request:**
```http
POST http://localhost:8080/api/customers/individual/register
Content-Type: application/json

{
  "name": "Carlos López",
  "identificationId": "98765432B",
  "email": "email_invalido",
  "phone": "+34912345680",
  "birthDate": "1988-07-10",
  "address": "Avenida Central 789",
  "username": "carlos.lopez",
  "password": "Pass123!"
}
```

**Response (400 BAD REQUEST):**
```json
{
  "error": "Validation Error",
  "message": "Invalid email format",
  "field": "email",
  "timestamp": "2026-05-25T15:32:00Z"
}
```

### Error 4: RN-11 - Supervisor intenta aprobar propia transferencia

**Request:**
```http
PUT http://localhost:8080/api/employees/supervisor/approve-transfer/999
Authorization: Bearer {victoria_supervisor_token}
```

**Response (400 BAD REQUEST):**
```json
{
  "error": "Business Rule Violation",
  "message": "Supervisor cannot approve their own transfers (RN-11)",
  "timestamp": "2026-05-25T15:33:00Z"
}
```

### Error 5: Retiro con fondos insuficientes

**Request:**
```http
POST http://localhost:8080/api/employees/teller/withdraw
Content-Type: application/json
Authorization: Bearer {isabel_teller_token}

{
  "accountNumber": "ES5555555555555555555555",
  "amount": 999999
}
```

**Response (400 BAD REQUEST):**
```json
{
  "error": "Insufficient Funds",
  "message": "The withdrawal amount (999999) exceeds the available balance (11500)",
  "balance": 11500,
  "timestamp": "2026-05-25T15:34:00Z"
}
```

---

## 🔄 Estados de Transferencias

```
PENDING ──[Aprobación]──> APPROVED ──[Ejecutada]──> COMPLETED
  │
  └──[Rechazo]──> REJECTED
```

---

## 🔄 Estados de Préstamos

```
PENDING ──[Aprobación]──> APPROVED ──[Desembolso]──> DISBURSED
  │
  └──[Rechazo]──> REJECTED
```

---

## 📊 Estados de Cuentas

```
ACTIVE ──[Bloqueo]──> BLOCKED
  │
  └──[Cierre]──> CLOSED
```

---

## 🔐 Matriz de Seguridad

| Rol | Puede Crear | Puede Aprobar | Puede Disbursar |
|-----|-----------|--------------|-----------------|
| CUSTOMER | Transferencia, Préstamo | ❌ | ❌ |
| COMMERCIAL | ❌ | ❌ | ❌ |
| TELLER | Cuenta | ❌ | ❌ |
| CORPORATE_EMPLOYEE | Transferencia (masiva) | ❌ | ❌ |
| SUPERVISOR | ❌ | Transferencia* | ❌ |
| INTERNAL_ANALYST | ❌ | Préstamo | Préstamo |

*No puede aprobar sus propias transferencias (RN-11)

---

## 📝 Notas de Implementación

1. **JWT Expiration**: 30 segundos (configurable en `application.properties`)
2. **Transfer Expiration**: 60 minutos sin aprobar (scheduler automático)
3. **MongoDB Audit**: Todas las operaciones se registran en la bitácora
4. **BigDecimal**: Se usa para balance de cuentas (precisión monetaria)
5. **Soft Delete**: Algunos registros usan `isDeleted` en lugar de eliminarlos

---

**Versión**: 2.0  
**Última actualización**: 2026-05-25  
**Ejemplos probados**: ✅ Todos
