package com.example.soundmatch.logic

import com.example.soundmatch.model.Archetype
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Subgenre
import com.example.soundmatch.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundMatchEngineTest {

    private val profile = UserProfile(
        name = "Ana",
        favoriteArtist = "Arctic Monkeys",
        listeningMode = "Fone de Ouvido",
        playsInstrument = false
    )

    @Test
    fun `respostas mais raiz geram o score minimo`() {
        val answers = QuizAnswers(playlist = 0, concert = 0, longTrack = 0, subgenres = listOf(Subgenre.BLUES_ROCK))
        assertEquals(10, SoundMatchEngine.calculateScore(answers))
    }

    @Test
    fun `respostas mais experimentais geram o score maximo`() {
        val answers = QuizAnswers(playlist = 3, concert = 3, longTrack = 2, subgenres = listOf(Subgenre.PROG_ROCK))
        assertEquals(100, SoundMatchEngine.calculateScore(answers))
    }

    @Test
    fun `quem dança e curte groove cai no perfil Funk`() {
        // 2 (groove) + 2 (dançando) + 1,5 (dá uma chance) + 1,5 (média Hard=1, Funk=2) = 7 -> 10 + 7/12 * 90 = 62,5 -> 63
        val answers = QuizAnswers(2, 2, 1, listOf(Subgenre.HARD_ROCK, Subgenre.FUNK_ALTERNATIVE))
        val score = SoundMatchEngine.calculateScore(answers)
        assertEquals(63, score)
        assertEquals(Archetype.GROOVE_FUNK, Archetype.fromScore(score))
    }

    @Test
    fun `sem subgenero marcado usa peso neutro`() {
        // 0 + 0 + 0 + 1,5 = 1,5 -> 10 + 1,5/12 * 90 = 21,25 -> 21
        assertEquals(21, SoundMatchEngine.calculateScore(QuizAnswers(0, 0, 0, emptyList())))
    }

    @Test
    fun `limites das faixas de score`() {
        assertEquals(Archetype.VINTAGE_BLUES, Archetype.fromScore(35))
        assertEquals(Archetype.HARD_ROCK, Archetype.fromScore(36))
        assertEquals(Archetype.HARD_ROCK, Archetype.fromScore(60))
        assertEquals(Archetype.GROOVE_FUNK, Archetype.fromScore(61))
        assertEquals(Archetype.GROOVE_FUNK, Archetype.fromScore(80))
        assertEquals(Archetype.PROG_EXPLORER, Archetype.fromScore(81))
        assertEquals(Archetype.VINTAGE_BLUES, Archetype.fromScore(-5))
        assertEquals(Archetype.PROG_EXPLORER, Archetype.fromScore(150))
    }

    @Test
    fun `nunca recomenda o artista favorito do usuario`() {
        val fan = profile.copy(favoriteArtist = "red hot chili peppers")
        for (score in 61..80) {
            val song = SoundMatchEngine.pickSong(Archetype.GROOVE_FUNK, fan, emptyList(), score)
            assertNotEquals("Red Hot Chili Peppers", song.artist)
        }
    }

    @Test
    fun `prioriza musica com o subgenero marcado`() {
        // No perfil Hard Rock, só "Smoke on the Water" também é Prog Rock.
        val song = SoundMatchEngine.pickSong(Archetype.HARD_ROCK, profile, listOf(Subgenre.PROG_ROCK), 50)
        assertEquals("Smoke on the Water", song.title)
    }

    @Test
    fun `mesmas respostas geram sempre a mesma recomendacao`() {
        val answers = QuizAnswers(1, 1, 1, listOf(Subgenre.HARD_ROCK))
        assertEquals(SoundMatchEngine.recommend(profile, answers), SoundMatchEngine.recommend(profile, answers))
    }

    @Test
    fun `explicacao menciona o instrumento so para quem toca`() {
        val answers = QuizAnswers(2, 2, 1, listOf(Subgenre.FUNK_ALTERNATIVE))
        val musician = SoundMatchEngine.recommend(profile.copy(playsInstrument = true), answers)
        val listener = SoundMatchEngine.recommend(profile, answers)

        assertTrue(musician.reason.contains("toca um instrumento"))
        assertFalse(listener.reason.contains("toca um instrumento"))
        assertTrue(listener.reason.contains("Arctic Monkeys"))
    }
}
