package com.example.phonetimer.ui

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonetimer.model.AppTimerItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class MobTimeUiState(
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val apps: List<AppTimerItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "All",
    val selectedAppForTimer: AppTimerItem? = null,
    val dailyGoalMinutes: Int = 240,
    val showEditGoalDialog: Boolean = false,
    val selectedTab: Int = 0 // 0: Overview/Home, 1: Active Timers, 2: Analytics
) {
    val totalUsageMinutes: Int
        get() = apps.sumOf { it.usageMinutes }

    val filteredApps: List<AppTimerItem>
        get() {
            return apps.filter { app ->
                val matchesSearch = searchQuery.isBlank() ||
                        app.appName.contains(searchQuery, ignoreCase = true) ||
                        app.packageName.contains(searchQuery, ignoreCase = true)

                val matchesFilter = when (selectedFilter) {
                    "Limits Set" -> app.isLimitSet
                    "Over Limit" -> app.isLimitExceeded
                    "All" -> true
                    else -> app.category.equals(selectedFilter, ignoreCase = true)
                }

                val matchesTab = when (selectedTab) {
                    1 -> app.isLimitSet // Timers tab shows apps with limits set
                    else -> true
                }

                matchesSearch && matchesFilter && matchesTab
            }.sortedByDescending { it.usageTimeMillis }
        }

    val appsWithLimits: List<AppTimerItem>
        get() = apps.filter { it.isLimitSet }

    val exceededApps: List<AppTimerItem>
        get() = apps.filter { it.isLimitExceeded }

    val maxUsageMinutes: Int
        get() = apps.maxOfOrNull { it.usageMinutes } ?: 1
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MobTimeUiState())
    val uiState: StateFlow<MobTimeUiState> = _uiState.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("mobtime_prefs", Context.MODE_PRIVATE)
    }

    fun checkAndRefresh(context: Context) {
        val hasPerm = checkUsagePermission(context)
        val prefs = getPrefs(context)
        val dailyGoal = prefs.getInt("daily_goal_minutes", 240)

        _uiState.update { it.copy(hasPermission = hasPerm, dailyGoalMinutes = dailyGoal) }

        if (hasPerm) {
            loadRealUsageStats(context)
        } else {
            loadMockUsageStats(context)
        }
    }

    private fun checkUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun loadRealUsageStats(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val prefs = getPrefs(context)
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            val packageManager = context.packageManager
            val appList = stats
                .filter { it.totalTimeInForeground > 0 }
                .map { usageStats ->
                    val pkgName = usageStats.packageName
                    val appIcon = try {
                        packageManager.getApplicationIcon(pkgName)
                    } catch (_: PackageManager.NameNotFoundException) {
                        null
                    }
                    val appName = try {
                        packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(pkgName, 0)
                        ).toString()
                    } catch (_: PackageManager.NameNotFoundException) {
                        pkgName
                    }

                    val limitMinutes = if (prefs.contains("limit_$pkgName")) {
                        prefs.getInt("limit_$pkgName", -1).takeIf { it > 0 }
                    } else null

                    val category = inferCategory(pkgName, appName)

                    AppTimerItem(
                        packageName = pkgName,
                        appName = appName,
                        appIcon = appIcon,
                        usageTimeMillis = usageStats.totalTimeInForeground,
                        limitMinutes = limitMinutes,
                        category = category
                    )
                }
                .sortedByDescending { it.usageTimeMillis }

            _uiState.update { it.copy(apps = appList, isLoading = false) }
        }
    }

    fun loadMockUsageStats(context: Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val prefs = context?.let { getPrefs(it) }

            val mockApps = listOf(
                AppTimerItem(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    usageTimeMillis = 5400000, // 1h 30m
                    limitMinutes = prefs?.let { getLimit(it, "com.google.android.youtube") } ?: 60,
                    category = "Video"
                ),
                AppTimerItem(
                    packageName = "com.instagram.android",
                    appName = "Instagram",
                    usageTimeMillis = 3600000, // 1h 00m
                    limitMinutes = prefs?.let { getLimit(it, "com.instagram.android") } ?: 45,
                    category = "Social"
                ),
                AppTimerItem(
                    packageName = "com.twitter.android",
                    appName = "X (Twitter)",
                    usageTimeMillis = 2700000, // 45m
                    limitMinutes = prefs?.let { getLimit(it, "com.twitter.android") },
                    category = "Social"
                ),
                AppTimerItem(
                    packageName = "com.whatsapp",
                    appName = "WhatsApp",
                    usageTimeMillis = 1800000, // 30m
                    limitMinutes = prefs?.let { getLimit(it, "com.whatsapp") },
                    category = "Social"
                ),
                AppTimerItem(
                    packageName = "com.spotify.music",
                    appName = "Spotify",
                    usageTimeMillis = 1500000, // 25m
                    limitMinutes = prefs?.let { getLimit(it, "com.spotify.music") },
                    category = "Audio"
                ),
                AppTimerItem(
                    packageName = "com.android.chrome",
                    appName = "Chrome",
                    usageTimeMillis = 1200000, // 20m
                    limitMinutes = prefs?.let { getLimit(it, "com.android.chrome") } ?: 30,
                    category = "Productivity"
                ),
                AppTimerItem(
                    packageName = "com.supercell.clashofclans",
                    appName = "Clash of Clans",
                    usageTimeMillis = 900000, // 15m
                    limitMinutes = prefs?.let { getLimit(it, "com.supercell.clashofclans") },
                    category = "Games"
                )
            )

            _uiState.update { it.copy(apps = mockApps, isLoading = false) }
        }
    }

    private fun getLimit(prefs: SharedPreferences, pkg: String): Int? {
        return if (prefs.contains("limit_$pkg")) {
            prefs.getInt("limit_$pkg", -1).takeIf { it > 0 }
        } else null
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectAppForTimer(app: AppTimerItem?) {
        _uiState.update { it.copy(selectedAppForTimer = app) }
    }

    fun setShowEditGoalDialog(show: Boolean) {
        _uiState.update { it.copy(showEditGoalDialog = show) }
    }

    fun saveAppLimit(context: Context, app: AppTimerItem, limitMinutes: Int) {
        val prefs = getPrefs(context)
        prefs.edit().putInt("limit_${app.packageName}", limitMinutes).apply()

        _uiState.update { state ->
            val updatedList = state.apps.map { item ->
                if (item.packageName == app.packageName) {
                    item.copy(limitMinutes = limitMinutes)
                } else item
            }
            state.copy(apps = updatedList, selectedAppForTimer = null)
        }
    }

    fun removeAppLimit(context: Context, app: AppTimerItem) {
        val prefs = getPrefs(context)
        prefs.edit().remove("limit_${app.packageName}").apply()

        _uiState.update { state ->
            val updatedList = state.apps.map { item ->
                if (item.packageName == app.packageName) {
                    item.copy(limitMinutes = null)
                } else item
            }
            state.copy(apps = updatedList, selectedAppForTimer = null)
        }
    }

    fun updateDailyGoal(context: Context, goalMinutes: Int) {
        val prefs = getPrefs(context)
        prefs.edit().putInt("daily_goal_minutes", goalMinutes).apply()
        _uiState.update { it.copy(dailyGoalMinutes = goalMinutes, showEditGoalDialog = false) }
    }

    private fun inferCategory(pkgName: String, appName: String): String {
        val name = appName.lowercase()
        val pkg = pkgName.lowercase()
        return when {
            ("youtube" in name || "youtube" in pkg) || ("netflix" in name || "netflix" in pkg) || ("tiktok" in name) || ("twitch" in name) -> "Video"
            ("instagram" in name || "instagram" in pkg) || ("twitter" in name || "twitter" in pkg) || ("facebook" in name) || ("whatsapp" in name) || ("telegram" in name) || ("snapchat" in name) || ("reddit" in name) -> "Social"
            ("game" in name || "game" in pkg) || ("clash" in name) || ("pubg" in name) || ("subway" in name) || ("candy" in name) -> "Games"
            ("chrome" in name || "chrome" in pkg) || ("gmail" in name) || ("docs" in name) || ("notes" in name) || ("calendar" in name) -> "Productivity"
            ("spotify" in name || "spotify" in pkg) || ("music" in name) || ("podcast" in name) -> "Audio"
            else -> "General"
        }
    }
}
