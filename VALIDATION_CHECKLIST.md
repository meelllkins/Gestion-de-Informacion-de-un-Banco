# ✅ CHECKLIST DE VALIDACIÓN - SISTEMA BANCARIO

## 📊 Estado General

**Proyecto**: Gestion-de-Informacion-de-un-Banco  
**Stack**: Java 17 + Spring Boot 4.0.3 + MySQL + MongoDB  
**Fecha de validación**: 2026-05-25  
**Total de Endpoints**: 25+  
**Total de Test Cases**: 60+

---

## 🔐 1. AUTENTICACIÓN Y SEGURIDAD

### Login y Tokens
- [ ] **1.1** Login exitoso retorna token JWT válido
- [ ] **1.2** Login con credenciales inválidas retorna 401 UNAUTHORIZED
- [ ] **1.3** Token JWT se puede validar (estructura correcta)
- [ ] **1.4** Token expira después de 30 segundos
- [ ] **1.5** Request sin Authorization header retorna 401
- [ ] **1.6** Request con token malformado retorna 401
- [ ] **1.7** Token contiene rol del usuario (CUSTOMER, INTERNAL_ANALYST, etc.)

### Control de Acceso (RBAC)
- [ ] **2.1** COMMERCIAL_EMPLOYEE no puede crear transferencias
- [ ] **2.2** TELLER no puede rechazar transferencias
- [ ] **2.3** SUPERVISOR no puede disbursar préstamos
- [ ] **2.4** INTERNAL_ANALYST solo ve préstamos PENDING
- [ ] **2.5** CUSTOMER solo ve sus propias cuentas
- [ ] **2.6** CORPORATE_EMPLOYEE solo ve transferencias de su corporación

---

## 👥 2. REGISTRO Y CREACIÓN DE USUARIOS

### Clientes Individuales
- [ ] **3.1** Registro exitoso (201 CREATED) con todos los campos
- [ ] **3.2** Cliente recibe rol CUSTOMER
- [ ] **3.3** Credenciales de login funcionan inmediatamente
- [ ] **3.4** Se valida email (formato válido)
- [ ] **3.5** Se valida teléfono (formato válido)
- [ ] **3.6** Se rechaza nombre vacío (400 BAD REQUEST)
- [ ] **3.7** Se rechaza email vacío (400 BAD REQUEST)
- [ ] **3.8** Se rechaza birthDate null (400 BAD REQUEST)
- [ ] **3.9** Se rechaza IdentificationId duplicado (409 CONFLICT)

### Clientes Corporativos
- [ ] **4.1** Registro exitoso con datos empresariales
- [ ] **4.2** Se valida businessName (no vacío)
- [ ] **4.3** Se valida legalRepresentative (no vacío)
- [ ] **4.4** Se valida identificationId (formato NIF)
- [ ] **4.5** Cliente recibe rol CORPORATE_CUSTOMER

### Empleados (Comercial, Cajero, Corporativo, Supervisor, Analista)
- [ ] **5.1** Registro exitoso requiere token (403 sin auth)
- [ ] **5.2** Solo INTERNAL_ANALYST puede registrar empleados (RN-???)
- [ ] **5.3** Empleados reciben rol correcto según tipo
- [ ] **5.4** Datos de empleado se validan correctamente

---

## 💰 3. OPERACIONES DE CAJERO (TELLER)

### Apertura de Cuentas
- [ ] **6.1** Crear cuenta exitosa (201 CREATED)
- [ ] **6.2** Cuenta se crea con estado ACTIVE
- [ ] **6.3** Balance inicial es correcto
- [ ] **6.4** Número de cuenta único (no duplicados)
- [ ] **6.5** Validar accountType (CHECKING, SAVINGS, etc.)

### Depósito
- [ ] **7.1** Depósito exitoso actualiza balance (+)
- [ ] **7.2** Depósito se registra en MongoDB (auditoría)
- [ ] **7.3** Se rechaza monto negativo (400 BAD REQUEST)
- [ ] **7.4** Se rechaza monto cero (400 BAD REQUEST)
- [ ] **7.5** Se valida cuenta existe

