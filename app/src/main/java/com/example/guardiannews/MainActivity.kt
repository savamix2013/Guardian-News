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
 * Під час виконання Лабораторних робіт №1–6 використовувався ШІ-асистент Google Gemini
 * для проєктування моделі GuardianArticle, верстки Jetpack Compose, налаштування ViewModel,
 * навігації NavHost та збереження розділу за замовчуванням у Preferences DataStore.
 */

// ============================================================================
// 1. МОДЕЛЬ ДАНИХ (поля відповідають JSON-відповіді The Guardian API)
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
        apiUrl = "https://content.guardianapis.com/technology/2026/oct/19/android-16-security-ai-features",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-tech-thumb-1.jpg",
            thumbnailRes = R.drawable.news_thumb_1,
            trailText = "Нові механізми ізоляції дозволяють виконувати моделі машинного навчання без передачі персональних даних на зовнішні сервери.",
            byline = "Alex Hern, редактор розділу технологій",
            wordcount = "620",
            bodyText = """
                Оновлення платформи Android приносить суттєві зміни в архітектуру безпеки мобільних пристроїв. Головний акцент зроблено на локальному виконанні обчислень безпосередньо на процесорі смартфона, щоб чутливі дані користувача ніколи не залишали межі пристрою.

                За словами інженерів проєкту Android Open Source Project, нова підсистема Private Compute Core відокремлює алгоритми аналізу тексту, зображень та голосових команд у захищену пісочницю без прямого доступу до мережі Інтернет.

                Окрім цього, користувачі отримали детальну панель моніторингу дозволів у реальному часі. Тепер система показує не лише факт звернення застосунку до камери, мікрофона чи геолокації, а й частоту запитів у фоновому режимі протягом останніх семи днів.
            """.trimIndent()
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
        apiUrl = "https://content.guardianapis.com/technology/2026/oct/18/open-source-mobile-browsers-eu-regulations",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-tech-thumb-2.jpg",
            thumbnailRes = R.drawable.news_thumb_1,
            trailText = "Уніфікація протоколів обміну даними та сучасних рушіїв рендерингу зменшує споживання заряду батареї на 18%.",
            byline = "Dan Milmo, оглядач глобальних технологій",
            wordcount = "540",
            bodyText = """
                Європейські регулятори та спільнота розробників програмного забезпечення з відкритим кодом узгодили новий пакет технічних вимог до мобільних платформ. Основна мета ініціативи — забезпечити однаково високу продуктивність сторонніх клієнтів і системних компонентів.

                Результати незалежного тестування на популярних моделях смартфонів показали, що перехід на сучасні декларативні фреймворки інтерфейсу та оптимізовані мережеві клієнти скорочує час холодного старту застосунків майже на чверть.
            """.trimIndent()
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
        apiUrl = "https://content.guardianapis.com/science/2026/oct/18/european-space-telescope-new-exoplanet-discovery",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-science-thumb-1.jpg",
            thumbnailRes = R.drawable.news_thumb_2,
            trailText = "Спектральний аналіз підтвердив наявність водяної пари та вуглекислого газу у верхніх шарах атмосфери планети на відстані 42 світлових років.",
            byline = "Ian Sample, науковий редактор",
            wordcount = "710",
            bodyText = """
                Міжнародна команда астрофізиків опублікувала результати багаторічних спостережень за системою червоного карлика у сузір'ї Живописця. Завдяки високоточному інфрачервоному спектрографу вченим вперше вдалося чітко виділити хімічний підпис атмосфери навколо кам'янистої планети земного типу.

                Отримані дані свідчать про те, що планета утримує щільну газову оболонку попри близьке розташування до свого світила. Це відкриває нові перспективи у пошуку потенційно придатних для життя світів у нашій галактиці.
            """.trimIndent()
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
        apiUrl = "https://content.guardianapis.com/science/2026/oct/16/quantum-sensors-navigation-without-satellites",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-science-thumb-2.jpg",
            thumbnailRes = R.drawable.news_thumb_2,
            trailText = "Компактні атомні інтерферометри визначають прискорення та положення у просторі там, де GPS недоступний.",
            byline = "Nicola Davis, наукова кореспондентка",
            wordcount = "590",
            bodyText = """
                Британські та українські дослідники в галузі прикладної фізики провели успішні польові випробування автономної інерціальної системи навігації нового покоління. В основі приладу лежить охолоджена лазером хмара атомів рубідію, яка реагує на найменші зміни гравітаційного поля та прискорення.

                Квантовий датчик позбавлений накопичення похибки і дозволяє утримувати точний маршрут під землею, під водою або в умовах радіоелектронних перешкод.
            """.trimIndent()
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
        apiUrl = "https://content.guardianapis.com/environment/2026/oct/17/clean-energy-grids-record-growth-europe",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-env-thumb-1.jpg",
            thumbnailRes = R.drawable.news_thumb_3,
            trailText = "Інтеграція промислових акумуляторних станцій дозволила стабілізувати вечірні піки споживання в енергомережі.",
            byline = "Jillian Ambrose, енергетична оглядачка",
            wordcount = "650",
            bodyText = """
                За даними європейських операторів систем передачі електроенергії, частка відновлюваних джерел у загальному балансі перевищила прогнозовані показники жовтня завдяки поєднанню високої вітрової активності у Північному морі та введенню в експлуатацію нових сонячних парків.

                Ключовим фактором успіху цього сезону стали великі мережеві накопичувачі енергії, які акумулюють надлишкову денну генерацію і віддають її ввечері.
            """.trimIndent()
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
        apiUrl = "https://content.guardianapis.com/environment/2026/oct/15/carpathian-forest-restoration-monitoring-drones",
        pillarName = "News",
        fields = GuardianFields(
            thumbnail = "https://media.guim.co.uk/sample-env-thumb-2.jpg",
            thumbnailRes = R.drawable.news_thumb_3,
            trailText = "Поєднання супутникових знімків високої роздільної здатності та відкритих баз даних дозволяє точно планувати висадку мішаних лісів.",
            byline = "Patrick Greenfield, кореспондент з питань біорізноманіття",
            wordcount = "580",
            bodyText = """
                Екологи з кількох карпатських природоохоронних заповідників впровадили цифрову систему спостереження за відновленням пралісів та високогірних смереково-букових масивів. Система автоматично порівнює мультиспектральні знімки місцевості та визначає ділянки, які найбільше потребують природної регенерації.
            """.trimIndent()
        ),
        isSaved = false
    )
)

