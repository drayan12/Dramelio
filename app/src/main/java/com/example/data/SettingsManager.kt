package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dramelio_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val configAdapter = moshi.adapter(BackendConfig::class.java)
    private val profileAdapter = moshi.adapter(UserProfile::class.java)
    private val movieListType = Types.newParameterizedType(List::class.java, MovieOrSeries::class.java)
    private val movieListAdapter = moshi.adapter<List<MovieOrSeries>>(movieListType)
    private val txListType = Types.newParameterizedType(List::class.java, PaymentTransaction::class.java)
    private val txListAdapter = moshi.adapter<List<PaymentTransaction>>(txListType)

    private val _backendConfig = MutableStateFlow(loadBackendConfig())
    val backendConfig: StateFlow<BackendConfig> = _backendConfig.asStateFlow()

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _movies = MutableStateFlow(loadMovies())
    val movies: StateFlow<List<MovieOrSeries>> = _movies.asStateFlow()

    private val _transactions = MutableStateFlow(loadTransactions())
    val transactions: StateFlow<List<PaymentTransaction>> = _transactions.asStateFlow()

    private fun loadBackendConfig(): BackendConfig {
        val json = prefs.getString("backend_config", null)
        val loaded = if (json != null) {
            configAdapter.fromJson(json) ?: BackendConfig()
        } else {
            BackendConfig()
        }
        // Force upgrade to Sophisticated Dark if user is still on old template defaults
        if (loaded.appThemePrimaryHex == "#E50914" || loaded.appThemeBgHex == "#141414") {
            val migrated = loaded.copy(
                appThemePrimaryHex = "#EAB308",
                appThemeBgHex = "#050505",
                appThemeAccentHex = "#FACC15"
            )
            prefs.edit().putString("backend_config", configAdapter.toJson(migrated)).apply()
            return migrated
        }
        return loaded
    }

    fun saveBackendConfig(config: BackendConfig) {
        prefs.edit().putString("backend_config", configAdapter.toJson(config)).apply()
        _backendConfig.value = config
    }

    suspend fun syncRemoteConfig(urlStr: String): Result<BackendConfig> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.inputStream.use { input ->
                val jsonString = input.bufferedReader().use { it.readText() }
                
                // Try parsing as the full RemoteControlResponse wrapper first
                val responseAdapter = moshi.adapter(RemoteControlResponse::class.java)
                var fetchedConfig: BackendConfig? = null
                var fetchedMovies: List<MovieOrSeries>? = null
                
                try {
                    val wrapper = responseAdapter.fromJson(jsonString)
                    if (wrapper != null) {
                        fetchedConfig = wrapper.config
                        fetchedMovies = wrapper.movies
                    }
                } catch (e: Exception) {
                    // Fallback to direct BackendConfig parsing if they uploaded just the config file
                    val directAdapter = moshi.adapter(BackendConfig::class.java)
                    fetchedConfig = directAdapter.fromJson(jsonString)
                }
                
                if (fetchedConfig != null) {
                    val merged = fetchedConfig.copy(
                        remoteConfigUrl = urlStr,
                        isRemoteConfigEnabled = true
                    )
                    saveBackendConfig(merged)
                    
                    // If movies are retrieved from PHP API, save them locally for daily content update!
                    if (fetchedMovies != null && fetchedMovies.isNotEmpty()) {
                        prefs.edit().putString("movies", movieListAdapter.toJson(fetchedMovies)).apply()
                        _movies.value = fetchedMovies
                    }
                    
                    Result.success(merged)
                } else {
                    Result.failure(Exception("Gagal mengurai format JSON dari server remote"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun loadUserProfile(): UserProfile {
        val json = prefs.getString("user_profile", null)
        return if (json != null) {
            profileAdapter.fromJson(json) ?: UserProfile("Hendra Drayan", "hendradrayan9@gmail.com", false, null, null)
        } else {
            UserProfile("Hendra Drayan", "hendradrayan9@gmail.com", false, null, null)
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit().putString("user_profile", profileAdapter.toJson(profile)).apply()
        _userProfile.value = profile
    }

    private fun loadTransactions(): List<PaymentTransaction> {
        val json = prefs.getString("transactions", null)
        return if (json != null) {
            txListAdapter.fromJson(json) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun addTransaction(tx: PaymentTransaction) {
        val current = _transactions.value.toMutableList()
        current.add(0, tx)
        prefs.edit().putString("transactions", txListAdapter.toJson(current)).apply()
        _transactions.value = current
    }

    fun updateTransactionStatus(id: String, status: String) {
        val current = _transactions.value.map {
            if (it.id == id) {
                it.copy(status = status)
            } else {
                it
            }
        }
        prefs.edit().putString("transactions", txListAdapter.toJson(current)).apply()
        _transactions.value = current

        // If status is PAID, automatically upgrade user subscription
        if (status == "PAID") {
            val tx = current.find { it.id == id }
            if (tx != null) {
                val durationDays = if (tx.planId == "premium") 30 else 30
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val expiryDate = sdf.format(Date(System.currentTimeMillis() + durationDays * 24L * 60 * 60 * 1000))
                saveUserProfile(
                    _userProfile.value.copy(
                        isPremium = true,
                        activePlanId = tx.planId,
                        planExpiryDate = expiryDate
                    )
                )
            }
        }
    }

    private fun loadMovies(): List<MovieOrSeries> {
        val json = prefs.getString("movies", null)
        return if (json != null) {
            movieListAdapter.fromJson(json) ?: getInitialMovies()
        } else {
            val initial = getInitialMovies()
            prefs.edit().putString("movies", movieListAdapter.toJson(initial)).apply()
            initial
        }
    }

    fun addImportedSeries(series: MovieOrSeries) {
        val current = _movies.value.toMutableList()
        // Prevent duplicate
        current.removeAll { it.id == series.id }
        current.add(0, series)
        prefs.edit().putString("movies", movieListAdapter.toJson(current)).apply()
        _movies.value = current
    }

    fun deleteMovie(id: String) {
        val current = _movies.value.toMutableList()
        current.removeAll { it.id == id }
        prefs.edit().putString("movies", movieListAdapter.toJson(current)).apply()
        _movies.value = current
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _backendConfig.value = BackendConfig()
        _userProfile.value = UserProfile("Hendra Drayan", "hendradrayan9@gmail.com", false, null, null)
        val initial = getInitialMovies()
        _movies.value = initial
        prefs.edit().putString("movies", movieListAdapter.toJson(initial)).apply()
        _transactions.value = emptyList()
    }

    private fun getInitialMovies(): List<MovieOrSeries> {
        return listOf(
            MovieOrSeries(
                id = "layangan_putus",
                title = "Layangan Putus",
                overview = "Kisah dramatis pengkhianatan dalam mahligai rumah tangga antara Aris, Kinan, dan Lydia yang menyayat hati pemirsa Indonesia.",
                posterUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=500",
                backdropUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1000",
                rating = 8.7,
                releaseDate = "2021-11-26",
                isSeries = true,
                genres = listOf("Drama", "Romantis", "Keluarga"),
                seasons = listOf(
                    Season(
                        id = 1,
                        name = "Season 1",
                        episodes = listOf(
                            Episode("lp_1_1", 1, "Awal Keretakan", "45:20", "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=300", "Kinan merasakan gelagat mencurigakan dari suaminya, Aris, yang sering pulang terlambat tanpa alasan jelas.", "https://dramelio.com/streams/layangan_putus_s1e1.mp4"),
                            Episode("lp_1_2", 2, "Anatomi Rasa Curiga", "42:15", "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=300", "Sebuah anting asing ditemukan di saku jas Aris, membuka mata rantai kecurigaan yang menyakitkan bagi Kinan.", "https://dramelio.com/streams/layangan_putus_s1e2.mp4"),
                            Episode("lp_1_3", 3, "Nama yang Disembunyikan", "48:10", "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=300", "Lydia Danira, sosok psikolog anak, mulai masuk ke pusaran kehidupan Aris dengan rahasia yang tersembunyi rapat.", "https://dramelio.com/streams/layangan_putus_s1e3.mp4")
                        )
                    )
                ),
                videoSources = listOf(
                    VideoSource("Auto", "https://dramelio.com/streams/hlse_lp_auto.m3u8"),
                    VideoSource("1080p", "https://dramelio.com/streams/lp_1080p.mp4"),
                    VideoSource("720p", "https://dramelio.com/streams/lp_720p.mp4"),
                    VideoSource("480p", "https://dramelio.com/streams/lp_480p.mp4")
                )
            ),
            MovieOrSeries(
                id = "gadis_kretek",
                title = "Gadis Kretek",
                overview = "Menguak rahasia industri kretek di era prapemberontakan 1965, berpusat pada kisah romantis Dasiyah (Jeng Yah) mencari cinta dan jatidiri di balik racikan saus tembakau termasyhur.",
                posterUrl = "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=500",
                backdropUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1000",
                rating = 9.1,
                releaseDate = "2023-11-02",
                isSeries = true,
                genres = listOf("Sejarah", "Drama", "Sastra"),
                seasons = listOf(
                    Season(
                        id = 1,
                        name = "Season 1",
                        episodes = listOf(
                            Episode("gk_1_1", 1, "Rasa Kebebasan", "55:30", "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=300", "Lebas mencari perempuan misterius bernama Jeng Yah atas mandat ayahnya yang sedang sekarat.", "https://dramelio.com/streams/gadis_kretek_s1e1.mp4"),
                            Episode("gk_1_2", 2, "Racikan Rahasia", "52:10", "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=300", "Dasiyah membuktikan kemampuannya meracik saus kretek terbaik di kota M, meruntuhkan stigma gender patriarkal.", "https://dramelio.com/streams/gadis_kretek_s1e2.mp4")
                        )
                    )
                ),
                videoSources = listOf(
                    VideoSource("Auto", "https://dramelio.com/streams/hlse_gk_auto.m3u8"),
                    VideoSource("1080p", "https://dramelio.com/streams/gk_1080p.mp4"),
                    VideoSource("720p", "https://dramelio.com/streams/gk_720p.mp4")
                )
            ),
            MovieOrSeries(
                id = "my_demon",
                title = "My Demon (Dramelio Exclusive)",
                overview = "Seorang iblis tampan kehilangan kekuatannya setelah terikat pernikahan kontrak dengan seorang pewaris konglomerat yang dingin.",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
                backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1000",
                rating = 8.5,
                releaseDate = "2023-11-24",
                isSeries = true,
                genres = listOf("Komedi", "Fantasi", "Romantis"),
                seasons = listOf(
                    Season(
                        id = 1,
                        name = "Season 1",
                        episodes = listOf(
                            Episode("md_1_1", 1, "Tato Iblis yang Hilang", "60:15", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300", "Gu-won terjun ke laut menyelamatkan Do Do-hee, namun segel kekuatannya malah berpindah ke pergelangan tangan sang gadis.", "https://dramelio.com/streams/my_demon_s1e1.mp4")
                        )
                    )
                ),
                videoSources = listOf(
                    VideoSource("Auto", "https://dramelio.com/streams/hlse_md_auto.m3u8"),
                    VideoSource("1080p", "https://dramelio.com/streams/md_1080p.mp4"),
                    VideoSource("720p", "https://dramelio.com/streams/md_720p.mp4")
                )
            ),
            MovieOrSeries(
                id = "love_between_fairy",
                title = "Love Between Fairy and Devil",
                overview = "Peri Orchid secara tidak sengaja melepaskan Raja Iblis Dongfang Qingcang yang kejam, memicu ikatan cinta magis yang mengguncang surga dan bumi.",
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500",
                backdropUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1000",
                rating = 9.3,
                releaseDate = "2022-08-07",
                isSeries = true,
                genres = listOf("Wuxia", "Romantis", "Drama"),
                seasons = listOf(
                    Season(
                        id = 1,
                        name = "Season 1",
                        episodes = listOf(
                            Episode("lbf_1_1", 1, "Pertukaran Tubuh", "45:00", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=300", "Mantra pembebas kuno menyebabkan jiwa Raja Iblis bertukar secara misterius dengan peri bunga kecil.", "https://dramelio.com/streams/lbf_s1e1.mp4")
                        )
                    )
                ),
                videoSources = listOf(
                    VideoSource("Auto", "https://dramelio.com/streams/hlse_lbf_auto.m3u8"),
                    VideoSource("1080p", "https://dramelio.com/streams/lbf_1080p.mp4"),
                    VideoSource("720p", "https://dramelio.com/streams/lbf_720p.mp4")
                )
            )
        )
    }
}
