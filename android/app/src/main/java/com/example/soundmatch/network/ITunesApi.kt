package com.example.soundmatch.network

import com.example.soundmatch.model.ITunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

// API pública da Apple, sem chave. Usada só para buscar o álbum e a capa da música.
// Exemplo: https://itunes.apple.com/search?term=Superstition+Stevie+Wonder&entity=song&limit=1
interface ITunesApi {

    @GET("search")
    suspend fun searchSong(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 1
    ): ITunesSearchResponse
}
