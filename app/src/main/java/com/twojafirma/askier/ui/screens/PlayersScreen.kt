package com.twojafirma.askier.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.twojafirma.askier.R
import com.twojafirma.askier.Screen
import com.twojafirma.askier.ui.data.PlayerProfile

@Composable
fun PlayersScreen(
    navController: NavController,
    isDialogMode: Boolean, // <-- Nowy, niezawodny sposób na rozróżnienie trybu
    onPlayerSelected: (PlayerProfile) -> Unit = {}
) {
    val playersViewModel: PlayersViewModel = viewModel()
    val uiState = playersViewModel.uiState

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { playersViewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            label = { Text("Szukaj po nazwisku lub PID") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ikona wyszukiwania") },
            singleLine = true
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.players.isEmpty() && uiState.searchQuery.isNotBlank()) {
                Text(
                    "Brak wyników dla \"${uiState.searchQuery}\"",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.players, key = { it.pid }) { player ->
                            PlayerRow(
                                player = player,
                                onClick = {
                                    if (isDialogMode) {
                                        onPlayerSelected(player)
                                    } else {
                                        navController.navigate(Screen.PlayerDetails.createRoute(player.pid))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerRow(
    player: PlayerProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = player.photoData,
                contentDescription = "Zdjęcie gracza ${player.name}",
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_players)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = player.name, style = MaterialTheme.typography.titleMedium)
                Text(text = player.club, style = MaterialTheme.typography.bodyMedium)
                Text(text = "PID: ${player.pid}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}