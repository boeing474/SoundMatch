package com.example.soundmatch.model

import java.io.Serializable

// match: de 10 a 100. Na descoberta por artistas parecidos, é o quanto a música chega perto
// do clima pedido; no quiz reserva, quanto dos votos foi para o gênero do resultado.
// surprise: o resultado saiu do estilo de sempre da pessoa (a tela mostra uma frase no lugar do %).
// fromSimilarArtists: a música veio dos artistas parecidos (e não do catálogo fixo).
data class SoundResult(
    val profile: UserProfile,
    val match: Int,
    val genre: Genre,
    val song: Song,
    val reason: String,
    val likedGenres: List<Genre>,
    val surprise: Boolean,
    val fromSimilarArtists: Boolean = false
) : Serializable {

    // Os estilos marcados, sem repetir o do próprio resultado.
    val likedGenresLabel: String
        get() = likedGenres.filter { it != genre }.joinToString(", ") { it.title }
}
