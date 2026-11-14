package com.example.guardiannews.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val FAVORITE_IDS_KEY = stringSetPreferencesKey("favorite_article_ids")
    }

    val favoriteIdsFlow: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FAVORITE_IDS_KEY] ?: emptySet()
        }

    suspend fun toggleFavorite(articleId: String) {
        context.dataStore.edit { preferences ->
            val currentIds = preferences[FAVORITE_IDS_KEY] ?: emptySet()
            preferences[FAVORITE_IDS_KEY] = if (articleId in currentIds) {
                currentIds - articleId
            } else {
                currentIds + articleId
            }
        }
    }
}
