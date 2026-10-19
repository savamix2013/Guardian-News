package com.example.guardiannews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.guardiannews.ui.theme.GuardianNewsTheme

/*
 * Примітка щодо використання ШІ (AI Disclosure):
 * Під час виконання Лабораторної роботи №2 використовувався ШІ-асистент Gemini
 * для проєктування структури data class відповідно до JSON-схеми The Guardian API,
 * створення векторних ілюстрацій-заглушок у res/drawable та структурування
 * базової верстки карток новин у Jetpack Compose (Scaffold, Column, Row, Box).
 */

// ============================================================================
// 1. МОДЕЛЬ ДАНИХ (назви полів відповідають JSON-відповіді The Guardian API)
// У JSON-відповіді API масив лежить у response.results[], а картинка — у fields.thumbnail
// ============================================================================

data class GuardianFields(
    val thumbnail: String,
    @DrawableRes val thumbnailRes: Int
)

data class GuardianArticle(
    val id: String,
    val type: String,
    val sectionId: String,
    val sectionName: String,
    val webPublicationDate: String,
    val webTitle: String,
    val webUrl: String,
    val pillarName: String,
    val fields: GuardianFields,
    val isSaved: Boolean = false
)

// ============================================================================
// 2. СПИСОК ОБ'ЄКТІВ ОКРЕМО ВІД ВЕРСТКИ (3 статті для головного екрана)
// ============================================================================

val sampleArticles = listOf(
    GuardianArticle(
        id = "technology/2026/oct/19/android-16-security-ai-features",
        type = "article",
        sectionId = "technology",
        sectionName = "Technology",
        webPublicationDate = "2026-10-19T14:30:00Z",
        webTitle = "Android розширює захист приватності та локальну обробку даних на смартфонах",
        webUrl = "https://www.theguardian.com/technology/2026/oct/19/android-16-security-ai-features",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-tech-thumb.jpg",
            thumbnailRes = R.drawable.news_thumb_1
        ),
        isSaved = true
    ),
    GuardianArticle(
        id = "science/2026/oct/18/european-space-telescope-new-exoplanet-discovery",
        type = "article",
        sectionId = "science",
        sectionName = "Science",
        webPublicationDate = "2026-10-18T09:15:00Z",
        webTitle = "Європейський космічний телескоп зафіксував атмосферу на кам'янистій екзопланеті",
        webUrl = "https://www.theguardian.com/science/2026/oct/18/european-space-telescope-new-exoplanet-discovery",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-science-thumb.jpg",
            thumbnailRes = R.drawable.news_thumb_2
        ),
        isSaved = false
    ),
    GuardianArticle(
        id = "environment/2026/oct/17/clean-energy-grids-record-growth-europe",
        type = "article",
        sectionId = "environment",
        sectionName = "Environment",
        webPublicationDate = "2026-10-17T18:45:00Z",
        webTitle = "Сонячна та вітрова генерація в Європі встановила новий осінній рекорд потужності",
        webUrl = "https://www.theguardian.com/environment/2026/oct/17/clean-energy-grids-record-growth-europe",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-env-thumb.jpg",
            thumbnailRes = R.drawable.news_thumb_3
        ),
        isSaved = true
    )
)

// ============================================================================
// 3. ГОЛОВНА АКТИВНІСТЬ ТА ЕКРАН СТРІЧКИ НОВИН
// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuardianNewsTheme {
                NewsFeedScreen(
                    articles = sampleArticles,
                    defaultSection = "Technology"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    articles: List<GuardianArticle>,
    defaultSection: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Guardian News",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Стрічка новин · Розділ: $defaultSection",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            articles.forEach { article ->
                ArticleCard(article = article)
            }
        }
    }
}

// ============================================================================
// 4. КАРТКА ОБ'ЄКТА (використовує Box, Column, Row та модифікатори розмірів/відступів)
// ============================================================================

@Composable
fun ArticleCard(
    article: GuardianArticle,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Box: зображення статті + позначка розділу та статус збереження поверх зображення
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = article.fields.thumbnailRes),
                    contentDescription = article.webTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Позначка (бейдж) розділу поверх зображення (лівий верхній кут)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC052962))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = article.sectionName.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Позначка збереженої статті поверх зображення (правий верхній кут)
                if (article.isSaved) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xE6FFE500))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "★ Збережено",
                            color = Color(0xFF052962),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Column: назва статті та її характеристики під зображенням
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Назва об'єкта (webTitle з API)
                Text(
                    text = article.webTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Row №1: Характеристика 1 (Рубрика / Pillar) та Характеристика 2 (Дата публікації)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Рубрика: ${article.pillarName} (${article.sectionId})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = article.webPublicationDate.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row №2: Характеристика 3 (Тип матеріалу з API та локальний статус)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Тип: ${article.type}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = if (article.isSaved) "У закладках на пристрої" else "Лише онлайн",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (article.isSaved) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewsFeedScreenPreview() {
    GuardianNewsTheme {
        NewsFeedScreen(
            articles = sampleArticles,
            defaultSection = "Technology"
        )
    }
}