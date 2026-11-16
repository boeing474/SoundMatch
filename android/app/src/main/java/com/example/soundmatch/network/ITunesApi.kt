package com.example.soundmatch.network

import com.example.soundmatch.model.ITunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

// API pública da Apple, sem chave. Usada para descobrir o gênero do artista favorito
// e para buscar o álbum e a capa da música recomendada.
// A loja do Brasil (country=BR) acerta mais os gêneros brasileiros (MPB, sertanejo, pagode).
// Exemplo: https://itunes.apple.com/search?term=bts&entity=song&limit=1&country=BR
interface ITunesApi {

    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 1,
        @Query("country") country: String = "BR"
    ): ITunesSearchResponse
}
