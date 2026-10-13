package com.example.phonetimer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.phonetimer.ui.theme.MobTimeTheme

@Composable
fun EditGoalDialog(
    currentGoalMinutes: Int,
    onDismissRequest: () -> Unit,
    onSaveGoal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMinutes by remember(currentGoalMinutes) {
        mutableFloatStateOf(currentGoalMinutes.toFloat())
    }

    val goalPresets = listOf(120, 180, 240, 300, 360, 480) // 2h, 3h, 4h, 5h, 6h, 8h

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Daily Screen Time Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Set your ideal overall phone usage limit per day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Current Goal Pill Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatGoalHours(selectedMinutes.toInt()),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Goal Presets
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    goalPresets.take(3).forEach { mins ->
                        val isSelected = selectedMinutes.toInt() == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMinutes = mins.toFloat() },
                            label = { Text(formatGoalHours(mins)) },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    goalPresets.drop(3).forEach { mins ->
                        val isSelected = selectedMinutes.toInt() == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMinutes = mins.toFloat() },
                            label = { Text(formatGoalHours(mins)) },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Duration Slider
                Slider(
                    value = selectedMinutes,
                    onValueChange = { selectedMinutes = it },
                    valueRange = 30f..600f,
                    steps = 18, // 30 min increments
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onSaveGoal(selectedMinutes.toInt()) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatGoalHours(minutes: Int): String {
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
fun EditGoalDialogPreview() {
    MobTimeTheme {
        EditGoalDialog(
            currentGoalMinutes = 240,
            onDismissRequest = {},
            onSaveGoal = {}
        )
    }
}
