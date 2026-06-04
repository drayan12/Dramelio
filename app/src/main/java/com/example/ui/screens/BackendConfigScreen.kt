package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendConfigScreen(
    currentConfig: BackendConfig,
    activeMovies: List<MovieOrSeries>,
    onSaveConfig: (BackendConfig) -> Unit,
    onImportSeries: (MovieOrSeries) -> Unit,
    onDeleteMovie: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Configuration Fields Input states
    var backendDomain by remember { mutableStateOf(currentConfig.domain) }
    var tripayApiKey by remember { mutableStateOf(currentConfig.tripayApiKey) }
    var tripayPrivateKey by remember { mutableStateOf(currentConfig.tripayPrivateKey) }
    var tripayMerchantCode by remember { mutableStateOf(currentConfig.tripayMerchantCode) }
    var tmdbApiKey by remember { mutableStateOf(currentConfig.tmdbApiKey) }
    var subscriptionBasicPrice by remember { mutableStateOf(currentConfig.subscriptionBasicPrice.toString()) }
    var subscriptionPremiumPrice by remember { mutableStateOf(currentConfig.subscriptionPremiumPrice.toString()) }
    var appThemePrimaryHex by remember { mutableStateOf(currentConfig.appThemePrimaryHex) }
    var appThemeBgHex by remember { mutableStateOf(currentConfig.appThemeBgHex) }
    var appThemeAccentHex by remember { mutableStateOf(currentConfig.appThemeAccentHex) }

    // TMDB Search State
    var tmdbSearchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<TmdbTvResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    // Fallbacks list of premium series to easily register/test without a live TMDB API key
    val demoTvShows = listOf(
        MovieOrSeries(
            id = "extra_squid_game",
            title = "Squid Game (TMDB 115421)",
            overview = "Ratusan pemain yang bangkrut menerima undangan aneh untuk bertanding dalam permainan anak-anak tradisional. Di dalam, hadiah menggiurkan menanti dengan taruhan mematikan yang tinggi.",
            posterUrl = "https://images.unsplash.com/photo-1627856013091-fed6e4e30025?w=500",
            backdropUrl = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=1000",
            rating = 8.8,
            releaseDate = "2021-09-17",
            isSeries = true,
            genres = listOf("Drama", "Misteri", "Komedi"),
            seasons = listOf(
                Season(
                    id = 1,
                    name = "Season 1",
                    episodes = listOf(
                        Episode("sg_1_1", 1, "Lampu Merah, Lampu Hijau", "59:40", "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=300", "Pembukaan permainan maut pertama yang legendaris.", "https://dramelio.com/streams/squid_game_s1e1.mp4"),
                        Episode("sg_1_2", 2, "Neraka", "62:10", "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=300", "Para peserta dihadapkan pada dilema moral kembali ke realita.", "https://dramelio.com/streams/squid_game_s1e2.mp4")
                    )
                )
            ),
            videoSources = listOf(
                VideoSource("Auto", "https://dramelio.com/streams/sg_auto.m3u8"),
                VideoSource("1080p", "https://dramelio.com/streams/sg_1080p.mp4"),
                VideoSource("720p", "https://dramelio.com/streams/sg_720p.mp4")
            )
        ),
        MovieOrSeries(
            id = "extra_crash_landing",
            title = "Crash Landing on You (TMDB 94796)",
            overview = "Seorang pewaris kaya Korsel mendarat darurat di Korea Utara akibat badai paralayang besar, bertemu seorang perwira tentara tampan yang melindunginya.",
            posterUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=500",
            backdropUrl = "https://images.unsplash.com/photo-1492446845049-9c50cc313f00?w=1000",
            rating = 8.9,
            releaseDate = "2019-12-14",
            isSeries = true,
            genres = listOf("Romantis", "Drama", "Komedi"),
            seasons = listOf(
                Season(
                    id = 1,
                    name = "Season 1",
                    episodes = listOf(
                        Episode("cloy_1_1", 1, "Pertemuan Pertama", "71:20", "https://images.unsplash.com/photo-1492446845049-9c50cc313f00?w=300", "Se-ri mendarat darurat di Zona Demiliterisasi Korut dan bertabrakan dengan Kapten Ri.", "https://dramelio.com/streams/cloy_s1e1.mp4")
                    )
                )
            ),
            videoSources = listOf(
                VideoSource("Auto", "https://dramelio.com/streams/cloy_auto.m3u8"),
                VideoSource("1080p", "https://dramelio.com/streams/cloy_1080p.mp4")
            )
        )
    )

    fun saveBackendSettings() {
        val basicPrice = subscriptionBasicPrice.toLongOrNull() ?: 29000
        val premiumPrice = subscriptionPremiumPrice.toLongOrNull() ?: 59000

        // Hex formats validation check
        if (!appThemePrimaryHex.startsWith("#") || appThemePrimaryHex.length !in 4..9 ||
            !appThemeBgHex.startsWith("#") || appThemeBgHex.length !in 4..9) {
            Toast.makeText(context, "Input hex warna harus berawalan '#' (e.g. #E50914)!", Toast.LENGTH_SHORT).show()
            return
        }

        val config = BackendConfig(
            domain = backendDomain,
            tripayApiKey = tripayApiKey,
            tripayPrivateKey = tripayPrivateKey,
            tripayMerchantCode = tripayMerchantCode,
            tmdbApiKey = tmdbApiKey,
            subscriptionBasicPrice = basicPrice,
            subscriptionPremiumPrice = premiumPrice,
            appThemePrimaryHex = appThemePrimaryHex,
            appThemeBgHex = appThemeBgHex,
            appThemeAccentHex = appThemeAccentHex
        )
        onSaveConfig(config)
        Toast.makeText(context, "Setelan Backend & Warna Default Berhasil Disimpan!", Toast.LENGTH_LONG).show()
    }

    // Live search TMDB utilizing Retrofit
    fun searchTmdbWeb() {
        if (tmdbApiKey.isBlank()) {
            Toast.makeText(context, "API Key TMDB Kosong! Menggunakan simulasi lokal TV Series.", Toast.LENGTH_SHORT).show()
            // Provide localized fallbacks if TMDB key is empty
            searchResults = demoTvShows.map {
                TmdbTvResult(
                    id = it.id.hashCode(),
                    name = it.title,
                    overview = it.overview,
                    posterPath = null,
                    backdropPath = null,
                    voteAverage = it.rating,
                    firstAirDate = it.releaseDate
                )
            }
            return
        }

        isSearching = true
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    TmdbClient.api.searchTv(tmdbApiKey, tmdbSearchQuery)
                }
                searchResults = response.results
                if (searchResults.isEmpty()) {
                    Toast.makeText(context, "Tidak ada hasil series untuk '$tmdbSearchQuery'", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error TMDB: ${e.localizedMessage}. Mengaktifkan demo fallback series.", Toast.LENGTH_LONG).show()
                // Auto fallback
                searchResults = demoTvShows.map {
                    TmdbTvResult(
                        id = it.id.hashCode(),
                        name = it.title,
                        overview = it.overview,
                        posterPath = null,
                        backdropPath = null,
                        voteAverage = it.rating,
                        firstAirDate = it.releaseDate
                    )
                }
            } finally {
                isSearching = false
            }
        }
    }

    // Importing chosen series
    fun importTmdbShow(resultId: Int, titleName: String) {
        isLoadingDetails = true
        coroutineScope.launch {
            try {
                // If this is a demo fallback ID
                val demoShow = demoTvShows.find { it.title == titleName || it.id.hashCode() == resultId }
                if (demoShow != null) {
                    onImportSeries(demoShow)
                    Toast.makeText(context, "Berhasil Impor Series Demo: ${demoShow.title}!", Toast.LENGTH_LONG).show()
                    isLoadingDetails = false
                    return@launch
                }

                if (tmdbApiKey.isBlank()) {
                    Toast.makeText(context, "TMDB API Key Diperlukan untuk impor langsung!", Toast.LENGTH_SHORT).show()
                    isLoadingDetails = false
                    return@launch
                }

                // Query details and episodes live from TMDB API
                val details = withContext(Dispatchers.IO) {
                    TmdbClient.api.getTvDetails(resultId, tmdbApiKey)
                }

                // Import 1st season's episodes
                val season1Num = details.seasons?.firstOrNull()?.seasonNumber ?: 1
                val episodesResult = try {
                    withContext(Dispatchers.IO) {
                        TmdbClient.api.getSeasonEpisodes(resultId, season1Num, tmdbApiKey)
                    }
                } catch (e: Exception) {
                    null
                }

                val customEpisodes = episodesResult?.episodes?.map { tmdbEp ->
                    Episode(
                        id = "tmdb_${resultId}_${tmdbEp.id}",
                        episodeNumber = tmdbEp.episodeNumber,
                        title = tmdbEp.name,
                        duration = "${tmdbEp.runtime ?: 45}m",
                        thumbnail = if (tmdbEp.stillPath != null) "https://image.tmdb.org/t/p/w300${tmdbEp.stillPath}" else "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=300",
                        overview = tmdbEp.overview ?: "Saksikan episode menarik ini kelanjutannya hanya di Dramelio.",
                        videoUrl = "https://dramelio.com/streams/tmdb_${resultId}_ep_${tmdbEp.episodeNumber}.mp4"
                    )
                } ?: listOf(
                    Episode("tmdb_${resultId}_def1", 1, "Episode Perdana", "45m", "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=300", "Pembukaan kisah perjuangan memikat hati penonton indonesia.", "https://dramelio.com/streams/show_${resultId}_ep1.mp4"),
                    Episode("tmdb_${resultId}_def2", 2, "Episode Kedua", "48m", "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=300", "Intrik persaingan memuncak seiring waktu berjalan.", "https://dramelio.com/streams/show_${resultId}_ep2.mp4")
                )

                val movieSeries = MovieOrSeries(
                    id = "tmdb_$resultId",
                    title = details.name,
                    overview = details.overview ?: "No Description provided. Sync via brand new TMDB Backend integrator API.",
                    posterUrl = if (details.posterPath != null) "https://image.tmdb.org/t/p/w500${details.posterPath}" else "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=500",
                    backdropUrl = if (details.backdropPath != null) "https://image.tmdb.org/t/p/w1000${details.backdropPath}" else "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1000",
                    rating = details.voteAverage,
                    releaseDate = details.firstAirDate ?: "2026-06-04",
                    isSeries = true,
                    genres = details.genres?.map { it.name } ?: listOf("Drama", "TV Series"),
                    seasons = listOf(
                        Season(id = 1, name = "Season 1", episodes = customEpisodes)
                    ),
                    videoSources = listOf(
                        VideoSource("Auto", "https://dramelio.com/streams/tmdb_${resultId}_auto.m3u8"),
                        VideoSource("1080p", "https://dramelio.com/streams/tmdb_${resultId}_1080p.mp4"),
                        VideoSource("720p", "https://dramelio.com/streams/tmdb_${resultId}_720p.mp4")
                    )
                )

                onImportSeries(movieSeries)
                Toast.makeText(context, "Berhasil Menambahkan TV Series: ${movieSeries.title}!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengimpor dari TMDB: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                // Attempt demo loading
                val demoShow = demoTvShows.firstOrNull()
                if (demoShow != null) {
                    onImportSeries(demoShow)
                    Toast.makeText(context, "Berhasil Memuat Demo Fallback: ${demoShow.title}!", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoadingDetails = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Kontrol Backend", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Domain
            item {
                Text(text = "1. Pengaturan Domain Host", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = backendDomain,
                            onValueChange = { backendDomain = it },
                            label = { Text("Domain Backend Utama", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("domain_config_input")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "*Seluruh aset video dan sinkronisasi profil dialokasikan ke: https://$backendDomain", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }

            // Section 2: Tripay PG & Subscription Fees
            item {
                Text(text = "2. Integrasi Tripay CO.ID & Tarif", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = tripayMerchantCode,
                            onValueChange = { tripayMerchantCode = it },
                            label = { Text("Kode Merchant Tripay", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tripayApiKey,
                            onValueChange = { tripayApiKey = it },
                            label = { Text("API Key Tripay (Sandbox/Production)", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tripayPrivateKey,
                            onValueChange = { tripayPrivateKey = it },
                            label = { Text("Private Key Tripay", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = subscriptionBasicPrice,
                                onValueChange = { subscriptionBasicPrice = it },
                                label = { Text("Tarif Bulanan (Rp)", color = Color.Gray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                modifier = Modifier.weight(1f).testTag("basic_price_input")
                            )
                            OutlinedTextField(
                                value = subscriptionPremiumPrice,
                                onValueChange = { subscriptionPremiumPrice = it },
                                label = { Text("Tarif Tahunan (Rp)", color = Color.Gray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                modifier = Modifier.weight(1f).testTag("premium_price_input")
                            )
                        }
                    }
                }
            }

            // Section 3: Color dynamic overriding
            item {
                Text(text = "3. Panel Desain Warna Tema Default", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Sesuaikan warna branding instan di aplikasi:", color = Color.LightGray, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = appThemePrimaryHex,
                                onValueChange = { appThemePrimaryHex = it },
                                label = { Text("Primer (e.g. #E50914)", color = Color.Gray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                modifier = Modifier.weight(1f).testTag("theme_primary_input")
                            )
                            OutlinedTextField(
                                value = appThemeBgHex,
                                onValueChange = { appThemeBgHex = it },
                                label = { Text("BG (e.g. #141414)", color = Color.Gray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = appThemeAccentHex,
                            onValueChange = { appThemeAccentHex = it },
                            label = { Text("Aksen IQIYI (e.g. #1CD260)", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section 4: Saving all backend configs
            item {
                Button(
                    onClick = { saveBackendSettings() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_backend_config_button")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Simpan Seluruh Setelan Backend", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Section 5: TMDB Series integration
            item {
                Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                Text(text = "4. Integrasi TMDB API - Tambah Series", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(text = "Masukkan API Key TMDB di atas lalu ketik kata kunci mencari serial drama Asia, Barat atau Anime populer secara real-time langsung dari satelit server TMDB.", color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
                
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = tmdbApiKey,
                            onValueChange = { tmdbApiKey = it },
                            label = { Text("API Key TMDB", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("tmdb_key_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = tmdbSearchQuery,
                                onValueChange = { tmdbSearchQuery = it },
                                label = { Text("Masukkan Judul Series (e.g. Demon Slayer)", color = Color.Gray) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                modifier = Modifier.weight(1f).testTag("tmdb_search_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { searchTmdbWeb() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                modifier = Modifier.height(54.dp).testTag("tmdb_search_button")
                            ) {
                                if (isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Cari")
                                }
                            }
                        }
                    }
                }
            }

            // TMDB Results Grid Render Area
            if (searchResults.isNotEmpty()) {
                item {
                    Text(text = "Hasil Penelusuran TMDB (${searchResults.size}):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                items(searchResults) { result ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mini poster representation
                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 70.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.DarkGray)
                            ) {
                                if (result.posterPath != null) {
                                    AsyncImage(
                                        model = "https://image.tmdb.org/t/p/w200${result.posterPath}",
                                        contentDescription = result.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Tv, contentDescription = null, tint = Color.LightGray, modifier = Modifier.align(Alignment.Center))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = result.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Rilis: ${result.firstAirDate ?: "-"} | Kelas: Rating ${result.voteAverage}", color = Color.LightGray, fontSize = 11.sp)
                                Text(
                                    text = result.overview ?: "No synopsis.",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { importTmdbShow(result.id, result.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp).testTag("import_button_${result.id}")
                            ) {
                                if (isLoadingDetails) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Text(text = "Impor", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Listing Active Movies list for deletion if needed
            item {
                Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                Text(text = "Daftar Konten Client Aktif (${activeMovies.size}):", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }

            items(activeMovies) { m ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = m.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Tipe: ${if (m.isSeries) "Series" else "Bioskop"} | Genre: ${m.genres.joinToString()}", color = Color.Gray, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = {
                                onDeleteMovie(m.id)
                                Toast.makeText(context, "Konten dihapus!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("delete_movie_${m.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
