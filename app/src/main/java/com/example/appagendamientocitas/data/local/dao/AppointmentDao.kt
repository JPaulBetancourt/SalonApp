package com.example.appagendamientocitas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Insert
    suspend fun insert(appointment: Appointment): Long

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getById(id: Int): Appointment?

    // Dashboard del dueño: todas las citas ordenadas
    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    fun observeAll(): Flow<List<Appointment>>

    // Historial del cliente
    @Query("SELECT * FROM appointments WHERE clientId = :clientId ORDER BY date DESC, time DESC")
    fun observeByClient(clientId: Int): Flow<List<Appointment>>

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: AppointmentStatus)

    // 🔑 Validación de horario ocupado:
    // REJECTED no bloquea el slot; PENDING/APPROVED/COMPLETED sí.
    @Query(
        """SELECT COUNT(*) FROM appointments 
           WHERE date = :date AND time = :time AND status != 'REJECTED'"""
    )
    suspend fun countByDateAndTime(date: String, time: String): Int

    // Para el scheduler de recordatorios (FASE 6)
    @Query("SELECT * FROM appointments WHERE status IN ('PENDING','APPROVED') ORDER BY date ASC, time ASC")
    suspend fun getActiveAppointments(): List<Appointment>
}