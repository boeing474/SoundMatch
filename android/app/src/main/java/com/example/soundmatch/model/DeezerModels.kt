package com.example.soundmatch.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Respostas da API do Deezer. Só mapeamos os campos que o app usa.
// As listas do Deezer vêm dentro de "data". Quando ele responde com erro
// (por exemplo, muitas chamadas em poucos segundos), "data" vem null.

data class DeezerList<T>(
    val data: List<T>?
)

// Serializable porque os artistas parecidos vão do perfil para o quiz pelo putExtra.
data class DeezerArtist(
    val id: Long,
    val name: String,
    @SerializedName("picture_medium") val picture: String?,
    @SerializedName("nb_fan") val fans: Int?
) : Serializable

data class DeezerAlbum(
    val title: String?,
    @SerializedName("cover_big") val cover: String?
)

// "rank" é a popularidade da música no Deezer (quanto maior, mais tocada).
// "isrc" é o código internacional da gravação; só vem quando pedimos a música pelo id.
data class DeezerTrack(
    val id: Long,
    val title: String,
    val duration: Int,
    val rank: Int,
    val isrc: String?,
    val artist: DeezerArtist,
    val album: DeezerAlbum?
)
