package com.jovan.descripix.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.datastore.dataStore
import com.jovan.descripix.data.source.local.room.DescripixDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    private val passpharase: ByteArray = SQLiteDatabase.getBytes("descripix".toCharArray())
    val factory = SupportFactory(passpharase)

    //Room
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): DescripixDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            DescripixDatabase::class.java,
            "Describit.db"
        ).fallbackToDestructiveMigration()
            .openHelperFactory(factory)
            .build()

    @Provides
    fun provideCaptionDao(database: DescripixDatabase) = database.captionDao()

    @Provides
    fun provideUserDao(database: DescripixDatabase) = database.userDao()

    //Datastore
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
    @Provides
    @Singleton
    fun provideUserPreference(dataStore: DataStore<Preferences>): UserPreference {
        return UserPreference(dataStore)
    }

}