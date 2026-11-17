package com.example.soundmatch.model

import java.io.Serializable

// energy: o clima da música (no catálogo fixo, o quiz reserva filtra por ele).
data class Song(
    val title: String,
    val artist: String,
    val energy: Energy
) : Serializable {

    val displayName: String get() = "$title – $artist"
}
