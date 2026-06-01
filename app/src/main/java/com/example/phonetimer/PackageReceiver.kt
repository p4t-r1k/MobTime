package com.example.phonetimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.phonetimer.db.AppDatabase
import com.example.phonetimer.db.AppUsageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_FULLY_REMOVED) {
            val packageName = intent.data?.schemeSpecificPart ?: return
            
            // In a real app, we might want to capture the last known usage before removal,
            // but ACTION_PACKAGE_FULLY_REMOVED happens AFTER uninstall.
            // For this prototype, we'll assume the MainActivity periodically syncs to DB.
        }
    }
}
