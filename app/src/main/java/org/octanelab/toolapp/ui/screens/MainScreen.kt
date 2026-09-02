package org.octanelab.toolapp.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.octanelab.toolapp.data.ScreenshotItem
import org.octanelab.toolapp.ui.theme.IOSBackground
import org.octanelab.toolapp.ui.theme.IOSBlue
import org.octanelab.toolapp.ui.theme.IOSGlassBottomBar
import org.octanelab.toolapp.ui.theme.IOSTextMuted
import org.octanelab.toolapp.ui.theme.IOSTextPrimary

enum class AppTab {
    GALLERY,
    DASHBOARD
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.GALLERY) }
    var selectedScreenshot by remember { mutableStateOf<ScreenshotItem?>(null) }

    // Permission Activity Launchers
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permiso de Galería otorgado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Se requiere permiso para gestionar capturas.", Toast.LENGTH_LONG).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permiso de notificaciones otorgado", Toast.LENGTH_SHORT).show()
        }
    }

    if (selectedScreenshot != null) {
        DetailViewerScreen(
            item = selectedScreenshot!!,
            onBack = { selectedScreenshot = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = IOSGlassBottomBar,
                    contentColor = IOSTextPrimary,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.GALLERY,
                        onClick = { currentTab = AppTab.GALLERY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Fototeca"
                            )
                        },
                        label = { Text("Fototeca", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IOSBlue,
                            selectedTextColor = IOSBlue,
                            indicatorColor = IOSBlue.copy(alpha = 0.15f),
                            unselectedIconColor = IOSTextMuted,
                            unselectedTextColor = IOSTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.DASHBOARD,
                        onClick = { currentTab = AppTab.DASHBOARD },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Ajustes"
                            )
                        },
                        label = { Text("Ajustes", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IOSBlue,
                            selectedTextColor = IOSBlue,
                            indicatorColor = IOSBlue.copy(alpha = 0.15f),
                            unselectedIconColor = IOSTextMuted,
                            unselectedTextColor = IOSTextMuted
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(IOSBackground)
            ) {
                when (currentTab) {
                    AppTab.GALLERY -> GalleryScreen(
                        onSelectScreenshot = { screenshot ->
                            selectedScreenshot = screenshot
                        }
                    )
                    AppTab.DASHBOARD -> DashboardScreen(
                        onRequestMediaPermission = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            storagePermissionLauncher.launch(permission)
                        },
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            }
        }
    }
}
