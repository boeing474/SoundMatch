package com.example.soundmatch.logic

import com.example.soundmatch.model.Energy.CHILL
import com.example.soundmatch.model.Energy.DANCE
import com.example.soundmatch.model.Energy.HEARTBREAK
import com.example.soundmatch.model.Energy.HEAVY
import com.example.soundmatch.model.Energy.SINGALONG
import com.example.soundmatch.model.Energy.TRIPPY
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Instrument.BASS
import com.example.soundmatch.model.Instrument.DRUMS
import com.example.soundmatch.model.Instrument.GUITAR
import com.example.soundmatch.model.Instrument.KEYS
import com.example.soundmatch.model.Instrument.VOCALS
import com.example.soundmatch.model.Song

// 12 músicas por gênero: 2 para cada energia, e todos os instrumentos aparecem em cada gênero.
// Nenhum artista daqui aparece nas opções do quiz, para a recomendação ser sempre uma novidade.
object SongCatalog {

    private val songs: Map<Genre, List<Song>> = mapOf(
        Genre.POP to listOf(
            Song("God's Menu", "Stray Kids", HEAVY, DRUMS),
            Song("good 4 u", "Olivia Rodrigo", HEAVY, GUITAR),
            Song("Espresso", "Sabrina Carpenter", DANCE, BASS),
            Song("Uptown Funk", "Mark Ronson ft. Bruno Mars", DANCE, GUITAR),
            Song("Shake It Off", "Taylor Swift", SINGALONG, VOCALS),
            Song("Dancing Queen", "ABBA", SINGALONG, KEYS),
            Song("Someone Like You", "Adele", HEARTBREAK, KEYS),
            Song("Love wins all", "IU", HEARTBREAK, VOCALS),
            Song("Video Games", "Lana Del Rey", TRIPPY, VOCALS),
            Song("Ditto", "NewJeans", TRIPPY, DRUMS),
            Song("Banana Pancakes", "Jack Johnson", CHILL, GUITAR),
            Song("Golden Hour", "JVKE", CHILL, KEYS)
        ),
        Genre.ROCK to listOf(
            Song("Black Dog", "Led Zeppelin", HEAVY, DRUMS),
            Song("Killing in the Name", "Rage Against the Machine", HEAVY, GUITAR),
            Song("Another One Bites the Dust", "Queen", DANCE, BASS),
            Song("Take Me Out", "Franz Ferdinand", DANCE, GUITAR),
            Song("Mr. Brightside", "The Killers", SINGALONG, VOCALS),
            Song("Livin' on a Prayer", "Bon Jovi", SINGALONG, BASS),
            Song("Creep", "Radiohead", HEARTBREAK, GUITAR),
            Song("Everybody Hurts", "R.E.M.", HEARTBREAK, VOCALS),
            Song("Riders on the Storm", "The Doors", TRIPPY, KEYS),
            Song("Comfortably Numb", "Pink Floyd", TRIPPY, GUITAR),
            Song("Here Comes the Sun", "The Beatles", CHILL, GUITAR),
            Song("Dreams", "Fleetwood Mac", CHILL, DRUMS)
        ),
        Genre.HIPHOP to listOf(
            Song("Lose Yourself", "Eminem", HEAVY, GUITAR),
            Song("X Gon' Give It to Ya", "DMX", HEAVY, DRUMS),
            Song("In Da Club", "50 Cent", DANCE, DRUMS),
            Song("Crazy in Love", "Beyoncé ft. Jay-Z", DANCE, VOCALS),
            Song("Hey Ya!", "OutKast", SINGALONG, GUITAR),
            Song("No Diggity", "Blackstreet", SINGALONG, KEYS),
            Song("Kill Bill", "SZA", HEARTBREAK, VOCALS),
            Song("Thinkin Bout You", "Frank Ocean", HEARTBREAK, KEYS),
            Song("Feel Good Inc.", "Gorillaz", TRIPPY, BASS),
            Song("Flashing Lights", "Kanye West", TRIPPY, KEYS),
            Song("Bad Habit", "Steve Lacy", CHILL, GUITAR),
            Song("Sunflower", "Post Malone & Swae Lee", CHILL, VOCALS)
        ),
        Genre.ELECTRONIC to listOf(
            Song("Scary Monsters and Nice Sprites", "Skrillex", HEAVY, BASS),
            Song("Firestarter", "The Prodigy", HEAVY, DRUMS),
            Song("Losing It", "FISHER", DANCE, DRUMS),
            Song("Lady (Hear Me Tonight)", "Modjo", DANCE, GUITAR),
            Song("Wake Me Up", "Avicii", SINGALONG, GUITAR),
            Song("Clarity", "Zedd ft. Foxes", SINGALONG, VOCALS),
            Song("Faded", "Alan Walker", HEARTBREAK, KEYS),
            Song("Silence", "Marshmello ft. Khalid", HEARTBREAK, VOCALS),
            Song("Strobe", "deadmau5", TRIPPY, KEYS),
            Song("Teardrop", "Massive Attack", TRIPPY, BASS),
            Song("Sunset Lover", "Petit Biscuit", CHILL, GUITAR),
            Song("Say My Name", "ODESZA ft. Zyra", CHILL, VOCALS)
        ),
        Genre.BRAZILIAN to listOf(
            Song("Roots Bloody Roots", "Sepultura", HEAVY, GUITAR),
            Song("Baile de Favela", "MC João", HEAVY, DRUMS),
            Song("País Tropical", "Jorge Ben Jor", DANCE, GUITAR),
            Song("Sossego", "Tim Maia", DANCE, BASS),
            Song("Evidências", "Chitãozinho & Xororó", SINGALONG, VOCALS),
            Song("Vou Festejar", "Beth Carvalho", SINGALONG, DRUMS),
            Song("Infiel", "Marília Mendonça", HEARTBREAK, VOCALS),
            Song("Atrás da Porta", "Elis Regina", HEARTBREAK, KEYS),
            Song("Balada do Louco", "Os Mutantes", TRIPPY, KEYS),
            Song("Cais", "Milton Nascimento", TRIPPY, VOCALS),
            Song("Aquarela", "Toquinho", CHILL, GUITAR),
            Song("Garota de Ipanema", "Tom Jobim", CHILL, KEYS)
        )
    )

    fun songsFor(genre: Genre): List<Song> = songs.getValue(genre)
}
