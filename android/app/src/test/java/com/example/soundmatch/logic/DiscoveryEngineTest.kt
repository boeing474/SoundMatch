package com.example.soundmatch.logic

import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.Candidate
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerTrack
import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Popularity
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Situation
import com.example.soundmatch.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoveryEngineTest {

    private val xand = DeezerArtist(1, "Xand Avião", null, null)
    private val nattan = DeezerArtist(2, "NATTAN", null, null)

    private val fan = UserProfile(
        name = "Ana",
        favoriteArtist = "Wesley Safadão",
        situation = Situation.STREET,
        artistGenre = Genre.BRAZILIAN,
        relatedArtists = listOf(xand, nattan)
    )

    private fun features(energy: Double = 0.5, valence: Double = 0.5, dance: Double = 0.5, acoustic: Double = 0.5) =
        AudioFeatures("ISRC", energy, valence, dance, acoustic)

    private fun candidate(
        title: String,
        rank: Int,
        features: AudioFeatures? = features(),
        artist: String = "Zé Vaqueiro",
        duration: Int = 200
    ) = Candidate(DeezerTrack(rank.toLong(), title, duration, rank, "ISRC", DeezerArtist(9, artist, null, null), null), features)

    private fun answers(
        energy: Energy = Energy.HEAVY,
        popularity: Popularity = Popularity.HIT,
        known: List<DeezerArtist> = emptyList(),
        risk: Boolean = false
    ) = QuizAnswers(energy = energy, surprise = risk, knownArtists = known, popularity = popularity)

    // Duas mais tocadas e duas menos tocadas, uma pesada e uma calma em cada metade.
    private val fourSongs = listOf(
        candidate("Hit calmo", rank = 900, features = features(energy = 0.3, acoustic = 0.8)),
        candidate("Hit pesado", rank = 800, features = features(energy = 0.95, acoustic = 0.05)),
        candidate("Pérola pesada", rank = 200, features = features(energy = 0.9, acoustic = 0.1)),
        candidate("Pérola calma", rank = 100, features = features(energy = 0.2, acoustic = 0.9))
    )

    // Clima e popularidade

    @Test
    fun `entre os hits ganha a musica mais perto do clima pedido`() {
        val result = DiscoveryEngine.recommend(fan, answers(Energy.HEAVY, Popularity.HIT), fourSongs)!!
        assertEquals("Hit pesado", result.song.title)
        assertEquals("Zé Vaqueiro", result.song.artist)
        assertEquals(100, result.match)
        assertTrue(result.fromSimilarArtists)
        assertFalse(result.surprise)
    }

    @Test
    fun `perola escolhe na metade menos tocada`() {
        val result = DiscoveryEngine.recommend(fan, answers(Energy.HEAVY, Popularity.GEM), fourSongs)!!
        assertEquals("Pérola pesada", result.song.title)
    }

    @Test
    fun `match e a distancia media ate o alvo do clima`() {
        // Sofrência só olha a alegria (alvo 0.15). Com 0.65, a distância é 0.5: 50%.
        val sad = DiscoveryEngine.targetFor(Energy.HEARTBREAK)
        val song = candidate("Meio alegre", rank = 500, features = features(valence = 0.65))
        val closeness = DiscoveryEngine.closeness(song.track, song.features!!, sad)
        assertEquals(50, DiscoveryEngine.matchPercent(closeness))
    }

    @Test
    fun `match nunca fica abaixo de 10`() {
        assertEquals(10, DiscoveryEngine.matchPercent(0.02))
    }

    @Test
    fun `viajada da mais pontos para musica longa`() {
        val trippy = DiscoveryEngine.targetFor(Energy.TRIPPY)
        val short = candidate("Curta", rank = 500, duration = 180)
        val long = candidate("Longa", rank = 500, duration = 400)
        assertTrue(
            DiscoveryEngine.closeness(long.track, long.features!!, trippy) >
                DiscoveryEngine.closeness(short.track, short.features!!, trippy)
        )
    }

    @Test
    fun `a situacao desempata musicas igualmente perto do clima`() {
        // As duas ficam a 0.1 da energia que o Dançante pede (0.8), uma para cada lado.
        val songs = listOf(
            candidate("Mais calma", rank = 900, features = features(energy = 0.7, dance = 0.85)),
            candidate("Mais agitada", rank = 800, features = features(energy = 0.9, dance = 0.85)),
            candidate("Outra", rank = 100),
            candidate("Mais uma", rank = 50)
        )
        val dance = answers(Energy.DANCE)
        val atGym = DiscoveryEngine.recommend(fan.copy(situation = Situation.GYM), dance, songs)!!
        val atHome = DiscoveryEngine.recommend(fan.copy(situation = Situation.RELAXING), dance, songs)!!
        assertEquals("Mais agitada", atGym.song.title)
        assertEquals("Mais calma", atHome.song.title)
    }

    // Quem entra na disputa

    @Test
    fun `nao recomenda o artista favorito nem os que a pessoa ja conhece`() {
        val songs = listOf(
            candidate("Do favorito", rank = 900, artist = "wesley safadão"),
            candidate("Do conhecido", rank = 800, artist = "Xand Avião"),
            candidate("Novidade", rank = 700, artist = "NATTAN")
        )
        val fresh = DiscoveryEngine.newToUser(songs, fan, answers(known = listOf(xand)))
        assertEquals(listOf("Novidade"), fresh.map { it.track.title })
    }

    @Test
    fun `a mesma musica so entra uma vez`() {
        val songs = listOf(candidate("5 da Manhã", rank = 900), candidate("5 Da Manhã", rank = 850))
        assertEquals(1, DiscoveryEngine.newToUser(songs, fan, answers()).size)
    }

    @Test
    fun `musica sem os numeros do som fica de fora`() {
        val songs = listOf(
            candidate("Sem dados", rank = 900, features = null),
            candidate("Com dados", rank = 100)
        )
        assertEquals("Com dados", DiscoveryEngine.recommend(fan, answers(), songs)!!.song.title)
    }

    @Test
    fun `sem nenhuma musica medida devolve null para a tela usar a reserva`() {
        assertNull(DiscoveryEngine.recommend(fan, answers(), emptyList()))
        assertNull(DiscoveryEngine.recommend(fan, answers(), listOf(candidate("Sem dados", 900, features = null))))
    }

    // Explicação

    @Test
    fun `explicacao cita nome, favorito, artista novo, clima, situacao e popularidade`() {
        val reason = DiscoveryEngine.recommend(fan, answers(Energy.HEAVY, Popularity.GEM), fourSongs)!!.reason
        assertTrue(reason.contains("Ana, você ouve Wesley Safadão e ainda não conhece Zé Vaqueiro"))
        assertTrue(reason.contains("uma música porradeira pra ouvir na rua, entre as menos conhecidas"))
        assertTrue(reason.contains("Pérola pesada foi a que chegou mais perto"))
    }

    @Test
    fun `explicacao muda quando a busca vai um passo alem`() {
        val risk = DiscoveryEngine.recommend(fan, answers(risk = true), fourSongs)!!.reason
        assertTrue(risk.contains("você quis se arriscar"))

        val knowsAll = DiscoveryEngine.recommend(fan, answers(known = listOf(xand, nattan)), fourSongs)!!.reason
        assertTrue(knowsAll.contains("você já conhece os parecidos com Wesley Safadão"))
    }
}
