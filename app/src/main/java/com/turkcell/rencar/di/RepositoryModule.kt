package com.turkcell.rencar.di

import com.turkcell.rencar.data.repository.auth.ApiAuthRepository
import com.turkcell.rencar.data.repository.license.ApiLicenseRepository
import com.turkcell.rencar.data.repository.profile.FileProfilePhotoRepository
import com.turkcell.rencar.data.repository.rental.ApiRentalRepository
import com.turkcell.rencar.data.repository.vehicle.ApiVehicleRepository
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.license.LicenseRepository
import com.turkcell.rencar.domain.profile.ProfilePhotoRepository
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.vehicle.VehicleRepository
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

    @Binds
    @Singleton
    abstract fun bindProfilePhotoRepository(
        implementation: FileProfilePhotoRepository
    ): ProfilePhotoRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(implementation: ApiVehicleRepository): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindRentalRepository(implementation: ApiRentalRepository): RentalRepository
}
