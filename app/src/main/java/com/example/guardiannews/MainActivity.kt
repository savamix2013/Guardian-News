package com.example.guardiannews

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.guardiannews.ui.theme.GuardianNewsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Примітка щодо використання ШІ (AI Disclosure):
 * Під час виконання Лабораторних робіт №1–7 використовувався ШІ-асистент Google Gemini
 * для проєктування моделі даних, верстки Jetpack Compose, налаштування ViewModel, NavHost,
 * Preferences DataStore та підключення The Guardian API через Retrofit і kotlinx.serialization.
 */

// ============================================================================
// 1. МОДЕЛЬ ДАНИХ ЗАСТОСУНКУ (вписаний у код список sampleArticles прибрано!)
// ============================================================================

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

// ============================================================================
// 2. СТАН ЕКРАНА ТА VIEWMODEL ІЗ ЗАПИТАМИ ДО THE GUARDIAN API (Лабораторна №7)
// ============================================================================

data class NewsFeedUiState(
    val sections: List<String> = availableSections,
    val defaultSection: String = UserPreferencesRepository.DEFAULT_SECTION_FALLBACK,
    val selectedSection: String = UserPreferencesRepository.DEFAULT_SECTION_FALLBACK,
    val allArticles: List<GuardianArticle> = emptyList(),
    val filteredArticles: List<GuardianArticle> = emptyList(),
    val isLoading: Boolean = false
)

class NewsFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application.applicationContext)
    private val savedArticleIds = mutableSetOf<String>()

    private val _uiState = MutableStateFlow(NewsFeedUiState(isLoading = true))
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    init {
        // При відкритті застосунку та при зміні розділу за замовчуванням у DataStore
        // оновлюємо обраний розділ і виконуємо запит до API
        viewModelScope.launch {
            preferencesRepository.defaultSectionFlow.collect { savedDefaultSection ->
                _uiState.update { currentState ->
                    currentState.copy(
                        defaultSection = savedDefaultSection,
                        selectedSection = savedDefaultSection
                    )
                }
                loadArticles(savedDefaultSection)
            }
        }
    }

    // Виклик запиту при зміні вибору розділу на головному екрані (з роботи 3)
    fun selectSection(section: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedSection = section)
        }
        loadArticles(section)
    }

    fun resetFilter() {
        selectSection(_uiState.value.defaultSection)
    }

    fun saveDefaultSection(section: String) {
        viewModelScope.launch {
            preferencesRepository.saveDefaultSection(section)
        }
    }

    // Запит до The Guardian API через Retrofit
    fun loadArticles(section: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val sectionParam = if (section.equals("All", ignoreCase = true)) {
                null
            } else {
                section.lowercase()
            }

            val loadedArticles = try {
                val response = GuardianNetwork.api.searchArticles(section = sectionParam)
                response.response?.results.orEmpty().map { dto ->
                    dto.toDomainModel(isSaved = savedArticleIds.contains(dto.id))
                }
            } catch (e: Exception) {
                // Повноцінна обробка помилок буде у роботі №8; тут головне, щоб застосунок не падав
                emptyList()
            }

            _uiState.update { currentState ->
                currentState.copy(
                    allArticles = loadedArticles,
                    filteredArticles = loadedArticles,
                    isLoading = false
                )
            }
        }
    }

    fun toggleSaved(articleId: String) {
        if (savedArticleIds.contains(articleId)) {
            savedArticleIds.remove(articleId)
        } else {
            savedArticleIds.add(articleId)
        }

        _uiState.update { currentState ->
            val updated = currentState.allArticles.map { article ->
                if (article.id == articleId) {
                    article.copy(isSaved = savedArticleIds.contains(articleId))
                } else {
                    article
                }
            }
            currentState.copy(
                allArticles = updated,
                filteredArticles = updated
            )
        }
    }
}

// ============================================================================
// 3. НАВІГАЦІЯ (NavHost): ГОЛОВНИЙ ЕКРАН, ДЕТАЛІ СТАТТІ ТА НАЛАШТУВАННЯ
// ============================================================================

object AppDestinations {
    const val FEED_ROUTE = "feed"
    const val SETTINGS_ROUTE = "settings"
    const val ARTICLE_ID_ARG = "articleId"
    const val DETAIL_ROUTE_TEMPLATE = "article/{$ARTICLE_ID_ARG}"

    fun createDetailRoute(articleId: String): String {
        return "article/${Uri.encode(articleId)}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuardianNewsTheme {
                GuardianNewsApp()
            }
        }
    }
}

