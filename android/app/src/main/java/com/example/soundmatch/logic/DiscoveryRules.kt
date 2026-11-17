package com.example.soundmatch.logic

import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.Candidate
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.DeezerTrack
import com.example.soundmatch.model.QuizAnswers
import java.text.Normalizer

// Regras que não dependem da rede, separadas para poder testar sem internet.
object DiscoveryRules {

    // A busca do Deezer devolve vários artistas com o mesmo nome (há mais de um "BTS"):
    // entre os de nome igual ao digitado, o verdadeiro é o que tem mais fãs.
    // Sem nome igual ("racionais" para "Racionais MC's"), vale o 1º resultado do Deezer.
    fun pickArtist(typed: String, results: List<DeezerArtist>): DeezerArtist? {
        val wanted = normalize(typed)
        return results.filter { normalize(it.name) == wanted }.maxByOrNull { it.fans ?: 0 }
            ?: results.firstOrNull()
    }

    // Minúsculas e sem acento: "Wesley Safadao" e "wesley safadão" viram a mesma coisa.
    private fun normalize(name: String): String =
        Normalizer.normalize(name.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")

    // Junta cada música com as características do som, ligando as duas pelo ISRC.
    // Música sem ISRC ou que o ReccoBeats não conhece fica com features = null.
    // Tira da lista o artista favorito, os que a pessoa já conhece e os repetidos.
    fun newArtists(artists: List<DeezerArtist>, favorite: String, known: List<DeezerArtist>): List<DeezerArtist> {
        val knownIds = known.map { it.id }.toSet()
        return artists
            .filterNot { it.id in knownIds || normalize(it.name) == normalize(favorite) }
            .distinctBy { it.id }
    }

    // A busca vai um passo além (os parecidos dos parecidos) quando a pessoa quer
    // se arriscar ou quando já conhece todos os parecidos.
    fun goesFarther(related: List<DeezerArtist>, answers: QuizAnswers): Boolean =
        answers.surprise || answers.knownArtists.size >= related.size

    fun attachFeatures(tracks: List<DeezerTrack>, features: List<AudioFeatures>): List<Candidate> {
        val byIsrc = features
            .filter { it.isrc != null }
            .groupBy { it.isrc!!.uppercase() }
            .mapValues { (_, measures) -> average(measures) }
        return tracks.map { track -> Candidate(track, track.isrc?.let { byIsrc[it.uppercase()] }) }
    }

    // O ReccoBeats devolve várias medições da mesma gravação (uma por lançamento), com números
    // um pouco diferentes. Fica a média, para o resultado não depender de qual delas veio por último.
    private fun average(measures: List<AudioFeatures>) = AudioFeatures(
        isrc = measures.first().isrc,
        energy = measures.map { it.energy }.average(),
        valence = measures.map { it.valence }.average(),
        danceability = measures.map { it.danceability }.average(),
        acousticness = measures.map { it.acousticness }.average()
    )
}
