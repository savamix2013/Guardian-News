package com.example.guardiannews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ProjectBriefScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBriefScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Guardian News — Бриф проєкту",
                        fontWeight = FontWeight.Bold
                    )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Назва та опис одним реченням
            BriefSectionCard(title = "Про застосунок") {
                Text(
                    text = "Guardian News (Стрічка новин)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Застосунок дозволяє читати свіжі новини The Guardian за обраним розділом, переглядати детальні картки статей з ілюстраціями та зберігати цікаві матеріали для читання на пристрої.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // 2. Перелік екранів
            BriefSectionCard(title = "Екрани застосунку (4 екрани)") {
                ScreenItem(
                    number = "1",
                    name = "Каталог (Стрічка новин)",
                    description = "Список новин за обраним розділом або пошуковим запитом із мініатюрами зображень."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ScreenItem(
                    number = "2",
                    name = "Деталі (Картка статті)",
                    description = "Повна інформація про обрану статтю: заголовок, рубрика, дата публікації, обкладинка, посилання на оригінал і кнопка збереження."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ScreenItem(
                    number = "3",
                    name = "Збережені матеріали",
                    description = "Список статей, збережених користувачем локально на пристрої."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ScreenItem(
                    number = "4",
                    name = "Налаштування",
                    description = "Вибір розділу новин за замовчуванням та параметрів відображення стрічки."
                )
            }

            // 3. Джерело даних (REST API)
            BriefSectionCard(title = "Джерело даних (REST API)") {
                InfoRow(label = "Провайдер:", value = "The Guardian Open Platform (без OAuth, безкоштовний ключ)")
                InfoRow(
                    label = "Ендпоінт:",
                    value = "https://content.guardianapis.com/search?q=android&show-fields=thumbnail&page-size=20&api-key=test",
                    isMonospace = true
                )
                InfoRow(label = "Масив об'єктів:", value = "response.results[]", isMonospace = true)
                InfoRow(label = "Поле зображення:", value = "fields.thumbnail (з параметром show-fields=thumbnail)", isMonospace = true)
                InfoRow(label = "Ліміт запитів:", value = "до 1 запиту/сек, до 500 запитів/день (Developer Key)")
            }

            // 4. Локальне збереження на пристрої
            BriefSectionCard(title = "Дані на пристрої (Локальне сховище)") {
                Text(
                    text = "• Розділ новин за замовчуванням (DataStore / SharedPreferences)\n• Список збережених статей у закладках (Room Database)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun BriefSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun ScreenItem(number: String, name: String, description: String) {
    Column {
        Text(
            text = "$number. $name",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, isMonospace: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProjectBriefScreenPreview() {
    MaterialTheme {
        ProjectBriefScreen()
    }
}