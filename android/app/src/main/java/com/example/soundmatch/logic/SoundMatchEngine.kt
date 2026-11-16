package com.example.soundmatch.logic

import com.example.soundmatch.model.Archetype
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Song
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.model.Subgenre
import com.example.soundmatch.model.UserProfile
import kotlin.math.roundToInt

// Regra de negócio do app, sem nada de Android (por isso dá para testar com JUnit puro).
// Fluxo: respostas -> score -> perfil -> música -> explicação.
object SoundMatchEngine {

    const val MIN_SCORE = 10
    const val MAX_SCORE = 100

    // A pergunta da música longa só tem 3 opções, então os pesos são 0, 1,5 e 3.
    private val LONG_TRACK_WEIGHTS = listOf(0.0, 1.5, 3.0)

    // Sem subgênero marcado, usamos o meio da escala.
    private const val NEUTRAL_SUBGENRE_WEIGHT = 1.5

    // 3 perguntas + subgêneros, cada um valendo até 3 pontos.
    private const val MAX_RAW_POINTS = 12.0

    fun recommend(profile: UserProfile, answers: QuizAnswers): SoundResult {
        val score = calculateScore(answers)
        val archetype = Archetype.fromScore(score)
        val song = pickSong(archetype, profile, answers.subgenres, score)
        val reason = buildReason(profile, archetype, song, answers.subgenres)
        return SoundResult(profile, score, archetype, song, reason, answers.subgenres)
    }

    // Cada resposta vale de 0 (mais "raiz") a 3 (mais "experimental").
    // A soma vai de 0 a 12 e é convertida para a escala de 10 a 100.
    fun calculateScore(answers: QuizAnswers): Int {
        require(answers.playlist in 0..3) { "playlist inválido: ${answers.playlist}" }
        require(answers.concert in 0..3) { "concert inválido: ${answers.concert}" }
        require(answers.longTrack in LONG_TRACK_WEIGHTS.indices) { "longTrack inválido: ${answers.longTrack}" }

        val subgenreWeight = if (answers.subgenres.isEmpty()) {
            NEUTRAL_SUBGENRE_WEIGHT
        } else {
            answers.subgenres.map { it.weight }.average()
        }

        val rawPoints = answers.playlist + answers.concert +
            LONG_TRACK_WEIGHTS[answers.longTrack] + subgenreWeight

        val score = MIN_SCORE + rawPoints / MAX_RAW_POINTS * (MAX_SCORE - MIN_SCORE)
        return score.roundToInt().coerceIn(MIN_SCORE, MAX_SCORE)
    }

    // Tira o artista favorito (a ideia é descobrir algo novo) e fica com as músicas que
    // têm mais subgêneros em comum. No empate, nome + score escolhem uma: assim pessoas
    // diferentes recebem músicas diferentes, mas as mesmas respostas dão sempre o mesmo resultado.
    fun pickSong(
        archetype: Archetype,
        profile: UserProfile,
        likedSubgenres: List<Subgenre>,
        score: Int
    ): Song {
        val catalog = SongCatalog.songsFor(archetype)
        val newToUser = catalog
            .filterNot { isSameArtist(it.artist, profile.favoriteArtist) }
            .ifEmpty { catalog }

        val bestMatchCount = newToUser.maxOf { matchCount(it, likedSubgenres) }
        val bestSongs = newToUser.filter { matchCount(it, likedSubgenres) == bestMatchCount }

        val index = (profile.name.trim().lowercase().hashCode() + score).mod(bestSongs.size)
        return bestSongs[index]
    }

    fun buildReason(
        profile: UserProfile,
        archetype: Archetype,
        song: Song,
        likedSubgenres: List<Subgenre>
    ): String {
        val matched = song.subgenres.filter { it in likedSubgenres }
        val because = if (matched.isEmpty()) {
            "seu perfil é ${archetype.title}"
        } else {
            "você curte ${matched.joinToString(" e ") { it.label }} e seu perfil é ${archetype.title}"
        }

        return buildString {
            append("Escolhemos esta música porque $because.")
            append(" Como você já ouve ${profile.favoriteArtist}, trouxemos algo fora da sua playlist.")
            if (profile.playsInstrument) {
                append(" Você toca um instrumento: preste atenção ${archetype.focus}!")
            }
        }
    }

    private fun matchCount(song: Song, likedSubgenres: List<Subgenre>): Int =
        song.subgenres.count { it in likedSubgenres }

    // "ac/dc" e "AC/DC " contam como o mesmo artista.
    private fun isSameArtist(songArtist: String, favoriteArtist: String): Boolean {
        val a = songArtist.trim().lowercase()
        val b = favoriteArtist.trim().lowercase()
        return b.isNotEmpty() && (a.contains(b) || b.contains(a))
    }
}
