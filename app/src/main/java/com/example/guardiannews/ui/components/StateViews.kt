package com.example.guardiannews.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptySearchState(
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ThemedStatusState(
        icon = Icons.Outlined.SearchOff,
        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        title = "Нічого не знайдено",
        description = "За вашим запитом або обраною рубрикою статей не знайдено. Спробуйте змінити параметри пошуку.",
        actionLabel = "Скинути фільтри",
        onActionClick = onResetClick,
        modifier = modifier
    )
}

@Composable
fun EmptyFavoritesState(
    modifier: Modifier = Modifier
) {
    ThemedStatusState(
        icon = Icons.Outlined.BookmarkBorder,
        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
        title = "Добірка поки порожня",
        description = "Зберігайте цікаві статті за допомогою іконки закладки, щоб швидко повертатися до них пізніше.",
        modifier = modifier
    )
}

@Composable
fun ErrorStateView(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ThemedStatusState(
        icon = Icons.Outlined.CloudOff,
        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
        iconTint = MaterialTheme.colorScheme.onErrorContainer,
        title = "Не вдалося завантажити дані",
        description = message,
        actionLabel = "Спробувати ще раз",
        onActionClick = onRetryClick,
        modifier = modifier
    )
}

@Composable
private fun ThemedStatusState(
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = iconContainerColor,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}