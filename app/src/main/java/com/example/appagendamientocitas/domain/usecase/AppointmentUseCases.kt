package com.example.appagendamientocitas.domain.usecase

import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import com.example.appagendamientocitas.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
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