### Retiro
- [ ] **8.1** Retiro exitoso actualiza balance (-)
- [ ] **8.2** Retiro se registra en auditoría
- [ ] **8.3** Se rechaza retiro sin fondos (400 BAD REQUEST)
- [ ] **8.4** Se rechaza retiro > balance disponible
- [ ] **8.5** Se rechaza monto negativo (400 BAD REQUEST)

### Consulta de Balance
- [ ] **9.1** GET balance retorna lista de cuentas
- [ ] **9.2** Balance refleja depósitos/retiros recientes
- [ ] **9.3** Solo cajero ve cuentas del titular (seguridad)

---

## 💳 4. TRANSFERENCIAS

### Transferencia Individual (Cliente Individual)
- [ ] **10.1** Crear transferencia exitosa (201 CREATED)
- [ ] **10.2** Transferencia inicia en estado PENDING
- [ ] **10.3** Se valida sourceAccount existe
- [ ] **10.4** Se valida destinationAccount existe
- [ ] **10.5** Se rechaza monto cero (400 BAD REQUEST)
- [ ] **10.6** Se rechaza monto negativo (400 BAD REQUEST)
- [ ] **10.7** Se rechaza si source = destination

### Transferencia Corporativa (Empleado Corporativo)
- [ ] **11.1** Crear transferencia exitosa
- [ ] **11.2** Requiere token de CORPORATE_EMPLOYEE
- [ ] **11.3** Solo puede crear empleado de su corporación

### Transferencias Masivas (Nómina)
- [ ] **12.1** Crear bulk transfer con múltiples transferencias (201 CREATED)
- [ ] **12.2** Retorna array de TransferResponse
- [ ] **12.3** Todas las transferencias inician PENDING
- [ ] **12.4** Se validan todas las transferencias antes de crear
- [ ] **12.5** Si una falla, todas fallan (transacción)

### Aprobación de Transferencias (Supervisor)
- [ ] **13.1** Ver transferencias PENDING (GET /pending-transfers)
- [ ] **13.2** Aprobar transferencia (PUT approve-transfer) → APPROVED
- [ ] **13.3** Rechazar transferencia (PUT reject-transfer) → REJECTED
- [ ] **13.4** Aprobación registra timestamp en approvalDate
- [ ] **13.5** Supervisor NO puede aprobar propias transferencias (RN-11) ❌
- [ ] **13.6** Se registra auditoría de aprobación en MongoDB

### Expiración de Transferencias
- [ ] **14.1** Transferencia expira en 60 minutos (scheduler)
- [ ] **14.2** Estado cambia a EXPIRED automáticamente
- [ ] **14.3** Scheduler se ejecuta cada X minutos

---

## 🏦 5. PRÉSTAMOS

### Solicitud de Préstamo (Cliente Individual)
- [ ] **15.1** Crear préstamo exitosa (201 CREATED)
- [ ] **15.2** Préstamo inicia en estado PENDING
- [ ] **15.3** Se valida monto > 0
- [ ] **15.4** Se valida termMonths > 0
- [ ] **15.5** Se valida destinationAccount existe
- [ ] **15.6** Se guarda productCode y productName

### Obtener Préstamos Pendientes (Analista)
- [ ] **16.1** GET /pending-loans retorna solo PENDING
- [ ] **16.2** Analista solo ve préstamos sin procesar
- [ ] **16.3** Retorna array de LoanResponse

### Aprobación de Préstamo (Analista)
- [ ] **17.1** Aprobar préstamo (PUT approve-loan) → APPROVED
- [ ] **17.2** Se requiere approvedAmount y interestRate
- [ ] **17.3** approvedAmount ≤ requestedAmount
- [ ] **17.4** interestRate > 0
- [ ] **17.5** Se registra approvalDate
- [ ] **17.6** Se rechaza interestRate negativo (400 BAD REQUEST)

### Rechazo de Préstamo (Analista)
- [ ] **18.1** Rechazar préstamo (PUT reject-loan) → REJECTED
- [ ] **18.2** Se registra rechazo
- [ ] **18.3** Cliente no recibe dinero

