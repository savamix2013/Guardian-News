package com.example.guardiannews

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ============================================================================
// 1. КЛАСИ ВІДПОВІДІ API (DTO) — ОКРЕМО ВІД МОДЕЛІ ЗАСТОСУНКУ (Лабораторна №7)
// Усі поля зроблені nullable з дефолтними значеннями, бо частина полів у API
// може приходити порожньою або не приходити зовсім
// ============================================================================

@Serializable
data class GuardianSearchResponseDto(
    val response: GuardianResponseBodyDto? = null
)

@Serializable
data class GuardianResponseBodyDto(
    val status: String? = null,
    val total: Int? = null,
    val results: List<GuardianArticleDto> = emptyList()
)

@Serializable
data class GuardianArticleDto(
    val id: String = "",
    val type: String? = null,
    val sectionId: String? = null,
    val sectionName: String? = null,
    val webPublicationDate: String? = null,
    val webTitle: String? = null,
    val webUrl: String? = null,
    val apiUrl: String? = null,
    val pillarName: String? = null,
    val fields: GuardianFieldsDto? = null
)

@Serializable
data class GuardianFieldsDto(
    val thumbnail: String? = null,
    val trailText: String? = null,
    val byline: String? = null,
    val wordcount: String? = null,
    val bodyText: String? = null
)

// ============================================================================
// 2. ПЕРЕТВОРЕННЯ (МАППЕР) МІЖ КЛАСАМИ ВІДПОВІДІ API ТА МОДЕЛЛЮ ЗАСТОСУНКУ
// Опрацьовує порожні або відсутні поля, щоб застосунок ніколи не падав
// ============================================================================

fun GuardianArticleDto.toDomainModel(isSaved: Boolean = false): GuardianArticle {
    val safeSectionId = sectionId?.takeIf { it.isNotBlank() } ?: "news"
    val safeSectionName = sectionName?.takeIf { it.isNotBlank() } ?: "News"

    // Поки що (до Лабораторної №10) використовуємо локальні ілюстрації з res/drawable
    val fallbackDrawable = when (safeSectionId.lowercase()) {
        "technology" -> R.drawable.news_thumb_1
        "science" -> R.drawable.news_thumb_2
        "environment" -> R.drawable.news_thumb_3
        else -> R.drawable.news_thumb_1
    }

    // Очищення trailText від можливих HTML-тегів, які іноді повертає The Guardian API
    val cleanTrailText = fields?.trailText
        ?.replace(Regex("<[^>]*>"), "")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "Короткий опис для цього матеріалу не надано у відповіді API."

    val cleanBodyText = fields?.bodyText
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "Повний текстовий вміст для цього матеріалу відсутній або оновлюється в режимі наживо."

    return GuardianArticle(
        id = id.ifBlank { "unknown-id-${System.currentTimeMillis()}" },
        type = type?.takeIf { it.isNotBlank() } ?: "article",
        sectionId = safeSectionId,
        sectionName = safeSectionName,
        webPublicationDate = webPublicationDate?.takeIf { it.isNotBlank() } ?: "Дата невідома",
        webTitle = webTitle?.takeIf { it.isNotBlank() } ?: "Без заголовка",
        webUrl = webUrl?.takeIf { it.isNotBlank() } ?: "https://www.theguardian.com",
        apiUrl = apiUrl?.takeIf { it.isNotBlank() } ?: "https://content.guardianapis.com",
        pillarName = pillarName?.takeIf { it.isNotBlank() } ?: "News",
        fields = GuardianFields(
            thumbnail = fields?.thumbnail?.takeIf { it.isNotBlank() }.orEmpty(),
            thumbnailRes = fallbackDrawable,
            trailText = cleanTrailText,
            byline = fields?.byline?.takeIf { it.isNotBlank() } ?: "Редакція The Guardian",
            wordcount = fields?.wordcount?.takeIf { it.isNotBlank() } ?: "—",
            bodyText = cleanBodyText
        ),
        isSaved = isSaved
    )
}

// ============================================================================
// 3. ІНТЕРФЕЙС RETROFIT ТА ЄДИНИЙ ЕКЗЕМПЛЯР НА ЗАСТОСУНОК (SINGLETON)
// ============================================================================

interface GuardianApiService {

    @GET("search")
    suspend fun searchArticles(
        @Query("section") section: String? = null,
        @Query("show-fields") showFields: String = "thumbnail,trailText,byline,wordcount,bodyText",
        @Query("page-size") pageSize: Int = 20,
        @Query("api-key") apiKey: String = BuildConfig.GUARDIAN_API_KEY
    ): GuardianSearchResponseDto
}

object GuardianNetwork {
    private const val BASE_URL = "https://content.guardianapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // Один екземпляр Retrofit на весь застосунок
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val api: GuardianApiService by lazy {
        retrofit.create(GuardianApiService::class.java)
    }
}