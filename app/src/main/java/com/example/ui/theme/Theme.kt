package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.ThemeAccentColor

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    isNeon: Boolean = true,
    accentColor: ThemeAccentColor = ThemeAccentColor.BLUE,
    content: @Composable () -> Unit,
) {
    val primaryColor = Color(accentColor.activePrimaryHex(darkTheme, isNeon))
    val onPrimaryColor = if (
        accentColor == ThemeAccentColor.YELLOW ||
        (isNeon && darkTheme && (accentColor == ThemeAccentColor.GREEN || accentColor == ThemeAccentColor.CYAN))
    ) {
        DevSlate950
    } else {
        Color.White
    }

    val bg = Color(accentColor.activeBgHex(darkTheme, isNeon))
    val surf = Color(accentColor.activeSurfaceHex(darkTheme, isNeon))
    val surfVar = Color(accentColor.activeSurfaceVariantHex(darkTheme, isNeon))

    val colorScheme = if (darkTheme) {
        val outline = if (isNeon) {
            primaryColor.copy(alpha = 0.72f)
        } else {
            Color(accentColor.darkOutlineHex)
        }
        val outlineVar = if (isNeon) {
            primaryColor.copy(alpha = 0.42f)
        } else {
            Color(accentColor.darkOutlineVariantHex)
        }

        darkColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryColor.copy(alpha = if (isNeon) 0.34f else 0.25f),
            onPrimaryContainer = DevSlate100,
            secondary = primaryColor,
            onSecondary = onPrimaryColor,
            secondaryContainer = surfVar,
            onSecondaryContainer = DevSlate100,
            tertiary = if (isNeon) Color(0xFF00FF87) else DevEmerald,
            onTertiary = DevSlate950,
            background = bg,
            onBackground = DevSlate100,
            surface = surf,
            onSurface = DevSlate100,
            surfaceVariant = surfVar,
            onSurfaceVariant = DevSlate300,
            outline = outline,
            outlineVariant = outlineVar,
            error = DevRose,
            onError = Color.White
        )
    } else {
        val outline = if (isNeon) {
            primaryColor.copy(alpha = 0.65f)
        } else {
            Color(accentColor.lightOutlineHex)
        }
        val outlineVar = if (isNeon) {
            primaryColor.copy(alpha = 0.38f)
        } else {
            Color(accentColor.lightOutlineVariantHex)
        }

        lightColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryColor.copy(alpha = if (isNeon) 0.28f else 0.20f),
            onPrimaryContainer = Color(0xFF0F172A),
            secondary = primaryColor,
            onSecondary = onPrimaryColor,
            secondaryContainer = surfVar,
            onSecondaryContainer = Color(0xFF0F172A),
            tertiary = Color(0xFF059669),
            onTertiary = Color.White,
            background = bg,
            onBackground = Color(0xFF0F172A),
            surface = surf,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = surfVar,
            onSurfaceVariant = Color(0xFF334155),
            outline = outline,
            outlineVariant = outlineVar,
            error = Color(0xFFE11D48),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
