package com.example.soundmatch.model

import java.io.Serializable

// Serializable para poder ir de uma tela para outra pelo putExtra.
// instrument fica null para quem não toca nada.
// artistGenre vem do iTunes e fica null se o artista não foi encontrado (ou sem internet).
// artistSongs: as músicas mais conhecidas do artista, para a 1ª pergunta do quiz
// (vazia se o artista não foi encontrado: aí o quiz usa a pergunta reserva).
data class UserProfile(
    val name: String,
    val favoriteArtist: String,
    val situation: Situation,
    val instrument: Instrument?,
    val artistGenre: Genre?,
    val artistSongs: List<ArtistSong> = emptyList()
) : Serializable {

    val playsInstrument: Boolean get() = instrument != null
}
