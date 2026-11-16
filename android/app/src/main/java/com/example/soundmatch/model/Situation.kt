package com.example.soundmatch.model

// Em que situação a pessoa mais ouve música. "context" entra nas frases do app,
// ex.: "Qual destas você colocaria pra tocar na academia?".
enum class Situation(val label: String, val context: String) {
    RELAXING("Relaxando em casa", "relaxando em casa"),
    GYM("Academia", "na academia"),
    STREET("Na rua", "na rua"),
    PARTY("Festa com amigos", "numa festa com amigos"),
    STUDYING("Estudando", "estudando")
}
