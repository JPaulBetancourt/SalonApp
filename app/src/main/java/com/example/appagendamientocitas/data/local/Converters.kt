package com.example.appagendamientocitas.data.local

import androidx.room.TypeConverter
import com.example.appagendamientocitas.data.local.entity.AppointmentStatus
import com.example.appagendamientocitas.data.local.entity.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromStatus(value: AppointmentStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): AppointmentStatus = AppointmentStatus.valueOf(value)
}