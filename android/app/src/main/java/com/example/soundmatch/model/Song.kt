package com.example.soundmatch.model

import java.io.Serializable

data class Song(
    val title: String,
    val artist: String,
    val subgenres: List<Subgenre>
) : Serializable {

    val displayName: String get() = "$title – $artist"
}