### Desembolso de Préstamo (Analista)
- [ ] **19.1** Desembolsar (POST disburse-loan) → DISBURSED
- [ ] **19.2** Se requiere destinationAccount válido
- [ ] **19.3** Dinero se transfiere a cuenta destino
- [ ] **19.4** Balance de cuenta destino aumenta
- [ ] **19.5** Se registra disbursementDate
- [ ] **19.6** Estados transitivos: PENDING → APPROVED → DISBURSED

---

## 🗄️ 6. BASE DE DATOS

### MySQL (Transaccional)
- [ ] **20.1** Tablas creadas: users, customers, loans, transfers, accounts
- [ ] **20.2** Relaciones de foreign key correctas
- [ ] **20.3** Índices en campos de búsqueda frecuente
- [ ] **20.4** Datos persisten después de reiniciar app

### MongoDB (Auditoría)
- [ ] **21.1** Colección de auditoría existe
- [ ] **21.2** Se registra CADA operación (login, transfer, loan, etc.)
- [ ] **21.3** Se registra timestamp, usuario, operación, resultado
- [ ] **21.4** Se guarda previousStatus y newStatus (RF-05-E)
- [ ] **21.5** Auditoría es inmutable (no se puede borrar)

---

## 📊 7. RESTRICCIONES DE NEGOCIO (Reglas)

### RN-11: Supervisor no aprueba propias transferencias
- [ ] **22.1** Supervisor A crea transferencia
- [ ] **22.2** Supervisor A intenta aprobarla → ❌ 400 BAD REQUEST
- [ ] **22.3** Error message: "Supervisor cannot approve own transfers"
- [ ] **22.4** Supervisor B puede aprobar transferencia de A

### CK-05: Validación rol INTERNAL_ANALYST
- [ ] **23.1** Solo rol INTERNAL_ANALYST puede registrar empleados
- [ ] **23.2** Rol COMMERCIAL no puede registrar (403 FORBIDDEN)
- [ ] **23.3** Rol CUSTOMER no puede registrar (403 FORBIDDEN)

### RF-05-E: PreviousStatus y NewStatus en logs
- [ ] **24.1** Transferencia: PENDING → APPROVED (registrado en MongoDB)
- [ ] **24.2** Préstamo: PENDING → REJECTED (registrado en MongoDB)
- [ ] **24.3** Préstamo: APPROVED → DISBURSED (registrado en MongoDB)
- [ ] **24.4** Cuenta: ACTIVE → BLOCKED (registrado en MongoDB)

### RF-04-C: Pagos masivos/nómina
- [ ] **25.1** POST /bulk-transfer acepta array de transferencias
- [ ] **25.2** Todas creadas con mismo sourceAccount
- [ ] **25.3** Múltiples destinationAccounts distintos
- [ ] **25.4** Todas requieren aprobación de supervisor

---

## 🔒 8. VALIDACIONES DE ENTRADA

### Campos Requeridos
- [ ] **26.1** Name/businessName: no vacío
- [ ] **26.2** Email: formato válido (regex)
- [ ] **26.3** Phone: formato válido (regex)
- [ ] **26.4** IdentificationId: no vacío, único
- [ ] **26.5** Username: único
- [ ] **26.6** Password: mínimo 8 caracteres
- [ ] **26.7** BirthDate: fecha válida (no futuro)
- [ ] **26.8** Amount: > 0, no decimales aleatoriamente

### Límites
- [ ] **27.1** Máximo monto de transferencia validado
- [ ] **27.2** Máximo monto de préstamo validado
- [ ] **27.3** Máximo plazo de préstamo (meses) validado
- [ ] **27.4** Mínimo plazo de préstamo (meses) validado

### Duplicados
- [ ] **28.1** IdentificationId único por customer/user
- [ ] **28.2** Email único por customer/user
- [ ] **28.3** Username único en la base de datos
- [ ] **28.4** AccountNumber único

---

## 📈 9. ESTADOS Y TRANSICIONES

### Estados de Transferencia
```
┌─ PENDING ─┬─ APPROVED ─┐
│           │            └─ COMPLETED
│           │
└─ REJECTED └─ EXPIRED (60 min sin aprobar)
```
- [ ] **29.1** PENDING: inicial
- [ ] **29.2** APPROVED: supervisor aprobó
- [ ] **29.3** REJECTED: supervisor rechazó
- [ ] **29.4** EXPIRED: 60 min transcurrieron
- [ ] **29.5** No se puede volver atrás de estado

