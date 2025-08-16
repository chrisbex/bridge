package com.twojafirma.askier.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val currentTheme by viewModel.themeSetting.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Ustawienia",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        item {
            Text("Motyw aplikacji", style = MaterialTheme.typography.titleMedium)
        }

        // Tworzymy opcje wyboru dla każdego motywu
        ThemeSetting.entries.forEach { theme ->
            item {
                ThemeOptionRow(
                    text = theme.toDisplayString(),
                    selected = currentTheme == theme,
                    onClick = { viewModel.onThemeSelected(theme) }
                )
            }
        }
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

// Funkcja pomocnicza do ładnego wyświetlania nazw
fun ThemeSetting.toDisplayString(): String {
    return when (this) {
        ThemeSetting.SYSTEM -> "Systemowy"
        ThemeSetting.LIGHT -> "Jasny"
        ThemeSetting.DARK -> "Ciemny"
    }
}