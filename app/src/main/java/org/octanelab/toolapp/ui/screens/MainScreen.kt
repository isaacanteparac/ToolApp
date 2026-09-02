package org.octanelab.toolapp.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.octanelab.toolapp.data.ScreenshotItem
import org.octanelab.toolapp.ui.theme.CyanPrimary
import org.octanelab.toolapp.ui.theme.DarkGlassHeader
import org.octanelab.toolapp.ui.theme.DarkSurface
import org.octanelab.toolapp.ui.theme.GlassBorder
import org.octanelab.toolapp.ui.theme.TextMuted
import org.octanelab.toolapp.ui.theme.TextPrimary

enum class AppTab {
    DASHBOARD,
    GALLERY
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var selectedScreenshot by remember { mutableStateOf<ScreenshotItem?>(null) }

    // Permission Activity Launchers
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Storage permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Storage permission is required to manage screenshots.", Toast.LENGTH_LONG).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
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
                    containerColor = DarkGlassHeader,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.DASHBOARD,
                        onClick = { currentTab = AppTab.DASHBOARD },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.GALLERY,
                        onClick = { currentTab = AppTab.GALLERY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Gallery"
                            )
                        },
                        label = { Text("Gallery") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentTab) {
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
                    AppTab.GALLERY -> GalleryScreen(
                        onSelectScreenshot = { screenshot ->
                            selectedScreenshot = screenshot
                        }
                    )
                }
            }
        }
    }
}
