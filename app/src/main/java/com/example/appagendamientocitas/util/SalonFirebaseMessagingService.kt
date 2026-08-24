package com.example.appagendamientocitas.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SalonFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Mensaje data: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            val title = it.title ?: "Recordatorio de cita"
            val body = it.body ?: "Tienes una cita próxima"
            NotificationHelper.showReminder(
                context = this,
                id = System.currentTimeMillis().toInt(),
                title = title,
                text = body
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "Token guardado en Firestore correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al guardar token", e)
                }
        }
    }
}