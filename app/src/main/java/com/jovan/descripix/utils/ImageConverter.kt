package com.jovan.descripix.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

object ImageConverter {
    @SuppressLint("ConstantLocale")
    private val timeStamp: String =
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())

    fun uriToFile(imageUri: Uri, context: Context): File {
        val myFile = createCustomTempFile(context)
        val inputStream = context.contentResolver.openInputStream(imageUri) as InputStream
        val outputStream = FileOutputStream(myFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) outputStream.write(buffer, 0, length)
        outputStream.close()
        inputStream.close()
        return myFile
    }

    fun createCustomTempFile(context: Context): File {
        val filesDir = context.externalCacheDir
        return File.createTempFile(timeStamp, ".jpg", filesDir)
    }

    suspend fun downloadImageToFile(context: Context, imageUrl: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()

                val inputStream = BufferedInputStream(url.openStream())
                val tempFile = createCustomTempFile(context)
                val outputStream = FileOutputStream(tempFile)

                inputStream.copyTo(outputStream)

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                tempFile.resizeIfTooLarge()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

