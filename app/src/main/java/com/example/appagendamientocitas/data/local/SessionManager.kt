package com.example.appagendamientocitas.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.appagendamientocitas.data.local.entity.User
import com.example.appagendamientocitas.data.local.entity.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sesión simple con SharedPreferences.
 * Permite saber quién está logueado al navegar entre pantallas
 * y al reiniciar la app.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(user: User) {
        prefs.edit()
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_ROLE, user.role.name)
            .apply()
    }

    fun getCurrentUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getCurrentUserName(): String =
        prefs.getString(KEY_USER_NAME, "").orEmpty()

    fun getCurrentRole(): UserRole? = prefs.getString(KEY_USER_ROLE, null)
        ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }

    fun isLoggedIn(): Boolean = getCurrentRole() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "salon_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_ROLE = "user_role"
    }
}