package com.example.soundmatch.logic

import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.Candidate
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerTrack

// Regras que não dependem da rede, separadas para poder testar sem internet.
object DiscoveryRules {

    // A busca do Deezer devolve vários artistas com o mesmo nome (há mais de um "BTS").
    // O verdadeiro é o que tem mais fãs.
    fun mostFamous(artists: List<DeezerArtist>): DeezerArtist? =
        artists.maxByOrNull { it.fans ?: 0 }

    // Junta cada música com as características do som, ligando as duas pelo ISRC.
    // Música sem ISRC ou que o ReccoBeats não conhece fica com features = null.
    fun attachFeatures(tracks: List<DeezerTrack>, features: List<AudioFeatures>): List<Candidate> {
        val byIsrc = features.filter { it.isrc != null }.associateBy { it.isrc!!.uppercase() }
        return tracks.map { track -> Candidate(track, track.isrc?.let { byIsrc[it.uppercase()] }) }
    }
}
