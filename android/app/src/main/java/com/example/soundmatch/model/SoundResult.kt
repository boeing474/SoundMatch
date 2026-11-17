package com.example.soundmatch.model

import java.io.Serializable

// match: de 10 a 100. Na descoberta por artistas parecidos, é o quanto a música chega perto
// do clima pedido; no quiz reserva, quanto dos votos foi para o gênero do resultado.
// genre: no quiz reserva, o estilo vencedor. Na descoberta, o estilo do artista favorito,
// usado só para a cor do card (null quando o iTunes não soube dizer).
// surprise: o resultado saiu do estilo de sempre da pessoa (a tela mostra uma frase no lugar do %).
// fromSimilarArtists: a música veio dos artistas parecidos (e não do catálogo fixo).
// popularity: hit ou pérola, para o card repetir o que a pessoa pediu (null no quiz reserva).
data class SoundResult(
    val profile: UserProfile,
    val match: Int,
    val genre: Genre?,
    val song: Song,
    val reason: String,
    val likedGenres: List<Genre>,
    val surprise: Boolean,
    val fromSimilarArtists: Boolean = false,
    val popularity: Popularity? = null
) : Serializable {

    // Os estilos marcados, sem repetir o do próprio resultado.
    val likedGenresLabel: String
        get() = likedGenres.filter { it != genre }.joinToString(", ") { it.title }
}
