package com.example.soundmatch.model

// A energia que a pessoa quer ouvir agora. O "hint" aparece embaixo da opção no quiz
// e também é o critério usado para classificar cada música do catálogo.
// "phrase" completa a explicação: "Você pediu uma música dançante".
enum class Energy(val label: String, val hint: String, val phrase: String) {
    HEAVY("Porradeira", "pesada e intensa, pra extravasar", "porradeira"),
    DANCE("Dançante", "batida pra mexer o corpo", "dançante"),
    SINGALONG("Pra cantar junto", "refrão que todo mundo sabe", "pra cantar junto"),
    HEARTBREAK("Sofrência", "triste, de dor de cotovelo", "de sofrência"),
    TRIPPY("Viajada", "longa e cheia de clima, pra se perder", "viajada"),
    CHILL("De boa", "leve e tranquila", "de boa")
}
