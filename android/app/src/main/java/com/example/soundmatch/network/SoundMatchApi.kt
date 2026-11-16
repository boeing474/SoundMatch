package com.example.soundmatch.network

import com.example.soundmatch.model.RatingUpdateRequest
import com.example.soundmatch.model.RecommendationRequest
import com.example.soundmatch.model.RecommendationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints da nossa API (os mesmos do Swagger em http://localhost:8000/docs).
interface SoundMatchApi {

    @POST("recommendations")
    suspend fun createRecommendation(@Body request: RecommendationRequest): RecommendationResponse

    @GET("recommendations")
    suspend fun listRecommendations(): List<RecommendationResponse>

    @PUT("recommendations/{id}")
    suspend fun updateRating(@Path("id") id: Int, @Body request: RatingUpdateRequest): RecommendationResponse

    // A API responde 204 (sem corpo), por isso Response<Unit>.
    @DELETE("recommendations/{id}")
    suspend fun deleteRecommendation(@Path("id") id: Int): Response<Unit>
}
