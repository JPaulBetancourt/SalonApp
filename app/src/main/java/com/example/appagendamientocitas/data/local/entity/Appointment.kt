package com.example.appagendamientocitas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE // Si se borra el cliente, sus citas también
        )
    ],
    indices = [Index("clientId")]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val clientName: String,
    val service: String,
    val date: String, // Formato "yyyy-MM-dd" → ordenable y fácil de comparar
    val time: String, // Formato "HH:mm" (24h) → validación de slot simple
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)