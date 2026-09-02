package org.octanelab.toolapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.octanelab.toolapp.data.MediaRepository
import org.octanelab.toolapp.data.ScreenshotItem
import org.octanelab.toolapp.ui.theme.IOSBackground
import org.octanelab.toolapp.ui.theme.IOSBlue
import org.octanelab.toolapp.ui.theme.IOSCardBackground
import org.octanelab.toolapp.ui.theme.IOSCardHighlight
import org.octanelab.toolapp.ui.theme.IOSCyan
import org.octanelab.toolapp.ui.theme.IOSPurple
import org.octanelab.toolapp.ui.theme.IOSTextMuted
import org.octanelab.toolapp.ui.theme.IOSTextPrimary
import org.octanelab.toolapp.ui.theme.IOSTextSecondary

enum class TimeBreakdownMode {
    TODO,
    HORA,   // 13:00 - 13:59
    DIA,    // 02 de Septiembre
    MES,    // Septiembre 2026
    ANO     // 2026
}

@Composable
fun GalleryScreen(
    onSelectScreenshot: (ScreenshotItem) -> Unit
) {
    val context = LocalContext.current
    var allScreenshots by remember { mutableStateOf<List<ScreenshotItem>>(emptyList()) }
    
    // STEP 1: Sticky App Filter State
    var selectedApp by remember { mutableStateOf("General") }

    // STEP 2: Time Breakdown Mode State
    var timeMode by remember { mutableStateOf(TimeBreakdownMode.HORA) }
    
    // STEP 3: Selected Time Slot State
    var selectedTimeSlot by remember { mutableStateOf("General") }

    var isLoading by remember { mutableStateOf(true) }

    fun loadMedia() {
        isLoading = true
        allScreenshots = MediaRepository.fetchScreenshots(context)
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadMedia()
    }

    // List of unique Apps available
    val appList = remember(allScreenshots) {
        val list = mutableListOf("Todas")
        val uniqueApps = allScreenshots.map { it.category }.distinct().sorted()
        list.addAll(uniqueApps)
        list
    }

    // Step 1: Filter screenshots by selected App (FIXED)
    val appFilteredScreenshots = remember(allScreenshots, selectedApp) {
        if (selectedApp == "Todas") {
            allScreenshots
        } else {
            allScreenshots.filter { it.category == selectedApp }
        }
    }

    // Extract available time slots for the currently selected App
    val timeSlotList = remember(appFilteredScreenshots, timeMode) {
        val list = mutableListOf("Todas")
        val slots = when (timeMode) {
            TimeBreakdownMode.TODO -> emptyList()
            TimeBreakdownMode.HORA -> appFilteredScreenshots.map { it.hourBucketLabel }.distinct().sorted()
            TimeBreakdownMode.DIA -> appFilteredScreenshots.map { it.dayLabel }.distinct().sorted()
            TimeBreakdownMode.MES -> appFilteredScreenshots.map { it.monthLabel }.distinct().sorted()
            TimeBreakdownMode.ANO -> appFilteredScreenshots.map { it.yearLabel }.distinct().sorted()
        }
        list.addAll(slots)
        list
    }

    // Reset selected time slot when app or time mode changes
    LaunchedEffect(selectedApp, timeMode) {
        selectedTimeSlot = "Todas"
    }

    // Step 2 & 3: Apply Time Slot filtering on top of App filter
    val finalScreenshots = remember(appFilteredScreenshots, timeMode, selectedTimeSlot) {
        if (selectedTimeSlot == "Todas" || timeMode == TimeBreakdownMode.TODO) {
            appFilteredScreenshots
        } else {
            when (timeMode) {
                TimeBreakdownMode.HORA -> appFilteredScreenshots.filter { it.hourBucketLabel == selectedTimeSlot }
                TimeBreakdownMode.DIA -> appFilteredScreenshots.filter { it.dayLabel == selectedTimeSlot }
                TimeBreakdownMode.MES -> appFilteredScreenshots.filter { it.monthLabel == selectedTimeSlot }
                TimeBreakdownMode.ANO -> appFilteredScreenshots.filter { it.yearLabel == selectedTimeSlot }
                TimeBreakdownMode.TODO -> appFilteredScreenshots
            }
        }
    }

    // Group for section headers in photo grid
    val groupedSections = remember(finalScreenshots, timeMode) {
        when (timeMode) {
            TimeBreakdownMode.TODO -> mapOf(selectedApp to finalScreenshots)
            TimeBreakdownMode.HORA -> finalScreenshots.groupBy { it.hourBucketLabel }
            TimeBreakdownMode.DIA -> finalScreenshots.groupBy { it.dayLabel }
            TimeBreakdownMode.MES -> finalScreenshots.groupBy { it.monthLabel }
            TimeBreakdownMode.ANO -> finalScreenshots.groupBy { it.yearLabel }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IOSBackground)
    ) {
        // iOS Large Header
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = "ScreenShot",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp
                    ),
                    color = IOSTextPrimary
                )
                Text(
                    text = "${finalScreenshots.size} capturas • App: $selectedApp",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = IOSCyan
                )
            }

        }

        // ROW 1: Sticky App Selector Carousel (Primary Filter)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(appList) { app ->
                val isAppSelected = app == selectedApp
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isAppSelected) IOSPurple else IOSCardBackground)
                        .clickable { selectedApp = app }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isAppSelected) Color.White else IOSPurple,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = app,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isAppSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isAppSelected) Color.White else IOSTextPrimary
                        )
                    }
                }
            }
        }

        // ROW 2: Time Breakdown Mode Segmenter (Todo | Hora | Día | Mes | Año)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IOSCardBackground)
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimeModeButton(
                title = "Todo",
                icon = Icons.Default.Apps,
                isSelected = timeMode == TimeBreakdownMode.TODO,
                onClick = { timeMode = TimeBreakdownMode.TODO },
                modifier = Modifier.weight(1f)
            )
            TimeModeButton(
                title = "Hora",
                icon = Icons.Default.Schedule,
                isSelected = timeMode == TimeBreakdownMode.HORA,
                onClick = { timeMode = TimeBreakdownMode.HORA },
                modifier = Modifier.weight(1f)
            )
            TimeModeButton(
                title = "Día",
                icon = Icons.Default.AccessTime,
                isSelected = timeMode == TimeBreakdownMode.DIA,
                onClick = { timeMode = TimeBreakdownMode.DIA },
                modifier = Modifier.weight(1f)
            )
            TimeModeButton(
                title = "Mes",
                icon = Icons.Default.CalendarMonth,
                isSelected = timeMode == TimeBreakdownMode.MES,
                onClick = { timeMode = TimeBreakdownMode.MES },
                modifier = Modifier.weight(1f)
            )
            TimeModeButton(
                title = "Año",
                icon = Icons.Default.CalendarToday,
                isSelected = timeMode == TimeBreakdownMode.ANO,
                onClick = { timeMode = TimeBreakdownMode.ANO },
                modifier = Modifier.weight(1f)
            )
        }

        // ROW 3: Secondary Time Slot Pills (e.g. 13:00 - 13:59 for selected App)
        if (timeMode != TimeBreakdownMode.TODO && timeSlotList.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(timeSlotList) { slot ->
                    val isSlotSelected = slot == selectedTimeSlot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSlotSelected) IOSBlue else IOSCardBackground)
                            .clickable { selectedTimeSlot = slot }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (slot != "Todas") {
                                Icon(
                                    imageVector = when (timeMode) {
                                        TimeBreakdownMode.HORA -> Icons.Default.Schedule
                                        TimeBreakdownMode.DIA -> Icons.Default.AccessTime
                                        TimeBreakdownMode.MES -> Icons.Default.CalendarMonth
                                        TimeBreakdownMode.ANO -> Icons.Default.CalendarToday
                                        TimeBreakdownMode.TODO -> Icons.Default.Apps
                                    },
                                    contentDescription = null,
                                    tint = if (isSlotSelected) Color.White else IOSTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = slot,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSlotSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = if (isSlotSelected) Color.White else IOSTextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid View displaying App Photos grouped by Time Slots
        if (finalScreenshots.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = IOSTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No hay capturas de '$selectedApp' en esta franja",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = IOSTextMuted
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                groupedSections.forEach { (sectionTitle, items) ->
                    if (timeMode != TimeBreakdownMode.TODO) {
                        // Section Header for Time Slot
                        item(span = { GridItemSpan(3) }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp, start = 4.dp)
                            ) {
                                Text(
                                    text = sectionTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = IOSCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${items.size})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = IOSTextMuted
                                )
                            }
                        }
                    }

                    items(items, key = { it.id }) { item ->
                        IOSPhotoGridItem(
                            item = item,
                            timeMode = timeMode,
                            onClick = { onSelectScreenshot(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeModeButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) IOSCardHighlight else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                tint = if (isSelected) IOSBlue else IOSTextMuted,
                contentDescription = null,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 12.sp
                ),
                color = if (isSelected) IOSTextPrimary else IOSTextMuted
            )
        }
    }
}

@Composable
private fun IOSPhotoGridItem(
    item: ScreenshotItem,
    timeMode: TimeBreakdownMode,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
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

        // Overlay Badge on Bottom Left showing complementary info
        val badgeText = when (timeMode) {
            TimeBreakdownMode.TODO -> item.hourBucketLabel
            TimeBreakdownMode.HORA -> item.category
            TimeBreakdownMode.DIA -> item.hourBucketLabel
            TimeBreakdownMode.MES -> item.dayLabel
            TimeBreakdownMode.ANO -> item.monthLabel
        }

        Box(
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 5.dp, vertical = 2.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                color = Color.White
            )
        }
    }
}
