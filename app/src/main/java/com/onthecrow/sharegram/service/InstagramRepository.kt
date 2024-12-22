package com.onthecrow.sharegram.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.time.Duration
import javax.inject.Inject

class InstagramRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    // todo move to a local data source later
    private val externalFilesDir = context.getExternalFilesDir(null)
    private val json by lazy(LazyThreadSafetyMode.NONE) { Json { ignoreUnknownKeys = true } }
    private val client by lazy(LazyThreadSafetyMode.NONE) {
        OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(DURATION_CALL_SECONDS))
            .build()
    }

    suspend fun downloadPost(url: String, isVideo: Boolean = true): Result<String> {
        val request = createDownloadRequest(URL(url))
        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (response.isSuccessful) {
                val outputFile = File(externalFilesDir, if (isVideo) REEL_FILE_NAME else POST_FILE_NAME)
                withContext(Dispatchers.IO) {
                    response.body?.byteStream().use { inputStream ->
                        FileOutputStream(outputFile).use { outputStream ->
                            copyStream(inputStream, outputStream)
                        }
                    }
                }
                return Result.success(outputFile.toString())
            } else {
                // todo add a proper error handling
                val exception = IllegalArgumentException("Request failed with code: ${response.code}")
                Log.e(this::class.java.simpleName, exception.message, exception)
                return Result.failure(exception)
            }
        } catch (error: Throwable) {
            Log.e(this::class.java.simpleName, error.message, error)
            return Result.failure(error)
        }
    }

    suspend fun getPost(shortCode: String): Result<InstagramPost> {
        val request = createPostRequest(shortCode)
        return withContext(Dispatchers.IO) {
            executeRequest(request).firstOrNull()
                .let { response ->
                    val resultResponseNull =
                        Result.failure<InstagramPost>(IllegalArgumentException("Response is null"))
                    when {
                        response == null -> resultResponseNull
                        response.isSuccess -> Result.success(json.decodeFromString(response.getOrThrow()))
                        response.isFailure -> Result.failure(response.exceptionOrNull()!!)
                        else -> resultResponseNull
                    }
                }
        }
    }

    private fun createPostRequest(shortCode: String): Request {
        val requestBody = "variables={\"shortcode\":\"$shortCode\"}&doc_id=8845758582119845"
            .toRequestBody(MEDIA_TYPE.toMediaType())
        return Request.Builder()
            .url(URL_INSTAGRAM_GET_POST)
            .post(requestBody)
            .build()
    }

    private fun createDownloadRequest(fileUrl: URL): Request {
        return Request.Builder()
            .url(fileUrl)
            .build()
    }

    private fun executeRequest(request: Request): Flow<Result<String>> {
        return callbackFlow {
            val call = client.newCall(request)
            call.enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        trySend(Result.failure(e))
                        Log.e(this::class.java.simpleName, e.message, e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            trySend(Result.success(responseBody))
                            Log.d(this::class.java.simpleName, "Response: $responseBody")
                        } else {
                            Log.d(
                                this::class.java.simpleName,
                                "Request failed with code: ${response.code}"
                            )
                        }
                    }
                }
            )
            awaitClose { call.cancel() }
        }
    }

    // todo move to utils or something
    private fun copyStream(input: InputStream?, output: FileOutputStream) {
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (input?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }

    companion object {
        private const val DURATION_CALL_SECONDS = 10L
        private const val URL_INSTAGRAM_GET_POST = "https://www.instagram.com/graphql/query"
        private const val MEDIA_TYPE = "application/x-www-form-urlencoded"
        private const val REEL_FILE_NAME = "video.mp4"
        private const val POST_FILE_NAME = "post.jpg"
    }
}