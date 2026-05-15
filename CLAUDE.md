Crea CLAUDE.md en la raíz del proyecto con este contenido exacto:

# CLAUDE.md — Guía de trabajo para Claude Code

## Al iniciar sesión o después de /compact:
1. Lee este archivo completo
2. Lee PROJECT_SPEC.md
3. Revisa el código actual para confirmar el estado
4. Di en qué punto estamos antes de tocar cualquier cosa

## Proyecto
Sistema bancario en Spring Boot con arquitectura hexagonal.
Stack: Java 17, Spring Boot, MySQL (transaccional), MongoDB (bitácora), JWT.

## Reglas de trabajo
- Para cambios críticos (security, auth) muestra diff antes de aplicar
- Para cambios normales aplica directamente y reporta resumen
- Un commit por grupo de cambios relacionados
- Nunca cambies lógica de negocio sin pedirlo explícitamente
- Si algo es ambiguo, pregunta UNA sola cosa antes de proceder

## Estado del proyecto
- Seguridad/Auth: ✅ Completo
- DataSeeder: ✅ Sin doble encoding
- LoanService/TransferService → SQL: ✅ Completo
- Todos los endpoints REST: ✅ Completo
- Scheduler vencimiento 60 min: ✅ Completo
- RN-11 Supervisor no aprueba propias transfers: ✅ Completo
- CK-05 Validación rol INTERNAL_ANALYST: ✅ Completo
- RF-05-E previousStatus/newStatus en logs transfers: ✅ Completo
- RF-04-C Pagos masivos/nómina: ✅ Completo

## Pendiente
- Tests unitarios de nuevos módulos (cuentas, préstamos, transferencias)
- Verificación en Postman de todos los endpoints
- balance como BigDecimal (bajo impacto)

Commitea y confirma.