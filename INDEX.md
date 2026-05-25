# 📚 ÍNDICE CENTRAL - DOCUMENTACIÓN Y TESTING

## 🎯 Quick Start (5 minutos)

**¿Acabas de llegar?** Lee esto primero:

1. 📖 [CLAUDE.md](CLAUDE.md) - Estado actual del proyecto
2. 📊 [PROJECT_SPEC.md](PROJECT_SPEC.md) - Qué debe hacer el sistema
3. 🚀 [README.md](README.md) - Cómo ejecutar

**Luego importa la colección Postman:**

4. 📮 [banco-comprehensive-api-test.postman_collection.json](banco-comprehensive-api-test.postman_collection.json) - Colección JSON

---

## 📂 ESTRUCTURA DE DOCUMENTOS

### 🏗️ Arquitectura y Especificación

| Archivo | Contenido | Audiencia |
|---------|-----------|-----------|
| [PROJECT_SPEC.md](PROJECT_SPEC.md) | Especificación técnica exhaustiva del proyecto (requisitos, arquitectura, reglas de negocio) | Developers, QA, Auditors |
| [CLAUDE.md](CLAUDE.md) | Guía de trabajo para desarrollo (estado, reglas, commit strategy) | Developers |
| [README.md](README.md) | Descripción general e instrucciones de instalación | Everyone |
| [LICENSE](LICENSE) | Licencia del proyecto | Legal |

### 🧪 Testing y Validación

| Archivo | Contenido | Audiencia |
|---------|-----------|-----------|
| **[banco-comprehensive-api-test.postman_collection.json](banco-comprehensive-api-test.postman_collection.json)** | **Colección JSON con 60+ requests** (Casos felices + negativos) | QA, Testers |
| [POSTMAN_COMPREHENSIVE_GUIDE.md](POSTMAN_COMPREHENSIVE_GUIDE.md) | Guía completa para usar la colección (setup, flujos, matriz de acceso) | QA, Testers, Developers |
| [POSTMAN_EXECUTION_EXAMPLES.md](POSTMAN_EXECUTION_EXAMPLES.md) | Ejemplos reales con request/response (7 casos de uso completos) | QA, Testers |
| [VALIDATION_CHECKLIST.md](VALIDATION_CHECKLIST.md) | 142 items para validar todas las funcionalidades | QA, Project Manager |

### 📮 API Documentation

| Archivo | Contenido | Audiencia |
|---------|-----------|-----------|
| [POSTMAN_COLLECTION_README.md](POSTMAN_COLLECTION_README.md) | README de colección anterior (versión 1.0, básica) | QA (Legacy) |

---

## 🚀 CÓMO EMPEZAR

### Paso 1: Setup Inicial

```bash
# 1. Clonar proyecto
git clone <repo>
cd Gestion-de-Informacion-de-un-Banco/Banco

# 2. Crear bases de datos
mysql -u root -p
CREATE DATABASE banco;
exit

# 3. Compilar y ejecutar
mvn clean install
mvn spring-boot:run
```

**Verificar**: http://localhost:8080/auth/login debe estar disponible

### Paso 2: Importar Colección Postman

```bash
# Opción A: GUI
# Postman → File → Import → Seleccionar banco-comprehensive-api-test.postman_collection.json

# Opción B: CLI
postman collection import banco-comprehensive-api-test.postman_collection.json
```

### Paso 3: Ejecutar Pruebas

```bash
# En Postman:
1. Ir a carpeta "1️⃣ AUTHENTICATION"
2. Ejecutar "1.1 Login - Internal Analyst (SUCCESS)"
3. Token se guarda automáticamente en {{analyst_token}}
4. Ejecutar requests sucesivos en orden
```

---

## 📊 MAPA DE ENDPOINTS

### 🔐 Authentication
```
POST /auth/login
├─ Request: { username, password }
└─ Response: { token, username, role }
```

### 👤 Individual Customers
```
POST   /api/customers/individual/register
POST   /api/customers/individual/request-loan        [AUTH]
POST   /api/customers/individual/transfer            [AUTH]
GET    /api/customers/individual/my-accounts         [AUTH]
```

