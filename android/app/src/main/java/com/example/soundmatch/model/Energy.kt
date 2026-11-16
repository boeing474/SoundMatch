package com.example.soundmatch.model

// A energia que a pessoa quer ouvir agora. O "hint" aparece embaixo da opção no quiz
// e também é o critério usado para classificar cada música do catálogo.
enum class Energy(val label: String, val hint: String) {
    HEAVY("Porradeira", "pesada e intensa, pra extravasar"),
    DANCE("Dançante", "batida pra mexer o corpo"),
    SINGALONG("Pra cantar junto", "refrão que todo mundo sabe"),
    HEARTBREAK("Sofrência", "triste, de dor de cotovelo"),
    TRIPPY("Viajada", "longa e cheia de clima, pra se perder"),
    CHILL("De boa", "leve e tranquila")
}
