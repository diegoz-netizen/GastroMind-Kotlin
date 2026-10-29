package com.gastromind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gastromind.app.ui.navigation.GastroMindNavHost
import com.gastromind.app.ui.theme.GastroMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as GastroMindApp).container
        setContent {
            GastroMindTheme {
                GastroMindNavHost(sesionStore = container.sesionStore)
            }
        }
    }
}
