package com.example.soundmatch.model

import java.io.Serializable

// energy: o clima da música (no catálogo fixo, o quiz reserva filtra por ele).
// album e coverUrl só vêm preenchidos nas músicas do Deezer; nas do catálogo fixo,
// a tela de resultado procura a capa no iTunes.
data class Song(
    val title: String,
    val artist: String,
    val energy: Energy,
    val album: String? = null,
    val coverUrl: String? = null
) : Serializable {

    val displayName: String get() = "$title – $artist"
}
