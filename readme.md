# Guardian News — Стрічка новин

Мобільний застосунок на Android (Kotlin, Jetpack Compose) для перегляду живих новин [The Guardian](https://www.google.com/search?q=https%3A%2F%2Fopen-platform.theguardian.com%2F) за обраним розділом, читання повних карток статей та збереження налаштувань і закладок на пристрої.

## Налаштування API-ключа (`local.properties`)

Перед збіркою проєкту додайте у файл `local.properties` у корені проєкту рядок зі своїм ключем [The Guardian Open Platform](https://www.google.com/search?q=https%3A%2F%2Fopen-platform.theguardian.com%2Faccess%2F) (якщо рядок відсутній, автоматично використовується тестовий ключ `test`):

```properties
GUARDIAN_API_KEY=ваш_ключ_тут
```

## Короткий бриф

* **Джерело даних**: [The Guardian Open Platform API](https://www.google.com/search?q=https%3A%2F%2Fopen-platform.theguardian.com%2Fdocumentation%2Fsearch) (`response.results`, поля `fields.thumbnail`, `trailText`, `byline`, `wordcount`, `bodyText`; ліміт — 1 запит/сек, 500 запитів/день).
* **Екрани**:
  1. **Каталог (Стрічка новин)** — живі новини з API за обраним розділом (`All`, `Technology`, `Science`, `Environment`, `Sport`).
  2. **Деталі статті** — повний текст і метадані статті зі скролом та збереженням у закладки.
  3. **Налаштування** — вибір стартового розділу за замовчуванням у `Preferences DataStore`.

## AI Disclosure

Під час виконання лабораторних робіт використовувався ШІ-асистент **Google Gemini** для допомоги з версткою Jetpack Compose, структурою `ViewModel`, навігацією `NavHost`, роботою з `Preferences DataStore` та підключенням `Retrofit` + `kotlinx.serialization`.