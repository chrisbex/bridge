package com.twojafirma.askier.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.twojafirma.askier.ui.data.SettingsRepository
import com.twojafirma.askier.ui.data.ThemeSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    // Obserwujemy ustawienie motywu z repozytorium
    val themeSetting: StateFlow<ThemeSetting> = settingsRepository.themeSettingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSetting.SYSTEM
        )

    // Funkcja wywoływana, gdy użytkownik wybierze nową opcję
    fun onThemeSelected(newTheme: ThemeSetting) {
        viewModelScope.launch {
            settingsRepository.setThemeSetting(newTheme)
        }
    }
}