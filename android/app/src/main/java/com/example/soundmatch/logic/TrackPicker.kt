package com.example.soundmatch.logic

import com.example.soundmatch.model.ITunesTrack

// Escolhe o que interessa entre os resultados de uma busca no iTunes,
// que misturam versões ao vivo, remixes e músicas de outros artistas.
object TrackPicker {

    private val VERSION_MARKERS = listOf(
        "live", "ao vivo", "remix", "acoustic", "acústico", "instrumental",
        "version", "versão", "sped up", "slowed", "karaoke"
    )

    private val EXTRAS = Regex("""[(\[].*?[)\]]| - .*$""")

    // A faixa certa para a capa: do artista certo e na versão de estúdio, se existir.
    fun bestMatch(tracks: List<ITunesTrack>, artist: String): ITunesTrack? {
        val mainArtist = artist.split(" ft. ", " & ").first()
        return tracks.firstOrNull { isByArtist(it, mainArtist) && !isAlternateVersion(it) }
            ?: tracks.firstOrNull { !isAlternateVersion(it) }
            ?: tracks.firstOrNull()
    }

    private fun isByArtist(track: ITunesTrack, artist: String): Boolean =
        track.artistName?.lowercase()?.contains(artist.trim().lowercase()) == true

    // Só olha o complemento do nome, como em "Dynamite (Live)" ou "Creep - Acoustic",
    // para não descartar músicas como "Alive".
    private fun isAlternateVersion(track: ITunesTrack): Boolean {
        val name = track.trackName ?: return true
        val extras = EXTRAS.findAll(name).joinToString(" ") { it.value }.lowercase()
        return VERSION_MARKERS.any { extras.contains(it) }
    }
}
