package com.example.soundmatch.model

// Características do som de uma música, vindas do ReccoBeats. Todas vão de 0 a 1:
// energy (calma → intensa), valence (triste → alegre),
// danceability (difícil → fácil de dançar) e acousticness (elétrica → acústica).
data class AudioFeatures(
    val isrc: String?,
    val energy: Double,
    val valence: Double,
    val danceability: Double,
    val acousticness: Double
)

data class AudioFeaturesResponse(
    val content: List<AudioFeatures>
)
