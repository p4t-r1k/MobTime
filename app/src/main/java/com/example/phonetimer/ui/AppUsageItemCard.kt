package com.example.phonetimer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAlarm
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.phonetimer.model.AppTimerItem
import com.example.phonetimer.ui.theme.MobTimeTheme

@Composable
fun AppUsageItemCard(
    app: AppTimerItem,
    maxUsageMinutes: Int,
    onSetTimerClick: (AppTimerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExceeded = app.isLimitExceeded
    val isLimitSet = app.isLimitSet

    val cardContainerColor by animateColorAsState(
        targetValue = when {
            isExceeded -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            isLimitSet -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "CardContainerColor"
    )

    val progressValue = when {
        isLimitSet -> app.progressRatio
        maxUsageMinutes > 0 -> (app.usageMinutes.toFloat() / maxUsageMinutes).coerceIn(0f, 1f)
        else -> 0f
    }

    val progressColor = when {
        isExceeded -> MaterialTheme.colorScheme.error
        isLimitSet -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isExceeded) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon or Letter Badge
                AppIconView(app = app)

                Spacer(modifier = Modifier.width(14.dp))

                // App Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Category Badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = app.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Used today: ${app.formattedUsageTime()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Button (Set / Edit Timer)
                FilledTonalButton(
                    onClick = { onSetTimerClick(app) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isLimitSet) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(
                        imageVector = if (isLimitSet) Icons.Rounded.Edit else Icons.Rounded.AddAlarm,
                        contentDescription = "Set Timer",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLimitSet) "Edit" else "Limit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Usage Progress Bar & Limit Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Timer Status Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExceeded) Icons.Rounded.LockClock else Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            isExceeded -> "LIMIT REACHED (${app.formattedLimitTime()})"
                            isLimitSet -> "Limit: ${app.formattedLimitTime()} (${app.remainingMinutes}m left)"
                            else -> "No limit set"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isExceeded || isLimitSet) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Text(
                    text = "${(progressValue * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Indicator Bar
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun AppIconView(app: AppTimerItem) {
    val drawable = app.appIcon
    if (drawable != null) {
        val bitmap = drawable.toBitmap(width = 96, height = 96).asImageBitmap()
        Image(
            bitmap = bitmap,
            contentDescription = app.appName,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
    } else {
        val initial = app.appName.firstOrNull()?.uppercaseChar() ?: 'A'
        val letterColor = generateColorForApp(app.packageName)

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(letterColor.copy(alpha = 0.2f))
                .border(1.5.dp, letterColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = letterColor
            )
        }
    }
}

private fun generateColorForApp(packageName: String): Color {
    val hash = packageName.hashCode()
    val r = (hash and 0xFF0000 shr 16) / 255f
    val g = (hash and 0x00FF00 shr 8) / 255f
    val b = (hash and 0x0000FF) / 255f
    return Color(
        red = 0.3f + r * 0.5f,
        green = 0.3f + g * 0.5f,
        blue = 0.3f + b * 0.5f
    )
}

@Preview(showBackground = true)
@Composable
fun AppUsageItemCardPreview() {
    MobTimeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppUsageItemCard(
                app = AppTimerItem(
                    packageName = "com.instagram.android",
                    appName = "Instagram",
                    usageTimeMillis = 3600000,
                    limitMinutes = 45,
                    category = "Social"
                ),
                maxUsageMinutes = 120,
                onSetTimerClick = {}
            )

            AppUsageItemCard(
                app = AppTimerItem(
                    packageName = "com.youtube.android",
                    appName = "YouTube",
                    usageTimeMillis = 5400000,
                    limitMinutes = 60,
                    category = "Video"
                ),
                maxUsageMinutes = 120,
                onSetTimerClick = {}
            )
        }
    }
}
