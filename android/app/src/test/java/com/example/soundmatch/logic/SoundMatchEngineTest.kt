package com.example.soundmatch.logic

import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Instrument
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Situation
import com.example.soundmatch.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundMatchEngineTest {

    private val btsFan = UserProfile(
        name = "Ana",
        favoriteArtist = "BTS",
        situation = Situation.GYM,
        instrument = null,
        artistGenre = Genre.POP
    )

    private fun answers(vararg picks: Genre, liked: List<Genre> = emptyList()) =
        QuizAnswers(picks[0], picks[1], picks[2], liked)

    @Test
    fun `fa de BTS que responde pop cai em Pop com 100 por cento`() {
        val result = SoundMatchEngine.recommend(btsFan, answers(Genre.POP, Genre.POP, Genre.POP))
        assertEquals(Genre.POP, result.genre)
        assertEquals(100, result.match)
    }

    @Test
    fun `artista favorito vale 3 votos`() {
        val votes = SoundMatchEngine.countVotes(btsFan, answers(Genre.ROCK, Genre.ROCK, Genre.HIPHOP))
        assertEquals(3, votes[Genre.POP])
        assertEquals(2, votes[Genre.ROCK])
        assertEquals(1, votes[Genre.HIPHOP])
    }

    @Test
    fun `respostas e estilos marcados podem vencer o artista favorito`() {
        // Pop: 3 (artista). Rock: 3 respostas + 1 estilo marcado = 4.
        val result = SoundMatchEngine.recommend(
            btsFan,
            answers(Genre.ROCK, Genre.ROCK, Genre.ROCK, liked = listOf(Genre.ROCK))
        )
        assertEquals(Genre.ROCK, result.genre)
        assertEquals(57, result.match) // 4 de 7 votos
    }

    @Test
    fun `no empate ganha o genero do artista favorito`() {
        // Pop: 3 (artista). Eletrônica: 3 respostas. Empate 3 x 3.
        val result = SoundMatchEngine.recommend(btsFan, answers(Genre.ELECTRONIC, Genre.ELECTRONIC, Genre.ELECTRONIC))
        assertEquals(Genre.POP, result.genre)
        assertEquals(50, result.match)
    }

    @Test
    fun `sem o genero do artista vale so o quiz`() {
        val offline = btsFan.copy(artistGenre = null)
        val result = SoundMatchEngine.recommend(offline, answers(Genre.BRAZILIAN, Genre.BRAZILIAN, Genre.POP))
        assertEquals(Genre.BRAZILIAN, result.genre)
        assertEquals(67, result.match) // 2 de 3 votos
    }

    @Test
    fun `match nunca fica abaixo de 10`() {
        val votes = Genre.entries.associateWith { 0 }
        assertEquals(10, SoundMatchEngine.matchPercent(votes, Genre.POP))
    }

    @Test
    fun `nunca recomenda o artista favorito`() {
        val queenFan = btsFan.copy(favoriteArtist = "queen", artistGenre = Genre.ROCK, instrument = Instrument.BASS)
        val song = SoundMatchEngine.pickSong(Genre.ROCK, queenFan)
        // Queen seria a música de baixo do Rock, mas é o artista favorito: sai outra.
        assertFalse(song.artist.contains("Queen"))
    }

    @Test
    fun `quem toca recebe a musica em que seu instrumento se destaca`() {
        val bassist = btsFan.copy(instrument = Instrument.BASS)
        assertEquals("Espresso", SoundMatchEngine.pickSong(Genre.POP, bassist).title)

        val singer = btsFan.copy(instrument = Instrument.VOCALS)
        assertEquals("Shake It Off", SoundMatchEngine.pickSong(Genre.POP, singer).title)
    }

    @Test
    fun `a mesma pessoa sempre recebe a mesma musica`() {
        val first = SoundMatchEngine.pickSong(Genre.HIPHOP, btsFan)
        val second = SoundMatchEngine.pickSong(Genre.HIPHOP, btsFan)
        assertEquals(first, second)
    }

    @Test
    fun `explicacao cita nome, artista e instrumento`() {
        val bassist = btsFan.copy(instrument = Instrument.BASS)
        val result = SoundMatchEngine.recommend(bassist, answers(Genre.POP, Genre.POP, Genre.POP))
        assertTrue(result.reason.contains("Ana"))
        assertTrue(result.reason.contains("BTS"))
        assertTrue(result.reason.contains("Você toca baixo"))
    }

    @Test
    fun `genero do iTunes vira o perfil certo`() {
        assertEquals(Genre.POP, Genre.fromITunes("K-pop"))
        assertEquals(Genre.ROCK, Genre.fromITunes("Metal"))
        assertEquals(Genre.HIPHOP, Genre.fromITunes("Hip-Hop/Rap"))
        assertEquals(Genre.ELECTRONIC, Genre.fromITunes("Dance"))
        assertEquals(Genre.BRAZILIAN, Genre.fromITunes("Sertanejo"))
        assertEquals(Genre.BRAZILIAN, Genre.fromITunes("Pop Brasileiro"))
        assertNull(Genre.fromITunes("Reggae"))
    }

    @Test
    fun `nome do artista vem com a grafia oficial`() {
        assertEquals("BTS", ArtistName.clean("bts", "BTS"))
        assertEquals("LUDMILLA", ArtistName.clean("ludmilla", "LUDMILLA & Caio Luccas"))
        assertEquals("Jorge & Mateus", ArtistName.clean("jorge e mateus", "Jorge & Mateus"))
    }
}
