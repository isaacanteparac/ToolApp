package org.octanelab.toolapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.octanelab.toolapp.ui.screens.MainScreen
import org.octanelab.toolapp.ui.theme.ToolAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolAppTheme {
                MainScreen()
            }
        }
    }
}