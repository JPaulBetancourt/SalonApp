package com.example.appagendamientocitas.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "salon_reminders"

    fun showReminder(context: Context, id: Int, title: String, text: String) {
        val manager = NotificationManagerCompat.from(context)
        createChannelIfNeeded(context, manager)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(if (id > 0) id else System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
        }
    }

    private fun createChannelIfNeeded(context: Context, manager: NotificationManagerCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de citas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Aviso 30 minutos antes de cada cita aprobada" }
            manager.createNotificationChannel(channel)
        }
    }
}