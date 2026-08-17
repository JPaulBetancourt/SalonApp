# 💇‍♀️ SalonApp — Gestión de citas para salones de belleza

![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-7F52FF?logo=kotlin)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)
![minSdk](https://img.shields.io/badge/minSdk-26-3DDC84)
![Arquitectura](https://img.shields.io/badge/MVVM%20+%20Clean%20Architecture-orange)
![Licencia](https://img.shields.io/badge/Licencia-MIT-yellow)

Aplicación nativa de Android (Kotlin + Jetpack Compose + Material 3) para la gestión
de citas de un salón de belleza con dos roles: **Cliente** y **Dueño**.
MVP 100% local (sin backend) con recordatorios mediante AlarmManager.

## ✨ Características

- 🔐 Login por roles: dueño con credenciales fijas (`admin` / `1234`) y registro local de clientes.
- 🔒 **Contraseñas hasheadas** con PBKDF2-HMAC-SHA256 (600k iteraciones, salt aleatorio).
- 📅 **Panel Cliente**: solicitud de citas (servicio, fecha, hora) con validación de horarios ocupados contra la BD.
- 📊 **Panel Dueño**: dashboard reactivo con contador de pendientes y acciones aprobar / rechazar / completar.
- 🔔 **Recordatorios locales**: notificación al dueño 30 min antes de cada cita aprobada (AlarmManager + NotificationCompat).
- 💾 Persistencia reactiva con Room (`Flow`) y sesión con SharedPreferences.

## 🛠️ Stack tecnológico

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.1.20 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Clean Architecture (3 capas) |
| Navegación | Navigation Compose (rutas type-safe `@Serializable`) |
| Base de datos | Room (entidades, DAOs, TypeConverters) |
| Inyección | Hilt (KSP) |
| Seguridad | PBKDF2-HMAC-SHA256 (OWASP 2024) |
| Recordatorios | AlarmManager + NotificationCompat |
| Async | Coroutines + StateFlow |

## 🏗️ Arquitectura

Separación en capas con **inversión de dependencia**: `domain` define contratos,
`data` los implementa y `ui` consume use cases. Hilt realiza el wiring.

```mermaid
flowchart TB
    subgraph UI["Presentación (ui)"]
        SC["Screens Compose"]
        VM["ViewModels"]
    end
    subgraph DOMAIN["Dominio (domain)"]
        UC["Use Cases"]
        IR["Interfaces Repository"]
    end
    subgraph DATA["Datos (data)"]
        RR["Repository Impls"]
        DAO["DAOs Room"]
        DB[("AppDatabase")]
        AL["AlarmScheduler"]
    end
    SC --> VM
    VM --> UC
    UC --> IR
    RR -. implementa .-> IR
    RR --> DAO
    DAO --> DB
    RR --> AL