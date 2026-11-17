package com.example.soundmatch.network

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // API publicada no Render. Para usar a API local no PC, troque por "http://localhost:8000/"
    // e rode: adb reverse tcp:8000 tcp:8000 (no emulador: "http://10.0.2.2:8000/")
    private const val BASE_URL = "https://soundmatch-api-dnh5.onrender.com/"

    private const val ITUNES_URL = "https://itunes.apple.com/"
    private const val DEEZER_URL = "https://api.deezer.com/"
    private const val RECCOBEATS_URL = "https://api.reccobeats.com/"

    // No plano grátis, o Render "dorme" a API e leva até ~1 min para acordar.
    // O padrão do OkHttp (10 s) desistiria antes disso.
    private val apiClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: SoundMatchApi by lazy { create(BASE_URL, apiClient) }

    // O iTunes fica com o tempo padrão: se demorar, o app segue sem capa.
    val itunes: ITunesApi by lazy { create(ITUNES_URL, OkHttpClient()) }

    // Deezer e ReccoBeats também ficam com o tempo padrão: se falharem, o app usa a reserva.
    val deezer: DeezerApi by lazy { create(DEEZER_URL, OkHttpClient()) }
    val reccoBeats: ReccoBeatsApi by lazy { create(RECCOBEATS_URL, OkHttpClient()) }

    private inline fun <reified T> create(baseUrl: String, client: OkHttpClient): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(T::class.java)
}
