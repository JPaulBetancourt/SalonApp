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

            val localId = appointmentDao.insert(appointment.copy(id = docRef.id.hashCode()))
            Result.success(localId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAll(): Flow<List<Appointment>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("appointments")
            .orderBy("date")
            .orderBy("time")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Appointment(
                            id = doc.id.hashCode(),
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
                } ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    override fun observeByClient(clientId: String): Flow<List<Appointment>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("appointments")
            .whereEqualTo("clientId", clientId)
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Appointment(
                            id = doc.id.hashCode(),
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
                } ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getById(id: Int): Appointment? =
        appointmentDao.getById(id)

    override suspend fun updateStatus(id: Int, status: AppointmentStatus) {
        appointmentDao.updateStatus(id, status)

        val appointment = appointmentDao.getById(id) ?: return
        firestore.collection("appointments")
            .document(id.toString())
            .update("status", status.name)
            .await()

        if (status == AppointmentStatus.APPROVED) {
            alarmScheduler.schedule(appointment)
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
}