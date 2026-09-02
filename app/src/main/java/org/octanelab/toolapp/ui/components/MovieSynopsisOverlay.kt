package org.octanelab.toolapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import org.octanelab.toolapp.ui.theme.CyanPrimary
import org.octanelab.toolapp.ui.theme.DarkGlassHeader
import org.octanelab.toolapp.ui.theme.EmeraldSuccess
import org.octanelab.toolapp.ui.theme.GlassBorder
import org.octanelab.toolapp.ui.theme.TextMuted
import org.octanelab.toolapp.ui.theme.TextPrimary
import org.octanelab.toolapp.ui.theme.VioletAccent
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
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(dateAdded * 1000))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(DarkGlassHeader)
            .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(20.dp)
    ) {
        Column {
            // Drag handle line
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GlassBorder)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Movie Style Title Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Category Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(VioletAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = VioletAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                color = VioletAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Timestamp Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Movie Synopsis / Description Text Area
            OutlinedTextField(
                value = synopsisText,
                onValueChange = {
                    synopsisText = it
                    isSavedSuccess = false
                },
                label = { Text("Movie-Style Synopsis / EXIF Description") },
                placeholder = { Text("Add plot notes, context or tags stored directly in EXIF metadata...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = CyanPrimary
                    )
                },
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = GlassBorder,
                    focusedLabelColor = CyanPrimary,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            Button(
                onClick = {
                    onSaveDescription(synopsisText)
                    isSavedSuccess = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSavedSuccess) EmeraldSuccess else CyanPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSavedSuccess) "Description Saved to EXIF Tag!" else "Embed Synopsis into EXIF Metadata",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }
    }
}
