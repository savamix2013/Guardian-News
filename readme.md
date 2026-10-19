# Guardian News — Стрічка новин

Мобільний застосунок на Android (Jetpack Compose) для читання актуальних новин за обраним розділом, перегляду карток статей з ілюстраціями та локального збереження обраних матеріалів на пристрої.

## Опис проєкту (одне речення)

Застосунок дозволяє читати свіжі новини [The Guardian](https://www.google.com/search?q=https%3A%2F%2Fopen-platform.theguardian.com%2F) за обраним розділом, переглядати детальні картки статей з обкладинками та зберігати цікаві матеріали для читання на пристрої.

## Головна дія користувача (Лабораторна робота №3)

Користувач натискає на перемикач розділу новин (`Усі новини`, `Technology`, `Science`, `Environment`, `Sport`) у верхньому ряді чіпів, після чого стрічка на екрані фільтрується і показує лише статті обраної рубрики (або екран порожнього стану з кнопкою скидання фільтра, якщо в обраному розділі новин немає).

## Перелік екранів

| № | Екран | Призначення |
| :--- | :--- | :--- |
| 1 | **Каталог (Стрічка новин)** | Відображення списку новин за обраним розділом або пошуковим запитом із мініатюрами зображень (`fields.thumbnail`). |
| 2 | **Деталі (Картка статті)** | Повна інформація про статтю: заголовок, лід (`fields.trailText`), автор (`fields.byline`), кількість слів (`fields.wordcount`), повний текст (`fields.bodyText`), посилання (`webUrl`) та кнопка збереження. |
| 3 | **Збережені матеріали** | Перегляд і видалення статей, збережених користувачем у локальній базі даних пристрою. |
| 4 | **Налаштування** | Вибір розділу новин за замовчуванням (наприклад, `technology`, `world`, `science`, `sport`) та параметрів стрічки. |

## Джерело даних (REST API)

* **API**: [The Guardian Open Platform](https://www.google.com/search?q=https%3A%2F%2Fopen-platform.theguardian.com%2Fdocumentation%2F) (публічний REST API без OAuth, підтримує навчальний ключ `test` та власний безкоштовний ключ розробника).
* **Тестовий запит**:
  ```text
  https://content.guardianapis.com/search?q=android&show-fields=thumbnail,trailText,byline,wordcount,bodyText&page-size=20&api-key=test
  ```
* **Масив об'єктів у JSON-відповіді**: `response.results` (кожен елемент містить поля `id`, `type`, `sectionId`, `sectionName`, `webPublicationDate`, `webTitle`, `webUrl`, `apiUrl`, `pillarName` та об'єкт `fields`).
* **Поле із зображенням та деталями статті**: `fields.thumbnail`, `fields.trailText`, `fields.byline`, `fields.wordcount`, `fields.bodyText`.
* **Ліміт запитів ([Developer Key](https://www.google.com/search?q=https%3A%2F%2Fopen-platform.theguardian.com%2Faccess%2F))**:
  * До **1 запиту на секунду** (1 call per second).
  * До **500 запитів на добу** (500 calls per day).

## Що зберігається локально на пристрої

* **Розділ новин за замовчуванням**: зберігається у `DataStore` / `SharedPreferences`, щоб при відкритті застосунку одразу завантажувалась улюблена рубрика.
* **Збережені статті (закладки)**: зберігаються у локальній базі даних `Room` (заголовок, дата, рубрика, URL мініатюри та посилання на статтю) для швидкого доступу без повторного пошуку.

## Виконані лабораторні роботи

* **Лабораторна робота №1**: створено бриф проєкту та початковий екран.
* **Лабораторна робота №2**: описано модель даних `GuardianArticle` та `GuardianFields` відповідно до схеми [The Guardian Search Endpoint](https://www.google.com/search?q=https%3A%2F%2Fopen-platform.theguardian.com%2Fdocumentation%2Fsearch), зверстано картку статті `ArticleCard` (`Box`, `Column`, `Row`) у `Scaffold` із `TopAppBar`.
* **Лабораторна робота №3**: розширено список `sampleArticles` до 6 статей у різних рубриках (`sectionName`), додано безстановий компонент ряду перемикачів `SectionFilterRow` та опрацьовано стан порожнього результату `EmptySectionState` (при виборі рубрики `Sport`).
* **Лабораторна робота №4**: стан головного екрана описано окремим незмінним `data class NewsFeedUiState` з полями `val` та перенесено у `NewsFeedViewModel` (`private val _uiState = MutableStateFlow(...)` і публічний `val uiState: StateFlow<NewsFeedUiState>`). Зміни виконуються через `copy()` у методах-подіях `selectSection()` та `resetFilter()`, отримання `ViewModel` у Composable реалізовано штатною функцією `viewModel()`, а `remember` для стану вибору прибрано з екрана.
* **Лабораторна робота №5**: екрани об'єднано під `NavHost` у `GuardianNewsApp` зі стартовим маршрутом `feed` та маршрутом деталей `article/{articleId}`. Екрани не залежать від `NavController` і приймають лише лямбди `onItemClick` та `onBackClick`. На екрані деталей `ArticleDetailScreen` відображаються розширені поля (`fields.trailText`, `fields.byline`, `fields.wordcount`, `fields.bodyText`, `webUrl`, `id`) із вертикальним скролом, додано стрілку «Назад» у `TopAppBar` (лише на екрані деталей) та опрацьовано стан `ArticleNotFoundState`, коли статтю за `id` не знайдено.

## Відкрита декларація використання ШІ (AI Disclosure)

Відповідно до правил курсу зазначається використання ШІ-асистента **Google Gemini** під час виконання лабораторних робіт:
* **Лабораторна робота №1**: структурування опису брифа проєкту у `README.md` та початкового екрана-візитівки.
* **Лабораторна робота №2**: генерація векторних ілюстрацій-заглушок (`news_thumb_1.xml`, `news_thumb_2.xml`, `news_thumb_3.xml`) для `res/drawable`, узгодження полів `data class GuardianArticle` зі схемою відповіді The Guardian API та допомога з версткою компонента `ArticleCard` у Jetpack Compose.
* **Лабораторна робота №3**: розширення тестового набору `sampleArticles` до 6 елементів та реалізація патерну State Hoisting для фільтрації новин за розділом (`SectionFilterRow`) і відображення порожнього стану (`EmptySectionState`).
* **Лабораторна робота №4**: перенесення стану екрана у `NewsFeedViewModel` із незмінним `data class NewsFeedUiState`, інкапсуляцією через `StateFlow` та оновленням стану методом `copy()`.
* **Лабораторна робота №5**: налаштування `NavHost` з передачею параметра `articleId` (через `Uri.encode`/`Uri.decode`), верстка екрана деталей `ArticleDetailScreen` з полями `trailText`, `byline`, `wordcount`, `bodyText` і `webUrl`, додавання кнопки «Назад» у `TopAppBar` та обробка стану `ArticleNotFoundState`.