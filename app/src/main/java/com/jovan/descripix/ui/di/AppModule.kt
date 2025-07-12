package com.jovan.descripix.ui.di

import com.jovan.descripix.domain.usecase.DescripixInteractor
import com.jovan.descripix.domain.usecase.DescripixUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class AppModule {

    @Binds
    @ViewModelScoped
    abstract fun provideDescribitUseCase(describitInteractor: DescripixInteractor): DescripixUseCase

}