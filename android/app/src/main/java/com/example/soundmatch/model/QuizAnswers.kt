package com.example.soundmatch.model

// firstPick: gênero da música escolhida na 1ª pergunta (null se o iTunes não souber o gênero).
// firstPickSong: o nome dessa música, para citar na explicação (null quando o quiz usa o plano B).
// surprise: a pessoa ligou o "Me surpreenda".
data class QuizAnswers(
    val firstPick: Genre?,
    val firstPickSong: String?,
    val energy: Energy,
    val likedGenres: List<Genre>,
    val surprise: Boolean
)
