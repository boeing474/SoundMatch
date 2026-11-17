package com.example.soundmatch.logic

import com.example.soundmatch.model.Energy.CHILL
import com.example.soundmatch.model.Energy.DANCE
import com.example.soundmatch.model.Energy.HEARTBREAK
import com.example.soundmatch.model.Energy.HEAVY
import com.example.soundmatch.model.Energy.SINGALONG
import com.example.soundmatch.model.Energy.TRIPPY
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Song

// O catálogo reserva, usado quando não há artistas parecidos para procurar a música.
// 12 músicas por gênero: 2 para cada energia.
object SongCatalog {

    private val songs: Map<Genre, List<Song>> = mapOf(
        Genre.POP to listOf(
            Song("God's Menu", "Stray Kids", HEAVY),
            Song("good 4 u", "Olivia Rodrigo", HEAVY),
            Song("Espresso", "Sabrina Carpenter", DANCE),
            Song("Uptown Funk", "Mark Ronson ft. Bruno Mars", DANCE),
            Song("Shake It Off", "Taylor Swift", SINGALONG),
            Song("Dancing Queen", "ABBA", SINGALONG),
            Song("Someone Like You", "Adele", HEARTBREAK),
            Song("Love wins all", "IU", HEARTBREAK),
            Song("Video Games", "Lana Del Rey", TRIPPY),
            Song("Ditto", "NewJeans", TRIPPY),
            Song("Banana Pancakes", "Jack Johnson", CHILL),
            Song("Golden Hour", "JVKE", CHILL)
        ),
        Genre.ROCK to listOf(
            Song("Black Dog", "Led Zeppelin", HEAVY),
            Song("Killing in the Name", "Rage Against the Machine", HEAVY),
            Song("Another One Bites the Dust", "Queen", DANCE),
            Song("Take Me Out", "Franz Ferdinand", DANCE),
            Song("Mr. Brightside", "The Killers", SINGALONG),
            Song("Livin' on a Prayer", "Bon Jovi", SINGALONG),
            Song("Creep", "Radiohead", HEARTBREAK),
            Song("Everybody Hurts", "R.E.M.", HEARTBREAK),
            Song("Riders on the Storm", "The Doors", TRIPPY),
            Song("Comfortably Numb", "Pink Floyd", TRIPPY),
            Song("Here Comes the Sun", "The Beatles", CHILL),
            Song("Dreams", "Fleetwood Mac", CHILL)
        ),
        Genre.HIPHOP to listOf(
            Song("Lose Yourself", "Eminem", HEAVY),
            Song("X Gon' Give It to Ya", "DMX", HEAVY),
            Song("In Da Club", "50 Cent", DANCE),
            Song("Crazy in Love", "Beyoncé ft. Jay-Z", DANCE),
            Song("Hey Ya!", "OutKast", SINGALONG),
            Song("No Diggity", "Blackstreet", SINGALONG),
            Song("Kill Bill", "SZA", HEARTBREAK),
            Song("Thinkin Bout You", "Frank Ocean", HEARTBREAK),
            Song("Feel Good Inc.", "Gorillaz", TRIPPY),
            Song("Flashing Lights", "Kanye West", TRIPPY),
            Song("Bad Habit", "Steve Lacy", CHILL),
            Song("Sunflower", "Post Malone & Swae Lee", CHILL)
        ),
        Genre.ELECTRONIC to listOf(
            Song("Scary Monsters and Nice Sprites", "Skrillex", HEAVY),
            Song("Firestarter", "The Prodigy", HEAVY),
            Song("Losing It", "FISHER", DANCE),
            Song("Lady (Hear Me Tonight)", "Modjo", DANCE),
            Song("Wake Me Up", "Avicii", SINGALONG),
            Song("Clarity", "Zedd ft. Foxes", SINGALONG),
            Song("Faded", "Alan Walker", HEARTBREAK),
            Song("Silence", "Marshmello ft. Khalid", HEARTBREAK),
            Song("Strobe", "deadmau5", TRIPPY),
            Song("Teardrop", "Massive Attack", TRIPPY),
            Song("Sunset Lover", "Petit Biscuit", CHILL),
            Song("Say My Name", "ODESZA ft. Zyra", CHILL)
        ),
        Genre.BRAZILIAN to listOf(
            Song("Roots Bloody Roots", "Sepultura", HEAVY),
            Song("Baile de Favela", "MC João", HEAVY),
            Song("País Tropical", "Jorge Ben Jor", DANCE),
            Song("Sossego", "Tim Maia", DANCE),
            Song("Evidências", "Chitãozinho & Xororó", SINGALONG),
            Song("Vou Festejar", "Beth Carvalho", SINGALONG),
            Song("Infiel", "Marília Mendonça", HEARTBREAK),
            Song("Atrás da Porta", "Elis Regina", HEARTBREAK),
            Song("Balada do Louco", "Os Mutantes", TRIPPY),
            Song("Cais", "Milton Nascimento", TRIPPY),
            Song("Aquarela", "Toquinho", CHILL),
            Song("Garota de Ipanema", "Tom Jobim", CHILL)
        )
    )

    fun songsFor(genre: Genre): List<Song> = songs.getValue(genre)
}
