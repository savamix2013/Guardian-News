package com.example.guardiannews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guardiannews.ui.theme.GuardianNewsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/*
 * Примітка щодо використання ШІ (AI Disclosure):
 * Під час виконання Лабораторної роботи №4 використовувався ШІ-асистент Google Gemini
 * для винесення стану головного екрана в окремий незмінний data class NewsFeedUiState,
 * реалізації NewsFeedViewModel із захищеним від зовнішнього запису StateFlow,
 * оновлення стану через copy() та підключення ViewModel у Composable штатним методом viewModel().
 */

// ============================================================================
// 1. МОДЕЛЬ ДАНИХ (назви полів відповідають JSON-відповіді The Guardian API)
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
// 2. ПОЧАТКОВІ ДАНІ (6 ОБ'ЄКТІВ ТА СПИСОК РОЗДІЛІВ)
// ============================================================================

val availableSections = listOf(
    "All",
    "Technology",
    "Science",
    "Environment",
    "Sport"
)

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
            thumbnail = "https://media.guim.co.uk/sample-tech-thumb-1.jpg",
            thumbnailRes = R.drawable.news_thumb_1
        ),
        isSaved = true
    ),
    GuardianArticle(
        id = "technology/2026/oct/18/open-source-mobile-browsers-eu-regulations",
        type = "article",
        sectionId = "technology",
        sectionName = "Technology",
        webPublicationDate = "2026-10-18T11:20:00Z",
        webTitle = "Нові стандарти відкритих мобільних екосистем пришвидшують роботу застосунків",
        webUrl = "https://www.theguardian.com/technology/2026/oct/18/open-source-mobile-browsers-eu-regulations",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-tech-thumb-2.jpg",
            thumbnailRes = R.drawable.news_thumb_1
        ),
        isSaved = false
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
            thumbnail = "https://media.guim.co.uk/sample-science-thumb-1.jpg",
            thumbnailRes = R.drawable.news_thumb_2
        ),
        isSaved = false
    ),
    GuardianArticle(
        id = "science/2026/oct/16/quantum-sensors-navigation-without-satellites",
        type = "article",
        sectionId = "science",
        sectionName = "Science",
        webPublicationDate = "2026-10-16T16:05:00Z",
        webTitle = "Фізики протестували квантові сенсори для точної навігації без супутникового сигналу",
        webUrl = "https://www.theguardian.com/science/2026/oct/16/quantum-sensors-navigation-without-satellites",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-science-thumb-2.jpg",
            thumbnailRes = R.drawable.news_thumb_2
        ),
        isSaved = true
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
            thumbnail = "https://media.guim.co.uk/sample-env-thumb-1.jpg",
            thumbnailRes = R.drawable.news_thumb_3
        ),
        isSaved = true
    ),
    GuardianArticle(
        id = "environment/2026/oct/15/carpathian-forest-restoration-monitoring-drones",
        type = "article",
        sectionId = "environment",
        sectionName = "Environment",
        webPublicationDate = "2026-10-15T12:10:00Z",
        webTitle = "Супутниковий моніторинг допомагає відновлювати гірські лісові екосистеми у Карпатах",
        webUrl = "https://www.theguardian.com/environment/2026/oct/15/carpathian-forest-restoration-monitoring-drones",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-env-thumb-2.jpg",
            thumbnailRes = R.drawable.news_thumb_3
        ),
        isSaved = false
    )
)

// ============================================================================
// 3. ОКРЕМИЙ DATA CLASS СТАНУ ЕКРАНА З ПОЛЯМИ VAL ТА VIEWMODEL (Лабораторна №4)
// ============================================================================

data class NewsFeedUiState(
    val sections: List<String> = availableSections,
    val selectedSection: String = availableSections.first(),
    val allArticles: List<GuardianArticle> = sampleArticles,
    val filteredArticles: List<GuardianArticle> = sampleArticles
)

