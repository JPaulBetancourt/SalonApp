package com.example.appagendamientocitas.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(AlarmScheduler.EXTRA_APPOINTMENT_ID, -1)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE)
            ?: "Recordatorio de cita"
        val text = intent.getStringExtra(AlarmScheduler.EXTRA_TEXT).orEmpty()
        NotificationHelper.showReminder(context, id, title, text)
    }
}