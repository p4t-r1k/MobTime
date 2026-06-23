package com.example.phonetimer

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phonetimer.databinding.ActivityMainBinding
import com.example.phonetimer.db.AppUsageDbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = UsageListAdapter()
    private val dbHelper by lazy { AppUsageDbHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usageRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.usageRecyclerView.adapter = adapter

        binding.grantPermissionButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasUsageStatsPermission()) {
            binding.permissionLayout.visibility = View.GONE
            binding.usageRecyclerView.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                loadUsageStats()
            }
        } else {
            binding.permissionLayout.visibility = View.VISIBLE
            binding.usageRecyclerView.visibility = View.GONE
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private suspend fun loadUsageStats() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // Get standard stats to identify apps used today
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // Single pass over events for efficiency
        val packageEvents = mutableMapOf<String, MutableList<Pair<Int, Long>>>()
        val screenEvents = mutableListOf<Pair<Int, Long>>()
        val allEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = android.app.usage.UsageEvents.Event()
        
        while (allEvents.hasNextEvent()) {
            allEvents.getNextEvent(event)
            val pkg = event.packageName
            if (pkg != null) {
                val list = packageEvents.getOrPut(pkg) { mutableListOf() }
                list.add(event.eventType to event.timeStamp)
            }
            if (event.eventType == android.app.usage.UsageEvents.Event.SCREEN_INTERACTIVE ||
                event.eventType == android.app.usage.UsageEvents.Event.SCREEN_NON_INTERACTIVE) {
                screenEvents.add(event.eventType to event.timeStamp)
            }
        }

        val packageManager = packageManager
        
        // Fetch historical data from DB
        val historicalData = withContext(Dispatchers.IO) {
            dbHelper.getAllUsage()
        }

        val usageInfos = stats
            .filter { it.totalTimeInForeground > 0 }
            .map { usageStats ->
                val packageName = usageStats.packageName
                val appIcon = try {
                    packageManager.getApplicationIcon(packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
                val appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(packageName, 0)
                    ).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    packageName
                }

                // Calculate current session times
                val (currentUsage, currentIdle) = calculateTimes(
                    packageName,
                    packageEvents[packageName] ?: emptyList(), 
                    screenEvents, 
                    startTime, 
                    endTime
                )

                // Add historical data if exists
                val history = historicalData[packageName]
                val totalUsage = currentUsage + (history?.usageTimeMillis ?: 0L)
                val totalIdle = currentIdle + (history?.idleTimeMillis ?: 0L)

                // Save/Update in DB periodically (or here for simplicity in prototype)
                withContext(Dispatchers.IO) {
                    dbHelper.insertOrUpdate(
                        packageName, 
                        appName, 
                        totalUsage, 
                        totalIdle, 
                        System.currentTimeMillis()
                    )
                }

                AppUsageInfo(packageName, appName, appIcon, totalUsage, totalIdle)
            }
            .toMutableList()

        // Add apps that are in DB but NOT in current stats (uninstalled apps)
        historicalData.values.forEach { entity ->
            if (usageInfos.none { it.packageName == entity.packageName }) {
                usageInfos.add(
                    AppUsageInfo(
                        entity.packageName,
                        entity.appName,
                        null, // Icon not available for uninstalled apps
                        entity.usageTimeMillis,
                        entity.idleTimeMillis
                    )
                )
            }
        }

        val sortedList = usageInfos
            .filter { it.usageTimeMillis > 0 || it.idleTimeMillis > 0 }
            .sortedByDescending { it.usageTimeMillis + it.idleTimeMillis }

        adapter.submitList(sortedList)
    }

    private fun calculateTimes(
        packageName: String,
        appEvents: List<Pair<Int, Long>>,
        screenEvents: List<Pair<Int, Long>>,
        startTime: Long,
        endTime: Long
    ): Pair<Long, Long> {
        val combinedEvents = (appEvents.map { it to true } + screenEvents.map { it to false })
            .sortedBy { it.first.second }

        var isAppInForeground = false
        var isScreenOn = true 
        var lastInteractionTime = -1L
        
        // Better screen state initialization: 
        // If the first screen event we see is "INTERACTIVE", it means the screen was likely "NON_INTERACTIVE" before that.
        for (item in combinedEvents) {
            if (!item.second) { // Screen event
                isScreenOn = item.first.first == android.app.usage.UsageEvents.Event.SCREEN_NON_INTERACTIVE
                break
            }
        }

        var usageTime = 0L
        var idleTime = 0L
        var lastTime = startTime

        val isDebugTarget = packageName == "com.brave.browser" // Zmień na paczkę, którą testujesz
        if (isDebugTarget) {
            android.util.Log.d("UsageDebug", "--- Start calculation for $packageName ---")
            android.util.Log.d("UsageDebug", "Initial State: AppFG=$isAppInForeground, ScreenOn=$isScreenOn")
        }

        val IDLE_THRESHOLD_MS = 30 * 1000L // 30 sekund bezczynności

        for (item in combinedEvents) {
            val event = item.first
            val isAppEvent = item.second
            val eventType = event.first
            val eventTime = event.second
            
            val duration = (eventTime - lastTime).coerceAtLeast(0)
            
            if (duration > 0 && isAppInForeground) {
                if (!isScreenOn) {
                    // Ekran wyłączony = zawsze bezczynność (zgodnie z poprzednią logiką i zdrowym rozsądkiem)
                    idleTime += duration
                } else {
                    // Ekran włączony - sprawdzamy czas od ostatniej interakcji
                    if (lastInteractionTime != -1L && (eventTime - lastInteractionTime) > IDLE_THRESHOLD_MS) {
                        // Jeśli minęło 30s od ostatniej interakcji
                        val activeDuration = (lastInteractionTime + IDLE_THRESHOLD_MS - lastTime).coerceAtLeast(0)
                        val idleDuration = (eventTime - (lastInteractionTime + IDLE_THRESHOLD_MS)).coerceAtLeast(0)
                        
                        // Ten fragment jest uproszczony - doliczamy do bezczynności tylko nadwyżkę ponad 30s
                        usageTime += activeDuration
                        idleTime += idleDuration
                    } else {
                        usageTime += duration
                    }
                }
            }

            if (isAppEvent) {
                when (eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                    android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        isAppInForeground = true
                        lastInteractionTime = eventTime // Resetujemy interakcję przy wejściu do aplikacji
                    }
                    
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                    android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED,
                    android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        isAppInForeground = false
                    }
                    
                    7 -> { // USER_INTERACTION (API 28+)
                        lastInteractionTime = eventTime
                    }
                }
                if (isDebugTarget) android.util.Log.d("UsageDebug", "Event: APP TYPE=$eventType at $eventTime. New FG=$isAppInForeground, LastInt=$lastInteractionTime")
            } else {
                when (eventType) {
                    android.app.usage.UsageEvents.Event.SCREEN_INTERACTIVE -> {
                        isScreenOn = true
                        lastInteractionTime = eventTime // Włączenie ekranu to też interakcja
                    }
                    android.app.usage.UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        isScreenOn = false
                    }
                }
                if (isDebugTarget) android.util.Log.d("UsageDebug", "Event: SCREEN TYPE=$eventType at $eventTime. New ScreenOn=$isScreenOn")
            }
            lastTime = eventTime
        }

        val finalDuration = (endTime - lastTime).coerceAtLeast(0)
        if (finalDuration > 0 && isAppInForeground) {
            if (!isScreenOn) {
                idleTime += finalDuration
            } else {
                if (lastInteractionTime != -1L && (endTime - lastInteractionTime) > IDLE_THRESHOLD_MS) {
                    val activeDuration = (lastInteractionTime + IDLE_THRESHOLD_MS - lastTime).coerceAtLeast(0)
                    val idleDuration = (endTime - (lastInteractionTime + IDLE_THRESHOLD_MS)).coerceAtLeast(0)
                    usageTime += activeDuration
                    idleTime += idleDuration
                } else {
                    usageTime += finalDuration
                }
            }
        }

        return usageTime to idleTime
    }





}
