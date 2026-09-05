package com.example.appagendamientocitas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.appagendamientocitas.data.local.dao.AppointmentDao
import com.example.appagendamientocitas.data.local.dao.UserDao
import com.example.appagendamientocitas.data.local.entity.Appointment
import com.example.appagendamientocitas.data.local.entity.User

@Database(
    entities = [User::class, Appointment::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        const val DATABASE_NAME = "salon_app_db"
    }
}