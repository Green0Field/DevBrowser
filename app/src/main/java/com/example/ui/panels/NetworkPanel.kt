package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkRequestEntry
import com.example.model.NetworkResourceType
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevBlue
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevRose

@Composable
fun NetworkPanel(
    requests: List<NetworkRequestEntry>,
    selectedFilter: NetworkResourceType,
    selectedRequest: NetworkRequestEntry?,
    preserveLog: Boolean,
    isOfflineThrottled: Boolean,
    onSelectFilter: (NetworkResourceType) -> Unit,
    onSelectRequest: (NetworkRequestEntry?) -> Unit,
    onTogglePreserveLog: () -> Unit,
    onToggleOffline: () -> Unit,
    onClearRequests: () -> Unit,
    onTriggerSyntheticFetch: (String, String, Int) -> Unit,
    onReplayRequest: (NetworkRequestEntry) -> Unit,
    onBuildCurl: (NetworkRequestEntry) -> String,
    modifier: Modifier = Modifier
) {
    var detailSubTab by remember { mutableIntStateOf(0) } // 0: Headers, 1: Response, 2: Payload, 3: Timing
    var copiedCurlFeedback by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val cs = MaterialTheme.colorScheme

    val filteredRequests = remember(requests, selectedFilter) {
        if (selectedFilter == NetworkResourceType.ALL) requests
        else requests.filter { it.resourceType == selectedFilter }
    }

    val maxEndTimeMs = remember(filteredRequests) {
        filteredRequests.maxOfOrNull { (it.startTimeOffsetMs + it.durationMs).coerceAtLeast(100L) } ?: 500L
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Network Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onClearRequests,
                modifier = Modifier
                    .size(26.dp)
                    .testTag("clear_network_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear network log",
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NetworkResourceType.entries.forEach { resType ->
                    val active = selectedFilter == resType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) cs.primaryContainer else cs.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (active) cs.primary else cs.outlineVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectFilter(resType) }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = resType.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (active) cs.primary else cs.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (preserveLog) DevAmber.copy(alpha = 0.2f) else cs.surfaceVariant)
                        .border(1.dp, if (preserveLog) DevAmber else cs.outlineVariant, RoundedCornerShape(6.dp))
                        .clickable { onTogglePreserveLog() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Preserve",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (preserveLog) DevAmber else cs.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isOfflineThrottled) DevRose.copy(alpha = 0.22f) else cs.surfaceVariant)
                        .border(1.dp, if (isOfflineThrottled) DevRose else cs.outlineVariant, RoundedCornerShape(6.dp))
                        .clickable { onToggleOffline() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isOfflineThrottled) DevRose else cs.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isOfflineThrottled) "Offline" else "Online",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOfflineThrottled) DevRose else cs.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DevEmerald.copy(alpha = 0.2f))
                    .border(1.dp, DevEmerald, RoundedCornerShape(6.dp))
                    .clickable {
                        onTriggerSyntheticFetch("GET", "/api/v1/ping?t=${System.currentTimeMillis() % 1000}", 200)
                    }
                    .padding(horizontal = 7.dp, vertical = 3.dp)
                    .testTag("network_test_fetch_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = DevEmerald,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Fetch",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DevEmerald
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

        // Upper Half: Waterfall Request Table
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .background(cs.background)
                .testTag("network_requests_list")
        ) {
            items(filteredRequests, key = { it.id }) { req ->
                val isSelected = selectedRequest?.id == req.id
                NetworkWaterfallRow(
                    req = req,
                    isSelected = isSelected,
                    maxEndTimeMs = maxEndTimeMs,
                    onClick = {
                        copiedCurlFeedback = false
                        onSelectRequest(req)
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = cs.outlineVariant)
            }
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outline)

        // Lower Half: Request Inspector Detail Pane with Copy as cURL & Replay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .background(cs.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf("Headers", "Response", "Payload", "Timing")
                tabs.forEachIndexed { idx, label ->
                    val active = detailSubTab == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) cs.primaryContainer else Color.Transparent)
                            .border(1.dp, if (active) cs.primary else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { detailSubTab = idx }
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (active) cs.primary else cs.onSurfaceVariant
                        )
                    }
                }

                if (selectedRequest != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    // Copy as cURL button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(cs.surface)
                            .border(1.dp, cs.outline, RoundedCornerShape(6.dp))
                            .clickable {
                                val curl = onBuildCurl(selectedRequest)
                                clipboardManager.setText(AnnotatedString(curl))
                                copiedCurlFeedback = true
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("copy_as_curl_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy as cURL",
                                tint = if (copiedCurlFeedback) DevEmerald else cs.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (copiedCurlFeedback) "Copied cURL!" else "Copy cURL",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (copiedCurlFeedback) DevEmerald else cs.primary
                            )
                        }
                    }

                    // Replay XHR button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(cs.surface)
                            .border(1.dp, cs.outline, RoundedCornerShape(6.dp))
                            .clickable { onReplayRequest(selectedRequest) }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("replay_network_request_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Replay Request",
                                tint = DevAmber,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Replay",
                                style = MaterialTheme.typography.labelSmall,
                                color = DevAmber
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

            if (selectedRequest == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select any request above to inspect headers, payload, or copy as cURL",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (detailSubTab) {
                        0 -> {
                            item {
                                Text(
                                    text = "General",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = cs.primary
                                )
                            }
                            item {
                                KeyValueRow("Request URL", selectedRequest.url)
                                KeyValueRow("Request Method", selectedRequest.method)
                                KeyValueRow("Status Code", "${selectedRequest.status} ${selectedRequest.statusText}")
                                KeyValueRow("MIME Type", selectedRequest.mimeType)
                            }
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Response Headers (${selectedRequest.responseHeaders.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DevEmerald
                                )
                            }
                            items(selectedRequest.responseHeaders.entries.toList(), key = { "resp_${it.key}" }) { (k, v) ->
                                KeyValueRow(k, v)
                            }
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Request Headers (${selectedRequest.requestHeaders.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DevAmber
                                )
                            }
                            items(selectedRequest.requestHeaders.entries.toList(), key = { "req_${it.key}" }) { (k, v) ->
                                KeyValueRow(k, v)
                            }
                        }

                        1 -> {
                            item {
                                Text(
                                    text = "Response Body (${selectedRequest.sizeBytes} B)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = cs.primary
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cs.background)
                                        .border(1.dp, cs.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = selectedRequest.responsePreview.ifBlank { "// Empty response body" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DevEmerald
                                    )
                                }
                            }
                        }

                        2 -> {
                            item {
                                Text(
                                    text = "Request Payload & cURL Preview",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DevAmber
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cs.background)
                                        .border(1.dp, cs.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = selectedRequest.requestBody.ifBlank { onBuildCurl(selectedRequest) },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = cs.onSurface
                                    )
                                }
                            }
                        }

                        3 -> {
                            item {
                                val stalledMs = (selectedRequest.durationMs * 0.15f).toLong().coerceAtLeast(2L)
                                val ttfbMs = (selectedRequest.durationMs * 0.55f).toLong().coerceAtLeast(5L)
                                val downloadMs = (selectedRequest.durationMs - stalledMs - ttfbMs).coerceAtLeast(2L)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Resource Timing Breakdown",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = cs.primary
                                    )
                                    KeyValueRow("Started at", "+${selectedRequest.startTimeOffsetMs} ms")
                                    KeyValueRow("Queueing & DNS", "$stalledMs ms")
                                    KeyValueRow("Waiting (TTFB)", "$ttfbMs ms")
                                    KeyValueRow("Content Download", "$downloadMs ms")
                                    KeyValueRow("Total Duration", "${selectedRequest.durationMs} ms")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkWaterfallRow(
    req: NetworkRequestEntry,
    isSelected: Boolean,
    maxEndTimeMs: Long,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val statusColor = when {
        req.isError || req.status >= 400 || req.status == 0 -> DevRose
        req.status in 300..399 -> DevAmber
        else -> DevEmerald
    }
    val barColor = when (req.resourceType) {
        NetworkResourceType.FETCH_XHR -> cs.primary
        NetworkResourceType.DOC -> DevBlue
        NetworkResourceType.JS -> DevAmber
        NetworkResourceType.CSS -> DevPurple
        NetworkResourceType.IMG -> DevEmerald
        else -> cs.onSurfaceVariant
    }

    val shortPath = remember(req.url) {
        req.url.removePrefix("https://").removePrefix("http://")
            .substringAfter("/", req.url)
            .ifEmpty { "/" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) cs.primaryContainer else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(statusColor.copy(alpha = 0.2f))
                .padding(horizontal = 5.dp, vertical = 1.5.dp)
        ) {
            Text(
                text = if (req.status == 0) "ERR" else "${req.status}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = statusColor
            )
        }

        Text(
            text = req.method,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = cs.primary,
            modifier = Modifier.width(34.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shortPath,
                style = MaterialTheme.typography.labelMedium,
                color = if (req.isError) DevRose else cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${req.resourceType.label} • ${formatBytes(req.sizeBytes)} • ${req.durationMs}ms",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = cs.onSurfaceVariant,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(cs.surface)
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val startFrac = (req.startTimeOffsetMs.toFloat() / maxEndTimeMs.toFloat()).coerceIn(0f, 0.78f)
            val widthFrac = (req.durationMs.toFloat() / maxEndTimeMs.toFloat()).coerceIn(0.14f, 1f - startFrac)
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                if (startFrac > 0.01f) {
                    Spacer(modifier = Modifier.fillMaxWidth(startFrac))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(widthFrac)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
private fun KeyValueRow(key: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$key:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = cs.onSurfaceVariant,
            modifier = Modifier.width(115.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatBytes(bytes: Int): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    return String.format(java.util.Locale.US, "%.1f KB", kb)
}
