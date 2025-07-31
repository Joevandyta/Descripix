package com.jovan.descripix.ui.di

import com.jovan.descripix.domain.usecase.DescripixInteractor
import com.jovan.descripix.domain.usecase.DescripixUseCase
import com.jovan.descripix.utils.credential.CredentialService
import com.jovan.descripix.utils.credential.ICredentialService
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
    abstract fun provideDescribitUseCase(descripixInteractor: DescripixInteractor): DescripixUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindCredentialService(credentialService: CredentialService): ICredentialService
}