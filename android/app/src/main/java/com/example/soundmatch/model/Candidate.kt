package com.example.soundmatch.model

// Uma música que pode ser recomendada: os dados do Deezer
// e, quando o ReccoBeats conhece a música, as características do som.
data class Candidate(
    val track: DeezerTrack,
    val features: AudioFeatures?
)
