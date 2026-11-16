package com.example.soundmatch.model

import java.io.Serializable

// Uma música do artista favorito, vinda do iTunes, para a 1ª pergunta do quiz.
// genre fica null quando o gênero do iTunes não entra em nenhum dos 5 perfis.
data class ArtistSong(
    val title: String,
    val genre: Genre?
) : Serializable
