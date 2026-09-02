package org.octanelab.toolapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.octanelab.toolapp.ui.theme.IOSBlue
import org.octanelab.toolapp.ui.theme.IOSCardBackground
import org.octanelab.toolapp.ui.theme.IOSGreen
import org.octanelab.toolapp.ui.theme.IOSTextMuted
import org.octanelab.toolapp.ui.theme.IOSTextPrimary
import org.octanelab.toolapp.ui.theme.IOSTextSecondary

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(IOSCardBackground)
            .clickable { if (!isGranted) onGrantClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // iOS Style Square Icon Badge
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

        // iOS Action / Status Indicator
        if (isGranted) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IOSGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Activo",
                    tint = IOSGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Configurar",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                    color = IOSBlue
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = IOSTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
