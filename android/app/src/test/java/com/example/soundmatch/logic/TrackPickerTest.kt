package com.example.soundmatch.logic

import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.ITunesTrack
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackPickerTest {

    private fun track(name: String, artist: String, genre: String = "K-pop") =
        ITunesTrack(name, artist, null, genre, null)

    @Test
    fun `musicas do artista sem versoes, sem outros artistas e sem repetir`() {
        val results = listOf(
            track("Dynamite", "BTS"),
            track("Dynamite (Live)", "BTS"),
            track("BTS (Brazil Trap Star)", "YUNG LIXO", "Hip-Hop/Rap"),
            track("Boy With Luv (feat. Halsey)", "BTS"),
            track("Dynamite", "BTS"),
            track("Alive", "BTS", "Pop")
        )
        val songs = TrackPicker.artistSongs(results, "BTS")
        assertEquals(listOf("Dynamite", "Boy With Luv", "Alive"), songs.map { it.title })
        assertEquals(Genre.POP, songs.first().genre)
    }

    @Test
    fun `no maximo 4 musicas`() {
        val results = (1..10).map { track("Música $it", "BTS") }
        assertEquals(4, TrackPicker.artistSongs(results, "BTS").size)
    }

    @Test
    fun `capa vem do artista certo e da versao de estudio`() {
        val results = listOf(
            track("Riders On the Storm (feat. The Doors) [Fredwreck Remix]", "Snoop Dogg"),
            track("Riders on the Storm", "The Doors")
        )
        assertEquals("The Doors", TrackPicker.bestMatch(results, "The Doors")?.artistName)
    }

    @Test
    fun `parceria no catalogo procura pelo artista principal`() {
        val results = listOf(
            track("Uptown Funk (Live)", "Mark Ronson"),
            track("Uptown Funk (feat. Bruno Mars)", "Mark Ronson")
        )
        assertEquals(
            "Uptown Funk (feat. Bruno Mars)",
            TrackPicker.bestMatch(results, "Mark Ronson ft. Bruno Mars")?.trackName
        )
    }
}
