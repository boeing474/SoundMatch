package com.example.soundmatch.model

// weight: de 0 (Blues, mais "raiz") a 3 (Prog, mais "experimental").
enum class Subgenre(val label: String, val weight: Int) {
    BLUES_ROCK("Blues Rock", 0),
    HARD_ROCK("Hard Rock", 1),
    FUNK_ALTERNATIVE("Funk/Alternative", 2),
    PROG_ROCK("Prog Rock", 3)
}
