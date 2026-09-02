package org.octanelab.toolapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.octanelab.toolapp.ui.theme.IOSBlue
import org.octanelab.toolapp.ui.theme.IOSCardBackground
import org.octanelab.toolapp.ui.theme.IOSGlassHeader
import org.octanelab.toolapp.ui.theme.IOSGreen
import org.octanelab.toolapp.ui.theme.IOSPurple
import org.octanelab.toolapp.ui.theme.IOSTextMuted
import org.octanelab.toolapp.ui.theme.IOSTextPrimary
import org.octanelab.toolapp.ui.theme.IOSTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MovieSynopsisOverlay(
    displayName: String,
    category: String,
    dateAdded: Long,
    initialDescription: String,
    onSaveDescription: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var synopsisText by remember(initialDescription) { mutableStateOf(initialDescription) }
    var isSavedSuccess by remember { mutableStateOf(false) }

    val formattedDate = remember(dateAdded) {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(dateAdded * 1000))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(IOSGlassHeader)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            // iOS Drag Bar Handle
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // iOS Photo Information Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = IOSTextPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // iOS Album Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(IOSPurple.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = IOSPurple,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                color = IOSPurple
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Timestamp Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = IOSTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = IOSTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // iOS Style Synopsis / EXIF Input Field
            OutlinedTextField(
                value = synopsisText,
                onValueChange = {
                    synopsisText = it
                    isSavedSuccess = false
                },
                label = { Text("Sinopsis / Descripción EXIF") },
                placeholder = { Text("Añada notas, resumen estilo película o etiquetas almacenadas en EXIF...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = IOSBlue
                    )
                },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IOSBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedLabelColor = IOSBlue,
                    unfocusedLabelColor = IOSTextMuted,
                    focusedTextColor = IOSTextPrimary,
                    unfocusedTextColor = IOSTextPrimary,
                    focusedContainerColor = IOSCardBackground.copy(alpha = 0.6f),
                    unfocusedContainerColor = IOSCardBackground.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // iOS Style Action Button
            Button(
                onClick = {
                    onSaveDescription(synopsisText)
                    isSavedSuccess = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSavedSuccess) IOSGreen else IOSBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSavedSuccess) "¡Metadatos EXIF Guardados!" else "Incrustar Sinopsis en EXIF",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