### Estados de Préstamo
```
┌─ PENDING ─┬─ APPROVED ─┐
│           │            └─ DISBURSED
│           │
└─ REJECTED
```
- [ ] **30.1** PENDING: inicial
- [ ] **30.2** APPROVED: analista aprobó
- [ ] **30.3** REJECTED: analista rechazó
- [ ] **30.4** DISBURSED: desembolsado
- [ ] **30.5** No se puede disbursar sin aprobar primero

### Estados de Cuenta
```
┌─ ACTIVE ─┬─ BLOCKED
│          │
└──────── CLOSED
```
- [ ] **31.1** ACTIVE: operativa
- [ ] **31.2** BLOCKED: rechaza operaciones
- [ ] **31.3** CLOSED: no puede reabrirse

---

## 📡 10. API REST - CÓDIGOS HTTP

### Códigos Exitosos
- [ ] **32.1** 201 CREATED: POST /register, /open-account, /transfer
- [ ] **32.2** 200 OK: GET, PUT (approve/reject)
- [ ] **32.3** 204 NO CONTENT: operaciones sin respuesta

### Códigos de Error del Cliente
- [ ] **33.1** 400 BAD REQUEST: validación de entrada
- [ ] **33.2** 401 UNAUTHORIZED: sin token o token inválido
- [ ] **33.3** 403 FORBIDDEN: no autorizado para rol
- [ ] **33.4** 409 CONFLICT: duplicado (email, ID, etc.)

### Códigos de Error del Servidor
- [ ] **34.1** 500 INTERNAL SERVER ERROR: error no controlado
- [ ] **34.2** GlobalExceptionHandler captura excepciones

---

## 📝 11. RESPUESTAS Y ERRORES

### Estructura de Response (Exitosa)
```json
{
  "field1": "value1",
  "field2": "value2",
  "status": "enum",
  "timestamp": "ISO-8601"
}
```
- [ ] **35.1** Todas las respuestas tienen estructura consistente
- [ ] **35.2** Timestamps en formato ISO-8601

### Estructura de Error
```json
{
  "error": "Error Type",
  "message": "Descripción clara",
  "field": "campo que falló (opcional)",
  "timestamp": "ISO-8601"
}
```
- [ ] **36.1** Errores incluyen mensaje descriptivo
- [ ] **36.2** Errores de validación indican qué campo falló
- [ ] **36.3** Errores de negocio claros (ej: RN-11)

---

## ⚡ 12. PERFORMANCE Y ESCALABILIDAD

### Tiempos de Respuesta
- [ ] **37.1** GET /pending-loans < 500ms
- [ ] **37.2** POST /transfer < 1s
- [ ] **37.3** PUT /approve-transfer < 500ms
- [ ] **37.4** POST /bulk-transfer (100 items) < 5s

### Concurrencia
- [ ] **38.1** App maneja múltiples requests simultáneos
- [ ] **38.2** No hay race conditions en transacciones
- [ ] **38.3** Base de datos maneja locks correctamente

---

## 🔍 13. AUDITORÍA Y LOGGING

### MongoDB Audit Log
- [ ] **39.1** Cada operación registra: timestamp, userId, action, old_value, new_value
- [ ] **39.2** Registros inmutables (no se pueden editar)
- [ ] **39.3** Se puede auditar: login, register, transfer, loan, deposit, etc.
- [ ] **39.4** previousStatus y newStatus guardados (RF-05-E)

### Logs de Aplicación
- [ ] **40.1** DEBUG level en desarrollo
- [ ] **40.2** INFO level en producción
- [ ] **40.3** ERROR level para excepciones
- [ ] **40.4** EndpointLogger registra todas las API calls

---

## 🔄 14. TRANSACCIONES

### MySQL (Transaccional)
- [ ] **41.1** Transfer crea: transfer record + 2 audit logs
- [ ] **41.2** Si falla 1 paso, TODO se revierte (ROLLBACK)
- [ ] **41.3** Loan approval: actualiza 3 campos atómicamente

