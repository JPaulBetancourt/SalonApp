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
            parentColumns = ["uid"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clientId")]
)
data class Appointment(
    @PrimaryKey val id: String = "",
    val clientId: String,
    val clientName: String,
    val service: String,
    val date: String,
    val time: String,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)