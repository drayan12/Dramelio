package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movie: MovieOrSeries,
    userProfile: UserProfile,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    primaryColor: Color
) {
    val context = LocalContext.current
    var isPlayingVideo by remember { mutableStateOf(false) }
    var selectedSeasonIndex by remember { mutableStateOf(0) }
    var activeEpisodeId by remember { mutableStateOf<String?>(null) }
    
    val selectedSeason = movie.seasons.getOrNull(selectedSeasonIndex)
    val episodesList = selectedSeason?.episodes ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Top section: either the VideoPlayer or static backdrop art
        if (isPlayingVideo) {
            VideoPlayer(
                movie = movie,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                onBack = { isPlayingVideo = false }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .testTag("detail_backdrop_box")
            ) {
                AsyncImage(
                    model = movie.backdropUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark elegant fading gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Action overlays
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                // Play floating CTA overlay
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .clickable {
                            isPlayingVideo = true
                            activeEpisodeId = episodesList.firstOrNull()?.id
                        }
                        .align(Alignment.Center)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Putar",
                        tint = Color.White,
                        modifier = Modifier
                            .size(38.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        // Meta Descriptions
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub meta tags matching premium series
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year
                Text(
                    text = movie.releaseDate.substring(0, 4),
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Age rating
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = movie.ageRating,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Duration
                Text(
                    text = movie.duration,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Stream Quality rating tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(primaryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = movie.quality,
                        color = primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Genres list
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                movie.genres.forEach { genre ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = genre, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Synopsis
            Text(
                text = movie.overview,
                color = Color.LightGray,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Episodes Section (for series) or Video source links
            if (movie.isSeries) {
                // Header Season Row Selection tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Episode Terbaru",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    if (movie.seasons.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clickable { expanded = true }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = movie.seasons[selectedSeasonIndex].name,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF2B2B2B))
                            ) {
                                movie.seasons.forEachIndexed { idx, s ->
                                    DropdownMenuItem(
                                        text = { Text(text = s.name, color = Color.White) },
                                        onClick = {
                                            selectedSeasonIndex = idx
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Seasons 1",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Render episode layoutcards
                episodesList.forEach { ep ->
                    // VIP Guard: Non-VIP users cannot stream episode numbers larger than 1 on exclusive series
                    val isLocked = !userProfile.isPremium && ep.episodeNumber > 1 && (movie.id == "my_demon" || movie.id == "gadis_kretek" || movie.id == "love_between_fairy")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                if (isLocked) {
                                    Toast.makeText(context, "Materi VIP - Selesaikan Pembayaran Langganan!", Toast.LENGTH_LONG).show()
                                    onNavigateToCheckout()
                                } else {
                                    isPlayingVideo = true
                                    activeEpisodeId = ep.id
                                    Toast.makeText(context, "Memutar Episode ${ep.episodeNumber}: ${ep.title}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .background(
                                if (activeEpisodeId == ep.id) Color.White.copy(alpha = 0.05f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Episode Thumbnail Container with play/lock badges
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 70.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.DarkGray)
                        ) {
                            AsyncImage(
                                model = ep.thumbnail,
                                contentDescription = ep.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Locked VIP banner overlay
                            if (isLocked) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Terkunci",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = ep.duration,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Eps ${ep.episodeNumber}",
                                    color = if (activeEpisodeId == ep.id) primaryColor else Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                if (isLocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "VIP",
                                        color = Color(0xFFFFD700),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(
                                                color = Color(0xFF3E3613),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = ep.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ep.overview,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            } else {
                // Standard Movie qualities streaming button
                Text(
                    text = "Streaming Video Kualitas Pilihan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Listing qualities directly
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    movie.videoSources.forEach { source ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .clickable {
                                    isPlayingVideo = true
                                    Toast.makeText(context, "Memulai streaming kualitas ${source.label}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = source.label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
