package org.octanelab.toolapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.octanelab.toolapp.data.MediaRepository
import org.octanelab.toolapp.data.ScreenshotItem
import org.octanelab.toolapp.ui.theme.CyanPrimary
import org.octanelab.toolapp.ui.theme.DarkSurface
import org.octanelab.toolapp.ui.theme.GlassBorder
import org.octanelab.toolapp.ui.theme.TextMuted
import org.octanelab.toolapp.ui.theme.TextPrimary
import org.octanelab.toolapp.ui.theme.VioletAccent

@Composable
fun GalleryScreen(
    onSelectScreenshot: (ScreenshotItem) -> Unit
) {
    val context = LocalContext.current
    var screenshots by remember { mutableStateOf<List<ScreenshotItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(true) }

    fun loadMedia() {
        isLoading = true
        screenshots = MediaRepository.fetchScreenshots(context)
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadMedia()
    }

    val categories = remember(screenshots) {
        val list = mutableListOf("All")
        val uniqueCategories = screenshots.map { it.category }.distinct().sorted()
        list.addAll(uniqueCategories)
        list
    }

    val filteredScreenshots = remember(screenshots, selectedCategory) {
        if (selectedCategory == "All") {
            screenshots
        } else {
            screenshots.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VioletAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = VioletAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Categorized Gallery",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${filteredScreenshots.size} items • ${categories.size - 1} categories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            IconButton(
                onClick = { loadMedia() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = CyanPrimary
                )
            }
        }

        // Category Filter Chips Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (category != "All") {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(text = category)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.background,
                        containerColor = DarkSurface,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid Content
        if (filteredScreenshots.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No screenshots found in this category",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredScreenshots, key = { it.id }) { item ->
                    ScreenshotGridCard(
                        item = item,
                        onClick = { onSelectScreenshot(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotGridCard(
    item: ScreenshotItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Category tag overlay
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanPrimary
                )
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
