package org.octanelab.toolapp.ui.screens

import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.octanelab.toolapp.service.ScreenshotObserverService
import org.octanelab.toolapp.service.VolumeOverlayService
import org.octanelab.toolapp.ui.components.PermissionCard
import org.octanelab.toolapp.ui.theme.IOSBackground
import org.octanelab.toolapp.ui.theme.IOSBlue
import org.octanelab.toolapp.ui.theme.IOSCardBackground
import org.octanelab.toolapp.ui.theme.IOSCardHighlight
import org.octanelab.toolapp.ui.theme.IOSCyan
import org.octanelab.toolapp.ui.theme.IOSGreen
import org.octanelab.toolapp.ui.theme.IOSOrange
import org.octanelab.toolapp.ui.theme.IOSPurple
import org.octanelab.toolapp.ui.theme.IOSRed
import org.octanelab.toolapp.ui.theme.IOSSeparator
import org.octanelab.toolapp.ui.theme.IOSTextMuted
import org.octanelab.toolapp.ui.theme.IOSTextPrimary
import org.octanelab.toolapp.ui.theme.IOSTextSecondary
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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IOSBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // iOS Style Title Header
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Ajustes y Servicios",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = IOSTextPrimary
        )
        Text(
            text = "Controlador de Volumen Flotante & Gestor de Capturas",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = IOSTextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 1: iOS Inset Grouped Services Card
        Text(
            text = "SERVICIOS ACTIVOS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = IOSTextMuted,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IOSCardBackground)
        ) {
            IOSServiceRow(
                title = "Control de Volumen Flotante",
                description = "Reemplaza botones de volumen dañados con deslizado lateral",
                icon = Icons.Default.VolumeUp,
                iconBgColor = IOSBlue,
                isEnabled = isVolumeServiceRunning,
                canEnable = hasOverlayPermission,
                onToggle = { enabled ->
                    val intent = Intent(context, VolumeOverlayService::class.java).apply {
                        action = if (enabled) VolumeOverlayService.ACTION_START else VolumeOverlayService.ACTION_STOP
                    }
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
                        else context.startService(intent)
                    } else {
                        context.startService(intent)
                    }
                    isVolumeServiceRunning = enabled
                }
            )

            HorizontalDivider(color = IOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 60.dp))

            IOSServiceRow(
                title = "Auto-Categorizar Capturas",
                description = "Organiza automáticamente capturas en carpetas por App",
                icon = Icons.Default.FolderSpecial,
                iconBgColor = IOSPurple,
                isEnabled = isScreenshotServiceRunning,
                canEnable = hasUsageStatsPermission && hasStoragePermission,
                onToggle = { enabled ->
                    val intent = Intent(context, ScreenshotObserverService::class.java).apply {
                        action = if (enabled) ScreenshotObserverService.ACTION_START else ScreenshotObserverService.ACTION_STOP
                    }
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
                        else context.startService(intent)
                    } else {
                        context.startService(intent)
                    }
                    isScreenshotServiceRunning = enabled
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 2: iOS Inset Grouped Permissions Checklist
        Text(
            text = "ESTADO DE PERMISOS REQUERIDOS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = IOSTextMuted,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IOSCardBackground)
        ) {
            PermissionCard(
                title = "Mostrar sobre otras Apps",
                description = "Requerido para la barra flotante de volumen",
                icon = Icons.Default.Layers,
                iconBgColor = IOSCyan,
                isGranted = hasOverlayPermission,
                onGrantClick = { PermissionUtils.openOverlayPermissionSettings(context) }
            )

            HorizontalDivider(color = IOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 60.dp))

            PermissionCard(
                title = "Acceso a Uso de Aplicaciones",
                description = "Detecta la app en pantalla al tomar la captura",
                icon = Icons.Default.QueryStats,
                iconBgColor = IOSOrange,
                isGranted = hasUsageStatsPermission,
                onGrantClick = { PermissionUtils.openUsageStatsSettings(context) }
            )

            HorizontalDivider(color = IOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 60.dp))

            PermissionCard(
                title = "Acceso a Galería & Archivos",
                description = "Para mover capturas e incrustar metadatos EXIF",
                icon = Icons.Default.FolderSpecial,
                iconBgColor = IOSGreen,
                isGranted = hasStoragePermission,
                onGrantClick = onRequestMediaPermission
            )

            HorizontalDivider(color = IOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 60.dp))

            PermissionCard(
                title = "Notificaciones en Primer Plano",
                description = "Mantiene los servicios ejecutándose en segundo plano",
                icon = Icons.Default.Notifications,
                iconBgColor = IOSRed,
                isGranted = hasNotificationPermission,
                onGrantClick = onRequestNotificationPermission
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun IOSServiceRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    isEnabled: Boolean,
    canEnable: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        color = IOSTextPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = IOSTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = { if (canEnable || !it) onToggle(it) },
                enabled = canEnable || isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = IOSGreen,
                    uncheckedThumbColor = IOSTextMuted,
                    uncheckedTrackColor = IOSCardHighlight
                )
            )
        }

        AnimatedVisibility(visible = !canEnable && !isEnabled) {
            Text(
                text = "Otorgue los permisos requeridos abajo para activar este servicio.",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = IOSOrange,
                modifier = Modifier.padding(top = 8.dp, start = 50.dp)
            )
        }
    }
}
