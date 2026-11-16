package com.example.soundmatch.model

import com.google.gson.annotations.SerializedName

/*
 * Classes que espelham o JSON da API FastAPI (convertidas automaticamente pelo Gson).
 * @SerializedName liga o nome em snake_case do JSON ao nome em camelCase do Kotlin.
 */

/** Corpo do POST /recommendations. */
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
    @SerializedName("personal_note") val personalNote: String
) {
    companion object {
        /** Monta o corpo do POST a partir do resultado do quiz + nota digitada. */
        fun from(result: SoundResult, personalNote: String) = RecommendationRequest(
            name = result.profile.name,
            favoriteArtist = result.profile.favoriteArtist,
            listeningMode = result.profile.listeningMode,
            playsInstrument = result.profile.playsInstrument,
            score = result.score,
            archetype = result.archetype.title,
            recommendedSong = result.song.title,
            songArtist = result.song.artist,
            subgenres = result.subgenresLabel,
            personalNote = personalNote
        )
    }
}

/** Corpo do PUT /recommendations/{id}: só a nota pessoal é editável. */
data class NoteUpdateRequest(
    @SerializedName("personal_note") val personalNote: String
)

/** Usuário que vem aninhado em cada recomendação (tabela users). */
data class UserResponse(
    val id: Int,
    val name: String,
    @SerializedName("favorite_artist") val favoriteArtist: String,
    @SerializedName("listening_mode") val listeningMode: String,
    @SerializedName("plays_instrument") val playsInstrument: Boolean
)

/** Recomendação retornada pela API (tabela recommendations + usuário). */
data class RecommendationResponse(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val score: Int,
    val archetype: String,
    @SerializedName("recommended_song") val recommendedSong: String,
    @SerializedName("song_artist") val songArtist: String,
    val subgenres: String,
    @SerializedName("personal_note") val personalNote: String,
    @SerializedName("created_at") val createdAt: String,
    val user: UserResponse
)
