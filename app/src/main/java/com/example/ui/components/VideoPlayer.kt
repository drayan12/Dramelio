package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieOrSeries
import com.example.data.VideoSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayer(
    movie: MovieOrSeries,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var bufferedProgress by remember { mutableStateOf(0.4f) }
    var currentProgress by remember { mutableStateOf(0.12f) }
    var totalDurationSeconds by remember { mutableStateOf(5400) } // 1.5 Hour default
    val currentDurationSeconds = (totalDurationSeconds * currentProgress).toInt()

    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var scaleMode by remember { mutableStateOf("Fit") } // Fit, Stretch, Zoom
    val videoSources = movie.videoSources.ifEmpty {
        listOf(
            VideoSource("Auto", ""),
            VideoSource("1080p", ""),
            VideoSource("720p", ""),
            VideoSource("480p", ""),
            VideoSource("360p", "")
        )
    }
    var selectedQuality by remember { mutableStateOf(videoSources.first().label) }
    var showQualitySheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Auto-hide controls timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    // Auto-play / scrubbing simulation logic
    LaunchedEffect(isPlaying, isBuffering) {
        while (isPlaying && !isBuffering) {
            delay(1000)
            if (currentProgress < 1.0f) {
                currentProgress += 0.001f
                if (bufferedProgress < 0.95f && currentProgress > bufferedProgress - 0.1f) {
                    bufferedProgress += 0.02f
                }
            } else {
                isPlaying = false
                currentProgress = 0.0f
            }
        }
    }

    fun formatTime(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color.Black)
            .testTag("video_player_box")
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls },
                    onDoubleTap = { offset ->
                        val sideWidth = size.width / 2
                        if (offset.x < sideWidth) {
                            // Skip backward 10s
                            val newProgress = (currentProgress - 10f / totalDurationSeconds).coerceAtLeast(0f)
                            currentProgress = newProgress
                            Toast.makeText(context, "Mundur 10 detik", Toast.LENGTH_SHORT).show()
                        } else {
                            // Skip forward 10s
                            val newProgress = (currentProgress + 10f / totalDurationSeconds).coerceAtMost(1f)
                            currentProgress = newProgress
                            Toast.makeText(context, "Maju 10 detik", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Video backdrop / simulation background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F0F))
        ) {
            // Visual representational styling for scaling
            val scaleModifier = when (scaleMode) {
                "Stretch" -> Modifier.fillMaxSize()
                "Zoom" -> Modifier.fillMaxSize().padding(horizontal = 0.dp, vertical = 0.dp) // Simulated filling entire frame
                else -> Modifier.aspectRatio(16f / 9f).align(Alignment.Center) // "Fit"
            }
            
            Box(
                modifier = scaleModifier
                    .background(Color(0xFF1C1C1C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MovieFilter,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Dramelio Player - Simulasi Pemutaran ${movie.title}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }
        }

        // Buffering Circle Spinner
        AnimatedVisibility(
            visible = isBuffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Menyangga jaringan ($selectedQuality)...",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Menu Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    Row {
                        // Scaling mode click feedback
                        IconButton(onClick = {
                            scaleMode = when (scaleMode) {
                                "Fit" -> "Stretch"
                                "Stretch" -> "Zoom"
                                else -> "Fit"
                            }
                            Toast.makeText(context, "Skala: $scaleMode", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Aspek Rasio",
                                tint = Color.White
                            )
                        }

                        // Quality selection toggle
                        Button(
                            onClick = { showQualitySheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = selectedQuality,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Middle controls (Skip backward, Play/Pause, Skip forward)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.6f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newProgress = (currentProgress - 0.05f).coerceAtLeast(0f)
                            currentProgress = newProgress
                            Toast.makeText(context, "-10 Detik", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Mundur 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newProgress = (currentProgress + 0.05f).coerceAtMost(1f)
                            currentProgress = newProgress
                            Toast.makeText(context, "+10 Detik", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Maju 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom control elements (Timeline slider, timers)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentDurationSeconds),
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = formatTime(totalDurationSeconds),
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    // Simulated seekbar showing real elapsed vs buffered progress
                    Slider(
                        value = currentProgress,
                        onValueChange = {
                            currentProgress = it
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .height(18.dp)
                            .testTag("player_seekbar")
                    )
                }
            }
        }
    }

    // Modern Material 3 quality selection bottomsheet
    if (showQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false },
            containerColor = Color(0xFF1E1E1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Pilih Kualitas Video",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                videoSources.forEach { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    showQualitySheet = false
                                    isBuffering = true
                                    delay(1500) // Buffer simulation
                                    selectedQuality = source.label
                                    isBuffering = false
                                    isPlaying = true
                                    Toast.makeText(
                                        context,
                                        "Kualitas diubah ke ${source.label}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (source.label == "Auto") "Auto (Optimal)" else source.label,
                            color = if (selectedQuality == source.label) MaterialTheme.colorScheme.primary else Color.White,
                            fontWeight = if (selectedQuality == source.label) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (selectedQuality == source.label) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Terpilih",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
