package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.BrowserDevToolsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialUrl = extractUrlFromIntent(intent)
        setContent {
            BrowserDevToolsScreen(initialUrl = initialUrl)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractUrlFromIntent(intent: Intent?): String? {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            return intent.data.toString()
        }
        return null
    }
}
