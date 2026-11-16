package com.example.soundmatch.logic

object ArtistName {

    // O iTunes às vezes devolve parcerias ("LUDMILLA & Caio Luccas"). Ficamos com a parte
    // que bate com o que a pessoa digitou, já com a grafia oficial ("bts" vira "BTS").
    // Duplas ("jorge e mateus" -> "Jorge & Mateus") ficam inteiras.
    fun clean(typed: String, fromITunes: String): String {
        val wanted = normalize(typed)
        if (normalize(fromITunes) == wanted) return fromITunes

        val parts = fromITunes.split(" & ", ", ", " feat. ", " ft. ", " x ").map { it.trim() }
        return parts.firstOrNull { normalize(it) == wanted }
            ?: parts.firstOrNull { normalize(it).contains(wanted) }
            ?: typed.trim()
    }

    private fun normalize(name: String) = name.trim().lowercase().replace(" & ", " e ")
}
