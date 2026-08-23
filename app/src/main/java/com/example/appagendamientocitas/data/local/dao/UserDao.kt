package com.example.appagendamientocitas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appagendamientocitas.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun findByUid(uid: String): User?

    @Query("SELECT * FROM users WHERE role = 'CLIENT' ORDER BY name ASC")
    fun observeClients(): Flow<List<User>>
}