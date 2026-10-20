package com.example.guardiannews.model

import androidx.annotation.DrawableRes

data class GuardianFields(
    val thumbnail: String,
    @DrawableRes val thumbnailRes: Int,
    val trailText: String,
    val byline: String,
    val wordcount: String,
    val bodyText: String
)

data class GuardianArticle(
    val id: String,
    val type: String,
    val sectionId: String,
    val sectionName: String,
    val webPublicationDate: String,
    val webTitle: String,
    val webUrl: String,
    val apiUrl: String,
    val pillarName: String,
    val fields: GuardianFields,
    val isSaved: Boolean = false
)

val availableSections = listOf(
    "All",
    "Technology",
    "Science",
    "Environment",
    "Sport"
)