class NewsFeedViewModel : ViewModel() {

    // Стан закритий від запису ззовні (private MutableStateFlow)
    private val _uiState = MutableStateFlow(NewsFeedUiState())

    // Назовні відкритий лише потік для читання (StateFlow)
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    // Функція-подія для вибору розділу (змінює стан виключно через copy())
    fun selectSection(section: String) {
        _uiState.update { currentState ->
            val filtered = if (section == "All") {
                currentState.allArticles
            } else {
                currentState.allArticles.filter { article ->
                    article.sectionName.equals(section, ignoreCase = true)
                }
            }
            currentState.copy(
                selectedSection = section,
                filteredArticles = filtered
            )
        }
    }

    // Функція-подія для скидання фільтра на початковий розділ "All"
    fun resetFilter() {
        selectSection("All")
    }
}

// ============================================================================
// 4. ГОЛОВНА АКТИВНІСТЬ ТА ЕКРАН СТРІЧКИ (БЕЗ remember ДЛЯ СТАНУ ЕКРАНА)
// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuardianNewsTheme {
                // ViewModel отримується всередині NewsFeedScreen штатним способом viewModel(),
                // а не створюється конструктором
                NewsFeedScreen()
            }
        }
    }
}

@Composable
fun NewsFeedScreen(
    modifier: Modifier = Modifier,
    newsViewModel: NewsFeedViewModel = viewModel()
) {
    // Підписка на незмінний стан із ViewModel (remember для стану вибору прибрано з екрана)
    val uiState by newsViewModel.uiState.collectAsState()

    NewsFeedContent(
        uiState = uiState,
        onSectionSelected = newsViewModel::selectSection,
        onResetFilter = newsViewModel::resetFilter,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedContent(
    uiState: NewsFeedUiState,
    onSectionSelected: (String) -> Unit,
    onResetFilter: () -> Unit,
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
                            text = "Розділ: ${uiState.selectedSection} · Статей: ${uiState.filteredArticles.size}",
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
        ) {
            // Ряд перемикачів угорі екрана (отримує список варіантів, обране значення і лямбду)
            SectionFilterRow(
                sections = uiState.sections,
                selectedSection = uiState.selectedSection,
                onSectionSelected = onSectionSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Відображення відфільтрованого списку або повідомлення "Нічого не знайдено"
            if (uiState.filteredArticles.isEmpty()) {
                EmptySectionState(
                    sectionName = uiState.selectedSection,
                    onResetFilter = onResetFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uiState.filteredArticles.forEach { article ->
                        ArticleCard(article = article)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. РЯД ПЕРЕМИКАЧІВ РОЗДІЛІВ (Stateless Composable: варіанти + обране + лямбда)
// ============================================================================

@Composable
fun SectionFilterRow(
    sections: List<String>,
    selectedSection: String,
    onSectionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        sections.forEach { section ->
            val isSelected = section == selectedSection
            FilterChip(
                selected = isSelected,
                onClick = { onSectionSelected(section) },
                label = {
                    Text(
                        text = if (section == "All") "Усі новини" else section,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ============================================================================
// 6. СТАН "НІЧОГО НЕ ЗНАЙДЕНО" (коли в обраному розділі немає статей)
// ============================================================================

@Composable
fun EmptySectionState(
    sectionName: String,
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Новин не знайдено",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "У розділі «$sectionName» наразі немає збережених або завантажених статей. Спробуйте обрати іншу рубрику.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onResetFilter) {
                    Text(text = "Показати всі новини")
                }
            }
        }
    }
}

// ============================================================================
// 7. КАРТКА ОБ'ЄКТА (Box + Column + Row)
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

                // Row №1: Рубрика та дата публікації
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

                // Row №2: Тип матеріалу та локальний статус
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
        NewsFeedContent(
            uiState = NewsFeedUiState(),
            onSectionSelected = {},
            onResetFilter = {}
        )
    }
}