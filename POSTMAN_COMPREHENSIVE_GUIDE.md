# 📊 Colección Postman - Guía de Uso

## 📌 Descripción General

Colección exhaustiva **`banco-comprehensive-api-test.postman_collection.json`** con **60+ requests** que valida todos los endpoints del sistema bancario, incluyendo:

- ✅ **Casos felices** - Flujos normales de negocio
- ❌ **Casos negativos** - Validaciones y restricciones
- 🔒 **Seguridad** - Validación de roles y tokens
- 📋 **Reglas de negocio** - RN-11, CK-05, etc.

---

## 🚀 Configuración Inicial

### 1. Importar la colección en Postman

```bash
# Opción A: Interfaz gráfica de Postman
File → Import → Seleccionar archivo JSON → Importar

# Opción B: CLI
postman collection import banco-comprehensive-api-test.postman_collection.json
```

### 2. Configurar Variables de Entorno

En Postman, edita **Collection > Variables**:

| Variable | Valor Inicial | Descripción |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8080` | URL base de la API |
| `auth_token` | (vacío) | Token JWT de usuario autenticado |
| `analyst_token` | (vacío) | Token del analista interno |
| `supervisor_token` | (vacío) | Token del supervisor corporativo |
| `teller_token` | (vacío) | Token del empleado de caja |
| `corporate_employee_token` | (vacío) | Token del empleado corporativo |
| `individual_customer_token` | (vacío) | Token del cliente individual |
| `transfer_id` | `1` | ID de transferencia para pruebas |
| `loan_id` | `1` | ID de préstamo para pruebas |

---

## 📋 Estructura de la Colección

### 1️⃣ **AUTHENTICATION**
Endpoints de login para acceder al sistema

```
✅ 1.1 Login - Internal Analyst (SUCCESS)
   POST /auth/login
   Body: { "username": "analyst_user", "password": "analyst_password" }
   Response: { "token": "eyJhbGc..." }
   [Script Post-test guardar token en {{analyst_token}}]

❌ 1.2 Login - Username vacío
❌ 1.3 Login - Password vacío
❌ 1.4 Login - Usuario no existe
```

**Flujo recomendado**: Ejecutar 1.1 PRIMERO para obtener tokens válidos.

---

### 2️⃣ **INDIVIDUAL CUSTOMERS**
Clientes individuales: registro, préstamos, transferencias

```
✅ 2.1 Register - Cliente Individual (SUCCESS)
   POST /api/customers/individual/register
   Response: 201 CREATED + datos cliente + rol (CUSTOMER)

✅ 2.5 Request Loan - Cliente Individual (SUCCESS)
   POST /api/customers/individual/request-loan
   Headers: Authorization: Bearer {{individual_customer_token}}
   Body: { "loanType": "PERSONAL", "requestedAmount": 5000, ... }
   Response: 201 CREATED + LoanResponse (status: PENDING)

✅ 2.6 Create Transfer - Cliente Individual (SUCCESS)
   POST /api/customers/individual/transfer
   Response: 201 CREATED + TransferResponse

✅ 2.7 Get My Accounts - Cliente Individual (SUCCESS)
   GET /api/customers/individual/my-accounts

❌ 2.8 Request Loan - Sin token (FORBIDDEN)
   Response: 401 UNAUTHORIZED (no Authorization header)
```

**Reglas de negocio**:
- Cliente solo puede ver sus propias cuentas
- Transferencias requieren aprobación de supervisor
- Préstamos requieren aprobación de analista

---

### 3️⃣ **CORPORATE CUSTOMERS**
Clientes corporativos

```
✅ 3.1 Register - Cliente Corporativo (SUCCESS)
   POST /api/customers/corporate/register
   Fields: businessName, identificationId, legalRepresentative, ...

❌ 3.2 Register - BusinessName vacío
❌ 3.3 Register - LegalRepresentative vacío
```

---

### 4️⃣ **COMMERCIAL EMPLOYEES**
Empleados comerciales (requiere token)

```
✅ 4.1 Register - Empleado Comercial (SUCCESS)
   POST /api/employees/commercial/register
   Headers: Authorization: Bearer {{analyst_token}}
   Response: 201 CREATED + rol (COMMERCIAL)

❌ 4.2 Register - Sin Authorization (FORBIDDEN)
   Response: 403 FORBIDDEN
