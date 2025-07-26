package com.jovan.descripix.di

import com.jovan.descripix.data.DescripixRepository
import com.jovan.descripix.domain.repository.IDescripixRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [NetworkModule::class, DatabaseModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideRepository(descripixRepository: DescripixRepository): IDescripixRepository
}