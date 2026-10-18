package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object GitHubUpdateApiClient {

    private const val TAG = "GitHubUpdateApiClient"
    const val DEFAULT_REPO_OWNER = "Green0Field"
    const val DEFAULT_REPO_NAME = "DevBrowser"
    private const val BASE_GITHUB_API = "https://api.github.com/repos/$DEFAULT_REPO_OWNER/$DEFAULT_REPO_NAME"

    sealed class Result {
        data class Success(val updateInfo: AppUpdateInfo) : Result()
        data class Error(val message: String) : Result()
    }

    /**
     * Compare semantic version numbers (e.g. "0.2" vs "0.1", "1.0.0" vs "0.9.9", "v0.1.1" vs "0.1").
     * Returns true if latest is strictly newer than current.
     */
    fun isVersionNewer(latest: String, current: String): Boolean {
        val cleanLatest = latest.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = current.trim().removePrefix("v").removePrefix("V")
        if (cleanLatest.isEmpty() || cleanCurrent.isEmpty()) return false
        if (cleanLatest == cleanCurrent) return false

        val latestParts = cleanLatest.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".", "-", "_").mapNotNull { it.toIntOrNull() }

        if (latestParts.isNotEmpty() && currentParts.isNotEmpty()) {
            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        }

        // Fallback alphanumeric comparison if not pure integers
        return cleanLatest.compareTo(cleanCurrent, ignoreCase = true) > 0
    }

    suspend fun checkForUpdates(
        apiKey: String = "",
        currentVersionName: String = BuildConfig.VERSION_NAME,
        includePrereleases: Boolean = false
    ): Result = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        val latestEndpoint = "$BASE_GITHUB_API/releases/latest"
        val releasesListEndpoint = "$BASE_GITHUB_API/releases"

        try {
            if (!includePrereleases) {
                val (statusCode, responseBody) = executeGet(latestEndpoint, trimmedKey)
                if (statusCode in 200..299 && responseBody != null) {
                    val releaseObj = JSONObject(responseBody)
                    val updateInfo = parseReleaseObject(releaseObj, currentVersionName)
                    return@withContext Result.Success(updateInfo)
                }
                if (statusCode != 404) {
                    return@withContext Result.Error(getErrorMessage(statusCode))
                }
            }

            val (listStatus, listBody) = executeGet(releasesListEndpoint, trimmedKey)
            if (listStatus in 200..299 && listBody != null) {
                val array = JSONArray(listBody)
                val targetObj = if (includePrereleases) {
                    if (array.length() > 0) array.getJSONObject(0) else null
                } else {
                    var found: JSONObject? = null
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val isPrerelease = obj.optBoolean("prerelease", false)
                        if (!isPrerelease) {
                            found = obj
                            break
                        }
                    }
                    found ?: if (array.length() > 0) array.getJSONObject(0) else null
                }

                if (targetObj != null) {
                    val updateInfo = parseReleaseObject(targetObj, currentVersionName)
                    return@withContext Result.Success(updateInfo)
                } else {
                    return@withContext Result.Success(
                        AppUpdateInfo(
                            latestVersionName = currentVersionName,
                            isUpdateAvailable = false,
                            releaseTitle = "No releases published yet"
                        )
                    )
                }
            }

            val errorMsg = getErrorMessage(listStatus)
            Result.Error(errorMsg)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates from GitHub", e)
            val msg = e.localizedMessage ?: e.message ?: "Failed to check for updates"
            Result.Error("Network error: $msg")
        }
    }

    private fun getErrorMessage(statusCode: Int): String = when (statusCode) {
        401 -> "GitHub API authentication failed. Please verify your Personal Access Token."
        403, 429 -> "GitHub API rate limit exceeded. Add an optional GitHub API key in settings to increase limits."
        404 -> "GitHub repository '$DEFAULT_REPO_OWNER/$DEFAULT_REPO_NAME' not found or has no releases."
        else -> "GitHub API returned HTTP $statusCode"
    }

    private fun parseReleaseObject(json: JSONObject, currentVersionName: String): AppUpdateInfo {
        val tagName = json.optString("tag_name", "").trim()
        val rawName = json.optString("name", "").trim()
        val cleanLatestVersion = tagName.removePrefix("v").removePrefix("V").ifEmpty {
            rawName.removePrefix("v").removePrefix("V")
        }
        val displayTitle = if (rawName.isNotBlank()) rawName else tagName
        val body = json.optString("body", "").trim()
        val htmlUrl = json.optString("html_url", "https://github.com/$DEFAULT_REPO_OWNER/$DEFAULT_REPO_NAME/releases")
        val publishedAt = json.optString("published_at", "")

        var downloadUrl = ""
        var apkFileName = ""
        var apkSizeFormatted = ""

        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val assetName = asset.optString("name", "")
                if (assetName.endsWith(".apk", ignoreCase = true)) {
                    apkFileName = assetName
                    downloadUrl = asset.optString("browser_download_url", "")
                    val sizeBytes = asset.optLong("size", 0L)
                    if (sizeBytes > 0) {
                        val sizeMb = sizeBytes / (1024.0 * 1024.0)
                        apkSizeFormatted = String.format(java.util.Locale.US, "%.1f MB", sizeMb)
                    }
                    // Prefer release APK over debug APK
                    if (assetName.contains("release", ignoreCase = true)) {
                        break
                    }
                }
            }
        }

        if (downloadUrl.isEmpty()) {
            downloadUrl = htmlUrl
        }

        val isNewer = isVersionNewer(cleanLatestVersion, currentVersionName)

        return AppUpdateInfo(
            latestVersionName = cleanLatestVersion.ifEmpty { currentVersionName },
            releaseTitle = displayTitle,
            releaseNotes = body,
            publishedAt = publishedAt,
            downloadUrl = downloadUrl,
            htmlUrl = htmlUrl,
            apkFileName = apkFileName,
            apkSizeFormatted = apkSizeFormatted,
            isUpdateAvailable = isNewer
        )
    }

    private fun executeGet(urlStr: String, apiKey: String): Pair<Int, String?> {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 12000
            conn.readTimeout = 12000
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.setRequestProperty("User-Agent", "DevBrowser-Android-App")

            if (apiKey.isNotBlank()) {
                val tokenHeader = if (apiKey.startsWith("ghp_") || apiKey.startsWith("github_pat_") || apiKey.startsWith("Bearer ")) {
                    if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                } else {
                    "token $apiKey"
                }
                conn.setRequestProperty("Authorization", tokenHeader)
            }

            val statusCode = conn.responseCode
            val isSuccess = statusCode in 200..299
            val inputStream = if (isSuccess) conn.inputStream else conn.errorStream

            val body = if (inputStream != null) {
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } else null

            Pair(statusCode, body)
        } finally {
            conn?.disconnect()
        }
    }
}
