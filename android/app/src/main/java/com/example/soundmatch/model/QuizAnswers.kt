package com.example.soundmatch.model

// surprise: a pessoa ligou o "Quero me arriscar".
// knownArtists: os parecidos que a pessoa já conhece (saem da disputa).
// popularity: hit ou pérola (null no quiz reserva, que não tem essa pergunta).
// likedGenres: os "outros estilos" marcados (só no quiz reserva).
data class QuizAnswers(
    val energy: Energy,
    val likedGenres: List<Genre> = emptyList(),
    val surprise: Boolean = false,
    val knownArtists: List<DeezerArtist> = emptyList(),
    val popularity: Popularity? = null
)
