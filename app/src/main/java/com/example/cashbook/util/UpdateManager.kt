package com.example.cashbook.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.google.gson.Gson
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class GithubRelease(
    val tag_name: String,
    val assets: List<GithubAsset>
)

data class GithubAsset(
    val browser_download_url: String,
    val name: String
)

object UpdateManager {
    private val client = OkHttpClient()
    private val gson = Gson()
    private const val GITHUB_API_URL = "https://api.github.com/repos/Ozkan95/CashBook/releases/latest"

    fun checkForUpdate(currentVersion: String, onResult: (GithubRelease?) -> Unit) {
        val request = Request.Builder().url(GITHUB_API_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    try {
                        val release = gson.fromJson(json, GithubRelease::class.java)
                        if (isNewer(release.tag_name, currentVersion)) {
                            onResult(release)
                        } else {
                            onResult(null)
                        }
                    } catch (e: Exception) {
                        onResult(null)
                    }
                } ?: onResult(null)
            }
        })
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val latestClean = latest.removePrefix("v").split(".")
        val currentClean = current.removePrefix("v").split(".")
        
        for (i in 0 until minOf(latestClean.size, currentClean.size)) {
            val l = latestClean[i].toIntOrNull() ?: 0
            val c = currentClean[i].toIntOrNull() ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return latestClean.size > currentClean.size
    }

    fun downloadAndInstall(context: Context, url: String, fileName: String, onProgress: (Float) -> Unit, onComplete: (Boolean) -> Unit) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onComplete(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body ?: return onComplete(false)
                val totalBytes = body.contentLength()
                val file = File(context.cacheDir, fileName)
                
                try {
                    body.byteStream().use { input ->
                        FileOutputStream(file).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var totalRead = 0L
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                if (totalBytes > 0) {
                                    onProgress(totalRead.toFloat() / totalBytes)
                                }
                            }
                        }
                    }
                    installApk(context, file)
                    onComplete(true)
                } catch (e: Exception) {
                    onComplete(false)
                }
            }
        })
    }

    private fun installApk(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
