package com.example.soundmatch.logic

import com.example.soundmatch.model.ArtistSong
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.ITunesTrack

// Escolhe o que interessa entre os resultados de uma busca no iTunes,
// que misturam versões ao vivo, remixes e músicas de outros artistas.
object TrackPicker {

    private val VERSION_MARKERS = listOf(
        "live", "ao vivo", "remix", "acoustic", "acústico", "instrumental",
        "version", "versão", "sped up", "slowed", "karaoke"
    )

    private val EXTRAS = Regex("""[(\[].*?[)\]]| - .*$""")

    // "Boy With Luv (feat. Halsey)" vira "Boy With Luv".
    private val FEAT = Regex("""\s*[(\[](feat\.|ft\.|with ).*?[)\]]""", RegexOption.IGNORE_CASE)

    // As músicas do artista para a 1ª pergunta: só as dele, sem versões e sem repetir.
    fun artistSongs(tracks: List<ITunesTrack>, artist: String, count: Int = 4): List<ArtistSong> =
        tracks
            .filter { isByArtist(it, artist) && !isAlternateVersion(it) }
            .mapNotNull { track ->
                track.trackName?.let { ArtistSong(cleanTitle(it), track.primaryGenreName?.let(Genre::fromITunes)) }
            }
            .distinctBy { it.title.lowercase() }
            .take(count)

    // A faixa certa para a capa: do artista certo e na versão de estúdio, se existir.
    fun bestMatch(tracks: List<ITunesTrack>, artist: String): ITunesTrack? {
        val mainArtist = artist.split(" ft. ", " & ").first()
        return tracks.firstOrNull { isByArtist(it, mainArtist) && !isAlternateVersion(it) }
            ?: tracks.firstOrNull { !isAlternateVersion(it) }
            ?: tracks.firstOrNull()
    }

    fun cleanTitle(title: String): String = title.replace(FEAT, "").trim()

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
