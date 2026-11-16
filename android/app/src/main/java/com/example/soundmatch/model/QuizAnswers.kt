package com.example.soundmatch.model

// Guarda a posição da opção escolhida em cada pergunta (0 = mais "raiz").
data class QuizAnswers(
    val playlist: Int,
    val concert: Int,
    val longTrack: Int,
    val subgenres: List<Subgenre>
)
