package com.twojafirma.askier.ui.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Definiujemy, jakie motywy są dostępne
enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

// Tworzymy delegata, który zapewni nam jedną instancję DataStore dla całej aplikacji
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // Definiujemy klucz, pod którym będziemy zapisywać nasze ustawienie motywu
    private val themeKey = stringPreferencesKey("theme_setting")

    // Zwracamy "Flow", który będzie emitował nową wartość za każdym razem,
    // gdy ustawienie motywu w DataStore się zmieni.
    val themeSettingFlow: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            // Odczytujemy zapisany string i konwertujemy go na nasz enum
            ThemeSetting.valueOf(
                preferences[themeKey] ?: ThemeSetting.SYSTEM.name
            )
        }

    // Funkcja do zapisywania nowego ustawienia motywu
    suspend fun setThemeSetting(themeSetting: ThemeSetting) {
        context.dataStore.edit { settings ->
            settings[themeKey] = themeSetting.name
        }
    }
}