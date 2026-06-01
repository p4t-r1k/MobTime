# Phone Usage Timer Prototype

A prototype Android application that tracks and displays the time spent on various apps throughout the current day.

## Features
- Displays app name and package name.
- Shows total foreground time for each app today.
- Includes app icons.
- Handles `PACKAGE_USAGE_STATS` permission request.

## Implementation Details
- Uses `UsageStatsManager` to query app usage.
- Built with Kotlin and View Binding.
- Uses `RecyclerView` for efficient list display.
