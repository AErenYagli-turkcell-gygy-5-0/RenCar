package com.turkcell.rencar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.turkcell.rencar.presentation.navigation.RenCarNavHost
import com.turkcell.rencar.presentation.theme.RenCarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RenCarTheme {
                RenCarNavHost()
            }
        }
    }
}