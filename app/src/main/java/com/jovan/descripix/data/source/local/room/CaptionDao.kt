package com.jovan.descripix.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(captionEntity: List<CaptionEntity>)

    @Query("DELETE FROM CaptionEntity")
    suspend fun deleteAll()

    @Query("SELECT * from CaptionEntity")
    fun getAllCaption(): Flow<List<CaptionEntity>>
}