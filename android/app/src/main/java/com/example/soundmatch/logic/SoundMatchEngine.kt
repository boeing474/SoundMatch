package com.example.soundmatch.logic

import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Song
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.model.UserProfile
import kotlin.math.roundToInt

// O motor reserva, sem nada de Android (por isso dá para testar com JUnit puro).
// Entra quando não há artistas parecidos para procurar (ver DiscoveryEngine).
// Em uma frase: o artista e os estilos marcados votam no estilo, o "Quero me arriscar"
// decide se a pessoa fica nele, e o clima escolhe a música no catálogo fixo.
object SoundMatchEngine {

    // O gênero do artista favorito (vindo do iTunes) vale mais que uma resposta do quiz.
    const val ARTIST_VOTES = 3

    private const val MIN_MATCH = 10
    private const val MAX_MATCH = 100

    fun recommend(profile: UserProfile, answers: QuizAnswers): SoundResult {
        val votes = countVotes(profile, answers)
        val usualGenre = pickGenre(votes, profile.artistGenre)
        val genre = if (answers.surprise) {
            surpriseGenre(votes, usualGenre, answers.likedGenres) ?: usualGenre
        } else {
            usualGenre
        }
        val song = pickSong(genre, answers.energy, profile)
        val reason = buildReason(profile, answers, usualGenre, genre, song)
        return SoundResult(
            profile = profile,
            match = matchPercent(votes, genre),
            genre = genre,
            song = song,
            reason = reason,
            likedGenres = answers.likedGenres,
            surprise = genre != usualGenre
        )
    }

    // Artista favorito: 3 votos. Cada estilo marcado: 1 voto.
    fun countVotes(profile: UserProfile, answers: QuizAnswers): Map<Genre, Int> {
        val votes = Genre.entries.associateWith { 0 }.toMutableMap()
        profile.artistGenre?.let { votes[it] = votes.getValue(it) + ARTIST_VOTES }
        answers.likedGenres.forEach { votes[it] = votes.getValue(it) + 1 }
        return votes
    }

    // Empate: ganha o gênero do artista; sem ele, o primeiro na ordem do enum Genre.
    fun pickGenre(votes: Map<Genre, Int>, artistGenre: Genre?): Genre {
        val most = votes.values.max()
        val tied = Genre.entries.filter { votes[it] == most }
        return artistGenre?.takeIf { it in tied } ?: tied.first()
    }

    // Entre os estilos marcados, o mais votado que não seja o de sempre.
    // null quando a pessoa não marcou nenhum outro estilo (a tela já pede para marcar).
    fun surpriseGenre(votes: Map<Genre, Int>, usualGenre: Genre, likedGenres: List<Genre>): Genre? =
        likedGenres.filter { it != usualGenre }.maxByOrNull { votes.getValue(it) }

    fun matchPercent(votes: Map<Genre, Int>, genre: Genre): Int {
        val total = votes.values.sum()
        if (total == 0) return MIN_MATCH
        val percent = votes.getValue(genre) * 100.0 / total
        return percent.roundToInt().coerceIn(MIN_MATCH, MAX_MATCH)
    }

    // Fica só com as músicas do clima pedido e tira o artista favorito (a ideia é descobrir
    // algo novo). A escolha é fixa pelo nome: pessoas diferentes ganham músicas diferentes,
    // mas a mesma pessoa sempre ganha a mesma.
    fun pickSong(genre: Genre, energy: Energy, profile: UserProfile): Song {
        val catalog = SongCatalog.songsFor(genre).filter { it.energy == energy }
        val newToUser = catalog
            .filterNot { isSameArtist(it.artist, profile.favoriteArtist) }
            .ifEmpty { catalog }

        val index = profile.name.trim().lowercase().hashCode().mod(newToUser.size)
        return newToUser[index]
    }

    fun buildReason(
        profile: UserProfile,
        answers: QuizAnswers,
        usualGenre: Genre,
        genre: Genre,
        song: Song
    ): String = buildString {
        val name = profile.name
        when {
            genre != usualGenre -> {
                append("$name, você quis sair da zona de conforto: em vez de ${usualGenre.title}, ")
                append("a descoberta vem de ${genre.title}, um dos estilos que você marcou.")
            }
            profile.artistGenre == genre ->
                append("$name, você ouve ${profile.favoriteArtist}: seu perfil é ${genre.title}.")
            else -> append("$name, suas respostas apontaram para ${genre.title}.")
        }
        append(" Você pediu uma música ${answers.energy.phrase} pra ouvir ${profile.situation.context}, ")
        append("então a descoberta é ${song.title}, de ${song.artist}.")
    }

    // "ac/dc" e "AC/DC " contam como o mesmo artista.
    private fun isSameArtist(songArtist: String, favoriteArtist: String): Boolean {
        val a = songArtist.trim().lowercase()
        val b = favoriteArtist.trim().lowercase()
        return b.isNotEmpty() && (a.contains(b) || b.contains(a))
    }
}
