package com.turkcell.rencar.data.remote.license

import com.turkcell.rencar.data.remote.license.dto.LicenseResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LicenseApiService {

    @Multipart
    @POST("license/upload")
    suspend fun upload(
        @Part front: MultipartBody.Part,
        @Part back: MultipartBody.Part
    ): LicenseResponseDto
}
