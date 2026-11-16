package com.example.soundmatch.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cria uma única instância do Retrofit para o app inteiro (padrão Singleton com "object").
 */
object RetrofitClient {

    /*
     * Endereço da API:
     * - Emulador: 10.0.2.2 é o "localhost" do computador visto de dentro do emulador.
     * - Celular físico (USB): troque para "http://localhost:8000/" e rode no PC:
     *       adb reverse tcp:8000 tcp:8000
     *   Isso redireciona a porta 8000 do celular para a porta 8000 do computador.
     */
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: SoundMatchApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON <-> classes Kotlin
            .build()
            .create(SoundMatchApi::class.java)
    }
}
