package com.example.soundmatch.model

import java.io.Serializable

/**
 * Dados do ouvinte coletados na UserProfileActivity.
 * É Serializable para poder ser enviado entre telas via Intent.putExtra.
 */
data class UserProfile(
    val name: String,
    val favoriteArtist: String,
    val listeningMode: String,
    val playsInstrument: Boolean
) : Serializable
