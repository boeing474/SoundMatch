package com.example.soundmatch.network

import android.graphics.Bitmap
import com.example.soundmatch.logic.TrackPicker
import retrofit2.HttpException
import java.io.IOException

// Busca a capa de uma música no iTunes e guarda na memória.
// Assim, quando um card sai e volta para a tela ao rolar a lista, a busca não se repete.
object CoverRepository {

    private val cache = mutableMapOf<String, Bitmap>()

    suspend fun coverFor(song: String, artist: String): Bitmap? {
        val key = "$song|$artist"
        cache[key]?.let { return it }

        return try {
            val results = RetrofitClient.itunes.search("$song $artist", limit = SEARCH_LIMIT).results
            val artwork = TrackPicker.bestMatch(results, artist)?.artworkUrl100 ?: return null
            ImageLoader.load(ImageLoader.smallCover(artwork))?.also { cache[key] = it }
        } catch (e: IOException) {
            null   // sem conexão: o card fica com o vinil
        } catch (e: HttpException) {
            null
        }
    }

    private const val SEARCH_LIMIT = 10
}
