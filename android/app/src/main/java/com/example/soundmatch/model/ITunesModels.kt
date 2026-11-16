package com.example.soundmatch.model

// Resposta da iTunes Search API. Só mapeamos os campos que o app usa.

data class ITunesSearchResponse(
    val results: List<ITunesTrack>
)

data class ITunesTrack(
    val collectionName: String?,
    val artworkUrl100: String?
)
