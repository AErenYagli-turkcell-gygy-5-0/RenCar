package com.turkcell.rencar.di

import com.turkcell.rencar.data.repository.ApiAuthRepository
import com.turkcell.rencar.domain.auth.AuthRepository
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
}
