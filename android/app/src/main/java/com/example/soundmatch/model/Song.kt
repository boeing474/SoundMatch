package com.example.soundmatch.model

import java.io.Serializable

// energy: a energia da música (o quiz filtra por ela).
// featured: o instrumento (ou a voz) que mais se destaca na música.
data class Song(
    val title: String,
    val artist: String,
    val energy: Energy,
    val featured: Instrument
) : Serializable {

    val displayName: String get() = "$title – $artist"
}
