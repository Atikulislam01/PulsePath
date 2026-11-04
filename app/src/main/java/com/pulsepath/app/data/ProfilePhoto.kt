package com.pulsepath.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File

private const val MAX_SIDE = 512

fun profilePhotoFile(context: Context) = File(context.filesDir, "profile_photo.jpg")

/** Loads the saved profile photo, or null if there is none. */
fun loadProfilePhoto(context: Context): Bitmap? {
    val file = profilePhotoFile(context)
    if (!file.exists()) return null
    return try {
        BitmapFactory.decodeFile(file.absolutePath)
    } catch (e: Exception) {
        null
    }
}

fun removeProfilePhoto(context: Context) {
    profilePhotoFile(context).delete()
}

/** Copies the chosen picture into the app as a small square JPEG. Call from a background thread. */
fun saveProfilePhoto(context: Context, uri: Uri): Boolean {
    return try {
        val resolver = context.contentResolver

        // Read the size first so big photos can be loaded smaller
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (bounds.outWidth / sample > MAX_SIDE * 2 || bounds.outHeight / sample > MAX_SIDE * 2) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return false

        // Fix photos that are stored sideways
        val rotation = resolver.openInputStream(uri)?.use { stream ->
            when (
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        // Crop to a centered square, then shrink
        val side = minOf(bitmap.width, bitmap.height)
        val square = Bitmap.createBitmap(
            bitmap,
            (bitmap.width - side) / 2,
            (bitmap.height - side) / 2,
            side,
            side
        )
        val finalBitmap = if (side > MAX_SIDE) {
            Bitmap.createScaledBitmap(square, MAX_SIDE, MAX_SIDE, true)
        } else {
            square
        }

        profilePhotoFile(context).outputStream().use {
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        true
    } catch (e: Exception) {
        false
    }
}