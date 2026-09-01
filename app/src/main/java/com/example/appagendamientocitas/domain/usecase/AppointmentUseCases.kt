package com.example.appagendamientocitas.domain.usecase

import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import com.example.appagendamientocitas.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CreateAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(appointment: Appointment): Result<Long> =
        repository.createAppointment(appointment)
}

class ObserveAllAppointmentsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(): Flow<List<Appointment>> =
        repository.observeAll()
}

class ObserveMyAppointmentsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(clientId: String): Flow<List<Appointment>> =
        repository.observeByClient(clientId)
}

class UpdateAppointmentStatusUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(id: Int, status: AppointmentStatus) =
        repository.updateStatus(id, status)
}

class IsSlotAvailableUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(date: String, time: String): Boolean =
        repository.isSlotAvailable(date, time)
}

class GetAvailableSlotsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    private val allPossibleSlots = listOf(
        "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
        "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
        "20:00", "21:00"
    )

    suspend operator fun invoke(date: String): List<String> {
        val appointments = repository.getAppointmentsByDate(date)
        val occupiedSlots = appointments
            .filter { it.status != AppointmentStatus.REJECTED }
            .map { it.time }
            .toSet()

        val available = allPossibleSlots.filter { it !in occupiedSlots }

        // Si es el día de hoy, filtrar horas que ya pasaron
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (date == todayStr) {
            val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            return available.filter { it > nowTime }
        }

        return available
    }
}