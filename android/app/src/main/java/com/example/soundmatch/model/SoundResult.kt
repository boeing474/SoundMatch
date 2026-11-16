package com.example.soundmatch.model

import java.io.Serializable

data class SoundResult(
    val profile: UserProfile,
    val score: Int,
    val archetype: Archetype,
    val song: Song,
    val reason: String,
    val subgenres: List<Subgenre>
) : Serializable {

    val subgenresLabel: String get() = subgenres.joinToString(", ") { it.label }
}
