package com.example.soundmatch.network

import com.example.soundmatch.model.AudioFeaturesResponse
import retrofit2.http.GET
import retrofit2.http.Query

// API gratuita do ReccoBeats, sem chave. Diz se uma música é calma ou intensa,
// triste ou alegre, dançante ou não. Aceita várias músicas de uma vez,
// pelo ISRC separado por vírgula.
// Exemplo: https://api.reccobeats.com/v1/audio-features?ids=GBAYE0601696
interface ReccoBeatsApi {

    @GET("v1/audio-features")
    suspend fun audioFeatures(@Query("ids") isrcs: String): AudioFeaturesResponse
}
