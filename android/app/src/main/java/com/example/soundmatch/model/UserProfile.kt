package com.example.soundmatch.model

import java.io.Serializable

// Serializable para poder ir de uma tela para outra pelo putExtra.
// instrument fica null para quem não toca nada.
// artistGenre vem do iTunes e fica null se o artista não foi encontrado (ou sem internet).
// artistSongs: as músicas mais conhecidas do artista, para a 1ª pergunta do quiz
// (vazia se o artista não foi encontrado: aí o quiz usa a pergunta reserva).
// relatedArtists: os artistas parecidos, vindos do Deezer (vazia se o Deezer falhou).
data class UserProfile(
    val name: String,
    val favoriteArtist: String,
    val situation: Situation,
    val instrument: Instrument?,
    val artistGenre: Genre?,
    val artistSongs: List<ArtistSong> = emptyList(),
    val relatedArtists: List<DeezerArtist> = emptyList()
) : Serializable {

    val playsInstrument: Boolean get() = instrument != null
}
