# Colección Postman: Banco Controllers Validation

## Descripción

Documento JSON que valida todos los controllers del módulo Banco mediante una colección Postman con **30 requests** organizados en **8 carpetas funcionales**. Incluye **casos felices** y **casos negativos** para validar el comportamiento correcto de la API ante entradas válidas e inválidas.

---

## 📋 Estructura de la Colección

### 1. **Authentication** (4 requests)
- **1.1** Login - Caso Feliz ✅
- **1.2** Login - Username vacío ❌
- **1.3** Login - Password vacío ❌
- **1.4** Login - Username null ❌

### 2. **Individual Customers** (5 requests)
- **2.1** Register Individual Customer - Caso Feliz ✅
- **2.2** Register - Name vacío ❌
- **2.3** Register - birthDate null ❌
- **2.4** Register - systemRole null ❌
- **2.5** Register - Email vacío ❌

### 3. **Corporate Customers** (4 requests)
- **3.1** Register Corporate Customer - Caso Feliz ✅
- **3.2** Register - businessName vacío ❌
- **3.3** Register - legalRepresentative vacío ❌
- **3.4** Register - identificationId vacío ❌

### 4. **Commercial Employees** (4 requests)
- **4.1** Register - Caso Feliz con token ✅
- **4.2** Register - Sin Authorization ❌
- **4.3** Register - Name vacío con token ❌
- **4.4** Register - birthDate null con token ❌

### 5. **Teller Employees** (3 requests)
- **5.1** Register - Caso Feliz con token ✅
- **5.2** Register - Sin Authorization ❌
- **5.3** Register - phone vacío con token ❌

### 6. **Corporate Employees** (3 requests)
- **6.1** Register - Caso Feliz con token ✅
- **6.2** Register - relatedId vacío con token ❌
- **6.3** Register - address vacío con token ❌

### 7. **Corporate Supervisors** (3 requests)
- **7.1** Register - Caso Feliz con token ✅
- **7.2** Register - Sin Authorization ❌
- **7.3** Register - email vacío con token ❌

### 8. **Internal Analysts** (4 requests)
- **8.1** Register - Caso Feliz con token ✅
- **8.2** Register - Sin Authorization ❌
- **8.3** Register - identificationId vacío con token ❌
- **8.4** Register - username vacío con token ❌

---

## 🔐 Variables de Entorno

La colección incluye **4 variables** que pueden ser personalizadas:

| Variable | Valor por defecto | Propósito |
|----------|------------------|----------|
| `base_url` | `http://localhost:8080` | URL base de la API |
| `token` | `` (vacío) | Bearer token obtenido post-login |
| `analyst_username` | `analyst_user` | Usuario para autenticación |
| `analyst_password` | `analyst_password` | Contraseña para autenticación |

---

## 🚀 Cómo Usar la Colección

### Paso 1: Importar en Postman
1. Abre Postman
2. Click en **Import**
3. Selecciona el archivo `banco-controllers-validation.postman_collection.json`
4. La colección debe aparecer en el panel izquierdo con todas las carpetas y requests

### Paso 2: Configurar Variables (Opcional)
1. En la pestaña **Collections**, busca tu colección
2. Click en **Edit** (icono de lápiz)
3. Ve a la pestaña **Variables**
4. Ajusta `base_url` si tu API corre en otro puerto/host
5. Guarda cambios

### Paso 3: Obtener Token de Autenticación
**Importante para ejecutar endpoints de empleados**

1. Abre el request **1.1 Login - Caso Feliz**
2. Click **Send**
3. En la **Response**, busca el campo `token`
4. Copia el valor del token
5. Ve a **Collections** → tu colección → **Variables**
6. Pega el token en `token` (Current value)
7. Guarda cambios

**Alternativamente, usa el script post-response:**
- Si el login retorna el token, añade un script post-response que lo asigne automáticamente a `pm.collectionVariables.set("token", pm.response.json().token)`

### Paso 4: Ejecutar Requests Públicos
Estos NO requieren token:
- ✅ **1. Authentication** → Todos los requests
- ✅ **2. Individual Customers** → Todos los requests
- ✅ **3. Corporate Customers** → Todos los requests

Simplemente click **Send** en cada uno.

### Paso 5: Ejecutar Requests Protegidos
Requieren Bearer token en el header `Authorization`:
- ✅ **4. Commercial Employees**
- ✅ **5. Teller Employees**
- ✅ **6. Corporate Employees**
- ✅ **7. Corporate Supervisors**
- ✅ **8. Internal Analysts**

**Asegúrate de haber completado Paso 3** antes de ejecutar estos requests.

### Paso 6: Validar Respuestas
- **Casos felices** (✅): Deben retornar **200 OK** o **201 Created**
- **Casos negativos** (❌): Deben retornar errores:
  - **400 Bad Request** — Validación fallida (@NotBlank, @NotNull)
  - **401 Unauthorized** — Token faltante o inválido
  - **403 Forbidden** — Token válido pero sin permisos requeridos
  - **409 Conflict** — identificationId duplicado (si está implementado)

---

## 📝 Validaciones Implementadas

### Validaciones Jakarta Persistence (@NotBlank, @NotNull)

| Tipo | Campo(s) | Escenario | Validación |
|------|----------|-----------|-----------|
| **@NotBlank** | `name`, `email`, `phone`, `address`, `username`, `password`, `businessName`, `legalRepresentative`, `identificationId`, `relatedId` | String vacío (`""`) | Debe retornar 400 Bad Request |
| **@NotNull** | `birthDate` | Valor null | Debe retornar 400 Bad Request |
| **@NotNull** | `systemRole` (en Individual/Commercial/Teller/etc.) | Valor null | Debe retornar 400 Bad Request |

