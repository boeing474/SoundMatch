package com.example.soundmatch.model

// Hit ou pérola: a música vem das mais tocadas ou das menos conhecidas dos artistas parecidos.
// Aparece no quiz no mesmo formato da energia: nome em negrito e o "hint" embaixo.
// "phrase" completa a explicação: "Você pediu uma música dançante, entre as mais tocadas".
enum class Popularity(val label: String, val hint: String, val phrase: String) {
    HIT("Hit do momento", "uma das mais tocadas, que todo mundo anda ouvindo", "entre as mais tocadas"),
    GEM("Pérola escondida", "menos conhecida, pra descobrir de verdade", "entre as menos conhecidas")
}