```

**Restricciones**: Solo pueden ser registrados por analista interno

---

### 5️⃣ **TELLER EMPLOYEES**
Empleados de caja: cuentas, depósitos, retiros

```
✅ 5.1 Register - Empleado de Caja (SUCCESS)
   POST /api/employees/teller/register

✅ 5.3 Open Account - Crear cuenta nueva
   POST /api/employees/teller/open-account
   Body: { "accountNumber", "accountType", "balance", ... }

✅ 5.4 Deposit - Depósito en cuenta
   POST /api/employees/teller/deposit
   Body: { "accountNumber": "ES5555...", "amount": 2500 }

✅ 5.5 Withdraw - Retiro de cuenta
   POST /api/employees/teller/withdraw

✅ 5.6 Get Account Balance
   GET /api/employees/teller/account-balance/{identificationId}

❌ 5.7 Deposit - Monto negativo
   Response: 400 BAD REQUEST (validación de negocio)
```

---

### 6️⃣ **CORPORATE EMPLOYEES**
Empleados corporativos: transferencias

```
✅ 6.1 Register - Empleado Corporativo (SUCCESS)
   POST /api/employees/corporate/register
   Field adicional: relatedId (ID de corporación)

✅ 6.2 Create Transfer - Transferencia individual
   POST /api/employees/corporate/transfer
   Body: { "sourceAccount", "destinationAccount", "amount" }
   [Requiere aprobación de supervisor]

✅ 6.3 Bulk Transfer - Transferencias masivas (NÓMINA)
   POST /api/employees/corporate/bulk-transfer
   Body: { "transfers": [ {...}, {...}, {...} ] }
   Response: Array de TransferResponse

✅ 6.4 Get My Transfers - Historial
   GET /api/employees/corporate/my-transfers/{accountNumber}

❌ 6.5 Create Transfer - Sin token
❌ 6.6 Create Transfer - Monto cero
```

---

### 7️⃣ **CORPORATE SUPERVISORS**
Supervisores: aprobar/rechazar transferencias

```
✅ 7.1 Register - Supervisor Corporativo (SUCCESS)
   POST /api/employees/supervisor/register

✅ 7.2 Get Pending Transfers
   GET /api/employees/supervisor/pending-transfers
   Response: Array de transferencias con status PENDING

✅ 7.3 Approve Transfer
   PUT /api/employees/supervisor/approve-transfer/{transferId}
   Response: TransferResponse + status: APPROVED + timestamp

✅ 7.4 Reject Transfer
   PUT /api/employees/supervisor/reject-transfer/{transferId}
   Response: TransferResponse + status: REJECTED

❌ 7.5 Approve Transfer - Sin token
   Response: 401 UNAUTHORIZED

❌ 7.6 Approve Own Transfer (RN-11 BUSINESS RULE)
   Response: 400 BAD REQUEST
   Error: "Supervisor cannot approve own transfers"
```

**Regla RN-11**: Un supervisor NO puede aprobar transferencias que él mismo creó.

---

### 8️⃣ **INTERNAL ANALYSTS**
Analistas: aprobar/rechazar/disbursar préstamos

```
✅ 8.1 Register - Analista Interno (SUCCESS)
   POST /api/employees/analyst/register

✅ 8.2 Get Pending Loans
   GET /api/employees/analyst/pending-loans
   Response: Array de LoanResponse con status PENDING

✅ 8.3 Approve Loan - Aprobación con monto e interés
   PUT /api/employees/analyst/approve-loan/{loanId}
   Body: { "approvedAmount": 5000, "interestRate": 4.5 }
   Response: LoanResponse + status: APPROVED

✅ 8.4 Reject Loan - Rechazar préstamo
   PUT /api/employees/analyst/reject-loan/{loanId}
   Response: LoanResponse + status: REJECTED

✅ 8.5 Disburse Loan - Desembolsar (enviar dinero)
   POST /api/employees/analyst/disburse-loan/{loanId}
   Body: { "destinationAccount": "ES1234..." }
   Response: LoanResponse + status: DISBURSED

❌ 8.6 Reject Loan - Sin token
❌ 8.7 Approve Loan - InterestRate negativo
```

---

### 9️⃣ **SECURITY & ROLE VALIDATION**
Validaciones de seguridad y restricciones de rol

```
❌ 9.1 Access without JWT token
   GET /api/customers/individual/my-accounts
   Response: 401 UNAUTHORIZED

