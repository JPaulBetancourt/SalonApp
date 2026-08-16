package com.example.appagendamientocitas.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.appagendamientocitas.data.local.dao.UserDao
import com.example.appagendamientocitas.data.local.entity.User
import com.example.appagendamientocitas.data.local.entity.UserRole
import com.example.appagendamientocitas.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        val cleanEmail = email.trim()

        if (cleanEmail == ADMIN_USER && password == ADMIN_PASS) {
            return Result.success(
                User(
                    id = ADMIN_ID,
                    name = "Dueño",
                    email = ADMIN_USER,
                    password = "",
                    role = UserRole.ADMIN
                )
            )
        }

        val user = userDao.findByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Credenciales inválidas"))

        return if (user.password == password) Result.success(user)
        else Result.failure(IllegalArgumentException("Credenciales inválidas"))
    }

    override suspend fun registerClient(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        val newUser = User(
            name = name.trim(),
            email = email.trim(),
            password = password,
            role = UserRole.CLIENT
        )
        return try {
            val id = userDao.insert(newUser).toInt()
            Result.success(newUser.copy(id = id))
        } catch (e: SQLiteConstraintException) {
            Result.failure(IllegalArgumentException("El email ya está registrado"))
        }
    }

    private companion object {
        const val ADMIN_USER = "admin"
        const val ADMIN_PASS = "1234"
        const val ADMIN_ID = 0
    }
}