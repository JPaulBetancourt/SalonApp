package com.example.appagendamientocitas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)] // Un usuario por email
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String, // Local/demo. En producción: hashing.
    val role: UserRole = UserRole.CLIENT
)