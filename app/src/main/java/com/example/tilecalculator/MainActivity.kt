package com.example.tilecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tilecalculator.navigation.TileCalculatorNavHost
import com.example.tilecalculator.ui.theme.TileCalculatorTheme
import com.example.tilecalculator.ui.viewmodel.TileCalculatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TileCalculatorViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            TileCalculatorTheme(
                appTheme = settings.theme,
                dynamicColor = settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TileCalculatorNavHost(viewModel = viewModel)
                }
            }
        }
    }
}