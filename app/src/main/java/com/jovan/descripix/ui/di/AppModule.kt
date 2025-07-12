package com.jovan.descripix.ui.di

import android.content.Context
import com.jovan.descripix.data.source.remote.network.AndroidConnectivityObserver
import com.jovan.descripix.data.source.remote.network.ConnectivityObserver
import com.jovan.descripix.domain.usecase.DescripixInteractor
import com.jovan.descripix.domain.usecase.DescripixUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class AppModule {

    @Binds
    @ViewModelScoped
    abstract fun provideDescribitUseCase(describitInteractor: DescripixInteractor): DescripixUseCase

}