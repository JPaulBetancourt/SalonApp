package com.example.appagendamientocitas.data.repository

import com.example.appagendamientocitas.data.local.dao.AppointmentDao
import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import com.example.appagendamientocitas.domain.repository.AppointmentRepository
import com.example.appagendamientocitas.util.AlarmScheduler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreAppointmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val appointmentDao: AppointmentDao,
    private val alarmScheduler: AlarmScheduler
) : AppointmentRepository {

    override suspend fun createAppointment(appointment: Appointment): Result<Long> {
        return try {
            if (!isSlotAvailable(appointment.date, appointment.time)) {
                return Result.failure(IllegalStateException("El horario ya no está disponible"))
            }

            val firestoreData = mapOf(
                "clientId" to appointment.clientId,
                "clientName" to appointment.clientName,
                "service" to appointment.service,
                "date" to appointment.date,
                "time" to appointment.time,
                "status" to appointment.status.name,
                "createdAt" to appointment.createdAt
            )

            val docRef = firestore.collection("appointments")
                .add(firestoreData)
                .await()

            val localId = appointmentDao.insert(appointment.copy(id = docRef.id))
            Result.success(localId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAll(): Flow<List<Appointment>> = callbackFlow {
        try {
            val listener: ListenerRegistration = firestore.collection("appointments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Firestore", "Error observando todas las citas: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }
                    val appointments = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Appointment(
                                id = doc.id,
                                clientId = doc.getString("clientId") ?: "",
                                clientName = doc.getString("clientName") ?: "",
                                service = doc.getString("service") ?: "",
                                date = doc.getString("date") ?: "",
                                time = doc.getString("time") ?: "",
                                status = AppointmentStatus.valueOf(doc.getString("status") ?: "PENDING"),
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }?.sortedWith(compareBy({ it.date }, { it.time })) ?: emptyList()
                    trySend(appointments)
                }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            android.util.Log.e("Firestore", "Error al iniciar listener observeAll: ${e.message}")
            trySend(emptyList())
        }
    }

    override fun observeByClient(clientId: String): Flow<List<Appointment>> = callbackFlow {
        try {
            val listener: ListenerRegistration = firestore.collection("appointments")
                .whereEqualTo("clientId", clientId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Firestore", "Error observando citas: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }
                    val appointments = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Appointment(
                                id = doc.id,
                                clientId = doc.getString("clientId") ?: "",
                                clientName = doc.getString("clientName") ?: "",
                                service = doc.getString("service") ?: "",
                                date = doc.getString("date") ?: "",
                                time = doc.getString("time") ?: "",
                                status = AppointmentStatus.valueOf(doc.getString("status") ?: "PENDING"),
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("Firestore", "Error parseando documento: ${doc.id}", e)
                            null
                        }
                    }?.sortedByDescending { it.createdAt } ?: emptyList()
                    trySend(appointments)
                }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            android.util.Log.e("Firestore", "Error al iniciar listener: ${e.message}")
            trySend(emptyList())
        }
    }

    override suspend fun getById(id: String): Appointment? =
        appointmentDao.getById(id)

    override suspend fun updateStatus(id: String, status: AppointmentStatus) {
        // Intentar actualizar localmente
        appointmentDao.updateStatus(id, status)

        // Obtener la cita (de Room o Firestore) para programar la alarma
        val appointment = appointmentDao.getById(id) ?: try {
            val doc = firestore.collection("appointments").document(id).get().await()
            if (doc.exists()) {
                Appointment(
                    id = doc.id,
                    clientId = doc.getString("clientId") ?: "",
                    clientName = doc.getString("clientName") ?: "",
                    service = doc.getString("service") ?: "",
                    date = doc.getString("date") ?: "",
                    time = doc.getString("time") ?: "",
                    status = AppointmentStatus.valueOf(doc.getString("status") ?: "PENDING"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                ).also {
                    // Guardar en Room para futuras referencias
                    appointmentDao.insert(it.copy(status = status))
                }
            } else null
        } catch (e: Exception) {
            null
        } ?: return

        // Actualizar Firestore
        firestore.collection("appointments")
            .document(id)
            .update("status", status.name)
            .await()

        if (status == AppointmentStatus.APPROVED) {
            alarmScheduler.schedule(appointment.copy(status = status))
        } else {
            alarmScheduler.cancel(id)
        }
    }

    override suspend fun isSlotAvailable(date: String, time: String): Boolean {
        val query = firestore.collection("appointments")
            .whereEqualTo("date", date)
            .whereEqualTo("time", time)
            .get()
            .await()

        val occupied = query.documents.any { doc ->
            val status = doc.getString("status") ?: "PENDING"
            status != AppointmentStatus.REJECTED.name
        }

        return !occupied
    }

    override suspend fun getAppointmentsByDate(date: String): List<Appointment> {
        val query = firestore.collection("appointments")
            .whereEqualTo("date", date)
            .get()
            .await()

        return query.documents.mapNotNull { doc ->
            try {
                Appointment(
                    id = doc.id,
                    clientId = doc.getString("clientId") ?: "",
                    clientName = doc.getString("clientName") ?: "",
                    service = doc.getString("service") ?: "",
                    date = doc.getString("date") ?: "",
                    time = doc.getString("time") ?: "",
                    status = AppointmentStatus.valueOf(doc.getString("status") ?: "PENDING"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}