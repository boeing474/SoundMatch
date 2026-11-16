package com.example.soundmatch.logic

import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Instrument.BASS
import com.example.soundmatch.model.Instrument.DRUMS
import com.example.soundmatch.model.Instrument.GUITAR
import com.example.soundmatch.model.Instrument.KEYS
import com.example.soundmatch.model.Instrument.VOCALS
import com.example.soundmatch.model.Song

// 5 músicas por gênero, uma para cada instrumento (assim quem toca sempre tem uma opção).
// Nenhum artista daqui aparece nas opções do quiz, para a recomendação ser sempre uma novidade.
object SongCatalog {

    private val songs: Map<Genre, List<Song>> = mapOf(
        Genre.POP to listOf(
            Song("Super Shy", "NewJeans", VOCALS),
            Song("God's Menu", "Stray Kids", DRUMS),
            Song("Espresso", "Sabrina Carpenter", BASS),
            Song("As It Was", "Harry Styles", KEYS),
            Song("Uptown Funk", "Mark Ronson ft. Bruno Mars", GUITAR)
        ),
        Genre.ROCK to listOf(
            Song("Mr. Brightside", "The Killers", VOCALS),
            Song("Black Dog", "Led Zeppelin", DRUMS),
            Song("Another One Bites the Dust", "Queen", BASS),
            Song("Riders on the Storm", "The Doors", KEYS),
            Song("Paranoid Android", "Radiohead", GUITAR)
        ),
        Genre.HIPHOP to listOf(
            Song("Kill Bill", "SZA", VOCALS),
            Song("In Da Club", "50 Cent", DRUMS),
            Song("Redbone", "Childish Gambino", BASS),
            Song("Juicy", "The Notorious B.I.G.", KEYS),
            Song("Lose Yourself", "Eminem", GUITAR)
        ),
        Genre.ELECTRONIC to listOf(
            Song("Clarity", "Zedd ft. Foxes", VOCALS),
            Song("Losing It", "FISHER", DRUMS),
            Song("Scary Monsters and Nice Sprites", "Skrillex", BASS),
            Song("Strobe", "deadmau5", KEYS),
            Song("Wake Me Up", "Avicii", GUITAR)
        ),
        Genre.BRAZILIAN to listOf(
            Song("Evidências", "Chitãozinho & Xororó", VOCALS),
            Song("Baile de Favela", "MC João", DRUMS),
            Song("Sossego", "Tim Maia", BASS),
            Song("Mania de Você", "Rita Lee", KEYS),
            Song("Aquarela", "Toquinho", GUITAR)
        )
    )

    fun songsFor(genre: Genre): List<Song> = songs.getValue(genre)
}
