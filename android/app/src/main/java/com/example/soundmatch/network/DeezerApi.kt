package com.example.soundmatch.network

import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerList
import com.example.soundmatch.model.DeezerTrack
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// API pública do Deezer, sem chave. Dá os artistas parecidos com o favorito
// e as músicas mais tocadas de cada um.
// Exemplo: https://api.deezer.com/artist/4611283/related
interface DeezerApi {

    @GET("search/artist")
    suspend fun searchArtist(
        @Query("q") name: String,
        @Query("limit") limit: Int = 5
    ): DeezerList<DeezerArtist>

    @GET("artist/{id}/related")
    suspend fun relatedArtists(
        @Path("id") artistId: Long,
        @Query("limit") limit: Int = 10
    ): DeezerList<DeezerArtist>

    @GET("artist/{id}/top")
    suspend fun topTracks(
        @Path("id") artistId: Long,
        @Query("limit") limit: Int = 5
    ): DeezerList<DeezerTrack>

    // Só esta chamada traz o ISRC, que liga a música ao ReccoBeats.
    @GET("track/{id}")
    suspend fun track(@Path("id") trackId: Long): DeezerTrack
}
