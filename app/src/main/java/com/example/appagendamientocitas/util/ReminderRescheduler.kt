package com.example.appagendamientocitas.util

import com.example.appagendamientocitas.data.local.dao.AppointmentDao
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ReminderRescheduler @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val alarmScheduler: AlarmScheduler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun rescheduleAll() {
        scope.launch {
            appointmentDao.getActiveAppointments()
                .filter { it.status == AppointmentStatus.APPROVED }
                .forEach { alarmScheduler.schedule(it) }
        }
    }
}