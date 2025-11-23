package com.example.geojournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.osmdroid.config.Configuration
import com.example.geojournal.BuildConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INISIALISASI OPENSTREETMAP ---
        // Penting agar peta bisa tampil user agent harus diset
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        // ----------------------------------

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JournalAppNavigation()
                }
            }
        }
    }
}