❌ 9.2 Access with Invalid JWT token
   Response: 401 UNAUTHORIZED (token malformado o expirado)

❌ 9.3 COMMERCIAL EMPLOYEE - No puede crear transferencia
   POST /api/employees/commercial/transfer
   Response: 403 FORBIDDEN (no autorizado para este endpoint)

❌ 9.4 TELLER - No puede rechazar transferencias
   Response: 403 FORBIDDEN

❌ 9.5 SUPERVISOR - No puede disbursar préstamos
   Response: 403 FORBIDDEN

❌ 9.6 RN-11: Supervisor no aprueba propias transferencias
   Response: 400 BAD REQUEST
```

---

### 🔟 **DATA VALIDATION EDGE CASES**
Validaciones de campos y límites

```
❌ 10.1 Register - Duplicated IdentificationId
   Response: 400 BAD REQUEST / 409 CONFLICT
   Error: "IdentificationId already exists"

❌ 10.2 Register - Phone inválido
   Response: 400 BAD REQUEST
   Error: "Invalid phone format"

❌ 10.3 Transfer - Cuenta origen = destino
   Response: 400 BAD REQUEST
   Error: "Source and destination accounts cannot be the same"

❌ 10.4 Loan - Amount > MaxLimit
   Response: 400 BAD REQUEST
   Error: "Amount exceeds maximum limit"

❌ 10.5 Withdraw - Fondos insuficientes
   Response: 400 BAD REQUEST
   Error: "Insufficient funds"
```

---

## ⚙️ Flujo de Ejecución Recomendado

### **Flujo Básico (5 min)**
```
1. 1.1 Login (obtener analyst_token)
2. 2.1 Register Individual Customer
3. 2.5 Request Loan
4. 2.6 Create Transfer
5. 7.3 Approve Transfer (como supervisor)
6. 8.3 Approve Loan (como analyst)
```

### **Flujo Completo (20 min)**
```
1. [AUTHENTICATION]
   ├─ 1.1 Login (Analyst) → guardar token

2. [REGISTRATION]
   ├─ 2.1 Register Individual Customer
   ├─ 3.1 Register Corporate Customer
   ├─ 4.1 Register Commercial Employee
   ├─ 5.1 Register Teller Employee
   ├─ 6.1 Register Corporate Employee
   ├─ 7.1 Register Corporate Supervisor
   └─ 8.1 Register Internal Analyst

3. [TELLER OPERATIONS]
   ├─ 5.3 Open Account
   ├─ 5.4 Deposit
   ├─ 5.5 Withdraw
   └─ 5.6 Get Account Balance

4. [CUSTOMER OPERATIONS]
   ├─ 2.5 Request Loan
   ├─ 2.6 Create Transfer
   ├─ 2.7 Get My Accounts
   └─ 6.3 Bulk Transfer

5. [SUPERVISOR OPERATIONS]
   ├─ 7.2 Get Pending Transfers
   ├─ 7.3 Approve Transfer
   └─ 7.4 Reject Transfer

6. [ANALYST OPERATIONS]
   ├─ 8.2 Get Pending Loans
   ├─ 8.3 Approve Loan
   ├─ 8.4 Reject Loan
   └─ 8.5 Disburse Loan

7. [SECURITY VALIDATION]
   ├─ 9.1 - 9.6 (Validaciones de acceso)
   └─ [EDGE CASES]
       ├─ 10.1 - 10.5 (Validaciones de datos)