### Validaciones de Seguridad

| Endpoint | Tipo | Authorization | Esperado |
|----------|------|---|----------|
| **POST /auth/login** | Público | ❌ No requerida | 200 + token |
| **POST /api/customers/individual/register** | Público | ❌ No requerida | 201 Created |
| **POST /api/customers/corporate/register** | Público | ❌ No requerida | 201 Created |
| **POST /api/employees/\*\*/register** | Protegido | ✅ Bearer required + `INTERNAL_ANALYST` role | 201 Created o 401/403 |

### Validaciones de Formato

| Campo | Formato | Ejemplo en Colección |
|-------|---------|----------------------|
| `birthDate` | `LocalDate` (ISO 8601: YYYY-MM-DD) | `"1990-05-15"` |
| `systemRole` | Enum | `"INDIVIDUAL_CUSTOMER"`, `"COMMERCIAL_EMPLOYEE"`, etc. |
| `identificationId`, `phone`, etc. | String | Cualquier cadena (sin validación de formato) |

---

## 🎯 Casos de Uso

### 1. Verificar que la API valida inputs correctamente
- Ejecuta todos los requests de una carpeta
- Verifica que los casos negativos retornen **400 Bad Request**
- Verifica el mensaje de error incluya el campo que falló

### 2. Verificar autenticación y autorización
- Ejecuta **4.2, 5.2, 7.2, 8.2** (sin Authorization)
- Verifica que retornen **401 Unauthorized**
- Ejecuta los casos felices con token
- Verifica que retornen **201 Created**

### 3. Generar datos de prueba
- Usa los examples de los requests felices como referencia
- Modifica `identificationId`, `email`, `username` para evitar duplicados
- Ejecuta contra tu base de datos

### 4. Documentar y comunicar API
- La estructura de la colección muestra claramente qué campos son obligatorios
- Los ejemplos sirven como documentación viva de cómo llamar cada endpoint
- Los casos negativos documentan el comportamiento de validación

---

## 📌 Notas Importantes

### 1. **Campo `relatedId`**
- Solo presente en **CorporateEmployeeController** y **CorporateSupervisorController**
- Debe referencia a un cliente corporativo existente (aunque no hay validación explícita en los requests)
- Es obligatorio (@NotBlank)

### 2. **Campo `systemRole`**
- Solo requerido en **IndividualCustomerController** y **CommercialEmployeeController**
- No es requerido en **TellerEmployeeController**
- Valores válidos: `INDIVIDUAL_CUSTOMER`, `CORPORATE_CUSTOMER`, `TELLER_EMPLOYEE`, `COMMERCIAL_EMPLOYEE`, `CORPORATE_EMPLOYEE`, `CORPORATE_SUPERVISOR`, `INTERNAL_ANALYST`

### 3. **Token de Autenticación**
- El token debe obtenerse ejecutando **1.1 Login - Caso Feliz**
- Se asume que exista un usuario `analyst_user` con rol `INTERNAL_ANALYST`
- El token tiene una vida útil (probablemente) → si expira, repetir login

### 4. **Duplicados**
- Los requests usan datos hardcodeados (nombres, emails, identificationIds)
- Si ejecutas múltiples veces, puede haber conflictos de duplicados
- **Solución**: Modifica los valores antes de enviar o limpia la BD entre ejecuciones

### 5. **Headers**
- Todos los requests incluyen `Content-Type: application/json`
- Los protegidos incluyen `Authorization: Bearer {{token}}`
- No hay headers adicionales requeridos (ej: CSRF, X-API-Key)

---

## 📂 Archivo Relacionado

- **Ruta**: `c:\Users\elkin\Gestion-de-Informacion-de-un-Banco\banco-controllers-validation.postman_collection.json`
- **Formato**: JSON Postman Collection v2.1
- **Tamaño**: ~70 KB
- **Compatibilidad**: Postman 9.0+, Insomnia, Thunder Client

---

## ✅ Checklist de Validación

Antes de considerar la colección como lista para producción:

- [ ] Importar en Postman sin errores
- [ ] Ejecutar 1.1 (Login) y obtener token válido
- [ ] Ejecutar todos los requests públicos (Auth, Customers) → Todos retornan éxito
- [ ] Ejecutar requests de empleados CON token → Todos retornan 201
- [ ] Ejecutar requests de empleados SIN token → Todos retornan 401
- [ ] Ejecutar casos negativos (campos vacíos/null) → Todos retornan 400
- [ ] Validar que los mensajes de error son descriptivos
- [ ] Verificar que no hay errores 500 Internal Server Error
- [ ] Confirmar que los datos se crean correctamente en la BD
- [ ] Documentar cualquier variación respecto a lo esperado

---

## 🔧 Troubleshooting

| Problema | Causa Probable | Solución |
|----------|----------------|----------|
| 401 Unauthorized en requests de empleados | Token no configurado o expirado | Repetir Paso 3: Obtener nuevo token |
| 400 Bad Request en caso feliz | Headers o body malformado | Revisar que `Content-Type: application/json` esté presente |
| 404 Not Found | URL base incorrecta | Verificar `base_url` en Variables |
| 409 Conflict | ID duplicado | Cambiar `identificationId`, `email`, `username` a valores únicos |
| Error de parsing JSON | Response no es JSON válido | Verificar logs del servidor; puede ser error 500 con HTML |

---

## 📞 Contacto y Soporte

Para preguntas sobre esta colección:
1. Revisa los comentarios descriptivos en cada folder
2. Consulta los ejemplos de payload en cada request
3. Valida que tu servidor esté ejecutándose en `base_url`
4. Verifica los logs del backend para errores detallados