### 🏢 Corporate Customers
```
POST   /api/customers/corporate/register
```

### 👨‍💼 Commercial Employees
```
POST   /api/employees/commercial/register            [AUTH]
```

### 🏦 Teller Employees
```
POST   /api/employees/teller/register                [AUTH]
POST   /api/employees/teller/open-account            [AUTH]
POST   /api/employees/teller/deposit                 [AUTH]
POST   /api/employees/teller/withdraw                [AUTH]
GET    /api/employees/teller/account-balance/{id}    [AUTH]
```

### 👨‍💼 Corporate Employees
```
POST   /api/employees/corporate/register             [AUTH]
POST   /api/employees/corporate/transfer             [AUTH]
POST   /api/employees/corporate/bulk-transfer        [AUTH]
GET    /api/employees/corporate/my-transfers/{acc}   [AUTH]
```

### 👔 Corporate Supervisors
```
POST   /api/employees/supervisor/register            [AUTH]
GET    /api/employees/supervisor/pending-transfers   [AUTH]
PUT    /api/employees/supervisor/approve-transfer/:id [AUTH]
PUT    /api/employees/supervisor/reject-transfer/:id  [AUTH]
```

### 📊 Internal Analysts
```
POST   /api/employees/analyst/register               [AUTH]
GET    /api/employees/analyst/pending-loans          [AUTH]
PUT    /api/employees/analyst/approve-loan/:id       [AUTH]
PUT    /api/employees/analyst/reject-loan/:id        [AUTH]
POST   /api/employees/analyst/disburse-loan/:id      [AUTH]
```

---

## 🧪 CASOS DE PRUEBA POR TIPO

### ✅ Casos Felices (Happy Path)
- Registro de usuario
- Login exitoso
- Crear transferencia
- Solicitar préstamo
- Aprobar/rechazar/disbursar
- Depósito/retiro
- Ver historial

**Dónde probar**: [POSTMAN_EXECUTION_EXAMPLES.md](POSTMAN_EXECUTION_EXAMPLES.md) - Casos 1-6

### ❌ Casos Negativos (Edge Cases)
- Email inválido
- IdentificationId duplicado
- Fondos insuficientes
- Monto negativo
- Token inválido
- Sin Authorization
- Supervisor aprueba propia transferencia (RN-11)

**Dónde probar**: Sección 9-10 de [banco-comprehensive-api-test.postman_collection.json](banco-comprehensive-api-test.postman_collection.json)

### 🔒 Casos de Seguridad
- Acceso sin token → 401
- Token expirado → 401
- Rol incorrecto → 403
- COMMERCIAL_EMPLOYEE crea transfer → 403
- TELLER rechaza transfer → 403

**Dónde probar**: Sección 9 "SECURITY & ROLE VALIDATION"

### 📋 Casos de Reglas de Negocio
- RN-11: Supervisor no aprueba propias transferencias
- CK-05: Solo INTERNAL_ANALYST registra empleados
- RF-05-E: PreviousStatus/NewStatus en logs
- RF-04-C: Pagos masivos/nómina

**Dónde probar**: [VALIDATION_CHECKLIST.md](VALIDATION_CHECKLIST.md) - Sección 7

---

## 📈 FLUJOS DE TRABAJO COMPLETOS

### 🏦 Flujo 1: Préstamo Completo (5 requests)

```
1. Cliente Individual se registra
   POST /api/customers/individual/register
   
2. Cliente hace login
   POST /auth/login
   
3. Cliente solicita préstamo
   POST /api/customers/individual/request-loan [AUTH]
   Status: PENDING
   
4. Analista ve préstamos pendientes
   GET /api/employees/analyst/pending-loans [AUTH]
   
5. Analista aprueba
   PUT /api/employees/analyst/approve-loan/1 [AUTH]
   Status: APPROVED
   
6. Analista desembolsa
   POST /api/employees/analyst/disburse-loan/1 [AUTH]
   Status: DISBURSED
```

