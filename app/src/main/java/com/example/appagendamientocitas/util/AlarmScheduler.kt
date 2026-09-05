package com.example.appagendamientocitas.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.appagendamientocitas.data.local.entity.Appointment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun schedule(appointment: Appointment) {
        val triggerMillis = triggerMillis(appointment) ?: return
        if (triggerMillis <= System.currentTimeMillis()) return

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_APPOINTMENT_ID, appointment.id.hashCode())
            putExtra(EXTRA_TITLE, "Cita en $MINUTES_BEFORE min")
            putExtra(
                EXTRA_TEXT,
                "${appointment.clientName} · ${appointment.service} · ${appointment.time}"
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent
            )
        }
    }

    fun cancel(appointmentId: String) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun triggerMillis(a: Appointment): Long? {
        val dt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .parse("${a.date} ${a.time}") ?: return null
        return dt.time - MINUTES_BEFORE * 60_000L
    }

    companion object {
        const val MINUTES_BEFORE = 30L
        const val EXTRA_APPOINTMENT_ID = "extra_appointment_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
    }
}