package com.example.soundmatch.model

import java.io.Serializable

// match: de 10 a 100, quanto dos votos foi para o gênero do resultado.
// surprise: o resultado saiu do estilo de sempre da pessoa (a tela mostra uma frase no lugar do %).
data class SoundResult(
    val profile: UserProfile,
    val match: Int,
    val genre: Genre,
    val song: Song,
    val reason: String,
    val likedGenres: List<Genre>,
    val surprise: Boolean
) : Serializable {

    val likedGenresLabel: String get() = likedGenres.joinToString(", ") { it.title }
}
