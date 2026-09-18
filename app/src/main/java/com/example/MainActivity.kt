package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.LocalPulseRepository
import com.example.ui.LocalPulseApp
import com.example.ui.LocalPulseViewModel
import com.example.ui.LocalPulseViewModelFactory
import com.example.ui.theme.LocalPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LocalPulseRepository(
            scanReportDao = database.zipScanReportDao(),
            leadDao = database.businessLeadDao(),
            campaignDao = database.outreachCampaignDao(),
            settingsDao = database.calculatorSettingsDao()
        )

        val factory = LocalPulseViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[LocalPulseViewModel::class.java]

        setContent {
            LocalPulseTheme {
                LocalPulseApp(viewModel = viewModel)
            }
        }
    }
}
