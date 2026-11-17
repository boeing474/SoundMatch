package com.example.soundmatch.logic

import com.example.soundmatch.model.AudioFeatures
import com.example.soundmatch.model.Candidate
import com.example.soundmatch.model.DeezerTrack
import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.Popularity
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Situation
import com.example.soundmatch.model.Song
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.model.UserProfile
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

// A descoberta por artistas parecidos, sem nada de Android (dá para testar com JUnit puro).
// Em uma frase: das músicas mais tocadas dos artistas que a pessoa não conhece, fica a metade
// pedida (hits ou pérolas) e ganha a que chega mais perto do clima.
// "Não conhece" aqui quer dizer: não é o favorito nem foi marcado no quiz como artista que já curte.
object DiscoveryEngine {

    private const val MIN_MATCH = 10
    private const val MAX_MATCH = 100

    // Quanto a situação do perfil pesa na escolha, perto do clima (que vale até 1).
    private const val SITUATION_WEIGHT = 0.1

    // A partir de 6 minutos a música já conta como longa para o clima "Viajada".
    private const val LONG_SONG_SECONDS = 360.0

    // O que um clima procura nos números do som, que vão de 0 a 1. null = tanto faz.
    data class MoodTarget(
        val energy: Double? = null,
        val valence: Double? = null,
        val danceability: Double? = null,
        val acousticness: Double? = null,
        val long: Boolean = false
    )

    // valence é a alegria da música: alta em música pra cima, baixa em música triste.
    fun targetFor(energy: Energy): MoodTarget = when (energy) {
        Energy.HEAVY -> MoodTarget(energy = 0.95, acousticness = 0.05)
        Energy.DANCE -> MoodTarget(danceability = 0.85, energy = 0.8)
        Energy.SINGALONG -> MoodTarget(valence = 0.9, energy = 0.75)
        Energy.HEARTBREAK -> MoodTarget(valence = 0.15)
        Energy.TRIPPY -> MoodTarget(danceability = 0.3, energy = 0.45, long = true)
        Energy.CHILL -> MoodTarget(energy = 0.3, valence = 0.6)
    }

    // Devolve null quando não dá para escolher pelo clima (nenhuma candidata, ou nenhuma
    // com os números do som). Aí a tela usa o motor reserva, o SoundMatchEngine.
    // alreadyShown: as músicas que já saíram desde que o app abriu (ver ShownSongs).
    fun recommend(
        profile: UserProfile,
        answers: QuizAnswers,
        candidates: List<Candidate>,
        alreadyShown: Set<String> = emptySet()
    ): SoundResult? {
        val measured = newToUser(candidates, profile, answers)
            .mapNotNull { candidate -> candidate.features?.let { candidate.track to it } }
        if (measured.isEmpty()) return null

        // Para não repetir, saem as que já foram recomendadas. Se todas já saíram, vale a lista inteira de novo.
        val notShown = measured.filterNot { (track, _) -> songKey(track.title, track.artist.name) in alreadyShown }
        val skippedSome = notShown.isNotEmpty() && notShown.size < measured.size

        val target = targetFor(answers.energy)
        val pool = popularityHalf(notShown.ifEmpty { measured }, answers.popularity)
        val (track, features) = pool.maxBy { (track, features) ->
            closeness(track, features, target) + SITUATION_WEIGHT * situationBonus(profile.situation, features)
        }
        val song = Song(track.title, track.artist.name, answers.energy, track.album?.title, track.album?.cover)

        return SoundResult(
            profile = profile,
            match = matchPercent(closeness(track, features, target)),
            genre = profile.artistGenre,
            song = song,
            reason = buildReason(profile, answers, song, skippedSome),
            likedGenres = emptyList(),
            surprise = false,
            fromSimilarArtists = true,
            popularity = answers.popularity
        )
    }

    // Tira as músicas do artista favorito e dos que a pessoa marcou como conhecidos,
    // e as repetidas (a mesma música aparece nas mais tocadas de dois artistas numa parceria).
    fun newToUser(candidates: List<Candidate>, profile: UserProfile, answers: QuizAnswers): List<Candidate> {
        val known = (answers.knownArtists.map { it.name } + profile.favoriteArtist).map { it.trim().lowercase() }
        return candidates
            .filterNot { it.track.artist.name.trim().lowercase() in known }
            .distinctBy { songKey(it.track.title, it.track.artist.name) }
    }

    // Identifica uma música pelo título e pelo artista, sem diferenciar maiúsculas.
    fun songKey(title: String, artist: String): String = "$title|$artist".lowercase()

    // Ordena pela popularidade no Deezer e corta no meio: hit fica com a metade
    // mais tocada, pérola com a outra.
    fun popularityHalf(
        measured: List<Pair<DeezerTrack, AudioFeatures>>,
        popularity: Popularity?
    ): List<Pair<DeezerTrack, AudioFeatures>> {
        val byRank = measured.sortedByDescending { (track, _) -> track.rank }
        val half = (byRank.size + 1) / 2
        return if (popularity == Popularity.GEM) byRank.drop(half).ifEmpty { byRank } else byRank.take(half)
    }

    // De 0 a 1: a média de quanto a música fica longe de cada número que o clima pede,
    // virada ao contrário (1 = em cima do alvo).
    fun closeness(track: DeezerTrack, features: AudioFeatures, target: MoodTarget): Double {
        val gaps = listOfNotNull(
            target.energy?.let { abs(it - features.energy) },
            target.valence?.let { abs(it - features.valence) },
            target.danceability?.let { abs(it - features.danceability) },
            target.acousticness?.let { abs(it - features.acousticness) },
            if (target.long) 1 - min(track.duration / LONG_SONG_SECONDS, 1.0) else null
        )
        return 1 - gaps.average()
    }

    // O empurrão da situação: entre duas músicas igualmente perto do clima, na academia
    // ganha a mais enérgica, na festa a mais dançante, e assim por diante.
    fun situationBonus(situation: Situation, features: AudioFeatures): Double = when (situation) {
        Situation.GYM -> features.energy
        Situation.PARTY -> features.danceability
        Situation.STUDYING -> (features.acousticness + 1 - features.energy) / 2
        Situation.RELAXING -> 1 - features.energy
        Situation.STREET -> 0.0
    }

    fun matchPercent(closeness: Double): Int =
        (closeness * 100).roundToInt().coerceIn(MIN_MATCH, MAX_MATCH)

    // skippedSome: havia músicas mais perto do clima, mas elas já tinham sido recomendadas.
    fun buildReason(
        profile: UserProfile,
        answers: QuizAnswers,
        song: Song,
        skippedSome: Boolean = false
    ): String = buildString {
        val name = profile.name
        val favorite = profile.favoriteArtist
        when {
            answers.surprise ->
                append("$name, você quis se arriscar: ${song.artist} fica um passo além dos parecidos com $favorite.")
            DiscoveryRules.goesFarther(profile.relatedArtists, answers) ->
                append("$name, você já curte os parecidos com $favorite, então a busca foi um passo além e chegou em ${song.artist}.")
            else ->
                append("$name, você ouve $favorite, e ${song.artist} tem um som parecido.")
        }
        append(" Você pediu uma música ${answers.energy.phrase} pra ouvir ${profile.situation.context}")
        answers.popularity?.let { append(", ${it.phrase}") }
        append(": ${song.title} foi a que chegou mais perto")
        append(if (skippedSome) ", sem repetir as que você já recebeu." else ".")
    }
}
