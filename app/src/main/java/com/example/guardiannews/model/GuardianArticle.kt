package com.example.guardiannews.model

import kotlinx.serialization.Serializable
import kotlin.math.ceil

@Serializable
data class GuardianSearchResponse(
    val response: GuardianResponseBody
)

@Serializable
data class GuardianSingleItemResponse(
    val response: GuardianSingleItemBody
)

@Serializable
data class GuardianResponseBody(
    val status: String = "",
    val results: List<GuardianArticleDto> = emptyList(),
    val editorsPicks: List<GuardianArticleDto> = emptyList()
)

@Serializable
data class GuardianSingleItemBody(
    val status: String = "",
    val content: GuardianArticleDto? = null
)

@Serializable
data class GuardianArticleDto(
    val id: String,
    val sectionId: String = "",
    val sectionName: String = "",
    val webPublicationDate: String = "",
    val webTitle: String = "",
    val webUrl: String = "",
    val fields: GuardianFieldsDto? = null
)

@Serializable
data class GuardianFieldsDto(
    val thumbnail: String? = null,
    val trailText: String? = null,
    val byline: String? = null,
    val bodyText: String? = null,
    val wordcount: String? = null
)

data class GuardianArticle(
    val id: String,
    val sectionId: String,
    val sectionName: String,
    val publishedAt: String,
    val title: String,
    val webUrl: String,
    val thumbnailUrl: String?,
    val trailText: String,
    val byline: String?,
    val bodyText: String?,
    val readingTimeMinutes: Int?,
    val isEditorsPick: Boolean = false
)

fun GuardianArticleDto.toDomain(isEditorsPick: Boolean = false): GuardianArticle {
    val words = fields?.wordcount?.toIntOrNull()
    val readMinutes = words?.takeIf { it > 0 }?.let { ceil(it / 200.0).toInt().coerceAtLeast(1) }

    return GuardianArticle(
        id = id,
        sectionId = sectionId,
        sectionName = sectionName.ifBlank { "News" },
        publishedAt = webPublicationDate,
        title = webTitle.trim(),
        webUrl = webUrl,
        thumbnailUrl = normalizeImageUrl(fields?.thumbnail),
        trailText = fields?.trailText
            ?.replace(Regex("<[^>]*>"), "")
            ?.trim()
            .orEmpty(),
        byline = fields?.byline?.trim()?.takeIf { it.isNotEmpty() },
        bodyText = fields?.bodyText?.trim()?.takeIf { it.isNotEmpty() },
        readingTimeMinutes = readMinutes,
        isEditorsPick = isEditorsPick
    )
}

fun GuardianArticleDto.toDomainModel(isEditorsPick: Boolean = false): GuardianArticle =
    toDomain(isEditorsPick)

fun normalizeImageUrl(rawUrl: String?): String? {
    val cleaned = rawUrl?.trim().orEmpty()
    if (cleaned.isEmpty()) return null

    val normalized = when {
        cleaned.startsWith("https://", ignoreCase = true) -> cleaned
        cleaned.startsWith("http://", ignoreCase = true) ->
            "https://" + cleaned.substring(7)
        cleaned.startsWith("//") -> "https:$cleaned"
        cleaned.startsWith("/") -> "https://media.guim.co.uk$cleaned"
        else -> "https://$cleaned"
    }

    return normalized.takeIf { it.startsWith("https://") && it.length > 8 }
}
