package com.example.soundmatch.model

/**
 * Respostas do quiz. Cada pergunta guarda o ÍNDICE da opção escolhida, e as
 * opções estão ordenadas da mais "raiz" (0) para a mais "experimental".
 *
 * @property element  0 Guitarra, 1 Bateria, 2 Baixo, 3 Teclados/Atmosfera
 * @property energy   0 Crua, 1 Pesada, 2 Dançante, 3 Psicodélica
 * @property duration 0 = 3 min, 1 = 5 min, 2 = +7 min
 */
data class QuizAnswers(
    val element: Int,
    val energy: Int,
    val duration: Int,
    val subgenres: List<Subgenre>
)
