package com.example.soundmatch.model

import java.io.Serializable

/** Resultado final do quiz, enviado da SoundQuizActivity para a ResultActivity. */
data class SoundResult(
    val profile: UserProfile,
    val score: Int,
    val archetype: Archetype,
    val song: Song,
    val reason: String,
    val subgenres: List<Subgenre>
) : Serializable {

    /** Subgêneros no formato salvo na API, ex.: "Hard Rock, Funk/Alternative". */
    val subgenresLabel: String get() = subgenres.joinToString(", ") { it.label }
}
