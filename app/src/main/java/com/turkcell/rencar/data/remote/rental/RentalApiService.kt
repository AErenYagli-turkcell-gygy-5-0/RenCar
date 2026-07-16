package com.turkcell.rencar.data.remote.rental

import com.turkcell.rencar.data.remote.rental.dto.ActiveRentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.CreateRentalRequestDto
import com.turkcell.rencar.data.remote.rental.dto.RentalHistoryItemResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalPhotosStateResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalStatsResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalSummaryResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RentalApiService {
    @POST("rentals")
    suspend fun create(@Body request: CreateRentalRequestDto): RentalResponseDto

    @GET("rentals")
    suspend fun listMine(): List<RentalSummaryResponseDto>

    @GET("rentals")
    suspend fun listMineDetailed(): List<RentalHistoryItemResponseDto>

    @GET("rentals/stats")
    suspend fun getStats(@Query("month") month: String? = null): RentalStatsResponseDto

    @Multipart
    @POST("rentals/{id}/photos")
    suspend fun uploadPhoto(
        @Path("id") id: String,
        @Part side: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): RentalPhotosStateResponseDto

    @GET("rentals/{id}/photos")
    suspend fun getPhotos(@Path("id") id: String): RentalPhotosStateResponseDto

    @POST("rentals/{id}/start")
    suspend fun start(@Path("id") id: String): RentalResponseDto

    @DELETE("rentals/{id}")
    suspend fun cancel(@Path("id") id: String)

    @GET("rentals/active")
    suspend fun getActive(): ActiveRentalResponseDto

    @POST("rentals/{id}/finish")
    suspend fun finish(@Path("id") id: String): RentalResponseDto
}
