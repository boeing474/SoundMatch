package com.example.soundmatch.network

import retrofit2.HttpException
import java.io.IOException

// Para as buscas que têm plano B: sem internet (IOException) ou com erro do servidor
// (HttpException), devolve null e a tela segue com o que tiver.
suspend fun <T> networkOrNull(call: suspend () -> T): T? =
    try {
        call()
    } catch (e: IOException) {
        null
    } catch (e: HttpException) {
        null
    }
