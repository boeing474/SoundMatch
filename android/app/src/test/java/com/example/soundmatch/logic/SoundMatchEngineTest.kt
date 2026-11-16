package com.example.soundmatch.logic

import com.example.soundmatch.model.Energy
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
        situation = Situation.PARTY,
        instrument = null,
        artistGenre = Genre.POP
    )

    private fun answers(
        firstPick: Genre? = Genre.POP,
        firstPickSong: String? = null,
        energy: Energy = Energy.DANCE,
        liked: List<Genre> = emptyList(),
        surprise: Boolean = false
    ) = QuizAnswers(firstPick, firstPickSong, energy, liked, surprise)

    // Votação

    @Test
    fun `fa de BTS que escolhe uma musica pop cai em Pop com 100 por cento`() {
        val result = SoundMatchEngine.recommend(btsFan, answers())
        assertEquals(Genre.POP, result.genre)
        assertEquals(100, result.match)
        assertFalse(result.surprise)
    }

    @Test
    fun `artista vale 3 votos, a 1a pergunta 1 e cada estilo marcado 1`() {
        val votes = SoundMatchEngine.countVotes(btsFan, answers(firstPick = Genre.ROCK, liked = listOf(Genre.ROCK, Genre.HIPHOP)))
        assertEquals(3, votes[Genre.POP])
        assertEquals(2, votes[Genre.ROCK])
        assertEquals(1, votes[Genre.HIPHOP])
    }

    @Test
    fun `marcar outros estilos diminui o match`() {
        // Pop: 3 (artista) + 1 (1ª pergunta) = 4. Rock e Eletrônica: 1 cada. 4 de 6 = 67%.
        val result = SoundMatchEngine.recommend(btsFan, answers(liked = listOf(Genre.ROCK, Genre.ELECTRONIC)))
        assertEquals(Genre.POP, result.genre)
        assertEquals(67, result.match)
    }

    @Test
    fun `no empate sem artista ganha o genero da 1a pergunta`() {
        // Sem internet: Brasileira 1 (1ª pergunta) x Rock 1 (marcado).
        val offline = btsFan.copy(artistGenre = null)
        val result = SoundMatchEngine.recommend(offline, answers(firstPick = Genre.BRAZILIAN, liked = listOf(Genre.ROCK)))
        assertEquals(Genre.BRAZILIAN, result.genre)
        assertEquals(50, result.match)
    }

    @Test
    fun `match nunca fica abaixo de 10`() {
        val votes = Genre.entries.associateWith { 0 }
        assertEquals(10, SoundMatchEngine.matchPercent(votes, Genre.POP))
    }

    // Me surpreenda

    @Test
    fun `surpresa pega o estilo marcado mais votado fora o de sempre`() {
        // Pop 3 (artista). Rock: 1ª pergunta + marcado = 2. Eletrônica: marcado = 1.
        val result = SoundMatchEngine.recommend(
            btsFan,
            answers(firstPick = Genre.ROCK, liked = listOf(Genre.ROCK, Genre.ELECTRONIC), surprise = true)
        )
        assertEquals(Genre.ROCK, result.genre)
        assertTrue(result.surprise)
    }

    @Test
    fun `surpresa sem outro estilo marcado fica no estilo de sempre`() {
        val result = SoundMatchEngine.recommend(btsFan, answers(liked = listOf(Genre.POP), surprise = true))
        assertEquals(Genre.POP, result.genre)
        assertFalse(result.surprise)
    }

    // Escolha da música

    @Test
    fun `a musica sempre tem a energia pedida`() {
        Genre.entries.forEach { genre ->
            Energy.entries.forEach { energy ->
                assertEquals(energy, SoundMatchEngine.pickSong(genre, energy, btsFan).energy)
            }
        }
    }

    @Test
    fun `nunca recomenda o artista favorito`() {
        // Queen seria a música de baixo do Rock dançante, mas é o artista favorito: sai a outra.
        val queenFan = btsFan.copy(favoriteArtist = "queen", artistGenre = Genre.ROCK, instrument = Instrument.BASS)
        val song = SoundMatchEngine.pickSong(Genre.ROCK, Energy.DANCE, queenFan)
        assertEquals("Take Me Out", song.title)
    }

    @Test
    fun `quem toca recebe a musica em que seu instrumento se destaca`() {
        val bassist = btsFan.copy(instrument = Instrument.BASS)
        assertEquals("Espresso", SoundMatchEngine.pickSong(Genre.POP, Energy.DANCE, bassist).title)

        val singer = btsFan.copy(instrument = Instrument.VOCALS)
        assertEquals("Shake It Off", SoundMatchEngine.pickSong(Genre.POP, Energy.SINGALONG, singer).title)
    }

    @Test
    fun `a mesma pessoa sempre recebe a mesma musica`() {
        val first = SoundMatchEngine.pickSong(Genre.HIPHOP, Energy.CHILL, btsFan)
        val second = SoundMatchEngine.pickSong(Genre.HIPHOP, Energy.CHILL, btsFan)
        assertEquals(first, second)
    }

    // Explicação

    @Test
    fun `explicacao cita nome, artista, musica escolhida, energia e instrumento`() {
        val bassist = btsFan.copy(instrument = Instrument.BASS)
        val reason = SoundMatchEngine.recommend(bassist, answers(firstPickSong = "Dynamite")).reason
        assertTrue(reason.contains("Ana"))
        assertTrue(reason.contains("BTS"))
        assertTrue(reason.contains("Dynamite pra tocar numa festa com amigos"))
        assertTrue(reason.contains("uma música dançante"))
        assertTrue(reason.contains("Você toca baixo"))
    }

    @Test
    fun `explicacao da surpresa cita o estilo de sempre e o novo`() {
        val reason = SoundMatchEngine.recommend(
            btsFan,
            answers(liked = listOf(Genre.ELECTRONIC), surprise = true)
        ).reason
        assertTrue(reason.contains("em vez de Pop & K-pop"))
        assertTrue(reason.contains("Eletrônica"))
    }

    // Catálogo e iTunes

    @Test
    fun `cada genero tem 2 musicas de cada energia e todos os instrumentos`() {
        Genre.entries.forEach { genre ->
            val songs = SongCatalog.songsFor(genre)
            Energy.entries.forEach { energy ->
                assertEquals("$genre $energy", 2, songs.count { it.energy == energy })
            }
            assertEquals("$genre", Instrument.entries.toSet(), songs.map { it.featured }.toSet())
        }
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
