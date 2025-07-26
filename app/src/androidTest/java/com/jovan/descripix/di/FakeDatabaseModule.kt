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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
class FakeDatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): DescripixDatabase =
        Room.inMemoryDatabaseBuilder(
            context.applicationContext,
            DescripixDatabase::class.java,
        ).allowMainThreadQueries().build()

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