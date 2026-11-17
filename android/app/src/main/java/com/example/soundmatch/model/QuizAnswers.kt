package com.example.soundmatch.model

// firstPick e firstPickSong eram da antiga 1ª pergunta (músicas do artista) e hoje vão sempre null.
// surprise: a pessoa ligou o "Quero me arriscar".
// knownArtists: os parecidos que a pessoa já conhece (saem da disputa).
// popularity: hit ou pérola (null no quiz reserva, que não tem essa pergunta).
data class QuizAnswers(
    val firstPick: Genre?,
    val firstPickSong: String?,
    val energy: Energy,
    val likedGenres: List<Genre>,
    val surprise: Boolean,
    val knownArtists: List<DeezerArtist> = emptyList(),
    val popularity: Popularity? = null
)
