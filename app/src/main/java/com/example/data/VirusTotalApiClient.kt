package com.example.data

import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.model.VirusTotalScanResult
import com.example.model.VirusTotalThreatLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object VirusTotalApiClient {

    private const val TAG = "VirusTotalApiClient"
    private const val BASE_URL = "https://www.virustotal.com/api/v3"

    suspend fun scanUrl(
        apiKey: String,
        targetUrl: String
    ): VirusTotalScanResult = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext VirusTotalScanResult(
                url = targetUrl,
                domain = extractDomain(targetUrl),
                errorMessage = "VirusTotal API key is not configured"
            )
        }

        val domain = extractDomain(targetUrl)

        // Generate VirusTotal URL identifier: base64 without padding of UTF-8 URL
        val urlId = try {
            val bytes = targetUrl.toByteArray(StandardCharsets.UTF_8)
            Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode URL for VirusTotal: $targetUrl", e)
            ""
        }

        // 1. Try URL lookup first if urlId is valid
        if (urlId.isNotEmpty()) {
            val urlEndpoint = "$BASE_URL/urls/$urlId"
            val result = queryVirusTotalEndpoint(urlEndpoint, trimmedKey, targetUrl, domain)
            if (result != null && result.errorMessage == null) {
                return@withContext result
            }
            // If error was unauthorized (401/403) or rate limit (429), return immediately
            if (result?.errorMessage?.contains("API key", ignoreCase = true) == true ||
                result?.errorMessage?.contains("Rate limit", ignoreCase = true) == true
            ) {
                return@withContext result
            }
        }

        // 2. If URL not found or error, query domain endpoint
        if (domain.isNotEmpty() && !domain.equals("localhost", ignoreCase = true)) {
            val domainEndpoint = "$BASE_URL/domains/$domain"
            val domainResult = queryVirusTotalEndpoint(domainEndpoint, trimmedKey, targetUrl, domain)
            if (domainResult != null) {
                return@withContext domainResult
            }
        }

        // Fallback: If site has no VT records, assume safe with 0 flags
        return@withContext VirusTotalScanResult(
            url = targetUrl,
            domain = domain,
            maliciousCount = 0,
            suspiciousCount = 0,
            harmlessCount = 0,
            undetectedCount = 0,
            totalEngines = 0,
            scanTimestamp = System.currentTimeMillis(),
            threatLevel = VirusTotalThreatLevel.SAFE,
            permalink = "https://www.virustotal.com/gui/domain/$domain"
        )
    }

    suspend fun testApiKey(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API key cannot be empty"))
        }

        try {
            // Check domain 'google.com' as lightweight test query
            val testUrl = URL("$BASE_URL/domains/google.com")
            val connection = (testUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("x-apikey", trimmedKey)
                setRequestProperty("User-Agent", "DevBrowser-Android")
                connectTimeout = 6000
                readTimeout = 6000
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                Result.success(true)
            } else if (responseCode == 401 || responseCode == 403) {
                Result.failure(IllegalArgumentException("Invalid VirusTotal API key (HTTP $responseCode)"))
            } else if (responseCode == 429) {
                Result.failure(IllegalStateException("VirusTotal rate limit exceeded (HTTP 429)"))
            } else {
                Result.failure(Exception("VirusTotal returned HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "VirusTotal API key verification failed", e)
            Result.failure(e)
        }
    }

    private fun queryVirusTotalEndpoint(
        endpointUrl: String,
        apiKey: String,
        originalUrl: String,
        domain: String
    ): VirusTotalScanResult? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("x-apikey", apiKey)
                setRequestProperty("User-Agent", "DevBrowser-Android")
                connectTimeout = 7000
                readTimeout = 7000
            }

            val responseCode = connection.responseCode
            if (responseCode == 401 || responseCode == 403) {
                return VirusTotalScanResult(
                    url = originalUrl,
                    domain = domain,
                    errorMessage = "Invalid VirusTotal API key"
                )
            }
            if (responseCode == 429) {
                return VirusTotalScanResult(
                    url = originalUrl,
                    domain = domain,
                    errorMessage = "VirusTotal API rate limit reached (Free tier: 4 req/min)"
                )
            }
            if (responseCode == 404) {
                // Not found on this specific endpoint
                return null
            }
            if (responseCode != 200) {
                return VirusTotalScanResult(
                    url = originalUrl,
                    domain = domain,
                    errorMessage = "VirusTotal error (HTTP $responseCode)"
                )
            }

            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val root = JSONObject(responseBody)
            val data = root.optJSONObject("data") ?: return null
            val attributes = data.optJSONObject("attributes") ?: return null
            val stats = attributes.optJSONObject("last_analysis_stats") ?: JSONObject()

            val malicious = stats.optInt("malicious", 0)
            val suspicious = stats.optInt("suspicious", 0)
            val harmless = stats.optInt("harmless", 0)
            val undetected = stats.optInt("undetected", 0)
            val totalEngines = malicious + suspicious + harmless + undetected

            val permalink = if (data.optString("type") == "url") {
                "https://www.virustotal.com/gui/url/${data.optString("id")}"
            } else {
                "https://www.virustotal.com/gui/domain/$domain"
            }

            VirusTotalScanResult(
                url = originalUrl,
                domain = domain,
                maliciousCount = malicious,
                suspiciousCount = suspicious,
                harmlessCount = harmless,
                undetectedCount = undetected,
                totalEngines = totalEngines,
                scanTimestamp = System.currentTimeMillis(),
                threatLevel = VirusTotalScanResult.calculateThreatLevel(malicious),
                permalink = permalink
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error querying VirusTotal at $endpointUrl", e)
            VirusTotalScanResult(
                url = originalUrl,
                domain = domain,
                errorMessage = "Network error: ${e.localizedMessage ?: "Unable to contact VirusTotal"}"
            )
        } finally {
            connection?.disconnect()
        }
    }

    fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host?.lowercase() ?: url
        } catch (e: Exception) {
            url
        }
    }
}
