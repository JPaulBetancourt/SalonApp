package com.example.appagendamientocitas.data.local.entity

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirestoreUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "CLIENT",
    val fcmToken: String = ""
) {
    fun toUser(): User {
        return User(
            uid = uid,
            name = name,
            email = email,
            role = UserRole.valueOf(role),
            fcmToken = fcmToken
        )
    }
}