package com.turkcell.rencar.di

import com.turkcell.rencar.data.repository.auth.ApiAuthRepository
import com.turkcell.rencar.data.repository.cards.ApiCardRepository
import com.turkcell.rencar.data.repository.license.ApiLicenseRepository
import com.turkcell.rencar.data.repository.location.ApiLocationRepository
import com.turkcell.rencar.data.repository.profile.FileProfilePhotoRepository
import com.turkcell.rencar.data.repository.rental.ApiRentalRepository
import com.turkcell.rencar.data.repository.reservation.ApiReservationRepository
import com.turkcell.rencar.data.repository.vehicle.ApiVehicleRepository
import com.turkcell.rencar.data.repository.wallet.ApiWalletRepository
import com.turkcell.rencar.data.session.SharedPreferencesCardPaymentTransactionStore
import com.turkcell.rencar.data.session.SharedPreferencesReservationPlanStore
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.cards.CardRepository
import com.turkcell.rencar.domain.license.LicenseRepository
import com.turkcell.rencar.domain.location.LocationRepository
import com.turkcell.rencar.domain.profile.ProfilePhotoRepository
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.reservation.ReservationPlanStore
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.wallet.CardPaymentTransactionStore
import com.turkcell.rencar.domain.wallet.WalletRepository
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

    @Binds
    @Singleton
    abstract fun bindReservationRepository(
        implementation: ApiReservationRepository
    ): ReservationRepository

    @Binds
    @Singleton
    abstract fun bindReservationPlanStore(
        implementation: SharedPreferencesReservationPlanStore
    ): ReservationPlanStore

    @Binds
    @Singleton
    abstract fun bindCardPaymentTransactionStore(
        implementation: SharedPreferencesCardPaymentTransactionStore
    ): CardPaymentTransactionStore

    @Binds
    @Singleton
    abstract fun bindLocationRepository(implementation: ApiLocationRepository): LocationRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(implementation: ApiWalletRepository): WalletRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(implementation: ApiCardRepository): CardRepository
}
