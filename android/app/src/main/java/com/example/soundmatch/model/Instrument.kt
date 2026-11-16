package com.example.soundmatch.model

// "practice" e "focus" completam a dica da explicação: "Você toca baixo: repare na linha de baixo de Espresso".
enum class Instrument(val label: String, val practice: String, val focus: String) {
    GUITAR("Guitarra", "toca guitarra", "na guitarra"),
    DRUMS("Bateria", "toca bateria", "na bateria"),
    BASS("Baixo", "toca baixo", "na linha de baixo"),
    KEYS("Teclado", "toca teclado", "nos teclados"),
    VOCALS("Canto", "canta", "na voz")
}
