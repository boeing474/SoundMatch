package com.example.soundmatch.model

import androidx.annotation.ColorRes
import com.example.soundmatch.R

/**
 * Os 4 Arquétipos Sonoros (perfis) do SoundMatch, definidos pela faixa de score.
 *
 * @property title      nome exibido e salvo na API.
 * @property highlight  descrição curta do perfil.
 * @property focus      elemento musical para o usuário prestar atenção (usado se ele toca instrumento).
 * @property colorRes   cor do perfil na interface (card do resultado e faixa do histórico).
 * @property scoreRange faixa de score que leva a este perfil.
 */
enum class Archetype(
    val title: String,
    val highlight: String,
    val focus: String,
    @ColorRes val colorRes: Int,
    val scoreRange: IntRange
) {
    VINTAGE_BLUES(
        title = "Vintage Blues & Classic Rock",
        highlight = "Riffs crus de guitarra, groove orgânico e raízes do blues rock.",
        focus = "nos riffs de guitarra",
        colorRes = R.color.archetype_blues,
        scoreRange = 10..35
    ),
    HARD_ROCK(
        title = "Hard Rock & Heavy Riffs",
        highlight = "Bateria pesada, dinâmica intensa e solos épicos.",
        focus = "na pegada da bateria",
        colorRes = R.color.archetype_hard,
        scoreRange = 36..60
    ),
    GROOVE_FUNK(
        title = "Groove & Funk Rock Alternativo",
        highlight = "Linhas de baixo marcantes, energia rítmica e batidas sincopadas.",
        focus = "na linha de baixo",
        colorRes = R.color.archetype_funk,
        scoreRange = 61..80
    ),
    PROG_EXPLORER(
        title = "Explorador Progressivo & Conceitual",
        highlight = "Atmosferas imersivas, arranjos longos e sintetizadores.",
        focus = "nos teclados e na atmosfera",
        colorRes = R.color.archetype_prog,
        scoreRange = 81..100
    );

    companion object {
        /** Retorna o perfil correspondente ao score (valores fora de 10..100 são ajustados). */
        fun fromScore(score: Int): Archetype {
            val safeScore = score.coerceIn(VINTAGE_BLUES.scoreRange.first, PROG_EXPLORER.scoreRange.last)
            return entries.first { safeScore in it.scoreRange }
        }

        /** Converte o título salvo na API de volta para o perfil (null se não reconhecer). */
        fun fromTitle(title: String): Archetype? = entries.firstOrNull { it.title == title }
    }
}
