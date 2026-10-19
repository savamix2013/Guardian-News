package com.example.guardiannews

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
import androidx.lifecycle.ViewModel
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

/*
 * Примітка щодо використання ШІ (AI Disclosure):
 * Під час виконання Лабораторної роботи №5 використовувався ШІ-асистент Google Gemini
 * для налаштування навігації Jetpack Navigation Compose (NavHost), передачі параметра id
 * у маршрут екрана деталей, розширення моделі GuardianFields додатковими полями The Guardian API
 * (trailText, byline, wordcount, bodyText) та верстки екрана деталей ArticleDetailScreen
 * із обробкою випадку, коли об'єкт за id не знайдено.
 */

// ============================================================================
// 1. МОДЕЛЬ ДАНИХ (поля відповідають JSON-відповіді The Guardian API)
// Додано поля для екрана деталей, яких немає на картці в списку:
// fields.trailText, fields.byline, fields.wordcount, fields.bodyText, webUrl, apiUrl
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
// 2. ПОЧАТКОВІ ДАНІ (6 ОБ'ЄКТІВ ІЗ ПОВНИМ ТЕКСТОМ ДЛЯ СКРОЛУ НА ЕКРАНІ ДЕТАЛЕЙ)
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

                За словами інженерів проєкту Android Open Source Project, нова підсистема Private Compute Core відокремлює алгоритми аналізу тексту, зображень та голосових команд у захищену пісочницю без прямого доступу до мережі Інтернет. Будь-яка взаємодія із зовнішніми сервісами відбувається лише через явно контрольовані системні інтерфейси.

                Окрім цього, користувачі отримали детальну панель моніторингу дозволів у реальному часі. Тепер система показує не лише факт звернення застосунку до камери, мікрофона чи геолокації, а й частоту запитів у фоновому режимі протягом останніх семи днів.

                Розробники мобільних застосунків на Jetpack Compose та Kotlin також отримали оновлені бібліотеки для безпечного локального збереження налаштувань та кешування контенту для офлайн-режиму. Фахівці з кібербезпеки відзначають, що такий підхід суттєво знижує ризики витоку метаданих під час користування публічними мережами Wi-Fi.
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

                Особливу увагу приділено прозорості роботи фонових синхронізацій. Тепер новинні агрегатори та читалки можуть завантажувати стиснені пакети статей за розкладом, не створюючи пікових навантажень на процесор та радіомодуль пристрою.

                Очікується, що протягом наступного півріччя більшість великих видавництв оновлять свої публічні REST API для підтримки легких форматів передачі медіафайлів та адаптивних мініатюр.
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

                Попередні спроби дослідити подібні об'єкти часто ускладнювалися спалахами материнської зорі, які спотворювали спектр під час транзиту планети. Цього разу дослідники застосували нову методику серії послідовних вимірювань на різних довжинах хвиль.

                Отримані дані свідчать про те, що планета утримує щільну газову оболонку попри близьке розташування до свого світила. Це відкриває нові перспективи у пошуку потенційно придатних для життя світів у нашій галактиці.

                Наступний етап спостережень заплановано на початок зими: астрономи планують оцінити швидкість атмосферних вітрів та перевірити наявність хмарного покриву на денній стороні екзопланети.
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

                Звичайні мікроелектронні акселерометри з часом накопичують похибку, тому без корекції за супутниками GPS точність визначення координат швидко падає. Квантовий датчик позбавлений цього дрейфу і дозволяє утримувати точний маршрут під землею, під водою або в умовах радіоелектронних перешкод.

                За останній рік інженерам вдалося зменшити габарити вакуумної камери та лазерного модуля з розміру лабораторної шафи до компактного переносного кейса, а енергоспоживання знизилося більш ніж утричі.

                У найближчі роки подібні сенсори планують використовувати на дослідницьких суднах, у цивільній авіації та в геофізичному моніторингу тектонічної активності.
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
            byline = "Jilliam Ambrose, енергетична оглядачка",
            wordcount = "650",
            bodyText = """
                За даними європейських операторів систем передачі електроенергії, частка відновлюваних джерел у загальному балансі перевищила прогнозовані показники жовтня завдяки поєднанню високої вітрової активності у Північному морі та введенню в експлуатацію нових сонячних парків.

                Ключовим фактором успіху цього сезону стали великі мережеві накопичувачі енергії. Протягом денних годин вони акумулюють надлишкову генерацію, а ввечері, коли домогосподарства вмикають опалення та освітлення, віддають потужність у мережу за лічені мілісекунди.

                Аналітики підкреслюють, що модернізація міждержавних інтерконекторів дозволила значно ефективніше перерозподіляти електроенергію між регіонами з різними погодними умовами.

                Водночас експерти наголошують на необхідності подальшого розвитку розумних локальних мереж (Smart Grids) та гнучких тарифів для споживачів, щоб стимулювати заряджання електромобілів у години максимальної генерації.
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

                Замість монокультурних насаджень, які часто виявляються вразливими до буревіїв та сезонних посух, фахівці формують мішані ділянки з місцевих видів дерев, стійких до кліматичних змін.

                Кожна дослідна ділянка має власний цифровий паспорт із координатами, показниками вологості ґрунту та історією спостережень. Усі зібрані матеріали публікуються у відкритому доступі для науковців та місцевих громад.

                За перші два роки роботи програми приживлюваність молодих дерев на гірських схилах зросла майже на третину, що також сприяє природному захисту долин від весняних паводків.
            """.trimIndent()
        ),
        isSaved = false
    )
)

