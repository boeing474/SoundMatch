package com.example.soundmatch.model

// Cada pergunta tem uma opção por gênero, na ordem do enum Genre.
// Por isso a resposta já é guardada como o gênero escolhido.
data class QuizAnswers(
    val songPick: Genre,
    val artistPick: Genre,
    val albumPick: Genre,
    val likedGenres: List<Genre>
)
