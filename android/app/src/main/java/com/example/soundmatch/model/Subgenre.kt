package com.example.soundmatch.model

/**
 * Subgêneros que o usuário pode marcar no quiz (CheckBox).
 *
 * @property label  texto mostrado e salvo na API.
 * @property weight peso no cálculo do score: 0 = mais "raiz" (Blues) ... 3 = mais "experimental" (Prog).
 */
enum class Subgenre(val label: String, val weight: Int) {
    BLUES_ROCK("Blues Rock", 0),
    HARD_ROCK("Hard Rock", 1),
    FUNK_ALTERNATIVE("Funk/Alternative", 2),
    PROG_ROCK("Prog Rock", 3)
}
