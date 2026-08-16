package com.example.appagendamientocitas.domain.repository

import com.example.appagendamientocitas.data.local.entity.User

interface AuthRepository {
    /** Login: dueño hardcodeado ("admin"/"1234") o cliente desde Room. */
    suspend fun login(email: String, password: String): Result<User>

    /** Registro local de clientes. Falla si el email ya existe. */
    suspend fun registerClient(name: String, email: String, password: String): Result<User>
}