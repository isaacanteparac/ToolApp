package org.octanelab.toolapp.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ScreenshotItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val category: String,
    val relativePath: String,
    val dateAdded: Long,
    val size: Long
) {
    val yearLabel: String get() {
        val sdf = SimpleDateFormat("yyyy", Locale("es", "ES"))
        return sdf.format(Date(dateAdded * 1000))
    }

    val monthLabel: String get() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        return sdf.format(Date(dateAdded * 1000)).replaceFirstChar { it.uppercase() }
    }

    val dayLabel: String get() {
        val sdf = SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES"))
        return sdf.format(Date(dateAdded * 1000))
    }

    val hourOfDay: Int get() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateAdded * 1000
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    val hourBucketLabel: String get() {
        val hour = hourOfDay
        return String.format(Locale.getDefault(), "%02d:00 - %02d:59", hour, hour)
    }
}

object MediaRepository {

    /**
     * Query all screenshots stored in MediaStore under Pictures/Screenshots directory.
     */
    fun fetchScreenshots(context: Context): List<ScreenshotItem> {
        val screenshots = mutableListOf<ScreenshotItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.RELATIVE_PATH else MediaStore.Images.Media.DATA
        )

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Images.Media.DATA} LIKE ?"
        }

        val selectionArgs = arrayOf("%Screenshots%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            } else {
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Screenshot"
                val date = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn) ?: ""
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                val category = extractCategoryFromPath(path)

                screenshots.add(
                    ScreenshotItem(
                        id = id,
                        uri = contentUri,
                        displayName = name,
                        category = category,
                        relativePath = path,
                        dateAdded = date,
                        size = size
                    )
                )
            }
        }

        return screenshots
    }

    /**
     * Safely move/copy a newly taken screenshot into a dedicated subfolder based on app category.
     * Uses Scoped Storage ContentResolver insert + copy + delete to ensure full compatibility.
     */
    fun categorizeAndMoveScreenshot(context: Context, sourceUri: Uri, appCategory: String): Uri? {
        return try {
            val contentResolver = context.contentResolver

            // Determine display name and mime type of original image
            var displayName = "Screenshot_${System.currentTimeMillis()}.png"
            var mimeType = "image/png"

            contentResolver.query(
                sourceUri,
                arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.MIME_TYPE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val mimeIdx = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                    if (nameIdx != -1) cursor.getString(nameIdx)?.let { displayName = it }
                    if (mimeIdx != -1) cursor.getString(mimeIdx)?.let { mimeType = it }
                }
            }

            val targetPath = "Pictures/Screenshots/$appCategory/"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, targetPath)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val destUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            // Copy input stream to output stream
            val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            val outputStream: OutputStream? = contentResolver.openOutputStream(destUri)

            if (inputStream != null && outputStream != null) {
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(destUri, contentValues, null, null)
                }

                // Delete original file to finalize move action
                try {
                    contentResolver.delete(sourceUri, null, null)
                } catch (e: Exception) {
                    // Ignored if delete permissions restricted; copy succeeded anyway
                }

                destUri
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractCategoryFromPath(path: String): String {
        val normalized = path.replace('\\', '/')
        if (normalized.contains("Screenshots/")) {
            val sub = normalized.substringAfter("Screenshots/").trim('/')
            val parts = sub.split('/')
            if (parts.isNotEmpty() && parts[0].isNotBlank() && !parts[0].endsWith(".png") && !parts[0].endsWith(".jpg")) {
                return parts[0]
            }
        }
        return "General"
    }
}
