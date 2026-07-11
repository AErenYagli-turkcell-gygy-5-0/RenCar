package com.turkcell.rencar.domain.profile

interface ProfilePhotoRepository {

    suspend fun saveProfilePhoto(userId: String, jpegBytes: ByteArray)

    suspend fun getProfilePhoto(userId: String): ByteArray?
}