// ============================================================================
// 3. СТАН ЕКРАНА ТА VIEWMODEL З ПІДПИСКОЮ НА DATASTORE (Лабораторні №4–6)
// ============================================================================

data class NewsFeedUiState(
    val sections: List<String> = availableSections,
    val defaultSection: String = UserPreferencesRepository.DEFAULT_SECTION_FALLBACK,
    val selectedSection: String = UserPreferencesRepository.DEFAULT_SECTION_FALLBACK,
    val allArticles: List<GuardianArticle> = sampleArticles,
    val filteredArticles: List<GuardianArticle> = sampleArticles
)

class NewsFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(NewsFeedUiState())
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    init {
        // Підписуємо головний екран на Flow із Preferences DataStore,
        // щоб при запуску та після зміни налаштувань головний екран оновлювався сам
        viewModelScope.launch {
            preferencesRepository.defaultSectionFlow.collect { savedDefaultSection ->
                _uiState.update { currentState ->
                    val filtered = filterBySection(currentState.allArticles, savedDefaultSection)
                    currentState.copy(
                        defaultSection = savedDefaultSection,
                        selectedSection = savedDefaultSection,
                        filteredArticles = filtered
                    )
                }
            }
        }
    }

    // Тимчасове перемикання фільтра на головному екрані
    fun selectSection(section: String) {
        _uiState.update { currentState ->
            val filtered = filterBySection(currentState.allArticles, section)
            currentState.copy(
                selectedSection = section,
                filteredArticles = filtered
            )
        }
    }

    // Скидання фільтра до збереженого у налаштуваннях розділу за замовчуванням
    fun resetFilter() {
        selectSection(_uiState.value.defaultSection)
    }

    // Запис нового розділу за замовчуванням у Preferences DataStore (викликає suspend-функцію)
    fun saveDefaultSection(section: String) {
        viewModelScope.launch {
            preferencesRepository.saveDefaultSection(section)
        }
    }

    fun toggleSaved(articleId: String) {
        _uiState.update { currentState ->
            val updatedAll = currentState.allArticles.map { article ->
                if (article.id == articleId) {
                    article.copy(isSaved = !article.isSaved)
                } else {
                    article
                }
            }
            val updatedFiltered = filterBySection(updatedAll, currentState.selectedSection)
            currentState.copy(
                allArticles = updatedAll,
                filteredArticles = updatedFiltered
            )
        }
    }

    private fun filterBySection(
        articles: List<GuardianArticle>,
        section: String
    ): List<GuardianArticle> {
        return if (section == "All") {
            articles
        } else {
            articles.filter { it.sectionName.equals(section, ignoreCase = true) }
        }
    }
}

// ============================================================================
// 4. НАВІГАЦІЯ (NavHost): ГОЛОВНИЙ ЕКРАН, ДЕТАЛІ СТАТТІ ТА НАЛАШТУВАННЯ (Лабораторні №5–6)
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

        // Маршрут екрана налаштувань (Лабораторна №6)
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
// 5. ГОЛОВНИЙ ЕКРАН СТРІЧКИ (з кнопкою входу в Налаштування у TopAppBar)
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
                    // Вхід на екран налаштувань з TopAppBar головного екрана (Лабораторна №6)
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

            if (uiState.filteredArticles.isEmpty()) {
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
// 6. ЕКРАН НАЛАШТУВАНЬ (Лабораторна №6)
// Показує на перемикачі значення напряму зі сховища DataStore без власної копії в remember
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
                        text = "Обраний розділ зберігається на пристрої у Preferences DataStore. При запуску застосунку та при зміні цього параметра головний екран автоматично показує новини цієї рубрики.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Перемикачі RadioButton читають savedDefaultSection напряму з DataStore Flow (без remember)
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
// 7. ЕКРАН ДЕТАЛЕЙ ОБ'ЄКТА (Лабораторна №5)
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
// 8. РЯД ПЕРЕМИКАЧІВ, ПОРОЖНІЙ СТАН СПИСКУ ТА КАРТКА В КАТАЛОЗІ
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
                    text = "У розділі «$sectionName» наразі немає збережених або завантажених статей. Спробуйте обрати іншу рубрику.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onResetFilter) {
                    Text(text = "Скинути до розділу за замовчуванням")
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
            uiState = NewsFeedUiState(),
            onSectionSelected = {},
            onResetFilter = {},
            onItemClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    GuardianNewsTheme {
        SettingsContent(
            sections = availableSections,
            savedDefaultSection = "Technology",
            onDefaultSectionSelected = {},
            onBackClick = {}
        )
    }
}