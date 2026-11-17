package com.example.soundmatch.logic

import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Situation
import com.example.soundmatch.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// O motor reserva: votação por estilo + catálogo fixo.
class SoundMatchEngineTest {

    private val btsFan = UserProfile(
        name = "Ana",
        favoriteArtist = "BTS",
        situation = Situation.PARTY,
        artistGenre = Genre.POP
    )

    private fun answers(
        energy: Energy = Energy.DANCE,
        liked: List<Genre> = emptyList(),
        surprise: Boolean = false
    ) = QuizAnswers(energy, liked, surprise)

    // Votação

    @Test
    fun `fa de BTS sem outros estilos cai em Pop com 100 por cento`() {
        val result = SoundMatchEngine.recommend(btsFan, answers())
        assertEquals(Genre.POP, result.genre)
        assertEquals(100, result.match)
        assertFalse(result.surprise)
        assertFalse(result.fromSimilarArtists)
    }

    @Test
    fun `artista vale 3 votos e cada estilo marcado 1`() {
        val votes = SoundMatchEngine.countVotes(btsFan, answers(liked = listOf(Genre.ROCK, Genre.HIPHOP)))
        assertEquals(3, votes[Genre.POP])
        assertEquals(1, votes[Genre.ROCK])
        assertEquals(1, votes[Genre.HIPHOP])
    }

    @Test
    fun `marcar outros estilos diminui o match`() {
        // Pop: 3 (artista). Rock e Eletrônica: 1 cada. 3 de 5 = 60%.
        val result = SoundMatchEngine.recommend(btsFan, answers(liked = listOf(Genre.ROCK, Genre.ELECTRONIC)))
        assertEquals(Genre.POP, result.genre)
        assertEquals(60, result.match)
    }

    @Test
    fun `no empate sem artista ganha o primeiro na ordem dos generos`() {
        // Artista não encontrado: Brasileira 1 x Rock 1. Rock vem antes no enum.
        val offline = btsFan.copy(artistGenre = null)
        val result = SoundMatchEngine.recommend(offline, answers(liked = listOf(Genre.BRAZILIAN, Genre.ROCK)))
        assertEquals(Genre.ROCK, result.genre)
        assertEquals(50, result.match)
    }

    @Test
    fun `match nunca fica abaixo de 10`() {
        val votes = Genre.entries.associateWith { 0 }
        assertEquals(10, SoundMatchEngine.matchPercent(votes, Genre.POP))
    }

    // Quero me arriscar

    @Test
    fun `quem se arrisca sai do estilo de sempre para um dos marcados`() {
        val result = SoundMatchEngine.recommend(btsFan, answers(liked = listOf(Genre.ROCK), surprise = true))
        assertEquals(Genre.ROCK, result.genre)
        assertTrue(result.surprise)
    }

    @Test
    fun `sem outro estilo marcado fica no estilo de sempre`() {
        val result = SoundMatchEngine.recommend(btsFan, answers(liked = listOf(Genre.POP), surprise = true))
        assertEquals(Genre.POP, result.genre)
        assertFalse(result.surprise)
    }

    // Escolha da música

    @Test
    fun `a musica sempre tem o clima pedido`() {
        Genre.entries.forEach { genre ->
            Energy.entries.forEach { energy ->
                assertEquals(energy, SoundMatchEngine.pickSong(genre, energy, btsFan).energy)
            }
        }
    }

    @Test
    fun `nunca recomenda o artista favorito`() {
        // O Rock dançante tem uma do Queen e Take Me Out: para fã de Queen, só sobra a segunda.
        val queenFan = btsFan.copy(favoriteArtist = "queen", artistGenre = Genre.ROCK)
        val song = SoundMatchEngine.pickSong(Genre.ROCK, Energy.DANCE, queenFan)
        assertEquals("Take Me Out", song.title)
    }

    @Test
    fun `a mesma pessoa sempre recebe a mesma musica`() {
        val first = SoundMatchEngine.pickSong(Genre.HIPHOP, Energy.CHILL, btsFan)
        val second = SoundMatchEngine.pickSong(Genre.HIPHOP, Energy.CHILL, btsFan)
        assertEquals(first, second)
    }

    // Explicação

    @Test
    fun `explicacao cita nome, artista, clima e situacao`() {
        val reason = SoundMatchEngine.recommend(btsFan, answers()).reason
        assertTrue(reason.contains("Ana, você ouve BTS"))
        assertTrue(reason.contains("uma música dançante pra ouvir numa festa com amigos"))
    }

    @Test
    fun `explicacao de quem se arrisca cita o estilo de sempre e o novo`() {
        val reason = SoundMatchEngine.recommend(
            btsFan,
            answers(liked = listOf(Genre.ELECTRONIC), surprise = true)
        ).reason
        assertTrue(reason.contains("em vez de Pop & K-pop"))
        assertTrue(reason.contains("Eletrônica"))
    }

    // Catálogo e iTunes

    @Test
    fun `cada genero tem 2 musicas de cada energia`() {
        Genre.entries.forEach { genre ->
            val songs = SongCatalog.songsFor(genre)
            Energy.entries.forEach { energy ->
                assertEquals("$genre $energy", 2, songs.count { it.energy == energy })
            }
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
