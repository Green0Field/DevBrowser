package com.example.ui.pages.error

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.model.SearchEngineItem
import com.example.model.WebPageError
import com.example.model.WebPageErrorType
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevRose
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WebErrorPage(
    error: WebPageError,
    searchEngine: SearchEngineItem,
    canGoBack: Boolean,
    onRetry: () -> Unit,
    onGoBack: () -> Unit,
    onGoHome: () -> Unit,
    onSearchWeb: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showTechDetails by remember { mutableStateOf(false) }

    val cleanHost = remember(error.url) {
        try {
            val uri = URI(error.url)
            uri.host ?: error.url.removePrefix("https://").removePrefix("http://").substringBefore("/")
        } catch (_: Exception) {
            error.url.removePrefix("https://").removePrefix("http://").substringBefore("/")
        }
    }

    val themeColor = when (error.errorType) {
        WebPageErrorType.WEBSITE_DOES_NOT_EXIST -> cs.primary
        WebPageErrorType.NO_INTERNET -> DevAmber
        WebPageErrorType.CONNECTION_TIMED_OUT -> DevAmber
        WebPageErrorType.CANNOT_CONNECT -> DevRose
        WebPageErrorType.SECURITY_SSL_ERROR -> DevRose
        WebPageErrorType.INVALID_ADDRESS -> DevAmber
        WebPageErrorType.UNEXPECTED_ERROR -> DevRose
    }

    val icon: ImageVector = when (error.errorType) {
        WebPageErrorType.WEBSITE_DOES_NOT_EXIST -> Icons.Default.Language
        WebPageErrorType.NO_INTERNET -> Icons.Default.WarningAmber
        WebPageErrorType.CONNECTION_TIMED_OUT -> Icons.Default.WarningAmber
        WebPageErrorType.CANNOT_CONNECT -> Icons.Default.ErrorOutline
        WebPageErrorType.SECURITY_SSL_ERROR -> Icons.Default.Security
        WebPageErrorType.INVALID_ADDRESS -> Icons.Default.Info
        WebPageErrorType.UNEXPECTED_ERROR -> Icons.Default.Warning
    }

    val badgeCode = when (error.errorType) {
        WebPageErrorType.WEBSITE_DOES_NOT_EXIST -> "DNS_PROBE_FINISHED_NXDOMAIN"
        WebPageErrorType.NO_INTERNET -> "ERR_INTERNET_DISCONNECTED"
        WebPageErrorType.CONNECTION_TIMED_OUT -> "ERR_CONNECTION_TIMED_OUT"
        WebPageErrorType.CANNOT_CONNECT -> "ERR_CONNECTION_REFUSED"
        WebPageErrorType.SECURITY_SSL_ERROR -> "ERR_SSL_PROTOCOL_ERROR"
        WebPageErrorType.INVALID_ADDRESS -> "ERR_INVALID_URL"
        WebPageErrorType.UNEXPECTED_ERROR -> "ERROR_CODE_${error.errorCode}"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error Icon with double-circle ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.12f))
                    .border(2.dp, themeColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Technical Error Pill Badge
            Surface(
                color = themeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = badgeCode,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = themeColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = error.errorType.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = cs.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = error.errorType.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // URL Chip with copy action
            Surface(
                color = cs.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = error.url,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(error.url))
                            Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy URL",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Prominent Unexpected Error Box (as requested: show error code for unexpected errors)
            if (error.errorType == WebPageErrorType.UNEXPECTED_ERROR) {
                Surface(
                    color = DevRose.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DevRose.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("unexpected_error_box")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DevRose, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Unknown Webview Error",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DevRose
                            )
                        }
                        Text(
                            text = "Error Code: ${error.errorCode} (${error.errorName})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        if (error.description.isNotBlank()) {
                            Text(
                                text = "Description: ${error.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Troubleshooting Suggestions Card
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Suggestions:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )

                    val tips = when (error.errorType) {
                        WebPageErrorType.WEBSITE_DOES_NOT_EXIST -> listOf(
                            "Check the web address for typing errors (e.g. '.com' vs '.co')",
                            "Use the search button below to search for '$cleanHost'",
                            "The website may have closed, moved to a new domain, or DNS records have changed"
                        )
                        WebPageErrorType.NO_INTERNET -> listOf(
                            "Check if Wi-Fi or Cellular Data is enabled in Android settings",
                            "Ensure Airplane mode is turned off",
                            "Check if other apps are able to connect to the internet",
                            "Restart your router or reconnect to your wireless network"
                        )
                        WebPageErrorType.CONNECTION_TIMED_OUT -> listOf(
                            "The server took too long to send data back to your device",
                            "The website may be experiencing heavy traffic or maintenance",
                            "Check if your internet connection is unusually slow or unstable"
                        )
                        WebPageErrorType.CANNOT_CONNECT -> listOf(
                            "The website's server is reachable but refused the connection",
                            "The service might be offline or port is blocked",
                            "If you use a proxy or VPN, try disabling it temporarily"
                        )
                        WebPageErrorType.SECURITY_SSL_ERROR -> listOf(
                            "The website's security certificate cannot be verified",
                            "Check that your device date and time are accurate",
                            "Attackers might be attempting to intercept your connection"
                        )
                        WebPageErrorType.INVALID_ADDRESS -> listOf(
                            "Check that the address starts with 'http://' or 'https://'",
                            "Ensure there are no illegal characters or trailing spaces"
                        )
                        WebPageErrorType.UNEXPECTED_ERROR -> listOf(
                            "Try reloading the page",
                            "Close other memory-intensive tabs in DevBrowser",
                            "Error code ${error.errorCode} (${error.errorName}) was returned by WebView"
                        )
                    }

                    for (tip in tips) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = themeColor
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Primary & Secondary Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("error_retry_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (error.errorType == WebPageErrorType.NO_INTERNET) "Check Connection" else "Try Again")
                    }

                    if (error.errorType == WebPageErrorType.WEBSITE_DOES_NOT_EXIST || error.errorType == WebPageErrorType.INVALID_ADDRESS) {
                        OutlinedButton(
                            onClick = { onSearchWeb(cleanHost) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("error_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search Web")
                        }
                    } else if (canGoBack) {
                        OutlinedButton(
                            onClick = onGoBack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("error_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go Back")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onGoHome,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("error_home_button")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Home")
                        }
                    }
                }

                if (canGoBack && (error.errorType == WebPageErrorType.WEBSITE_DOES_NOT_EXIST || error.errorType == WebPageErrorType.INVALID_ADDRESS)) {
                    OutlinedButton(
                        onClick = onGoHome,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Go to Home")
                    }
                }
            }

            // Expandable Technical Details Drawer
            Surface(
                color = cs.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTechDetails = !showTechDetails },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = cs.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Technical Details",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = cs.onSurface
                            )
                        }

                        Text(
                            text = if (showTechDetails) "Hide" else "Show",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = cs.primary
                        )
                    }

                    AnimatedVisibility(visible = showTechDetails) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(4.dp))

                            TechDetailRow("URL", error.url)
                            TechDetailRow("Error Code", "${error.errorCode} (${error.errorName})")
                            TechDetailRow("Description", error.description.ifBlank { "None provided" })
                            TechDetailRow("Category", error.errorType.name)
                            TechDetailRow(
                                "Timestamp",
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(error.timestamp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechDetailRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = cs.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = cs.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Utility to check internet connectivity and map WebView error code to WebPageError.
 */
fun resolveWebPageError(
    context: Context,
    failingUrl: String,
    errorCode: Int,
    description: String
): WebPageError {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val activeNet = cm?.activeNetwork
    val caps = activeNet?.let { cm.getNetworkCapabilities(it) }
    val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    val errorName = when (errorCode) {
        -1 -> "ERROR_UNKNOWN"
        -2 -> "ERROR_HOST_LOOKUP"
        -3 -> "ERROR_UNSUPPORTED_AUTH_SCHEME"
        -4 -> "ERROR_AUTHENTICATION"
        -5 -> "ERROR_PROXY_AUTHENTICATION"
        -6 -> "ERROR_CONNECT"
        -7 -> "ERROR_IO"
        -8 -> "ERROR_TIMEOUT"
        -9 -> "ERROR_REDIRECT_LOOP"
        -10 -> "ERROR_UNSUPPORTED_SCHEME"
        -11 -> "ERROR_FAILED_SSL_HANDSHAKE"
        -12 -> "ERROR_BAD_URL"
        -13 -> "ERROR_FILE"
        -14 -> "ERROR_FILE_NOT_FOUND"
        -15 -> "ERROR_TOO_MANY_REQUESTS"
        -16 -> "ERROR_UNSAFE_RESOURCE"
        else -> "ERROR_$errorCode"
    }

    val type = when {
        !isConnected -> WebPageErrorType.NO_INTERNET
        errorCode == -2 || description.contains("ERR_NAME_NOT_RESOLVED", ignoreCase = true) -> WebPageErrorType.WEBSITE_DOES_NOT_EXIST
        errorCode == -8 || description.contains("ERR_CONNECTION_TIMED_OUT", ignoreCase = true) || description.contains("timed out", ignoreCase = true) -> WebPageErrorType.CONNECTION_TIMED_OUT
        errorCode == -6 || description.contains("ERR_CONNECTION_REFUSED", ignoreCase = true) -> WebPageErrorType.CANNOT_CONNECT
        errorCode == -11 || description.contains("ERR_CERT", ignoreCase = true) || description.contains("SSL", ignoreCase = true) -> WebPageErrorType.SECURITY_SSL_ERROR
        errorCode == -12 || errorCode == -10 -> WebPageErrorType.INVALID_ADDRESS
        else -> WebPageErrorType.UNEXPECTED_ERROR
    }

    return WebPageError(
        url = failingUrl,
        errorCode = errorCode,
        errorName = errorName,
        description = description,
        errorType = type
    )
}
