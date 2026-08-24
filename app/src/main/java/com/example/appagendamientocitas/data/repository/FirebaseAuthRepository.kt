package com.example.appagendamientocitas.data.repository

import com.example.appagendamientocitas.data.local.dao.UserDao
import com.example.appagendamientocitas.data.local.entity.FirestoreUser
import com.example.appagendamientocitas.data.local.entity.User
import com.example.appagendamientocitas.data.local.entity.UserRole
import com.example.appagendamientocitas.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
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
            val cleanEmail = email.trim()
            val authResult = firebaseAuth.signInWithEmailAndPassword(cleanEmail, password).await()
            val authUser = authResult.user ?: throw Exception("Usuario no encontrado")

            val userDoc = firestore.collection("users")
                .document(authUser.uid)
                .get()
                .await()

            val fsUser = userDoc.toObject(FirestoreUser::class.java)

            val finalFsUser = if (fsUser == null) {
                android.util.Log.w("Login", "Documento no existe en Firestore, creando para uid: ${authUser.uid}")
                val defaultUser = FirestoreUser(
                    uid = authUser.uid,
                    name = cleanEmail.substringBefore("@"),
                    email = cleanEmail,
                    role = "CLIENT",
                    fcmToken = ""
                )
                firestore.collection("users")
                    .document(authUser.uid)
                    .set(defaultUser)
                    .await()
                defaultUser
            } else {
                fsUser
            }

            val user = finalFsUser.toUser()

            updateFcmToken(authUser.uid)
            userDao.insert(user)
            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(IllegalArgumentException("Usuario no encontrado"))
        } catch (e: Exception) {
            android.util.Log.e("Login", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun registerClient(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val cleanEmail = email.trim()
            val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val authUser = authResult.user ?: throw Exception("Error al crear usuario")

            val userData = mapOf(
                "uid" to authUser.uid,
                "name" to name.trim(),
                "email" to cleanEmail,
                "role" to UserRole.CLIENT.name,
                "fcmToken" to ""
            )

            firestore.collection("users")
                .document(authUser.uid)
                .set(userData)
                .await()

            val newUser = User(
                uid = authUser.uid,
                name = name.trim(),
                email = cleanEmail,
                role = UserRole.CLIENT
            )

            updateFcmToken(authUser.uid)
            userDao.insert(newUser)
            Result.success(newUser)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(IllegalArgumentException("La contraseña es muy débil (mínimo 6 caracteres)"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalArgumentException("El email ya está registrado"))
        } catch (e: Exception) {
            android.util.Log.e("Register", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun updateFcmToken(uid: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            firestore.collection("users")
                .document(uid)
                .set(
                    mapOf("fcmToken" to token),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()

            android.util.Log.d("FCM", "Token guardado correctamente para uid: $uid")
        } catch (e: Exception) {
            android.util.Log.e("FCM", "Error al guardar token (no fatal)", e)
        }
    }
}