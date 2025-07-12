package com.jovan.descripix.data.source.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jovan.descripix.data.source.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)

    @Query("DELETE FROM UserEntity")
    suspend fun deleteUser()

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    fun getUser(id: String): Flow<UserEntity?>
}