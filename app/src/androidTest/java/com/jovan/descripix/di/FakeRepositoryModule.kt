package com.jovan.descripix.di

import com.jovan.descripix.data.DescripixRepository
import com.jovan.descripix.domain.repository.IDescripixRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module(includes = [FakeNetworkModule::class, FakeDatabaseModule::class])
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class FakeRepositoryModule {
    @Binds
    abstract fun provideRepository(descripixRepository: DescripixRepository): IDescripixRepository
}