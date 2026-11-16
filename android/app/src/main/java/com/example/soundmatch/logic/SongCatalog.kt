package com.example.soundmatch.logic

import com.example.soundmatch.model.Archetype
import com.example.soundmatch.model.Song
import com.example.soundmatch.model.Subgenre.BLUES_ROCK
import com.example.soundmatch.model.Subgenre.FUNK_ALTERNATIVE
import com.example.soundmatch.model.Subgenre.HARD_ROCK
import com.example.soundmatch.model.Subgenre.PROG_ROCK

/**
 * Catálogo de músicas por Arquétipo Sonoro.
 * Cada música tem os subgêneros a que pertence, usados para personalizar a escolha.
 */
object SongCatalog {

    private val songs: Map<Archetype, List<Song>> = mapOf(
        Archetype.VINTAGE_BLUES to listOf(
            Song("Tumbling Dice", "The Rolling Stones", listOf(BLUES_ROCK)),
            Song("Pride and Joy", "Stevie Ray Vaughan", listOf(BLUES_ROCK)),
            Song("La Grange", "ZZ Top", listOf(BLUES_ROCK, HARD_ROCK)),
            Song("Sunshine of Your Love", "Cream", listOf(BLUES_ROCK, PROG_ROCK)),
            Song("Seven Nation Army", "The White Stripes", listOf(BLUES_ROCK, FUNK_ALTERNATIVE))
        ),
        Archetype.HARD_ROCK to listOf(
            Song("Black Dog", "Led Zeppelin", listOf(HARD_ROCK, BLUES_ROCK)),
            Song("Highway to Hell", "AC/DC", listOf(HARD_ROCK)),
            Song("Paranoid", "Black Sabbath", listOf(HARD_ROCK)),
            Song("Smoke on the Water", "Deep Purple", listOf(HARD_ROCK, PROG_ROCK)),
            Song("Sweet Child O' Mine", "Guns N' Roses", listOf(HARD_ROCK))
        ),
        Archetype.GROOVE_FUNK to listOf(
            Song("Give It Away", "Red Hot Chili Peppers", listOf(FUNK_ALTERNATIVE)),
            Song("Superstition", "Stevie Wonder", listOf(FUNK_ALTERNATIVE)),
            Song("Another One Bites the Dust", "Queen", listOf(FUNK_ALTERNATIVE, HARD_ROCK)),
            Song("Feel Good Inc.", "Gorillaz", listOf(FUNK_ALTERNATIVE)),
            Song("Take Me Out", "Franz Ferdinand", listOf(FUNK_ALTERNATIVE, BLUES_ROCK))
        ),
        Archetype.PROG_EXPLORER to listOf(
            Song("Time", "Pink Floyd", listOf(PROG_ROCK)),
            Song("Roundabout", "Yes", listOf(PROG_ROCK)),
            Song("Tom Sawyer", "Rush", listOf(PROG_ROCK, HARD_ROCK)),
            Song("Paranoid Android", "Radiohead", listOf(PROG_ROCK, FUNK_ALTERNATIVE)),
            Song("21st Century Schizoid Man", "King Crimson", listOf(PROG_ROCK, HARD_ROCK))
        )
    )

    fun songsFor(archetype: Archetype): List<Song> = songs.getValue(archetype)
}
