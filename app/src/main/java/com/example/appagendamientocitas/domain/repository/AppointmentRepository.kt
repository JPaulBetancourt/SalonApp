package com.example.appagendamientocitas.domain.repository

import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    suspend fun createAppointment(appointment: Appointment): Result<Long>

    fun observeAll(): Flow<List<Appointment>>

    fun observeByClient(clientId: String): Flow<List<Appointment>>

    suspend fun getById(id: String): Appointment?

    suspend fun updateStatus(id: String, status: AppointmentStatus)

    suspend fun isSlotAvailable(date: String, time: String): Boolean

    suspend fun getAppointmentsByDate(date: String): List<Appointment>
}