**Referencia**: [POSTMAN_EXECUTION_EXAMPLES.md](POSTMAN_EXECUTION_EXAMPLES.md) - Caso 2

---

### 💳 Flujo 2: Transferencia Corporativa (6 requests)

```
1. Registrar corporación
   POST /api/customers/corporate/register
   
2. Registrar empleado corporativo
   POST /api/employees/corporate/register [AUTH]
   
3. Registrar supervisor
   POST /api/employees/supervisor/register [AUTH]
   
4. Empleado crea transferencia
   POST /api/employees/corporate/transfer [AUTH]
   Status: PENDING
   
5. Supervisor ve pendientes
   GET /api/employees/supervisor/pending-transfers [AUTH]
   
6. Supervisor aprueba
   PUT /api/employees/supervisor/approve-transfer/2 [AUTH]
   Status: APPROVED
```

**Referencia**: [POSTMAN_EXECUTION_EXAMPLES.md](POSTMAN_EXECUTION_EXAMPLES.md) - Caso 4

---

### 💰 Flujo 3: Operaciones de Cajero (5 requests)

```
1. Registrar cajero
   POST /api/employees/teller/register [AUTH]
   
2. Abrir cuenta
   POST /api/employees/teller/open-account [AUTH]
   
3. Depósito
   POST /api/employees/teller/deposit [AUTH]
   
4. Retiro
   POST /api/employees/teller/withdraw [AUTH]
   
5. Consultar balance
   GET /api/employees/teller/account-balance/{id} [AUTH]
```

**Referencia**: [POSTMAN_EXECUTION_EXAMPLES.md](POSTMAN_EXECUTION_EXAMPLES.md) - Caso 6

---

## 📊 MATRIZ DE ACCESO POR ROL

| Endpoint | CUSTOMER | COMMERCIAL | TELLER | CORP_EMP | SUPERVISOR | ANALYST |
|----------|----------|-----------|--------|----------|-----------|---------|
| Register Customer | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Request Loan | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Create Transfer | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Open Account | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Deposit/Withdraw | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Pending Transfers | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Approve Transfer | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Pending Loans | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Approve Loan | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Disburse Loan | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

**Referencia**: [POSTMAN_COMPREHENSIVE_GUIDE.md](POSTMAN_COMPREHENSIVE_GUIDE.md) - Sección "Matriz de Acceso por Rol"

---

## 🔍 CÓMO BUSCAR INFORMACIÓN

