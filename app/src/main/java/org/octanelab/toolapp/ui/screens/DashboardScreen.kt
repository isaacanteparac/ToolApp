package org.octanelab.toolapp.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.octanelab.toolapp.service.ScreenshotObserverService
import org.octanelab.toolapp.service.VolumeOverlayService
import org.octanelab.toolapp.ui.components.PermissionCard
import org.octanelab.toolapp.ui.theme.CyanPrimary
import org.octanelab.toolapp.ui.theme.EmeraldSuccess
import org.octanelab.toolapp.ui.theme.GlassBorder
import org.octanelab.toolapp.ui.theme.RoseError
import org.octanelab.toolapp.ui.theme.TextMuted
import org.octanelab.toolapp.ui.theme.TextPrimary
import org.octanelab.toolapp.utils.PermissionUtils

@Composable
fun DashboardScreen(
    onRequestMediaPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission States
    var hasOverlayPermission by remember { mutableStateOf(PermissionUtils.hasOverlayPermission(context)) }
    var hasUsageStatsPermission by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }
    var hasStoragePermission by remember { mutableStateOf(PermissionUtils.hasStoragePermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(PermissionUtils.hasNotificationPermission(context)) }

    // Service Toggle States
    var isVolumeServiceRunning by remember {
        mutableStateOf(PermissionUtils.isServiceRunning(context, VolumeOverlayService::class.java))
    }
    var isScreenshotServiceRunning by remember {
        mutableStateOf(PermissionUtils.isServiceRunning(context, ScreenshotObserverService::class.java))
    }

    // Refresh state when coming back from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = PermissionUtils.hasOverlayPermission(context)
                hasUsageStatsPermission = PermissionUtils.hasUsageStatsPermission(context)
                hasStoragePermission = PermissionUtils.hasStoragePermission(context)
                hasNotificationPermission = PermissionUtils.hasNotificationPermission(context)
                isVolumeServiceRunning = PermissionUtils.isServiceRunning(context, VolumeOverlayService::class.java)
                isScreenshotServiceRunning = PermissionUtils.isServiceRunning(context, ScreenshotObserverService::class.java)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // App Header Banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Smart Tool Controller",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Hardware Button & Screenshot Suite",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 1: Service Controls
        Text(
            text = "BACKGROUND SERVICES",
            style = MaterialTheme.typography.labelMedium,
            color = CyanPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Volume Floating Edge Service Card
        ServiceControlCard(
            title = "Volume Edge Gesture Controller",
            description = "Floating side strip replacing broken physical volume keys. Swipe UP/DOWN to adjust volume.",
            icon = Icons.Default.VolumeUp,
            isEnabled = isVolumeServiceRunning,
            canEnable = hasOverlayPermission,
            onToggle = { enabled ->
                if (enabled) {
                    val intent = Intent(context, VolumeOverlayService::class.java).apply {
                        action = VolumeOverlayService.ACTION_START
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    isVolumeServiceRunning = true
                } else {
                    val intent = Intent(context, VolumeOverlayService::class.java).apply {
                        action = VolumeOverlayService.ACTION_STOP
                    }
                    context.startService(intent)
                    isVolumeServiceRunning = false
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Auto Screenshot Categorizer Service Card
        ServiceControlCard(
            title = "Auto-Categorize Screenshots",
            description = "Detects active foreground app upon capture and organizes photos into subfolders.",
            icon = Icons.Default.FolderSpecial,
            isEnabled = isScreenshotServiceRunning,
            canEnable = hasUsageStatsPermission && hasStoragePermission,
            onToggle = { enabled ->
                if (enabled) {
                    val intent = Intent(context, ScreenshotObserverService::class.java).apply {
                        action = ScreenshotObserverService.ACTION_START
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    isScreenshotServiceRunning = true
                } else {
                    val intent = Intent(context, ScreenshotObserverService::class.java).apply {
                        action = ScreenshotObserverService.ACTION_STOP
                    }
                    context.startService(intent)
                    isScreenshotServiceRunning = false
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 2: System Permissions Checklist
        Text(
            text = "SYSTEM PERMISSIONS CHECKLIST",
            style = MaterialTheme.typography.labelMedium,
            color = CyanPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionCard(
            title = "Display Over Other Apps (Overlay)",
            description = "Required to display floating volume gesture handle",
            icon = Icons.Default.Layers,
            isGranted = hasOverlayPermission,
            onGrantClick = { PermissionUtils.openOverlayPermissionSettings(context) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = "Usage Access Statistics",
            description = "Required to identify active foreground app when screenshot is taken",
            icon = Icons.Default.QueryStats,
            isGranted = hasUsageStatsPermission,
            onGrantClick = { PermissionUtils.openUsageStatsSettings(context) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = "Media & Storage Read/Write",
            description = "Required to move screenshots & edit EXIF metadata",
            icon = Icons.Default.FolderSpecial,
            isGranted = hasStoragePermission,
            onGrantClick = onRequestMediaPermission
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = "Foreground Notifications",
            description = "Required to keep background services running reliably",
            icon = Icons.Default.Notifications,
            isGranted = hasNotificationPermission,
            onGrantClick = onRequestNotificationPermission
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ServiceControlCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    canEnable: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isEnabled) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled) CyanPrimary else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = { if (canEnable || !it) onToggle(it) },
                enabled = canEnable || isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = CyanPrimary,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        AnimatedVisibility(visible = !canEnable && !isEnabled) {
            Text(
                text = "⚠️ Grant required permissions below first to enable this service.",
                style = MaterialTheme.typography.labelMedium,
                color = RoseError,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}
