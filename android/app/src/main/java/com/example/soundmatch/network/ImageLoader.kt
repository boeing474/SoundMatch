package com.example.soundmatch.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object ImageLoader {

    // O iTunes devolve a capa em 100x100; trocando no link pedimos uma maior.
    fun bigCover(artworkUrl100: String): String = artworkUrl100.replace("100x100", "600x600")

    // Download em Dispatchers.IO, porque o Android não deixa usar a rede na thread da tela.
    suspend fun load(url: String): Bitmap? = withContext(Dispatchers.IO) {
        URL(url).openStream().use { BitmapFactory.decodeStream(it) }
    }
}
