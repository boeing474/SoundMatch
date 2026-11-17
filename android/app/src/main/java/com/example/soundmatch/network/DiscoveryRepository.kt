package com.example.soundmatch.network

import com.example.soundmatch.logic.DiscoveryRules
import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.Candidate
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerTrack
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.UserProfile
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
        DiscoveryRules.pickArtist(name, deezer.searchArtist(name).data.orEmpty())

    suspend fun relatedArtists(artistId: Long): List<DeezerArtist> =
        deezer.relatedArtists(artistId).data.orEmpty()

    // As músicas candidatas para as respostas do quiz. Normalmente vêm dos parecidos que
    // a pessoa não conhece; se ela quis se arriscar (ou conhece todos), dos parecidos desses.
    suspend fun candidatesFor(profile: UserProfile, answers: QuizAnswers): List<Candidate> {
        val related = profile.relatedArtists
        val unknown = DiscoveryRules.newArtists(related, profile.favoriteArtist, answers.knownArtists)
        val artists = if (DiscoveryRules.goesFarther(related, answers)) {
            fartherArtists(unknown.ifEmpty { related }, profile.favoriteArtist, related)
        } else {
            unknown
        }
        return candidates(artists.take(MAX_ARTISTS))
    }

    // Os parecidos dos primeiros artistas da lista, fora os que já eram parecidos do favorito.
    private suspend fun fartherArtists(
        startingPoints: List<DeezerArtist>,
        favorite: String,
        related: List<DeezerArtist>
    ): List<DeezerArtist> = coroutineScope {
        val found = startingPoints.take(STARTING_POINTS)
            .map { artist -> async { relatedArtists(artist.id) } }
            .awaitAll()
            .flatten()
        DiscoveryRules.newArtists(found, favorite, related)
    }

    // As mais tocadas de cada artista, pedidas em paralelo.
    suspend fun candidates(artists: List<DeezerArtist>, perArtist: Int = TRACKS_PER_ARTIST): List<Candidate> =
        coroutineScope {
            val tops = artists
                .map { artist -> async { deezer.topTracks(artist.id, perArtist).data.orEmpty() } }
                .awaitAll()
                .flatten()
            val tracks = tops.map { top -> async { withIsrc(top) } }.awaitAll()
            DiscoveryRules.attachFeatures(tracks, audioFeatures(tracks))
        }

    // A lista das mais tocadas não traz o ISRC: ele só vem pedindo a música pelo id.
    // Se esse pedido falhar, a música segue sem ISRC (e sem as características do som).
    private suspend fun withIsrc(top: DeezerTrack): DeezerTrack =
        networkOrNull { deezer.track(top.id) }?.takeIf { it.isrc != null } ?: top

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

    // O Deezer aceita 50 chamadas a cada 5 segundos: 8 artistas x 4 músicas dão 40.
    private const val MAX_ARTISTS = 8
    private const val TRACKS_PER_ARTIST = 4
    private const val STARTING_POINTS = 3
    private const val ISRCS_PER_CALL = 40
}
