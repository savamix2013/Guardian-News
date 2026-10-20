package com.example.guardiannews

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "guardian_news_settings")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val DEFAULT_SECTION_KEY = stringPreferencesKey("default_section")
        const val DEFAULT_SECTION_FALLBACK = "All"
    }

    val defaultSectionFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_SECTION_KEY] ?: DEFAULT_SECTION_FALLBACK
    }

    // Запис нового значення у Preferences DataStore у suspend-функції
    suspend fun saveDefaultSection(section: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_SECTION_KEY] = section
        }
    }
}