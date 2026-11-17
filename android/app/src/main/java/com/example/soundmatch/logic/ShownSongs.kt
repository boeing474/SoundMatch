package com.example.soundmatch.logic

import com.example.soundmatch.model.Song

// As músicas que o app já recomendou enquanto está aberto, para o quiz não repetir.
// É um object: existe um só no app inteiro e dura até o app ser fechado.
object ShownSongs {

    private val keys = mutableSetOf<String>()

    val all: Set<String> get() = keys

    // Se a música já estava na lista, é porque todas as candidatas já tinham saído
    // e o motor voltou ao começo: a lista recomeça a partir dela.
    fun remember(song: Song) {
        val key = DiscoveryEngine.songKey(song.title, song.artist)
        if (!keys.add(key)) {
            keys.clear()
            keys.add(key)
        }
    }

    fun clear() = keys.clear()
}
