package com.jovan.descripix.ui.di

import com.jovan.descripix.domain.usecase.DescripixInteractor
import com.jovan.descripix.domain.usecase.DescripixUseCase
import com.jovan.descripix.utils.credential.CredentialService
import com.jovan.descripix.utils.credential.FakeCredentialService
import com.jovan.descripix.utils.credential.ICredentialService
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [ViewModelComponent::class],
    replaces = [AppModule::class]
)
abstract class FakeAppModule {

    @Binds
    @ViewModelScoped
    abstract fun provideDescribitUseCase(descripixInteractor: DescripixInteractor): DescripixUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindCredentialService(credentialService: FakeCredentialService): ICredentialService
}