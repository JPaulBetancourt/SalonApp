package com.example.appagendamientocitas.data.local.entity

enum class AppointmentStatus {
    PENDING,    // Solicitada por el cliente, esperando aprobación
    APPROVED,   // Aprobada por el dueño
    REJECTED,   // Rechazada (libera el horario)
    COMPLETED   // Atendida y finalizada
}