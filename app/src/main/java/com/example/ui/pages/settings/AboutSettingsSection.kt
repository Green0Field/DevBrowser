package com.example.ui.pages.settings

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R

@Composable
fun AboutSettingsSection(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val githubUrl = "https://github.com/Green0Field/DevBrowser"
    val repoName = "Green0Field/DevBrowser"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section: Big Logo, Name, and Version
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Big Logo
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .shadow(16.dp, shape = RoundedCornerShape(28.dp), ambientColor = cs.primary, spotColor = cs.primary)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(cs.primaryContainer, cs.surfaceVariant)
                            )
                        )
                        .border(2.dp, cs.primary, RoundedCornerShape(28.dp))
                        .testTag("about_app_logo"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_launcher_icon_1791732376975),
                        contentDescription = "DevBrowser Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(22.dp))
                    )
                }

                // Name under logo
                Text(
                    text = "DevBrowser",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = cs.onSurface,
                    modifier = Modifier.testTag("about_app_name")
                )

                // Version Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(cs.primaryContainer)
                        .border(1.dp, cs.primary, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("about_version_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cs.primary)
                        )
                        Text(
                            text = "Version v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = cs.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "A open browser with cool features and made for everyone!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("about_app_description")
                )
            }
        }

        // GitHub Repository Link Card
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cs.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .clickable {
                    onNavigate(githubUrl)
                }
                .testTag("about_github_link_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(cs.primaryContainer)
                            .border(1.dp, cs.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "GitHub Repository",
                            tint = cs.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "GitHub Repository",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = repoName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = cs.primary
                        )
                        Text(
                            text = "Releases, source code, and changelogs",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open GitHub",
                    tint = cs.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