### "¿Dónde está el endpoint X?"
→ [Mapa de Endpoints](#-mapa-de-endpoints) arriba

### "¿Cuál es la estructura de request/response?"
→ [POSTMAN_EXECUTION_EXAMPLES.md](POSTMAN_EXECUTION_EXAMPLES.md)

### "¿Cómo ejecuto un flujo completo?"
→ [POSTMAN_COMPREHENSIVE_GUIDE.md](POSTMAN_COMPREHENSIVE_GUIDE.md) - Sección "Flujo de Ejecución Recomendado"

### "¿Qué debo validar después de cada cambio?"
→ [VALIDATION_CHECKLIST.md](VALIDATION_CHECKLIST.md)

### "¿Cuáles son las reglas de negocio?"
→ [PROJECT_SPEC.md](PROJECT_SPEC.md) - Sección 6 "Reglas de Negocio"

### "¿Cómo es la arquitectura?"
→ [PROJECT_SPEC.md](PROJECT_SPEC.md) - Sección 2 "Tecnologías y Arquitectura"

### "¿Cuáles son las restricciones por rol?"
→ [PROJECT_SPEC.md](PROJECT_SPEC.md) - Sección 8 "Restricciones por Rol"

### "¿Qué casos de prueba existen?"
→ [banco-comprehensive-api-test.postman_collection.json](banco-comprehensive-api-test.postman_collection.json) (60+ requests)

---

## 📋 CHECKLIST RÁPIDO

- [ ] Leí CLAUDE.md (estado actual)
- [ ] Leí PROJECT_SPEC.md (especificación)
- [ ] Importé colección Postman
- [ ] Ejecuté 1.1 Login exitosamente
- [ ] Ejecuté al menos un request con token
- [ ] Ejecuté al menos un request que falla (validación)
- [ ] Revisé POSTMAN_EXECUTION_EXAMPLES.md

---

## 🔗 LINKS RÁPIDOS

### Documentación
- [PROJECT_SPEC.md](PROJECT_SPEC.md) - 📖 Especificación técnica
- [CLAUDE.md](CLAUDE.md) - 👨‍💻 Guía de desarrollo
- [README.md](README.md) - 📄 Inicio rápido

### Testing
- [banco-comprehensive-api-test.postman_collection.json](banco-comprehensive-api-test.postman_collection.json) - 📮 **Colección JSON (60+ requests)**
- [POSTMAN_COMPREHENSIVE_GUIDE.md](POSTMAN_COMPREHENSIVE_GUIDE.md) - 📘 Guía de uso
- [POSTMAN_EXECUTION_EXAMPLES.md](POSTMAN_EXECUTION_EXAMPLES.md) - 🎯 Ejemplos reales
- [VALIDATION_CHECKLIST.md](VALIDATION_CHECKLIST.md) - ✅ 142 items de validación

### Legacy
- [POSTMAN_COLLECTION_README.md](POSTMAN_COLLECTION_README.md) - 📜 Versión anterior (v1.0)

---

## 📞 SOPORTE

### Error: 401 UNAUTHORIZED
→ Ejecutar 1.1 Login primero
→ Verificar que Authorization header sea: `Bearer {token}`

### Error: 403 FORBIDDEN
→ Verificar rol del usuario
→ Consultar matriz de acceso arriba

### Error: 400 BAD REQUEST
→ Revisar validaciones en PROJECT_SPEC.md - Sección 7
→ Ver POSTMAN_EXECUTION_EXAMPLES.md - Caso 7 (Errores)

### ¿Cómo reseteo la base de datos?
→ `DROP DATABASE banco; CREATE DATABASE banco;`
→ Reiniciar app (DataSeeder recreará datos)

---

## 📊 ESTADÍSTICAS

- **Total de Endpoints**: 25
- **Total de Requests (Postman)**: 60+
- **Casos de Uso Documentados**: 7 completos
- **Reglas de Negocio**: 10+
- **Validaciones**: 142 items
- **Roles**: 6 (CUSTOMER, COMMERCIAL, TELLER, CORP_EMP, SUPERVISOR, ANALYST)
- **Estados de Negocio**: 3 (Transferencias, Préstamos, Cuentas)

---

## 🎯 OBJETIVOS

✅ Sistema bancario funcional  
✅ Transferencias con aprobación  
✅ Préstamos con análisis  
✅ Seguridad por roles  
✅ Auditoría completa  
✅ Tests exhaustivos  

---

## 📝 Archivos Nuevos en v2.0

| Archivo | Descripción | Creado |
|---------|-------------|--------|
| **banco-comprehensive-api-test.postman_collection.json** | Colección JSON exhaustiva (60+ requests) | ✨ Nuevo |
| **POSTMAN_COMPREHENSIVE_GUIDE.md** | Guía completa de uso de Postman | ✨ Nuevo |
| **POSTMAN_EXECUTION_EXAMPLES.md** | 7 casos completos con request/response | ✨ Nuevo |
| **VALIDATION_CHECKLIST.md** | 142 items de validación | ✨ Nuevo |
| **INDEX.md** (este archivo) | Índice central de documentación | ✨ Nuevo |

---

**Versión**: 2.0  
**Última actualización**: 2026-05-25  
**Estado**: ✅ Completo y listo para testing

---

## 🚀 ¿Listo para empezar?

1. ✅ Ejecuta el proyecto: `mvn spring-boot:run`
2. 📮 Importa colección Postman
3. 🧪 Ejecuta request 1.1 Login
4. ✅ Comienza a testear

¡Bienvenido al sistema bancario! 🏦
