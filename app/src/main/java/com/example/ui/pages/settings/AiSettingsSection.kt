package com.example.ui.pages.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import com.example.model.GeminiModelChoice
import com.example.data.GeminiApiClient
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import kotlinx.coroutines.launch

@Composable
fun AiSettingsSection(
    isAiMasterEnabled: Boolean,
    geminiApiKey: String,
    effectiveApiKey: String,
    isAiProtectionEnabled: Boolean,
    isAiSummaryEnabled: Boolean,
    isAiTranslateEnabled: Boolean,
    isVirusTotalEnabled: Boolean,
    cachedScansCount: Int,
    onSetAiMasterEnabled: (Boolean) -> Unit,
    onSaveGeminiApiKey: (String) -> Unit,
    onSetAiProtectionEnabled: (Boolean) -> Unit,
    onSetAiSummaryEnabled: (Boolean) -> Unit,
    onSetAiTranslateEnabled: (Boolean) -> Unit,
    onClearAiScanCache: () -> Unit,
    selectedGeminiModel: GeminiModelChoice = GeminiModelChoice.AUTO,
    onSelectGeminiModel: (GeminiModelChoice) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var keyInput by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isTestingKey by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testSuccess by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_ai_settings"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Master Toggle Card at the Top
        Surface(
            color = if (isAiMasterEnabled) cs.primaryContainer.copy(alpha = 0.25f) else cs.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isAiMasterEnabled) cs.primary else cs.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isAiMasterEnabled) cs.primary.copy(alpha = 0.2f) else cs.outline.copy(alpha = 0.15f))
                        .border(1.dp, if (isAiMasterEnabled) cs.primary else cs.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isAiMasterEnabled) cs.primary else cs.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Features Master Switch",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAiMasterEnabled) {
                            "Enabled • AI features (Protection, Ai Chat, Translate) are active."
                        } else {
                            "Disabled • All AI features are inactive and grayed out."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isAiMasterEnabled,
                    onCheckedChange = { onSetAiMasterEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = cs.onPrimary,
                        checkedTrackColor = cs.primary
                    ),
                    modifier = Modifier.testTag("ai_master_switch")
                )
            }
        }

        // 2. Gemini API Key Card (under master toggle)
        val apiKeyCardAlpha = if (isAiMasterEnabled) 1f else 0.45f
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(apiKeyCardAlpha)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = cs.primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Gemini API Key",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                    }

                    val hasKey = effectiveApiKey.isNotBlank()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (hasKey) DevEmerald.copy(alpha = 0.15f) else DevAmber.copy(alpha = 0.15f))
                            .border(1.dp, if (hasKey) DevEmerald else DevAmber, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasKey) "Key Configured" else "Key Required",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (hasKey) DevEmerald else DevAmber
                        )
                    }
                }

                Text(
                    text = "Enter your Google Gemini API key to power AI Protection, Ai Chat, and Translation. You have full control over your key and privacy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    enabled = isAiMasterEnabled,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (keyInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { keyInput = "" },
                                    enabled = isAiMasterEnabled
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(
                                onClick = { isKeyVisible = !isKeyVisible },
                                enabled = isAiMasterEnabled
                            ) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle key visibility",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input")
                )

                // Actions: Paste, Save, Test
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val text = clipboardManager.getText()?.text ?: ""
                            if (text.isNotBlank()) {
                                keyInput = text.trim()
                                Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = isAiMasterEnabled,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste")
                    }

                    Button(
                        onClick = {
                            onSaveGeminiApiKey(keyInput)
                            Toast.makeText(context, "Gemini API key saved", Toast.LENGTH_SHORT).show()
                        },
                        enabled = isAiMasterEnabled,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gemini_api_key_save_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Key")
                    }

                    OutlinedButton(
                        onClick = {
                            val keyToTest = keyInput.trim().ifBlank { effectiveApiKey }
                            if (keyToTest.isBlank()) {
                                testResult = "Please enter an API key first"
                                testSuccess = false
                                return@OutlinedButton
                            }
                            isTestingKey = true
                            testResult = null
                            testSuccess = null
                            scope.launch {
                                val result = GeminiApiClient.testApiKey(keyToTest, selectedGeminiModel)
                                isTestingKey = false
                                result.fold(
                                    onSuccess = { workingModel ->
                                        testSuccess = true
                                        testResult = "Connection successful! Working model: $workingModel"
                                    },
                                    onFailure = { err ->
                                        testSuccess = false
                                        testResult = "Connection failed: ${err.message ?: "Unknown error"}"
                                    }
                                )
                            }
                        },
                        enabled = isAiMasterEnabled && !isTestingKey,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingKey) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Test")
                        }
                    }
                }

                // Test connection result banner
                if (testResult != null) {
                    val isSuccess = testSuccess == true
                    Surface(
                        color = if (isSuccess) DevEmerald.copy(alpha = 0.15f) else DevRose.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSuccess) DevEmerald else DevRose),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResult!!,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isSuccess) DevEmerald else DevRose,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // 3. AI Model Selection Card
        val modelCardAlpha = if (isAiMasterEnabled) 1f else 0.45f
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(modelCardAlpha)
                .testTag("card_ai_model_selection")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = cs.primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "AI Model",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(cs.primary.copy(alpha = 0.12f))
                            .border(1.dp, cs.primary.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedGeminiModel.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = cs.primary
                        )
                    }
                }

                Text(
                    text = "Choose which Gemini model to use for AI Chat, Protection, and Translation, or select Auto to cascade from the fastest model down the list if any fail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, cs.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    GeminiModelChoice.entries.forEachIndexed { index, modelChoice ->
                        val isSelected = selectedGeminiModel == modelChoice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = isAiMasterEnabled) {
                                    onSelectGeminiModel(modelChoice)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("radio_model_${modelChoice.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (isAiMasterEnabled) onSelectGeminiModel(modelChoice) },
                                enabled = isAiMasterEnabled,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = cs.primary,
                                    unselectedColor = cs.onSurfaceVariant
                                ),
                                modifier = Modifier.size(20.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = modelChoice.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) cs.primary else cs.onSurface
                                    )
                                    if (modelChoice == GeminiModelChoice.AUTO) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DevEmerald.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Cascading Fallback",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = DevEmerald
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = modelChoice.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }

                        if (index < GeminiModelChoice.entries.size - 1) {
                            HorizontalDivider(
                                color = cs.outlineVariant.copy(alpha = 0.35f),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: AI Features Controls
        Text(
            text = "AI FEATURES",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = cs.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )

        // 3. AI Protection Card
        // Rule: "Grayed out when Virustotal is enabled. When off still save the Ai features toggles but dont use them unless the master toggle is on."
        val isAiProtectionGrayedOut = !isAiMasterEnabled || isVirusTotalEnabled
        val aiProtectionAlpha = if (isAiProtectionGrayedOut) 0.45f else 1f

        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(aiProtectionAlpha)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DevEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = DevEmerald, modifier = Modifier.size(20.dp))
                        }

                        Column {
                            Text(
                                text = "Ai Protection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = cs.onSurface
                            )
                            Text(
                                text = when {
                                    isVirusTotalEnabled -> "Grayed out (VirusTotal is enabled)"
                                    !isAiMasterEnabled -> "Inactive (Master switch off)"
                                    isAiProtectionEnabled -> "Active • Scanning URLs with AI"
                                    else -> "Disabled"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isVirusTotalEnabled) DevAmber else cs.onSurfaceVariant
                            )
                        }
                    }

                    // Toggle still saves even when master is off, but cannot be changed if VT is enabled
                    Switch(
                        checked = isAiProtectionEnabled,
                        onCheckedChange = { onSetAiProtectionEnabled(it) },
                        enabled = !isVirusTotalEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = cs.onPrimary,
                            checkedTrackColor = cs.primary
                        ),
                        modifier = Modifier.testTag("ai_protection_switch")
                    )
                }

                Text(
                    text = "When you visit a website, the URL is analyzed by Gemini AI and classified into Safe, Careful, or Danger with concise reasoning. Uses the same warning pages as VirusTotal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                // Alert if VirusTotal is enabled
                if (isVirusTotalEnabled) {
                    Surface(
                        color = DevAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DevAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = DevAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = "AI Protection is grayed out because VirusTotal is enabled. Disable VirusTotal in settings to activate AI Protection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurface
                            )
                        }
                    }
                }

                if (cachedScansCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cached AI site analyses: $cachedScansCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                onClearAiScanCache()
                                Toast.makeText(context, "AI scan cache cleared", Toast.LENGTH_SHORT).show()
                            },
                            enabled = !isAiProtectionGrayedOut,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear Cache", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // 4. Summary Sub-card
        val summaryAlpha = if (isAiMasterEnabled) 1f else 0.45f
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(summaryAlpha)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cs.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Summarize, contentDescription = null, tint = cs.primary, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ai Chat",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Adds an 'Ai Chat' button to the 3-dot menu to open an AI assistant popup pre-loaded with current webpage context and auto-generated suggestions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isAiSummaryEnabled,
                    onCheckedChange = { onSetAiSummaryEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = cs.onPrimary,
                        checkedTrackColor = cs.primary
                    ),
                    modifier = Modifier.testTag("ai_summary_switch")
                )
            }
        }

        // 5. Translate Sub-card
        val translateAlpha = if (isAiMasterEnabled) 1f else 0.45f
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(translateAlpha)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cs.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = cs.secondary, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Translate",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Adds a 'Translate' button to the 3-dot menu. Auto-detects original page language before translating into your choice of language.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isAiTranslateEnabled,
                    onCheckedChange = { onSetAiTranslateEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = cs.onPrimary,
                        checkedTrackColor = cs.primary
                    ),
                    modifier = Modifier.testTag("ai_translate_switch")
                )
            }
        }
    }
}
