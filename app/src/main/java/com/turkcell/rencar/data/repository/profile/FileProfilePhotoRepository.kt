package com.turkcell.rencar.data.repository.profile

import android.content.Context
import com.turkcell.rencar.domain.profile.ProfilePhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

class FileProfilePhotoRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ProfilePhotoRepository {

    override suspend fun saveProfilePhoto(userId: String, jpegBytes: ByteArray) {
        withContext(Dispatchers.IO) {
            val directory = profilePhotoDirectory()
            if (!directory.exists()) directory.mkdirs()
            profilePhotoFile(userId).writeBytes(jpegBytes)
        }
    }

    override suspend fun getProfilePhoto(userId: String): ByteArray? =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = profilePhotoFile(userId)
                if (file.exists()) file.readBytes() else null
            }.getOrNull()
        }

    private fun profilePhotoDirectory(): File =
        File(context.filesDir, PROFILE_PHOTO_DIRECTORY)

    private fun profilePhotoFile(userId: String): File =
        File(profilePhotoDirectory(), "${userId.sha256()}.jpg")

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    private companion object {
        const val PROFILE_PHOTO_DIRECTORY = "profile_photos"
    }
}
