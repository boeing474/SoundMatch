package com.example.soundmatch.logic

import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertEquals(6982223L, DiscoveryRules.mostFamous(results)?.id)
    }

    @Test
    fun `busca vazia nao acha artista`() {
        assertNull(DiscoveryRules.mostFamous(emptyList()))
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
    fun `musica sem ISRC ou desconhecida continua, mas sem caracteristicas`() {
        val tracks = listOf(track(1, null), track(2, "BRCCC0000003"))
        val candidates = DiscoveryRules.attachFeatures(tracks, listOf(features(null, 0.7)))

        assertEquals(2, candidates.size)
        assertNull(candidates[0].features)
        assertNull(candidates[1].features)
    }
}
