package com.example.soundmatch.model

import java.io.Serializable

/** Uma música do catálogo, marcada com os subgêneros a que pertence. */
data class Song(
    val title: String,
    val artist: String,
    val subgenres: List<Subgenre>
) : Serializable {

    /** Texto usado na busca do YouTube e no compartilhamento. */
    val displayName: String get() = "$title – $artist"
}
