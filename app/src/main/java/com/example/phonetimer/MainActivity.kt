package com.example.phonetimer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.phonetimer.ui.MainViewModel
import com.example.phonetimer.ui.MobTimeHomeScreen
import com.example.phonetimer.ui.theme.MobTimeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MobTimeTheme {
                MobTimeHomeScreen(
                    viewModel = viewModel,
                    onGrantPermissionClick = {
                        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAndRefresh(this)
    }
}

