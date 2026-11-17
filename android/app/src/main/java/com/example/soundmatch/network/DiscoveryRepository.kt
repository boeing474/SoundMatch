package com.example.soundmatch.network

import com.example.soundmatch.logic.DiscoveryRules
import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.Candidate
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerTrack
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

// Junta o Deezer e o ReccoBeats: acha o artista, os parecidos
// e as músicas candidatas já com as características do som.
// Erros do Deezer sobem para a tela decidir usar a reserva (iTunes + catálogo).
object DiscoveryRepository {

    private val deezer get() = RetrofitClient.deezer
    private val reccoBeats get() = RetrofitClient.reccoBeats

    suspend fun findArtist(name: String): DeezerArtist? =
        DiscoveryRules.pickArtist(name, deezer.searchArtist(name).data)

    suspend fun relatedArtists(artistId: Long): List<DeezerArtist> =
        deezer.relatedArtists(artistId).data

    // As mais tocadas de cada artista, pedidas em paralelo.
    // Depois cada música é pedida de novo pelo id, porque só assim vem o ISRC.
    suspend fun candidates(artists: List<DeezerArtist>, perArtist: Int = TRACKS_PER_ARTIST): List<Candidate> =
        coroutineScope {
            val tops = artists
                .map { artist -> async { deezer.topTracks(artist.id, perArtist).data } }
                .awaitAll()
                .flatten()
            val tracks = tops.map { top -> async { deezer.track(top.id) } }.awaitAll()
            DiscoveryRules.attachFeatures(tracks, audioFeatures(tracks))
        }

    // Se o ReccoBeats falhar, segue sem as características (a escolha fica pela popularidade).
    private suspend fun audioFeatures(tracks: List<DeezerTrack>): List<AudioFeatures> {
        val isrcs = tracks.mapNotNull { it.isrc }
        if (isrcs.isEmpty()) return emptyList()
        return networkOrNull {
            isrcs.chunked(ISRCS_PER_CALL).flatMap { chunk ->
                reccoBeats.audioFeatures(chunk.joinToString(",")).content
            }
        }.orEmpty()
    }

    private const val TRACKS_PER_ARTIST = 5
    private const val ISRCS_PER_CALL = 40
}
