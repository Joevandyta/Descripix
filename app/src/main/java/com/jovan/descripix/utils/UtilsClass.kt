package com.jovan.descripix.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.jovan.descripix.data.source.remote.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt
import androidx.core.graphics.scale

private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"

@SuppressLint("ConstantLocale")
private val timeStamp: String =
    SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())
private const val MAXIMAL_SIZE = 1000000
private const val MAXPIXELCOUNT = 33177600
fun simpleToast(context: Context, string: String) {
    Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
}


suspend fun <T> handleApiException(
    apiCall: suspend () -> ApiResponse<T>
): ApiResponse<T> {
    return try {
        apiCall()
    } catch (e: SocketException) {
        ApiResponse(false, "Connection Timeout", null)
    } catch (e: SocketTimeoutException) {
        ApiResponse(false, "Connection Timeout", null)
    } catch (e: HttpException) {
        val errorMessage = try {
            val errorJson = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorJson, ApiResponse::class.java)
            Log.d("handleApiException", errorResponse.message.toString())
            errorResponse.message.toString()
        } catch (parseException: Exception) {
            e.message.toString()
        }

        ApiResponse(false, errorMessage, null)
    }
}

fun isConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

}

fun dateFormater(date: Date): String {
    val formatter = SimpleDateFormat("dd-MM-yyyy, HH:mm", Locale.getDefault())
    return formatter.format(date)
}

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

fun File.reduceFileSize(): File {
    val file = this
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPictByteArray = bmpStream.toByteArray()
        streamLength = bmpPictByteArray.size
        compressQuality -= 5
    } while (streamLength > MAXIMAL_SIZE)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

fun File.resizeIfTooLarge(): File {
    val file = this
    val maxPixelCount = 33177600 // 33.1 MP

    val bitmap = BitmapFactory.decodeFile(file.path)
    val pixelCount = bitmap.width * bitmap.height

    if (pixelCount > maxPixelCount) {
        // Hitung rasio pengurangan
        val scale = sqrt(maxPixelCount.toDouble() / pixelCount)
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        val resized = bitmap.scale(newWidth, newHeight)
        FileOutputStream(file).use {
            resized.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }

    return file
}
fun File.moveFileToPersistentStorage(context: Context): File {
    val cacheFile =this
    val targetDir = File(context.getExternalFilesDir("images"), cacheFile.name)
    cacheFile.copyTo(targetDir, overwrite = true)
    return targetDir
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

fun String.convertBirthDate(): String {
    if (this.isBlank()) return this

    return try {
        if (this.contains("-") && this.length == 10) {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val date = inputFormat.parse(this)
            date?.let { outputFormat.format(it) } ?: this
        } else {
            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val date = inputFormat.parse(this)
            date?.let { outputFormat.format(it) } ?: this
        }
    } catch (e: Exception) {
        this
    }
}
