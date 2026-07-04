package com.turkcell.rencar.di

import com.turkcell.rencar.data.repository.auth.ApiAuthRepository
import com.turkcell.rencar.data.repository.license.ApiLicenseRepository
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.license.LicenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: ApiAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLicenseRepository(implementation: ApiLicenseRepository): LicenseRepository
}
