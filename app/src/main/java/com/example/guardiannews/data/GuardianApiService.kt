package com.example.guardiannews.data

import com.example.guardiannews.model.GuardianSearchResponse
import com.example.guardiannews.model.GuardianSingleItemResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GuardianApiService {

    @GET("search")
    suspend fun getArticlesByIds(
        @Query("ids") ids: String,
        @Query("api-key") apiKey: String,
        @Query("show-fields") showFields: String = "thumbnail,trailText,bodyText,byline,wordcount",
        @Query("page-size") pageSize: Int = 50
    ): GuardianSearchResponse

    @GET("search")
    suspend fun searchArticles(
        @Query("api-key") apiKey: String,
        @Query("section") section: String? = null,
        @Query("q") query: String? = null,
        @Query("page-size") pageSize: Int = 20,
        @Query("show-fields") showFields: String = "thumbnail,trailText,byline"
    ): GuardianSearchResponse

    @GET("search")
    suspend fun getEditorsPicks(
        @Query("api-key") apiKey: String,
        @Query("section") section: String? = null,
        @Query("page-size") pageSize: Int = 5,
        @Query("show-editors-picks") showEditorsPicks: Boolean = true,
        @Query("show-fields") showFields: String = "thumbnail,trailText,byline"
    ): GuardianSearchResponse

    @GET("{articleId}")
    suspend fun getArticleDetails(
        @Path(value = "articleId", encoded = true) articleId: String,
        @Query("api-key") apiKey: String,
        @Query("show-fields") showFields: String = "thumbnail,trailText,bodyText,byline,wordcount"
    ): GuardianSingleItemResponse

    companion object {
        private const val BASE_URL = "https://content.guardianapis.com/"

        fun create(): GuardianApiService {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(GuardianApiService::class.java)
        }
    }
}