```

---

## 🔐 Autenticación y Tokens

### Variables de Token Automáticas

Algunos requests incluyen **scripts post-test** que guardan tokens automáticamente:

```javascript
// Ejemplo en 1.1 Login
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set('analyst_token', jsonData.token);
    console.log('✅ Analyst Token guardado');
}
```

### Tokens de Prueba (Usuarios Seeder)

El proyecto incluye usuarios pre-creados en `DataSeeder.java`:

```
Username: analyst_user
Password: analyst_password
Rol: INTERNAL_ANALYST
```

**Para crear más usuarios**: Ejecutar requests de registro (2.1, 3.1, etc.)

---

## 📊 Matriz de Acceso por Rol

| Endpoint | CUSTOMER | COMMERCIAL | TELLER | CORP_EMP | SUPERVISOR | ANALYST |
|----------|----------|-----------|--------|----------|-----------|---------|
| POST /register | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| POST /request-loan | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| POST /transfer | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| POST /open-account | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| POST /deposit | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| GET /pending-transfers | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| PUT /approve-transfer | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| GET /pending-loans | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| PUT /approve-loan | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| POST /disburse-loan | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## 🐛 Resolución de Problemas

### Error: 401 UNAUTHORIZED
**Causa**: Token inválido o expirado
**Solución**: 
1. Ejecutar 1.1 Login nuevamente
2. Copiar token en la variable correspondiente
3. Verificar que Authorization header tenga formato: `Bearer {token}`

### Error: 403 FORBIDDEN
**Causa**: Usuario no tiene permisos para este endpoint
**Solución**: 
1. Verificar el rol del usuario
2. Usar el endpoint correcto para el rol
3. Consultar matriz de acceso arriba

### Error: 400 BAD REQUEST
**Causa**: Datos inválidos o violación de regla de negocio
**Solución**:
1. Revisar el response error message
2. Validar formato de datos en el body
3. Consultar casos negativos (❌) correspondientes

### Error: 409 CONFLICT
**Causa**: El recurso ya existe (ej: IdentificationId duplicado)
**Solución**:
1. Usar valores únicos (ej: ID diferente)
2. Limpiar base de datos si es necesario

---

## 📝 Casos de Prueba Especiales

### ✅ Flujo de Préstamo Completo
```
2.1 Register Customer
  ↓
2.5 Request Loan (status: PENDING)
  ↓
8.2 Get Pending Loans (como Analyst)
  ↓
8.3 Approve Loan (status: APPROVED)
  ↓
8.5 Disburse Loan (status: DISBURSED)
```

### ✅ Flujo de Transferencia Completa
```
6.1 Register Corporate Employee
  ↓
6.2 Create Transfer (status: PENDING)
  ↓
7.2 Get Pending Transfers (como Supervisor)
  ↓
7.3 Approve Transfer (status: APPROVED)
```

### ❌ Regla RN-11 (Supervisor no aprueba propias)
```
6.1 Register Corporate Employee X
  ↓
6.1 Register Corporate Supervisor Y (mismo relatedId)
  ↓
6.2 Create Transfer (Employee X) (status: PENDING)
  ↓
7.3 Approve Transfer (Supervisor Y) → ✅ SUCCESS

PERO:
6.2 Create Transfer (Supervisor Y) (status: PENDING)
  ↓
7.3 Approve Transfer (Supervisor Y) → ❌ FORBIDDEN (RN-11)
```

---

## 📌 Notas Importantes

1. **DataSeeder**: Crea automáticamente usuario `analyst_user` al iniciar
2. **Scheduler**: Transferencias expiran en 60 minutos (si no son aprobadas)
3. **MongoDB**: Bitácora de auditoría en cada operación (automatizada)
4. **JWT**: Token expira en 30 segundos (valor en `application.properties`)
5. **BigDecimal**: Balance de cuentas usa `BigDecimal` (alta precisión)

---

## 🔗 Recursos Relacionados

- [PROJECT_SPEC.md](PROJECT_SPEC.md) - Especificación técnica completa
- [CLAUDE.md](CLAUDE.md) - Guía de trabajo para desarrollo
- [POSTMAN_COLLECTION_README.md](POSTMAN_COLLECTION_README.md) - Versión anterior
- [banco-comprehensive-api-test.postman_collection.json](banco-comprehensive-api-test.postman_collection.json) - Archivo JSON

---

## ✅ Checklist de Validación

Después de ejecutar la colección, verifica:

- [ ] ✅ Todos los requests de casos felices retornan códigos 2xx
- [ ] ❌ Todos los requests de casos negativos retornan códigos 4xx
- [ ] 🔒 Endpoints protegidos rechazar sin token
- [ ] 📋 Supervisors no pueden aprobar propias transferencias (RN-11)
- [ ] 📋 Internal Analysts solo pueden ver préstamos PENDING
- [ ] 📋 Corporate Supervisors solo pueden ver transferencias de su corporación
- [ ] 🔄 Transferencias pasan por estados: PENDING → APPROVED/REJECTED
- [ ] 🔄 Préstamos pasan por estados: PENDING → APPROVED/REJECTED → DISBURSED
- [ ] 💾 MongoDB contiene registros de auditoría
- [ ] 📊 Balance de cuentas se actualiza correctamente

---

**Última actualización**: 2026-05-25
**Versión**: 2.0 (Exhaustiva)
**Total de Requests**: 60+
**Secciones**: 10