// ============================================================================
// 3. СТАН ЕКРАНА ТА VIEWMODEL (Лабораторні №4–5)
// ============================================================================

data class NewsFeedUiState(
    val sections: List<String> = availableSections,
    val selectedSection: String = availableSections.first(),
    val allArticles: List<GuardianArticle> = sampleArticles,
    val filteredArticles: List<GuardianArticle> = sampleArticles
)

class NewsFeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NewsFeedUiState())
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    fun selectSection(section: String) {
        _uiState.update { currentState ->
            val filtered = filterBySection(currentState.allArticles, section)
            currentState.copy(
                selectedSection = section,
                filteredArticles = filtered
            )
        }
    }

    fun resetFilter() {
        selectSection("All")
    }

    // Пошук статті за її id для екрана деталей (повертає null, якщо об'єкт не знайдено)
    fun getArticleById(articleId: String): GuardianArticle? {
        return _uiState.value.allArticles.find { it.id == articleId }
    }

    // Перемикання статусу збереження статті у закладках через copy()
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
// 4. ГОЛОВНА АКТИВНІСТЬ ТА НАВІГАЦІЯ (NavHost) (Лабораторна №5)
// Екрани не знають про NavController — вони отримують лише лямбди onItemClick і onBackClick
// ============================================================================

object AppDestinations {
    const val FEED_ROUTE = "feed"
    const val ARTICLE_ID_ARG = "articleId"
    const val DETAIL_ROUTE_TEMPLATE = "article/{$ARTICLE_ID_ARG}"

    fun createDetailRoute(articleId: String): String {
        // Кодуємо id, оскільки id у The Guardian API містить слеші (напр. technology/2026/...)
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
        // Стартовий головний екран (не знає про NavController, отримує лише onItemClick)
        composable(route = AppDestinations.FEED_ROUTE) {
            NewsFeedScreen(
                newsViewModel = newsViewModel,
                onItemClick = { articleId ->
                    navController.navigate(AppDestinations.createDetailRoute(articleId))
                }
            )
        }

        // Екран деталей об'єкта: читає id з маршруту, бере за ним об'єкт і отримує onBackClick
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
    }
}

// ============================================================================
// 5. ГОЛОВНИЙ ЕКРАН СТРІЧКИ (без стрілки "Назад", бо це стартовий екран)
// ============================================================================

@Composable
fun NewsFeedScreen(
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    newsViewModel: NewsFeedViewModel = viewModel()
) {
    val uiState by newsViewModel.uiState.collectAsState()

    NewsFeedContent(
        uiState = uiState,
        onSectionSelected = newsViewModel::selectSection,
        onResetFilter = newsViewModel::resetFilter,
        onItemClick = onItemClick,
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
// 6. ЕКРАН ДЕТАЛЕЙ ОБ'ЄКТА (зі стрілкою "Назад", скролом і обробкою відсутності id)
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
                    // Стрілка «Назад» у TopAppBar (присутня лише на вкладеному екрані деталей)
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
            // Випадок, коли об'єкт за переданим id не знайшовся
            ArticleNotFoundState(
                articleId = articleId,
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            // Повний вміст статті з вертикальним скролом
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Обкладинка статті з бейджем рубрики
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
                    // Заголовок статті
                    Text(
                        text = article.webTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Підзаголовок / лід статті (fields.trailText — поле, якого немає на картці списку)
                    Text(
                        text = article.fields.trailText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )

                    // Кнопка збереження в закладки на пристрої
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

                    // Картка розширених метаданих (byline, wordcount, id, webUrl — відсутні на картці списку)
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

                    // Повний текст статті (fields.bodyText — довгий вміст для прокрутки)
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
                    text = "У розділі «$sectionName» наразі немає збережених або завантажених статей. Спробуйте обрати іншу рубрику.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onResetFilter) {
                    Text(text = "Показати всі новини")
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
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArticleDetailScreenPreview() {
    GuardianNewsTheme {
        ArticleDetailContent(
            articleId = sampleArticles.first().id,
            article = sampleArticles.first(),
            onBackClick = {},
            onToggleSaved = {}
        )
    }
}