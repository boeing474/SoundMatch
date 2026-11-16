package com.example.soundmatch.logic

import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Song
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.model.UserProfile
import kotlin.math.roundToInt

// Regra de negócio do app, sem nada de Android (por isso dá para testar com JUnit puro).
// Ideia: cada resposta é um voto em um gênero. O gênero com mais votos vira o perfil,
// e o "match" é a porcentagem dos votos que ele recebeu.
object SoundMatchEngine {

    // O gênero do artista favorito (vindo do iTunes) vale mais que uma resposta do quiz.
    const val ARTIST_VOTES = 3

    private const val MIN_MATCH = 10
    private const val MAX_MATCH = 100

    fun recommend(profile: UserProfile, answers: QuizAnswers): SoundResult {
        val votes = countVotes(profile, answers)
        val genre = pickGenre(votes, profile.artistGenre)
        val match = matchPercent(votes, genre)
        val song = pickSong(genre, profile)
        val reason = buildReason(profile, genre, song)
        return SoundResult(profile, match, genre, song, reason, answers.likedGenres)
    }

    // Artista favorito: 3 votos. Cada pergunta: 1 voto. Cada estilo marcado: 1 voto.
    fun countVotes(profile: UserProfile, answers: QuizAnswers): Map<Genre, Int> {
        val votes = Genre.entries.associateWith { 0 }.toMutableMap()
        profile.artistGenre?.let { votes[it] = votes.getValue(it) + ARTIST_VOTES }
        listOf(answers.songPick, answers.artistPick, answers.albumPick).forEach {
            votes[it] = votes.getValue(it) + 1
        }
        answers.likedGenres.forEach { votes[it] = votes.getValue(it) + 1 }
        return votes
    }

    // Empate: ganha o gênero do artista favorito, se ele estiver entre os empatados.
    fun pickGenre(votes: Map<Genre, Int>, artistGenre: Genre?): Genre {
        val most = votes.values.max()
        val tied = Genre.entries.filter { votes[it] == most }
        return if (artistGenre in tied) artistGenre!! else tied.first()
    }

    fun matchPercent(votes: Map<Genre, Int>, genre: Genre): Int {
        val total = votes.values.sum()
        if (total == 0) return MIN_MATCH
        val percent = votes.getValue(genre) * 100.0 / total
        return percent.roundToInt().coerceIn(MIN_MATCH, MAX_MATCH)
    }

    // Tira o artista favorito (a ideia é descobrir algo novo). Quem toca recebe a música
    // em que o seu instrumento se destaca; os outros recebem uma escolha fixa pelo nome,
    // assim pessoas diferentes ganham músicas diferentes, mas sempre a mesma para a mesma pessoa.
    fun pickSong(genre: Genre, profile: UserProfile): Song {
        val catalog = SongCatalog.songsFor(genre)
        val newToUser = catalog
            .filterNot { isSameArtist(it.artist, profile.favoriteArtist) }
            .ifEmpty { catalog }

        newToUser.firstOrNull { it.featured == profile.instrument }?.let { return it }

        val index = profile.name.trim().lowercase().hashCode().mod(newToUser.size)
        return newToUser[index]
    }

    fun buildReason(profile: UserProfile, genre: Genre, song: Song): String = buildString {
        if (profile.artistGenre == genre) {
            append("${profile.name}, você ouve ${profile.favoriteArtist} e suas respostas confirmaram: ")
            append("seu perfil é ${genre.title}.")
        } else {
            append("${profile.name}, suas respostas apontaram para ${genre.title}.")
        }
        append(" Como você já conhece ${profile.favoriteArtist}, a descoberta é de outro artista: ${song.artist}.")
        profile.instrument?.let {
            append(" Você ${it.practice}: repare ${it.focus} de ${song.title}.")
        }
    }

    // "ac/dc" e "AC/DC " contam como o mesmo artista.
    private fun isSameArtist(songArtist: String, favoriteArtist: String): Boolean {
        val a = songArtist.trim().lowercase()
        val b = favoriteArtist.trim().lowercase()
        return b.isNotEmpty() && (a.contains(b) || b.contains(a))
    }
}
