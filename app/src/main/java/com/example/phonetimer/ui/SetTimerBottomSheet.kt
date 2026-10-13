package com.example.phonetimer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phonetimer.model.AppTimerItem
import com.example.phonetimer.ui.theme.MobTimeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTimerBottomSheet(
    app: AppTimerItem,
    onDismissRequest: () -> Unit,
    onSaveTimer: (AppTimerItem, Int) -> Unit, // app, limitMinutes
    onRemoveTimer: (AppTimerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(app) {
        mutableFloatStateOf((app.limitMinutes ?: 30).toFloat())
    }
    var enforceLock by remember { mutableStateOf(true) }

    val currentMinutes = sliderValue.toInt()

    val presets = listOf(15, 30, 45, 60, 90, 120, 180)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header: App Title & Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Set Daily Timer Limit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timer Limit Selected Display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Limit Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatLimitDisplay(currentMinutes),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Preset Chips
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(4).forEach { presetMinutes ->
                    val isSelected = currentMinutes == presetMinutes
                    FilterChip(
                        selected = isSelected,
                        onClick = { sliderValue = presetMinutes.toFloat() },
                        label = {
                            Text(
                                text = formatLimitDisplay(presetMinutes),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = CircleShape
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.drop(4).forEach { presetMinutes ->
                    val isSelected = currentMinutes == presetMinutes
                    FilterChip(
                        selected = isSelected,
                        onClick = { sliderValue = presetMinutes.toFloat() },
                        label = {
                            Text(
                                text = formatLimitDisplay(presetMinutes),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = CircleShape
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fine Adjustment Slider
            Text(
                text = "Custom Duration Slider",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 5f..240f,
                steps = 46, // step by 5 mins
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Enforcement Toggle Switch
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Enforce App Limit",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Show reminder when timer expires",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = enforceLock,
                        onCheckedChange = { enforceLock = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (app.isLimitSet) {
                    OutlinedButton(
                        onClick = { onRemoveTimer(app) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove")
                    }
                }

                Button(
                    onClick = { onSaveTimer(app, currentMinutes) },
                    modifier = Modifier.weight(2f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (app.isLimitSet) "Update Timer" else "Set Timer",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatLimitDisplay(minutes: Int): String {
    val hrs = minutes / 60
    val mins = minutes % 60
    return when {
        hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
        hrs > 0 -> "${hrs}h"
        else -> "${mins}m"
    }
}

@Preview(showBackground = true)
@Composable
fun SetTimerBottomSheetPreview() {
    MobTimeTheme {
        SetTimerBottomSheet(
            app = AppTimerItem(
                packageName = "com.instagram.android",
                appName = "Instagram",
                usageTimeMillis = 3600000,
                limitMinutes = 30
            ),
            onDismissRequest = {},
            onSaveTimer = { _, _ -> },
            onRemoveTimer = {}
        )
    }
}
