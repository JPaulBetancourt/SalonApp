package com.example.appagendamientocitas.domain.repository

import com.example.appagendamientocitas.data.local.entity.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>

    suspend fun registerClient(name: String, email: String, password: String): Result<User>
}