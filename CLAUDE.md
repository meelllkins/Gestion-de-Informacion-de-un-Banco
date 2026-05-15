# CLAUDE.md — Guía de trabajo para Claude Code

## Al iniciar sesión o después de /compact, haz esto:
1. Lee este archivo completo
2. Lee PROJECT_SPEC.md
3. Confirma el estado actual leyendo los controllers y services
4. Di en qué punto estamos antes de tocar cualquier cosa

## Proyecto
Sistema bancario en Spring Boot con arquitectura hexagonal.
Stack: Java 17, Spring Boot, MySQL (transaccional), MongoDB (bitácora), JWT.

## Reglas de trabajo
- Muestra diff SOLO cuando el cambio sea en un archivo crítico (security, auth)
- Para cambios normales (controllers, DTOs), aplica directamente y reporta resumen
- Un commit por grupo de cambios relacionados
- Nunca cambies lógica de negocio existente sin pedirlo explícitamente
- Si algo está ambiguo, pregunta UNA sola cosa antes de proceder

## Estado actual (actualizar después de cada sesión)
- Seguridad/Auth: ✅ Completo
- DataSeeder: ✅ Sin doble encoding
- LoanService/TransferService → SQL: ✅ Completo
- Controllers operacionales: 🔄 En progreso
- @Scheduled vencimiento 60 min: ❌ Pendiente
- Supervisor no aprueba propias transfers: ❌ Pendiente
- previousStatus/newStatus en logs transfers: ❌ Pendiente
- Pagos masivos/nómina: ❌ Pendiente