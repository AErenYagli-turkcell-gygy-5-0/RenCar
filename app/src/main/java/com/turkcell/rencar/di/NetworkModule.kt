package com.turkcell.rencar.di

import com.turkcell.rencar.BuildConfig
import com.turkcell.rencar.data.remote.AuthInterceptor
import com.turkcell.rencar.data.remote.auth.AuthApiService
import com.turkcell.rencar.data.remote.cards.CardsApiService
import com.turkcell.rencar.data.remote.license.LicenseApiService
import com.turkcell.rencar.data.remote.rental.RentalApiService
import com.turkcell.rencar.data.remote.reservation.ReservationApiService
import com.turkcell.rencar.data.remote.vehicle.VehicleApiService
import com.turkcell.rencar.data.remote.wallet.WalletApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .applyDebugLogging()
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideLicenseApiService(retrofit: Retrofit): LicenseApiService =
        retrofit.create(LicenseApiService::class.java)

    @Provides
    @Singleton
    fun provideVehicleApiService(retrofit: Retrofit): VehicleApiService =
        retrofit.create(VehicleApiService::class.java)

    @Provides
    @Singleton
    fun provideRentalApiService(retrofit: Retrofit): RentalApiService =
        retrofit.create(RentalApiService::class.java)

    @Provides
    @Singleton
    fun provideReservationApiService(retrofit: Retrofit): ReservationApiService =
        retrofit.create(ReservationApiService::class.java)

    @Provides
    @Singleton
    fun provideWalletApiService(retrofit: Retrofit): WalletApiService =
        retrofit.create(WalletApiService::class.java)

    @Provides
    @Singleton
    fun provideCardsApiService(retrofit: Retrofit): CardsApiService =
        retrofit.create(CardsApiService::class.java)
}
