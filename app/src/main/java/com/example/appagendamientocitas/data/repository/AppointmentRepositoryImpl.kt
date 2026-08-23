package com.example.appagendamientocitas.data.repository

import com.example.appagendamientocitas.data.local.dao.AppointmentDao
import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import com.example.appagendamientocitas.domain.repository.AppointmentRepository
import com.example.appagendamientocitas.util.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val alarmScheduler: AlarmScheduler
) : AppointmentRepository {

    override suspend fun createAppointment(appointment: Appointment): Result<Long> {
        if (!isSlotAvailable(appointment.date, appointment.time)) {
            return Result.failure(
                IllegalStateException("El horario ya no está disponible")
            )
        }
        return try {
            Result.success(appointmentDao.insert(appointment))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAll(): Flow<List<Appointment>> =
        appointmentDao.observeAll()

    override fun observeByClient(clientId: String): Flow<List<Appointment>> =
        appointmentDao.observeByClient(clientId)

    override suspend fun getById(id: Int): Appointment? =
        appointmentDao.getById(id)

    override suspend fun updateStatus(id: Int, status: AppointmentStatus) {
        appointmentDao.updateStatus(id, status)

        // 🔔 Invariante central: el recordatorio sigue al estado de la cita
        val appointment = appointmentDao.getById(id) ?: return
        if (status == AppointmentStatus.APPROVED) {
            alarmScheduler.schedule(appointment)
        } else {
            alarmScheduler.cancel(id)
        }
    }

    override suspend fun isSlotAvailable(date: String, time: String): Boolean =
        appointmentDao.countByDateAndTime(date, time) == 0
}