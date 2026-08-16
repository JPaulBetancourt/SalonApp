package com.example.appagendamientocitas.domain.repository

import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    /** Inserta solo si el slot está libre (double-check contra la BD). */
    suspend fun createAppointment(appointment: Appointment): Result<Long>

    /** Dashboard del dueño: todas las citas (reactivo). */
    fun observeAll(): Flow<List<Appointment>>

    /** Historial del cliente (reactivo). */
    fun observeByClient(clientId: Int): Flow<List<Appointment>>

    suspend fun getById(id: Int): Appointment?

    /** Aprobar / rechazar / completar. */
    suspend fun updateStatus(id: Int, status: AppointmentStatus)

    /** Validación en vivo del formulario del cliente. */
    suspend fun isSlotAvailable(date: String, time: String): Boolean
}