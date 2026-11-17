package com.example.soundmatch.logic

import com.example.soundmatch.model.ITunesTrack
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackPickerTest {

    private fun track(name: String, artist: String, genre: String = "K-pop") =
        ITunesTrack(name, artist, null, genre, null)

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
