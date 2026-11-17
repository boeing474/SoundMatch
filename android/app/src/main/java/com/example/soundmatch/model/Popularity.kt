package com.example.soundmatch.model

// Hit ou pérola: a música vem das mais tocadas ou das menos conhecidas dos artistas parecidos.
// Aparece no quiz no mesmo formato da energia: nome em negrito e o "hint" embaixo.
enum class Popularity(val label: String, val hint: String) {
    HIT("Hit do momento", "uma das mais tocadas, que todo mundo anda ouvindo"),
    GEM("Pérola escondida", "menos conhecida, pra descobrir de verdade")
}