@Composable
fun GuardianNewsApp(
    newsViewModel: NewsFeedViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.FEED_ROUTE
    ) {
        composable(route = AppDestinations.FEED_ROUTE) {
            NewsFeedScreen(
                newsViewModel = newsViewModel,
                onItemClick = { articleId ->
                    navController.navigate(AppDestinations.createDetailRoute(articleId))
                },
                onSettingsClick = {
                    navController.navigate(AppDestinations.SETTINGS_ROUTE)
                }
            )
        }

        composable(
            route = AppDestinations.DETAIL_ROUTE_TEMPLATE,
            arguments = listOf(
                navArgument(AppDestinations.ARTICLE_ID_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedId = backStackEntry.arguments?.getString(AppDestinations.ARTICLE_ID_ARG).orEmpty()
            val articleId = Uri.decode(encodedId)

            ArticleDetailScreen(
                articleId = articleId,
                newsViewModel = newsViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                newsViewModel = newsViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// ============================================================================
// 4. ГОЛОВНИЙ ЕКРАН СТРІЧКИ (показує індикатор завантаження під час запиту)
// ============================================================================

@Composable
fun NewsFeedScreen(
    onItemClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    newsViewModel: NewsFeedViewModel = viewModel()
) {
    val uiState by newsViewModel.uiState.collectAsState()

    NewsFeedContent(
        uiState = uiState,
        onSectionSelected = newsViewModel::selectSection,
        onResetFilter = newsViewModel::resetFilter,
        onItemClick = onItemClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedContent(
    uiState: NewsFeedUiState,
    onSectionSelected: (String) -> Unit,
    onResetFilter: () -> Unit,
    onItemClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
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
                            text = "За замовч.: ${uiState.defaultSection} · Обрано: ${uiState.selectedSection} (${uiState.filteredArticles.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Налаштування"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SectionFilterRow(
                sections = uiState.sections,
                selectedSection = uiState.selectedSection,
                onSectionSelected = onSectionSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Поки триває мережевий запит — показуємо індикатор завантаження (Лабораторна №7)
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Завантаження новин із The Guardian API...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (uiState.filteredArticles.isEmpty()) {
                EmptySectionState(
                    sectionName = uiState.selectedSection,
                    onResetFilter = onResetFilter,
                    onTestMissingArticleClick = {
                        onItemClick("missing/2026/oct/99/non-existent-article")
                    },
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
                        ArticleCard(
                            article = article,
                            onClick = { onItemClick(article.id) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. ЕКРАН НАЛАШТУВАНЬ (Лабораторна №6)
// ============================================================================

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    newsViewModel: NewsFeedViewModel = viewModel()
) {
    val uiState by newsViewModel.uiState.collectAsState()

    SettingsContent(
        sections = uiState.sections,
        savedDefaultSection = uiState.defaultSection,
        onDefaultSectionSelected = newsViewModel::saveDefaultSection,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    sections: List<String>,
    savedDefaultSection: String,
    onDefaultSectionSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Налаштування",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Розділ новин за замовчуванням",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Обраний розділ зберігається у Preferences DataStore. При запуску застосунку та при зміні цього параметра автоматично виконується запит до The Guardian API для обраної рубрики.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    sections.forEach { section ->
                        val isSelected = section == savedDefaultSection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onDefaultSectionSelected(section) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onDefaultSectionSelected(section) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (section == "All") "Усі новини (All)" else section,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Text(
                                        text = "Збережено в DataStore як стартовий розділ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 6. ЕКРАН ДЕТАЛЕЙ ОБ'ЄКТА (показує справжні поля, завантажені з API)
// ============================================================================

@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    newsViewModel: NewsFeedViewModel = viewModel()
) {
    val uiState by newsViewModel.uiState.collectAsState()
    val article = uiState.allArticles.find { it.id == articleId }

    ArticleDetailContent(
        articleId = articleId,
        article = article,
        onBackClick = onBackClick,
        onToggleSaved = { newsViewModel.toggleSaved(articleId) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailContent(
    articleId: String,
    article: GuardianArticle?,
    onBackClick: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = article?.sectionName ?: "Статтю не знайдено",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад до списку новин"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (article == null) {
            ArticleNotFoundState(
                articleId = articleId,
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    Image(
                        painter = painterResource(id = article.fields.thumbnailRes),
                        contentDescription = article.webTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC052962))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${article.pillarName} · ${article.sectionName}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = article.webTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = article.fields.trailText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = onToggleSaved,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (article.isSaved) {
                                "★ У збережених матеріалах (натисніть, щоб прибрати)"
                            } else {
                                "☆ Зберегти статтю на пристрої"
                            }
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DetailMetaRow(label = "Автор (fields.byline):", value = article.fields.byline)
                            DetailMetaRow(label = "Дата і час публікації:", value = article.webPublicationDate)
                            DetailMetaRow(label = "Обсяг (fields.wordcount):", value = "${article.fields.wordcount} слів")
                            DetailMetaRow(label = "ID у The Guardian API:", value = article.id, isMonospace = true)
                            DetailMetaRow(label = "Веб-адреса (webUrl):", value = article.webUrl, isMonospace = true)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Повний текст матеріалу",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = article.fields.bodyText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DetailMetaRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ArticleNotFoundState(
    articleId: String,
    onBackClick: () -> Unit,
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
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
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
                    text = "Об'єкт не знайдено",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Статтю з ідентифікатором «$articleId» не знайдено у списку завантажених матеріалів.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onBackClick) {
                    Text(text = "Повернутися до стрічки")
                }
            }
        }
    }
}

// ============================================================================
// 7. РЯД ПЕРЕМИКАЧІВ, ПОРОЖНІЙ СТАН СПИСКУ ТА КАРТКА В КАТАЛОЗІ
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

@Composable
fun EmptySectionState(
    sectionName: String,
    onResetFilter: () -> Unit,
    onTestMissingArticleClick: () -> Unit,
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
                    text = "Не вдалося завантажити статті для розділу «$sectionName» або список порожній.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onResetFilter) {
                    Text(text = "Спробувати знову")
                }
                OutlinedButton(onClick = onTestMissingArticleClick) {
                    Text(text = "Перевірити неіснуючий ID")
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: GuardianArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        text = "Натисніть, щоб читати →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
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
            uiState = NewsFeedUiState(isLoading = true),
            onSectionSelected = {},
            onResetFilter = {},
            onItemClick = {},
            onSettingsClick = {}
        )
    }
}