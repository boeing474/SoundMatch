package com.example.soundmatch.network

import com.example.soundmatch.model.NoteUpdateRequest
import com.example.soundmatch.model.RecommendationRequest
import com.example.soundmatch.model.RecommendationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * REQUISITO: CRUD Retrofit. Cada função corresponde a um endpoint da API FastAPI
 * (documentados no Swagger em http://localhost:8000/docs).
 * "suspend" = a chamada roda em segundo plano, sem travar a tela.
 */
interface SoundMatchApi {

    /** CREATE: salva a recomendação (cria ou reaproveita o usuário). */
    @POST("recommendations")
    suspend fun createRecommendation(@Body request: RecommendationRequest): RecommendationResponse

    /** READ: lista todas as recomendações, com o usuário aninhado. */
    @GET("recommendations")
    suspend fun listRecommendations(): List<RecommendationResponse>

    /** UPDATE: altera a nota pessoal. */
    @PUT("recommendations/{id}")
    suspend fun updateNote(@Path("id") id: Int, @Body request: NoteUpdateRequest): RecommendationResponse

    /** DELETE: remove a recomendação (a API responde 204, sem corpo). */
    @DELETE("recommendations/{id}")
    suspend fun deleteRecommendation(@Path("id") id: Int): Response<Unit>
}
