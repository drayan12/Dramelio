package com.example.data

import com.squareup.moshi.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// TMDB API Response Classes
data class TmdbSearchResponse(
    @Json(name = "results") val results: List<TmdbTvResult>
)

data class TmdbTvResult(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "first_air_date") val firstAirDate: String?
)

data class TmdbTvDetailResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "genres") val genres: List<TmdbGenre>?,
    @Json(name = "seasons") val seasons: List<TmdbSeason>?
)

data class TmdbGenre(
    @Json(name = "name") val name: String
)

data class TmdbSeason(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "episode_count") val episodeCount: Int,
    @Json(name = "poster_path") val posterPath: String?
)

data class TmdbSeasonEpisodesResponse(
    @Json(name = "episodes") val episodes: List<TmdbEpisode>?
)

data class TmdbEpisode(
    @Json(name = "id") val id: Int,
    @Json(name = "episode_number") val episodeNumber: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String?,
    @Json(name = "still_path") val stillPath: String?,
    @Json(name = "runtime") val runtime: Int?
)

interface TmdbApi {
    @GET("search/tv")
    suspend fun searchTv(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "id-ID"
    ): TmdbSearchResponse

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "id-ID"
    ): TmdbTvDetailResponse

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonEpisodes(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "id-ID"
    ): TmdbSeasonEpisodesResponse
}

object TmdbClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: TmdbApi = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(TmdbApi::class.java)
}
