package com.example.phonetimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_FULLY_REMOVED) {
            val packageName = intent.data?.schemeSpecificPart
            if (packageName != null) {
                Log.d("PackageReceiver", "Package fully removed: $packageName")
                // We keep the data in DB so it shows up as "uninstalled" in the list.
                // If we wanted to clean up, we would call db.appUsageDao().delete(packageName)
            }
        }
    }
}
