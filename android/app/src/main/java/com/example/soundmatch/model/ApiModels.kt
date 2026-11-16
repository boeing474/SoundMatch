package com.example.soundmatch.model

import com.google.gson.annotations.SerializedName

// JSON da nossa API. @SerializedName liga o snake_case do Python ao camelCase do Kotlin.

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

data class NoteUpdateRequest(
    @SerializedName("personal_note") val personalNote: String
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
    @SerializedName("personal_note") val personalNote: String,
    @SerializedName("created_at") val createdAt: String,
    val user: UserResponse
)
