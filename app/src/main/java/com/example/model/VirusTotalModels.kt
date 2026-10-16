package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VirusTotalThreatLevel(val displayName: String) {
    SAFE("Safe"),
    CAREFUL("Careful"),
    DANGER("Danger!"),
    NOT_SCANNED("Not Scanned")
}

enum class VirusTotalRescanInterval(val displayName: String, val durationMillis: Long) {
    HOURS_1("1 Hour", 1 * 60 * 60 * 1000L),
    HOURS_6("6 Hours", 6 * 60 * 60 * 1000L),
    HOURS_24("24 Hours (1 Day)", 24 * 60 * 60 * 1000L),
    DAYS_7("7 Days", 7 * 24 * 60 * 60 * 1000L),
    DAYS_30("30 Days", 30L * 24 * 60 * 60 * 1000L),
    ALWAYS("Always Rescan", 0L)
}

data class VirusTotalScanResult(
    val url: String,
    val domain: String,
    val maliciousCount: Int = 0,
    val suspiciousCount: Int = 0,
    val harmlessCount: Int = 0,
    val undetectedCount: Int = 0,
    val totalEngines: Int = 0,
    val scanTimestamp: Long = System.currentTimeMillis(),
    val threatLevel: VirusTotalThreatLevel = calculateThreatLevel(maliciousCount),
    val permalink: String? = null,
    val scanDateFormatted: String = formatDate(scanTimestamp),
    val errorMessage: String? = null
) {
    companion object {
        fun calculateThreatLevel(maliciousCount: Int): VirusTotalThreatLevel {
            return when {
                maliciousCount >= 9 -> VirusTotalThreatLevel.DANGER
                maliciousCount in 4..8 -> VirusTotalThreatLevel.CAREFUL
                else -> VirusTotalThreatLevel.SAFE
            }
        }

        fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        fun notScanned(url: String, domain: String): VirusTotalScanResult {
            return VirusTotalScanResult(
                url = url,
                domain = domain,
                maliciousCount = 0,
                suspiciousCount = 0,
                harmlessCount = 0,
                undetectedCount = 0,
                totalEngines = 0,
                scanTimestamp = 0L,
                threatLevel = VirusTotalThreatLevel.NOT_SCANNED,
                permalink = null,
                scanDateFormatted = "Not scanned yet"
            )
        }
    }
}

data class VirusTotalWarningState(
    val targetUrl: String,
    val domain: String,
    val scanResult: VirusTotalScanResult,
    val threatLevel: VirusTotalThreatLevel,
    val showAreYouSureDialog: Boolean = false,
    val isAiProtection: Boolean = false,
    val aiReasoning: String? = null
)
