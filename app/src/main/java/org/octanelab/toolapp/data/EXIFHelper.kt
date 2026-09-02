package org.octanelab.toolapp.data

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

object EXIFHelper {

    /**
     * Read the image description from EXIF tags (TAG_IMAGE_DESCRIPTION or TAG_USER_COMMENT).
     */
    fun readDescription(context: Context, uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val desc = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)
                val comment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT)
                when {
                    !desc.isNull_or_blank() -> desc
                    !comment.isNull_or_blank() -> comment
                    else -> ""
                }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Write the user's movie synopsis / description directly into the image's EXIF metadata tags.
     * Uses openFileDescriptor in "rw" mode to modify ContentResolver URIs natively on Scoped Storage.
     */
    fun writeDescription(context: Context, uri: Uri, description: String): Boolean {
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "rw")
            pfd?.use { descriptor ->
                val exif = ExifInterface(descriptor.fileDescriptor)
                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description)
                exif.setAttribute(ExifInterface.TAG_USER_COMMENT, description)
                exif.saveAttributes()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this.isNullOrEmpty() || this.trim().isEmpty()
    }
}
