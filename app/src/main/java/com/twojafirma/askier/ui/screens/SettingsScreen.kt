package com.twojafirma.askier.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.lazy.LazyColumn // Już nie jest potrzebny tutaj, jeśli tylko opcje motywu
// import androidx.compose.foundation.lazy.items // Już nie jest potrzebny tutaj
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twojafirma.askier.ui.data.ThemeSetting
// Usunięto importy związane z wyświetlaniem turnieju

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
    // Usunięto parametr onNavigateToPlayerProfile
) {
    val currentTheme by viewModel.themeSetting.collectAsState()
    // Usunięto stan showParsedResults

    // Używamy Column zamiast LazyColumn, jeśli zawartość jest statyczna i krótka
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        // verticalArrangement = Arrangement.spacedBy(8.dp) // Można dodać, jeśli potrzebne
    ) {
        Text(
            text = "Ustawienia",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text("Motyw aplikacji", style = MaterialTheme.typography.titleMedium)

        ThemeSetting.entries.forEach { theme ->
            ThemeOptionRow(
                text = theme.toDisplayString(),
                selected = currentTheme == theme,
                onClick = { viewModel.onThemeSelected(theme) }
            )
        }

        // Usunięto przycisk "Pokaż wyniki testu HTML"
        // Usunięto warunkowe wywołanie DisplayParsedTournamentResults
    }
}

@Composable
fun ThemeOptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

fun ThemeSetting.toDisplayString(): String {
    return when (this) {
        ThemeSetting.SYSTEM -> "Systemowy"
        ThemeSetting.LIGHT -> "Jasny"
        ThemeSetting.DARK -> "Ciemny"
    }
}

// Usunięto funkcje DisplayParsedTournamentResults, ParsedPairResultItem, ParsedPlayerInfoView
