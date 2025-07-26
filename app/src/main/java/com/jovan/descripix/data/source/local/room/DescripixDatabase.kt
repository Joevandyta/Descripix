package com.jovan.descripix.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity

@Database(entities = [CaptionEntity::class, UserEntity::class], version = 1, exportSchema = false)
abstract class DescripixDatabase: RoomDatabase() {
    abstract fun captionDao(): CaptionDao
    abstract fun userDao(): UserDao
}