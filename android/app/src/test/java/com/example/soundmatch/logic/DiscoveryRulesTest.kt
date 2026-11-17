package com.example.soundmatch.logic

import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerTrack
import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.QuizAnswers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoveryRulesTest {

    private fun artist(id: Long, name: String, fans: Int?) = DeezerArtist(id, name, null, fans)

    private fun track(id: Long, isrc: String?) =
        DeezerTrack(id, "Música $id", 200, 500_000, isrc, artist(1, "Artista", null), null)

    private fun features(isrc: String?, energy: Double) = AudioFeatures(isrc, energy, 0.5, 0.5, 0.5)

    @Test
    fun `entre artistas com o mesmo nome, fica o de mais fas`() {
        val results = listOf(
            artist(67158302, "BTS", 4_328),
            artist(6982223, "BTS", 3_878_290),
            artist(268175432, "Bts", null)
        )
        assertEquals(6982223L, DiscoveryRules.pickArtist("bts", results)?.id)
    }

    @Test
    fun `acento e maiuscula nao atrapalham a comparacao do nome`() {
        val results = listOf(artist(1, "Wesley Safadão Cover", 900_000), artist(2, "Wesley Safadão", 500_000))
        assertEquals(2L, DiscoveryRules.pickArtist("wesley safadao", results)?.id)
    }

    @Test
    fun `sem nome igual, vale o primeiro resultado do Deezer`() {
        val results = listOf(artist(1, "Racionais MC's", 800_000), artist(2, "Racionais Tributo", 2_000_000))
        assertEquals(1L, DiscoveryRules.pickArtist("racionais", results)?.id)
    }

    @Test
    fun `busca vazia nao acha artista`() {
        assertNull(DiscoveryRules.pickArtist("bts", emptyList()))
    }

    @Test
    fun `artistas novos nao incluem o favorito, os conhecidos nem repetidos`() {
        val xand = artist(1, "Xand Avião", null)
        val found = listOf(xand, artist(2, "NATTAN", null), artist(3, "Wesley Safadão", null), artist(2, "NATTAN", null))
        val fresh = DiscoveryRules.newArtists(found, "wesley safadao", known = listOf(xand))
        assertEquals(listOf(2L), fresh.map { it.id })
    }

    @Test
    fun `a busca vai um passo alem para quem se arrisca ou ja conhece todos`() {
        val related = listOf(artist(1, "Xand Avião", null), artist(2, "NATTAN", null))
        assertFalse(DiscoveryRules.goesFarther(related, QuizAnswers(Energy.DANCE, knownArtists = related.take(1))))
        assertTrue(DiscoveryRules.goesFarther(related, QuizAnswers(Energy.DANCE, knownArtists = related)))
        assertTrue(DiscoveryRules.goesFarther(related, QuizAnswers(Energy.DANCE, surprise = true)))
    }

    @Test
    fun `liga cada musica as suas caracteristicas pelo ISRC`() {
        val tracks = listOf(track(1, "BRAAA0000001"), track(2, "usbbb0000002"))
        val found = listOf(features("USBBB0000002", 0.9), features("BRAAA0000001", 0.2))

        val candidates = DiscoveryRules.attachFeatures(tracks, found)

        assertEquals(0.2, candidates[0].features!!.energy, 0.0)
        assertEquals(0.9, candidates[1].features!!.energy, 0.0)   // maiúscula ou minúscula, tanto faz
    }

    @Test
    fun `varias medicoes da mesma gravacao viram a media`() {
        val found = listOf(features("BRAAA0000001", 0.6), features("BRAAA0000001", 0.8))
        val candidate = DiscoveryRules.attachFeatures(listOf(track(1, "BRAAA0000001")), found).single()
        assertEquals(0.7, candidate.features!!.energy, 0.0001)
    }

    @Test
    fun `musica sem ISRC ou desconhecida continua, mas sem caracteristicas`() {
        val tracks = listOf(track(1, null), track(2, "BRCCC0000003"))
        val candidates = DiscoveryRules.attachFeatures(tracks, listOf(features(null, 0.7)))

        assertEquals(2, candidates.size)
        assertNull(candidates[0].features)
        assertNull(candidates[1].features)
    }
}
