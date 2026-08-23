package com.example.appagendamientocitas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val password: String = "",
    val role: UserRole = UserRole.CLIENT,
    val fcmToken: String = ""
)