### MongoDB (Eventual Consistency)
- [ ] **42.1** Audit logs pueden tardar ms en replicar
- [ ] **42.2** No es problema (auditoría, no transaccional)

---

## 📲 15. POSTMAN COLLECTION

### Archivos
- [ ] **43.1** `banco-comprehensive-api-test.postman_collection.json` existe
- [ ] **43.2** `POSTMAN_COMPREHENSIVE_GUIDE.md` con instrucciones
- [ ] **43.3** `POSTMAN_EXECUTION_EXAMPLES.md` con ejemplos reales

### Contenido Collection
- [ ] **44.1** 60+ requests (casos felices + negativos)
- [ ] **44.2** Variables de entorno configurables
- [ ] **44.3** 10 carpetas temáticas
- [ ] **44.4** Scripts de test que extraen tokens

### Ejecución
- [ ] **45.1** Importar en Postman sin errores
- [ ] **45.2** Configurar base_url = http://localhost:8080
- [ ] **45.3** Ejecutar 1.1 Login (obtener token)
- [ ] **45.4** Ejecutar carpeta completa sin fallos

---

## 🚀 16. DEPLOYMENT Y CONFIGURACIÓN

### application.properties
- [ ] **46.1** MySQL URI correcta
- [ ] **46.2** MongoDB URI correcta
- [ ] **46.3** JWT secret key único (no default)
- [ ] **46.4** JWT expiration = 30000ms (30s)
- [ ] **46.5** Spring profiles activos

### Build y Run
- [ ] **47.1** `mvn clean install` compila sin errores
- [ ] **47.2** `mvn spring-boot:run` inicia aplicación
- [ ] **47.3** Puerto 8080 accesible
- [ ] **47.4** Bases de datos se conectan exitosamente
- [ ] **47.5** DataSeeder crea usuario analyst_user

---

## ✨ 17. EXTRAS

### Documentación
- [ ] **48.1** README.md describe el proyecto
- [ ] **48.2** PROJECT_SPEC.md es exhaustivo
- [ ] **48.3** CLAUDE.md tiene guía de desarrollo
- [ ] **48.4** Javadoc en clases principales

### Arquitectura
- [ ] **49.1** Arquitectura Hexagonal implementada correctamente
- [ ] **49.2** Controllers → UseCases → Services
- [ ] **49.3** Interfaces (Ports) para repositorios
- [ ] **49.4** DTOs para request/response

### Código
- [ ] **50.1** Lombok used (@Getter, @Setter, @Data)
- [ ] **50.2** Validaciones con @Valid (Jakarta)
- [ ] **50.3** Exception handling global (GlobalExceptionHandler)
- [ ] **50.4** Security configurado (SecurityConfig)

---

## 📋 RESUMEN FINAL

| Categoría | Total | ✅ Pasaron | ❌ Fallaron | % Éxito |
|-----------|-------|-----------|----------|---------|
| Autenticación | 7 | - | - | - |
| Usuarios | 9 | - | - | - |
| Cajero | 6 | - | - | - |
| Transferencias | 14 | - | - | - |
| Préstamos | 12 | - | - | - |
| Bases de Datos | 5 | - | - | - |
| Reglas de Negocio | 8 | - | - | - |
| Validaciones | 8 | - | - | - |
| Estados | 9 | - | - | - |
| API REST | 7 | - | - | - |
| Respuestas | 6 | - | - | - |
| Performance | 4 | - | - | - |
| Auditoría | 4 | - | - | - |
| Transacciones | 5 | - | - | - |
| Postman | 6 | - | - | - |
| Deployment | 5 | - | - | - |
| Extras | 5 | - | - | - |
| **TOTAL** | **142** | **-** | **-** | **-%** |

---

## 📝 Notas de Validación

- Ejecutar checklist después de cada cambio importante
- Marcar ✅ cuando se verifica manualmente o con Postman
- Marcar ❌ si hay fallo con descripción del problema
- Documentar cualquier comportamiento inesperado

---

**Última actualización**: 2026-05-25  
**Versión**: 2.0  
**Creado por**: GitHub Copilot + User
