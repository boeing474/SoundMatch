package com.example.soundmatch.model

import androidx.annotation.ColorRes
import com.example.soundmatch.R

// Os 5 perfis sonoros. A ordem importa: a opção N de cada pergunta do quiz vota no gênero N.
// "keywords" são pedaços do nome de gênero que o iTunes devolve (ex.: "K-pop", "MPB", "Hip-Hop/Rap").
enum class Genre(
    val title: String,
    val highlight: String,
    @ColorRes val colorRes: Int,
    private val keywords: List<String>
) {
    POP(
        title = "Pop & K-pop",
        highlight = "Refrões que grudam, produção caprichada e muita personalidade.",
        colorRes = R.color.genre_pop,
        keywords = listOf("pop")
    ),
    ROCK(
        title = "Rock & Alternativo",
        highlight = "Guitarras, bateria pesada e banda tocando junto.",
        colorRes = R.color.genre_rock,
        keywords = listOf("rock", "metal", "alternativ", "punk", "indie", "grunge")
    ),
    HIPHOP(
        title = "Hip-hop & R&B",
        highlight = "Batida marcada, flow e letras que contam histórias.",
        colorRes = R.color.genre_hiphop,
        keywords = listOf("hip-hop", "rap", "r&b", "soul", "trap")
    ),
    ELECTRONIC(
        title = "Eletrônica",
        highlight = "Sintetizadores, batida contínua e o momento do drop.",
        colorRes = R.color.genre_electronic,
        keywords = listOf("dance", "electronic", "eletrônica", "house", "techno")
    ),
    BRAZILIAN(
        title = "Música Brasileira",
        highlight = "Suingue, violão e letra em português, do samba ao funk.",
        colorRes = R.color.genre_brazilian,
        keywords = listOf("mpb", "sertanejo", "samba", "pagode", "axé", "bossa", "forró", "brasil")
    );

    companion object {
        // Brasileira vem antes de Pop na busca: "Pop" é a palavra mais genérica de todas.
        private val SEARCH_ORDER = listOf(BRAZILIAN, HIPHOP, ELECTRONIC, ROCK, POP)

        fun fromITunes(genreName: String): Genre? {
            val name = genreName.lowercase()
            return SEARCH_ORDER.firstOrNull { genre -> genre.keywords.any { it in name } }
        }

        // O histórico só recebe o título da API; daqui sai a cor do card.
        fun fromTitle(title: String): Genre? = entries.firstOrNull { it.title == title }
    }
}
