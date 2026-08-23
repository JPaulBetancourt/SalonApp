package com.example.appagendamientocitas.data.repository

import com.example.appagendamientocitas.data.local.dao.UserDao
import com.example.appagendamientocitas.data.local.entity.User
import com.example.appagendamientocitas.data.local.entity.UserRole
import com.example.appagendamientocitas.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Usuario no encontrado")
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("Datos de usuario no encontrados en Firestore")
            userDao.insert(user)

            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerClient(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al crear usuario")
            val newUser = User(
                uid = firebaseUser.uid,
                name = name.trim(),
                email = email.trim(),
                role = UserRole.CLIENT
            )
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(newUser)
                .await()
            userDao.insert(newUser)

            Result.success(newUser)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(IllegalArgumentException("La contraseña es muy débil (mínimo 6 caracteres)"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalArgumentException("El email ya está registrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}