package com.turkcell.rencar.domain.license

import android.net.Uri

interface LicenseRepository {

    suspend fun uploadLicense(
        frontImageUri: Uri,
        backImageUri: Uri,
        selfieJpegBytes: ByteArray
    ): LicenseResult<UploadedLicense>

    suspend fun getLicenseStatus(): LicenseResult<LicenseStatus>
}
