package com.example.soundmatch.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // No celular, "localhost" só chega no PC depois de rodar: adb reverse tcp:8000 tcp:8000
    // (no emulador, sem adb reverse, daria para usar http://10.0.2.2:8000/)
    private const val BASE_URL = "http://localhost:8000/"

    private const val ITUNES_URL = "https://itunes.apple.com/"

    val api: SoundMatchApi by lazy { create(BASE_URL) }

    val itunes: ITunesApi by lazy { create(ITUNES_URL) }

    private inline fun <reified T> create(baseUrl: String): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(T::class.java)
}
