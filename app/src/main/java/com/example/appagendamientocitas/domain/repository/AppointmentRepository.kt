package com.example.appagendamientocitas.domain.repository

import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    suspend fun createAppointment(appointment: Appointment): Result<Long>

    fun observeAll(): Flow<List<Appointment>>

    fun observeByClient(clientId: Int): Flow<List<Appointment>>

    suspend fun getById(id: Int): Appointment?

    suspend fun updateStatus(id: Int, status: AppointmentStatus)

    suspend fun isSlotAvailable(date: String, time: String): Boolean
}