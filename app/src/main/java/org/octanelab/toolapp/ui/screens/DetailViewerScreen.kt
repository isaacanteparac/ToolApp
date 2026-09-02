package org.octanelab.toolapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.octanelab.toolapp.data.EXIFHelper
import org.octanelab.toolapp.data.ScreenshotItem
import org.octanelab.toolapp.ui.components.MovieSynopsisOverlay
import org.octanelab.toolapp.ui.theme.IOSBackground
import org.octanelab.toolapp.ui.theme.IOSCardBackground

@Composable
fun DetailViewerScreen(
    item: ScreenshotItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Zoom & Pan gesture states
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    // EXIF metadata description state
    var exifDescription by remember { mutableStateOf("") }

    LaunchedEffect(item.uri) {
        exifDescription = EXIFHelper.readDescription(context, item.uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IOSBackground)
    ) {
        // Fullscreen Image Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                                offset = Offset.Zero
                            }
                        }
                    )
                }
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Left iOS Circular Back Navigation Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 44.dp, start = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(IOSCardBackground.copy(alpha = 0.85f))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        // Bottom Movie Synopsis & EXIF Metadata Sheet
        MovieSynopsisOverlay(
            displayName = item.displayName,
            category = item.category,
            dateAdded = item.dateAdded,
            initialDescription = exifDescription,
            onSaveDescription = { newDesc ->
                val success = EXIFHelper.writeDescription(context, item.uri, newDesc)
                if (success) {
                    exifDescription = newDesc
                    Toast.makeText(context, "¡Sinopsis guardada en metadatos EXIF!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al guardar en EXIF.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
