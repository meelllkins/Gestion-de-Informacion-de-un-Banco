# 🏦 Banco App - develop

Sistema de gestión de información bancaria desarrollado como proyecto **Construcción de Software 2**.

---

## 👥 Integrantes

| Nombre | Rol en el equipo |
|--------|-----------------|
| Elkin Palomino Castañeda | Desarrollador |
| Ana Maria Lopez Orozco | Desarrolladora |
| Luis Esteban Villegas | Desarrollador |

**Institución:** TDEA  
**Materia:** Construcción de Software II  
**Docente:** Andrés Alberto Restrepo

---

## 📋 Descripción

Banco App es una aplicación backend que actúa como **core transaccional** de una entidad bancaria. Permite gestionar clientes (personas naturales y empresas), cuentas bancarias, préstamos y transferencias, con control de acceso basado en roles y flujos de aprobación definidos.

---

## 🛠️ Tecnologías

| Tecnología | Uso |
|-----------|-----|
| Java 17 | Lenguaje principal |
| Spring Boot 4.0.2 | Framework base |
| Spring Data JPA | Persistencia relacional |
| Spring Security | Autenticación y autorización |
| MySQL | Base de datos relacional (cuentas, préstamos, transferencias) |
| MongoDB | Base de datos NoSQL (Bitácora de operaciones) |
| Lombok | Reducción de código boilerplate |
| Maven | Gestión de dependencias |

---

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura en capas** con separación clara de responsabilidades:

```
src/
└── main/
    └── java/app/domain/
        ├── enums/          # Estados y tipos del sistema
        ├── models/         # Entidades del dominio
        └── services/
            ├── interfaces/         # Contratos de cada servicio
            └── implementations/    # Lógica de negocio
```

### Servicios principales

- **AuthService** — Autenticación y validación de roles
- **UserService** — Registro y gestión de clientes (personas naturales y empresas)
- **AccountService** — Apertura de cuentas, depósitos y retiros
- **LoanService** — Solicitud, aprobación y desembolso de préstamos
- **TransferService** — Transferencias individuales y pagos masivos (nómina)
- **LogService** — Bitácora de operaciones (almacenamiento NoSQL)

---

## 👤 Roles del Sistema

| Rol | Descripción |
|-----|-------------|
| `INDIVIDUAL_CUSTOMER` | Cliente persona natural |
| `CORPORATE_CUSTOMER` | Representante legal de empresa |
| `TELLER_EMPLOYEE` | Empleado de ventanilla (cajero) |
| `COMMERCIAL_EMPLOYEE` | Asesor comercial |
| `CORPORATE_EMPLOYEE` | Empleado operativo de empresa |
| `CORPORATE_SUPERVISOR` | Supervisor aprobador de empresa |
| `INTERNAL_ANALYST` | Analista interno del banco (mayor acceso) |

---

## 🔄 Flujos de Aprobación

### Préstamos
```
Solicitud → PENDING → APPROVED / REJECTED → DISBURSED
```
Solo el `INTERNAL_ANALYST` puede aprobar, rechazar o desembolsar.

### Transferencias empresariales de alto monto
```
Creación(PENDING) → WAITING_APPROVAL → EXECUTED / APPROVED / REJECTED / EXPIRED
```
- Si el monto supera el umbral definido, queda en espera de aprobación del `CORPORATE_SUPERVISOR`.
- Si no es aprobada en **60 minutos**, el sistema la marca automáticamente como `EXPIRED`.

---

## ▶️ Cómo ejecutar

### Requisitos previos
- Java 17+
- Maven 3.8+
- MySQL corriendo localmente
- MongoDB corriendo localmente

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/meelllkins/Gestion-de-Informacion-de-un-Banco
cd banco

# 2. Configurar base de datos en application.properties
# spring.datasource.url=jdbc:mysql://localhost:3306/banco_db
# spring.data.mongodb.uri=mongodb://localhost:27017/banco_bitacora

# 3. Compilar y ejecutar
mvn spring-boot:run
```

---

## 📦 Dependencias principales (`pom.xml`)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## 📝 Notas de desarrollo

- La **Bitácora de Operaciones** es inmutable: solo se insertan registros, nunca se modifican ni eliminan.
- El saldo de las cuentas se gestiona exclusivamente en la base de datos relacional (MySQL). La bitácora NoSQL es solo para auditoría.
- El patrón de diseño utilizado es **Interfaz → Implementación** en la capa de servicios, sin inyección de dependencias de Spring en el dominio puro.

---

*Proyecto académico — Construcción de Software 2*
