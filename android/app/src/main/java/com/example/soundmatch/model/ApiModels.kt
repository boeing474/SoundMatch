package com.example.soundmatch.model

import com.google.gson.annotations.SerializedName

// JSON da nossa API. @SerializedName liga o snake_case do Python ao camelCase do Kotlin.
// Os nomes das colunas ficaram da primeira versão: "archetype" guarda o título do card do resultado
// (ex.: "Parecido com BTS" ou "Pop & K-pop"), "score" guarda o % de match e "subgenres" a linha
// de detalhe do card (ex.: "Dançante · Hit do momento" ou os outros estilos marcados).
// "surprise" diz se a música saiu do estilo de sempre no "Quero me arriscar" (aí o histórico não mostra o %).

data class RecommendationRequest(
    val name: String,
    @SerializedName("favorite_artist") val favoriteArtist: String,
    @SerializedName("listening_mode") val listeningMode: String,
    @SerializedName("plays_instrument") val playsInstrument: Boolean,
    val score: Int,
    val archetype: String,
    @SerializedName("recommended_song") val recommendedSong: String,
    @SerializedName("song_artist") val songArtist: String,
    val subgenres: String,
    val liked: Boolean,
    val surprise: Boolean
) {
    companion object {
        fun from(result: SoundResult, liked: Boolean, cardTitle: String, cardDetail: String) = RecommendationRequest(
            name = result.profile.name,
            favoriteArtist = result.profile.favoriteArtist,
            listeningMode = result.profile.situation.label,
            playsInstrument = false,   // o app não pergunta mais; a coluna continua na API
            score = result.match,
            archetype = cardTitle,
            recommendedSong = result.song.title,
            songArtist = result.song.artist,
            subgenres = cardDetail,
            liked = liked,
            surprise = result.surprise
        )
    }
}

data class RatingUpdateRequest(
    val liked: Boolean
)

data class UserResponse(
    val id: Int,
    val name: String,
    @SerializedName("favorite_artist") val favoriteArtist: String,
    @SerializedName("listening_mode") val listeningMode: String,
    @SerializedName("plays_instrument") val playsInstrument: Boolean
)

data class RecommendationResponse(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val score: Int,
    val archetype: String,
    @SerializedName("recommended_song") val recommendedSong: String,
    @SerializedName("song_artist") val songArtist: String,
    val subgenres: String,
    val liked: Boolean,
    val surprise: Boolean,
    @SerializedName("created_at") val createdAt: String,
    val user: UserResponse
)
