package com.example.soundmatch.model

import java.io.Serializable

// Serializable para poder ir de uma tela para outra pelo putExtra.
// instrument fica null para quem não toca nada.
// artistGenre vem do iTunes e fica null se o artista não foi encontrado (ou sem internet).
data class UserProfile(
    val name: String,
    val favoriteArtist: String,
    val situation: Situation,
    val instrument: Instrument?,
    val artistGenre: Genre?
) : Serializable {

    val playsInstrument: Boolean get() = instrument != null
}
