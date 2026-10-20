# Guardian News

Мобільний застосунок на Android (Kotlin, Jetpack Compose) для перегляду новин The Guardian за обраною рубрикою, читання повних статей та збереження налаштувань і закладок.

## Екрани
1. **Стрічка новин:** Список актуальних матеріалів із фільтрацією за рубриками (`All`, `Technology`, `Science`, `Environment`, `Sport`).
2. **Деталі статті:** Повний текст матеріалу, інформація про автора, дату публікації та можливість зберегти статтю в закладки.
3. **Налаштування:** Вибір стартової рубрики новин за замовчуванням (`Preferences DataStore`).

## Джерело даних
- **API:** `https://content.guardianapis.com/search`
- **Параметри запиту:** `section`, `show-fields=thumbnail,trailText,byline,wordcount,bodyText`, `page-size=20`, `api-key`.

## Налаштування API-ключа (`local.properties`)
У корені проєкту у файлі `local.properties` додайте рядок зі своїм ключем The Guardian Open Platform (якщо ключ не вказано, використовується тестовий ключ `test`):

```properties
GUARDIAN_API_KEY=ваш_ключ
```
