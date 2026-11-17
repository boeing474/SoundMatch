package com.example.soundmatch.model

import java.io.Serializable

// Serializable para poder ir de uma tela para outra pelo putExtra.
// artistGenre vem do iTunes e fica null se o artista não foi encontrado (ou sem internet).
// relatedArtists: os artistas parecidos, vindos do Deezer (vazia se o Deezer falhou).
data class UserProfile(
    val name: String,
    val favoriteArtist: String,
    val situation: Situation,
    val artistGenre: Genre?,
    val relatedArtists: List<DeezerArtist> = emptyList()
) : Serializable
