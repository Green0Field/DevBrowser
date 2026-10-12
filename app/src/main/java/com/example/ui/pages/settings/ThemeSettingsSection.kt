package com.example.ui.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ThemeAccentColor

@Composable
fun ThemeSettingsSection(
    isDarkTheme: Boolean,
    isNeonMode: Boolean,
    accentColor: ThemeAccentColor,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetNeonMode: (Boolean) -> Unit,
    onSelectAccentColor: (ThemeAccentColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Appearance Mode
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Appearance Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface
                        )
                        Text(
                            text = "Sends prefers-color-scheme: ${if (isDarkTheme) "dark" else "light"} to websites so sites auto-switch theme",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dark Mode Pill Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (isDarkTheme) cs.primaryContainer else cs.surfaceVariant)
                            .border(
                                width = if (isDarkTheme) 2.dp else 1.dp,
                                color = if (isDarkTheme) cs.primary else cs.outlineVariant,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onSetDarkTheme(true) }
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                            .testTag("theme_mode_dark"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (isDarkTheme) cs.primary else cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Dark (Default)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isDarkTheme) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = cs.onSurface
                            )
                        }
                    }

                    // Light Mode Pill Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (!isDarkTheme) cs.primaryContainer else cs.surfaceVariant)
                            .border(
                                width = if (!isDarkTheme) 2.dp else 1.dp,
                                color = if (!isDarkTheme) cs.primary else cs.outlineVariant,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onSetDarkTheme(false) }
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                            .testTag("theme_mode_light"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (!isDarkTheme) cs.primary else cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Light Mode",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (!isDarkTheme) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = cs.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(color = cs.outlineVariant)

                // Neon Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Neon Mode",
                                style = MaterialTheme.typography.titleMedium,
                                color = cs.onSurface
                            )
                            Text(
                                text = if (isNeonMode) "Vibrant neon glow & saturated backgrounds (Default: On)"
                                else "Standard muted tonal palette (No Neon)",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isNeonMode,
                        onCheckedChange = onSetNeonMode,
                        modifier = Modifier.testTag("neon_mode_switch")
                    )
                }
            }
        }

        // 2. Accent Color Picker
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Theme Colors",
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface
                        )
                        Text(
                            text = "Choose an accent color for tabs, active controls, and DevTools",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeAccentColor.entries.chunked(4).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowColors.forEach { colorOption ->
                                val isSelected = accentColor == colorOption
                                val swatchColor = Color(colorOption.activePrimaryHex(isDarkTheme, isNeonMode))
                                val tileBgColor = if (isSelected) {
                                    swatchColor.copy(alpha = if (isNeonMode) 0.34f else 0.26f)
                                } else {
                                    Color(colorOption.activeSurfaceVariantHex(isDarkTheme, isNeonMode))
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(tileBgColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) swatchColor else cs.outlineVariant,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .clickable { onSelectAccentColor(colorOption) }
                                        .padding(vertical = 12.dp, horizontal = 6.dp)
                                        .testTag("theme_color_${colorOption.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (colorOption == ThemeAccentColor.YELLOW) Color.Black else Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = colorOption.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = cs.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
