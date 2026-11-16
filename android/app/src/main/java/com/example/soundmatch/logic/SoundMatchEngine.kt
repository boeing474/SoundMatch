package com.example.soundmatch.logic

import com.example.soundmatch.model.Archetype
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Song
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.model.Subgenre
import com.example.soundmatch.model.UserProfile
import kotlin.math.roundToInt

/**
 * Processamento dos dados do SoundMatch (regra de negócio, sem nada de tela).
 *
 * 1. calculateScore: transforma as respostas do quiz em um score de 10 a 100.
 * 2. Archetype.fromScore: o score define o perfil sonoro.
 * 3. pickSong: escolhe uma música do perfil que combine com o gosto do usuário.
 * 4. buildReason: explica para o usuário o porquê da recomendação.
 */
object SoundMatchEngine {

    const val MIN_SCORE = 10
    const val MAX_SCORE = 100

    /** Pesos da duração da faixa: 3 min = 0, 5 min = 1,5, +7 min = 3. */
    private val DURATION_WEIGHTS = listOf(0.0, 1.5, 3.0)

    /** Peso usado quando o usuário não marca nenhum subgênero (meio da escala 0..3). */
    private const val NEUTRAL_SUBGENRE_WEIGHT = 1.5

    /** Soma máxima possível: elemento (3) + energia (3) + duração (3) + subgêneros (3). */
    private const val MAX_RAW_POINTS = 12.0

    /** Executa o fluxo completo e devolve o resultado pronto para a tela. */
    fun recommend(profile: UserProfile, answers: QuizAnswers): SoundResult {
        val score = calculateScore(answers)
        val archetype = Archetype.fromScore(score)
        val song = pickSong(archetype, profile, answers.subgenres, score)
        val reason = buildReason(profile, archetype, song, answers.subgenres)
        return SoundResult(profile, score, archetype, song, reason, answers.subgenres)
    }

    /**
     * Cada resposta vale de 0 a 3 pontos (do mais "raiz" ao mais "experimental").
     * Os subgêneros entram pela média dos pesos marcados.
     * A soma (0..12) é convertida proporcionalmente para a escala 10..100.
     */
    fun calculateScore(answers: QuizAnswers): Int {
        require(answers.element in 0..3) { "element inválido: ${answers.element}" }
        require(answers.energy in 0..3) { "energy inválido: ${answers.energy}" }
        require(answers.duration in DURATION_WEIGHTS.indices) { "duration inválido: ${answers.duration}" }

        val subgenreWeight = if (answers.subgenres.isEmpty()) {
            NEUTRAL_SUBGENRE_WEIGHT
        } else {
            answers.subgenres.map { it.weight }.average()
        }

        val rawPoints = answers.element + answers.energy +
            DURATION_WEIGHTS[answers.duration] + subgenreWeight

        val score = MIN_SCORE + rawPoints / MAX_RAW_POINTS * (MAX_SCORE - MIN_SCORE)
        return score.roundToInt().coerceIn(MIN_SCORE, MAX_SCORE)
    }

    /**
     * Escolhe a música recomendada dentro do perfil:
     * - descarta músicas do artista favorito (a ideia é DESCOBRIR algo novo);
     * - prioriza as músicas com mais subgêneros em comum com os marcados;
     * - entre as empatadas, a escolha varia com o nome e o score, então pessoas
     *   diferentes recebem descobertas diferentes, mas o resultado é sempre o mesmo
     *   para as mesmas respostas.
     */
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

    /** Monta o texto explicando a recomendação. */
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

    /** Compara artistas ignorando maiúsculas/espaços ("ac/dc" == "AC/DC"). */
    private fun isSameArtist(songArtist: String, favoriteArtist: String): Boolean {
        val a = songArtist.trim().lowercase()
        val b = favoriteArtist.trim().lowercase()
        return b.isNotEmpty() && (a.contains(b) || b.contains(a))
    }
}
