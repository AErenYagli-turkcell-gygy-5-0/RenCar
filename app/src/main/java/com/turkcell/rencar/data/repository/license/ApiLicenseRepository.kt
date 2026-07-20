package com.turkcell.rencar.data.repository.license

import android.content.Context
import android.net.Uri
import com.turkcell.rencar.data.remote.license.LicenseApiService
import com.turkcell.rencar.data.remote.license.dto.LicenseStatusResponseDto
import com.turkcell.rencar.domain.license.LicenseError
import com.turkcell.rencar.domain.license.LicenseReviewStatus
import com.turkcell.rencar.domain.license.LicenseRepository
import com.turkcell.rencar.domain.license.LicenseResult
import com.turkcell.rencar.domain.license.LicenseStatus
import com.turkcell.rencar.domain.license.UploadedLicense
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiLicenseRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiService: LicenseApiService
) : LicenseRepository {

    override suspend fun uploadLicense(
        frontImageUri: Uri,
        backImageUri: Uri,
        selfieJpegBytes: ByteArray
    ): LicenseResult<UploadedLicense> = runRequest {
        val response = apiService.upload(
            front = frontImageUri.toMultipartPart(FRONT_FIELD_NAME),
            back = backImageUri.toMultipartPart(BACK_FIELD_NAME),
            selfie = selfieJpegBytes.toMultipartPart(SELFIE_FIELD_NAME)
        )
        UploadedLicense(
            id = response.id,
            status = response.status,
            frontImageUrl = response.frontImageUrl,
            backImageUrl = response.backImageUrl
        )
    }

    override suspend fun getLicenseStatus(): LicenseResult<LicenseStatus> = runRequest {
        apiService.getStatus().toDomain()
    }

    private suspend fun <T> runRequest(request: suspend () -> T): LicenseResult<T> = try {
        LicenseResult.Success(request())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        LicenseResult.Failure(error.code().toLicenseError())
    } catch (error: IOException) {
        LicenseResult.Failure(LicenseError.Network)
    } catch (error: Exception) {
        LicenseResult.Failure(LicenseError.Unexpected)
    }

    private fun Uri.toMultipartPart(fieldName: String): MultipartBody.Part {
        val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() }
            ?: throw IOException("Görsel okunamadı.")
        val mimeType = context.contentResolver.getType(this) ?: DEFAULT_MIME_TYPE
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, "$fieldName.jpg", requestBody)
    }

    private fun ByteArray.toMultipartPart(fieldName: String): MultipartBody.Part {
        val requestBody = toRequestBody(DEFAULT_MIME_TYPE.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, "$fieldName.jpg", requestBody)
    }

    private fun Int.toLicenseError(): LicenseError = when (this) {
        HTTP_BAD_REQUEST -> LicenseError.InvalidFile
        HTTP_UNAUTHORIZED -> LicenseError.Unauthorized
        HTTP_CONFLICT -> LicenseError.AlreadyReviewedOrCustomer
        HTTP_PAYLOAD_TOO_LARGE -> LicenseError.FileTooLarge
        else -> LicenseError.Unexpected
    }

    private companion object {
        const val FRONT_FIELD_NAME = "front"
        const val BACK_FIELD_NAME = "back"
        const val SELFIE_FIELD_NAME = "selfie"
        const val DEFAULT_MIME_TYPE = "image/jpeg"
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_CONFLICT = 409
        const val HTTP_PAYLOAD_TOO_LARGE = 413
    }
}

internal fun LicenseStatusResponseDto.toDomain() = LicenseStatus(
    reviewStatus = LicenseReviewStatus.valueOf(status),
    frontImageUrl = frontImageUrl,
    backImageUrl = backImageUrl,
    rejectReason = rejectReason,
    reviewedAt = reviewedAt
)
