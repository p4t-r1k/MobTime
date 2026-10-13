package com.example.phonetimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAlarm
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phonetimer.model.AppTimerItem
import com.example.phonetimer.ui.theme.MobTimeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobTimeHomeScreen(
    viewModel: MainViewModel,
    onGrantPermissionClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    MobTimeHomeScreenContent(
        uiState = uiState,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onFilterSelect = { viewModel.setFilter(it) },
        onTabSelect = { viewModel.setSelectedTab(it) },
        onEditGoalClick = { viewModel.setShowEditGoalDialog(true) },
        onGrantPermissionClick = onGrantPermissionClick,
        onSetTimerClick = { viewModel.selectAppForTimer(it) },
        onDismissTimerSheet = { viewModel.selectAppForTimer(null) },
        onDismissGoalDialog = { viewModel.setShowEditGoalDialog(false) },
        onSaveTimer = { app, limitMins -> viewModel.saveAppLimit(context, app, limitMins) },
        onRemoveTimer = { app -> viewModel.removeAppLimit(context, app) },
        onSaveGoal = { goalMins -> viewModel.updateDailyGoal(context, goalMins) },
        onRefreshClick = { viewModel.checkAndRefresh(context) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobTimeHomeScreenContent(
    uiState: MobTimeUiState,
    onSearchQueryChange: (String) -> Unit = {},
    onFilterSelect: (String) -> Unit = {},
    onTabSelect: (Int) -> Unit = {},
    onEditGoalClick: () -> Unit = {},
    onGrantPermissionClick: () -> Unit = {},
    onSetTimerClick: (AppTimerItem) -> Unit = {},
    onDismissTimerSheet: () -> Unit = {},
    onDismissGoalDialog: () -> Unit = {},
    onSaveTimer: (AppTimerItem, Int) -> Unit = { _, _ -> },
    onRemoveTimer: (AppTimerItem) -> Unit = {},
    onSaveGoal: (Int) -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    val filters = listOf("All", "Limits Set", "Over Limit", "Social", "Video", "Productivity", "Games")
    val todayFormatted = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "MobTime",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefreshClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { onTabSelect(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Overview"
                        )
                    },
                    label = { Text("Overview") }
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { onTabSelect(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.appsWithLimits.isNotEmpty()) {
                                    Badge(
                                        containerColor = if (uiState.exceededApps.isNotEmpty())
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(text = "${uiState.appsWithLimits.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HourglassBottom,
                                contentDescription = "Timers"
                            )
                        }
                    },
                    label = { Text("Timers") }
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { onTabSelect(2) },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = "Analytics"
                        )
                    },
                    label = { Text("Analytics") }
                )
            }
        },
        floatingActionButton = {
            if (uiState.apps.isNotEmpty()) {
                ExtendedFloatingActionFab(
                    onClick = {
                        // Pick first app without limit or top used app to open sheet
                        val targetApp = uiState.apps.firstOrNull { !it.isLimitSet } ?: uiState.apps.first()
                        onSetTimerClick(targetApp)
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        when (uiState.selectedTab) {
            0 -> {
                // TAB 0: OVERVIEW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!uiState.hasPermission) {
                        item {
                            PermissionCard(onGrantPermissionClick = onGrantPermissionClick)
                        }
                    }

                    item {
                        HeroUsageSummary(
                            totalUsageMinutes = uiState.totalUsageMinutes,
                            dailyGoalMinutes = uiState.dailyGoalMinutes,
                            apps = uiState.apps,
                            onEditGoalClick = onEditGoalClick
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Search apps or categories...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Clear,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true
                        )
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "App Usage List",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(filters) { filter ->
                                val isSelected = uiState.selectedFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onFilterSelect(filter) },
                                    label = {
                                        Text(
                                            text = filter,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    val filteredApps = uiState.filteredApps
                    if (filteredApps.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No matching apps found",
                                message = "Try clearing search or filter selection."
                            )
                        }
                    } else {
                        items(
                            items = filteredApps,
                            key = { it.packageName }
                        ) { app ->
                            AppUsageItemCard(
                                app = app,
                                maxUsageMinutes = uiState.maxUsageMinutes,
                                onSetTimerClick = onSetTimerClick
                            )
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: ACTIVE TIMERS
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "ACTIVE APP TIMERS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${uiState.appsWithLimits.size} App Limits Configured",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (uiState.exceededApps.isNotEmpty())
                                        "⚠️ ${uiState.exceededApps.size} app(s) have reached daily screen time limits."
                                    else "All app usage is within set daily targets.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    val appsWithLimits = uiState.appsWithLimits
                    if (appsWithLimits.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No App Timers Set",
                                message = "Tap 'Set Limit' on any app to configure daily timers and manage screen time."
                            )
                        }
                    } else {
                        items(
                            items = appsWithLimits,
                            key = { it.packageName }
                        ) { app ->
                            AppUsageItemCard(
                                app = app,
                                maxUsageMinutes = uiState.maxUsageMinutes,
                                onSetTimerClick = onSetTimerClick
                            )
                        }
                    }

                    // Section for other top apps to easily add limits
                    val appsWithoutLimits = uiState.apps.filter { !it.isLimitSet }
                    if (appsWithoutLimits.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Quickly Add Timer for Other Apps",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(
                            items = appsWithoutLimits,
                            key = { it.packageName }
                        ) { app ->
                            AppUsageItemCard(
                                app = app,
                                maxUsageMinutes = uiState.maxUsageMinutes,
                                onSetTimerClick = onSetTimerClick
                            )
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: ANALYTICS
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AnalyticsCategoryBreakdownCard(apps = uiState.apps)
                    }

                    item {
                        DailyDistributionChartCard(apps = uiState.apps)
                    }
                }
            }
        }

        // Modal Bottom Sheet for Timer Limits
        uiState.selectedAppForTimer?.let { appForTimer ->
            SetTimerBottomSheet(
                app = appForTimer,
                onDismissRequest = onDismissTimerSheet,
                onSaveTimer = onSaveTimer,
                onRemoveTimer = onRemoveTimer
            )
        }

        // Edit Goal Dialog
        if (uiState.showEditGoalDialog) {
            EditGoalDialog(
                currentGoalMinutes = uiState.dailyGoalMinutes,
                onDismissRequest = onDismissGoalDialog,
                onSaveGoal = onSaveGoal
            )
        }
    }
}

@Composable
private fun ExtendedFloatingActionFab(
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.AddAlarm,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Set Limit",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalyticsCategoryBreakdownCard(apps: List<AppTimerItem>) {
    val categoryTotals = apps
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.usageMinutes } }
        .entries
        .sortedByDescending { it.value }

    val totalMins = categoryTotals.sumOf { it.value }.coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CATEGORY BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Screen Time Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            categoryTotals.forEach { (category, mins) ->
                val percentage = ((mins.toFloat() / totalMins) * 100).toInt()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${mins}m ($percentage%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { mins.toFloat() / totalMins },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = when (category) {
                            "Social" -> MaterialTheme.colorScheme.primary
                            "Video" -> MaterialTheme.colorScheme.tertiary
                            "Games" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyDistributionChartCard(apps: List<AppTimerItem>) {
    val topApps = apps.take(5)
    val maxMins = topApps.maxOfOrNull { it.usageMinutes }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "TOP 5 USED APPS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Usage Comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                topApps.forEach { app ->
                    val heightRatio = (app.usageMinutes.toFloat() / maxMins).coerceIn(0.1f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "${app.usageMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(heightRatio)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (app.isLimitExceeded) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app.appName.take(6),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MobTimeHomeScreenPreview() {
    MobTimeTheme {
        val sampleApps = listOf(
            AppTimerItem(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                usageTimeMillis = 5400000,
                limitMinutes = 60,
                category = "Video"
            ),
            AppTimerItem(
                packageName = "com.instagram.android",
                appName = "Instagram",
                usageTimeMillis = 3600000,
                limitMinutes = 30,
                category = "Social"
            ),
            AppTimerItem(
                packageName = "com.twitter.android",
                appName = "X (Twitter)",
                usageTimeMillis = 2700000,
                category = "Social"
            ),
            AppTimerItem(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                usageTimeMillis = 1800000,
                category = "Social"
            )
        )

        MobTimeHomeScreenContent(
            uiState = MobTimeUiState(
                hasPermission = true,
                apps = sampleApps,
                dailyGoalMinutes = 240
            )
        )
    }
}
