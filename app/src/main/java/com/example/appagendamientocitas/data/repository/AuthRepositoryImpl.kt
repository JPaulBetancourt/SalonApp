package com.example.appagendamientocitas.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.appagendamientocitas.data.local.dao.UserDao
import com.example.appagendamientocitas.data.local.entity.User
import com.example.appagendamientocitas.data.local.entity.UserRole
import com.example.appagendamientocitas.domain.repository.AuthRepository
import com.example.appagendamientocitas.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        val cleanEmail = email.trim()

        // 1) Dueño: credencial demo embebida en el binario.
        //    ADR-01 (README): el hashing protege contraseñas ALMACENADAS;
        //    esta credencial es un backdoor de demo, decisión consciente.
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

        // 2) Cliente: verificación contra el hash almacenado en Room
        val user = userDao.findByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Credenciales inválidas"))

        // PBKDF2 es costoso → fuera del hilo principal
        val ok = withContext(Dispatchers.IO) {
            PasswordHasher.verify(password, user.password)
        }

        return if (ok) Result.success(user)
        else Result.failure(IllegalArgumentException("Credenciales inválidas"))
    }

    override suspend fun registerClient(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        // Nunca guardamos texto plano: solo "salt:hash"
        val hashed = withContext(Dispatchers.IO) { PasswordHasher.hash(password) }

        val newUser = User(
            name = name.trim(),
            email = email.trim(),
            password = hashed,
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