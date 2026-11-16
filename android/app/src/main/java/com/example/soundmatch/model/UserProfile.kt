package com.example.soundmatch.model

import java.io.Serializable

// Serializable para poder ir de uma tela para outra pelo putExtra.
data class UserProfile(
    val name: String,
    val favoriteArtist: String,
    val listeningMode: String,
    val playsInstrument: Boolean
) : Serializable
