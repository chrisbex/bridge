package com.twojafirma.askier.ui.screens

// Existing imports from context:
import androidx.core.text.HtmlCompat
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.twojafirma.askier.ui.data.TournamentDetails
import com.twojafirma.askier.ui.data.TournamentPairResult
import com.twojafirma.askier.ui.data.TournamentPlayerInfo
// Removed: import com.twojafirma.askier.util.TournamentHtmlParser // Not used directly in SpiderScreen anymore
// Removed: import com.twojafirma.askier.R // For R.raw.sample_tournament_html - Not used anymore
// Removed: import java.io.InputStreamReader // Not used anymore
// Removed: import java.nio.charset.StandardCharsets // Not used anymore
// Removed: import kotlinx.coroutines.CoroutineScope // Not used directly here for HTML loading
// Removed: import kotlinx.coroutines.Dispatchers // Not used directly here for HTML loading
// Removed: import kotlinx.coroutines.launch // Not used directly here for HTML loading
// Removed: import kotlinx.coroutines.withContext // Not used directly here for HTML loading

import androidx.lifecycle.viewmodel.compose.viewModel
import com.twojafirma.askier.ui.data.TournamentData

@Composable
fun SpiderScreen(
    navController: NavController,
    onNavigateToPlayerProfile: (playerId: Int) -> Unit,
    spiderViewModel: SpiderViewModel = viewModel()
) {
    val uiState by spiderViewModel.uiState.collectAsState()
    // val context = LocalContext.current // Not directly needed here anymore

    LaunchedEffect(Unit) {
        if (uiState.tournaments.isEmpty() && !uiState.isLoadingTournaments) {
            spiderViewModel.fetchTournaments()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            // 1. Loading tournament details
            uiState.isLoadingDetails -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ładowanie szczegółów turnieju...")
                    }
                }
            }
            // 2. Error loading tournament details
            uiState.detailsErrorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Błąd ładowania szczegółów:", color = MaterialTheme.colorScheme.error)
                    Text(uiState.detailsErrorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { spiderViewModel.clearSelectedTournamentDetails() }) {
                        Text("Wróć do listy")
                    }
                }
            }
            // 3. Displaying selected tournament details
            uiState.selectedTournamentDetails != null -> {
                DisplayParsedTournamentResults(
                    details = uiState.selectedTournamentDetails!!,
                    onBack = { spiderViewModel.clearSelectedTournamentDetails() },
                    onPlayerClick = onNavigateToPlayerProfile
                )
            }
            // 4. Displaying list of tournaments or its loading/error states
            else -> {
                Text("Lista Turniejów", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoadingTournaments && uiState.tournaments.isEmpty()) {
                    // Initial loading of the list
                    CircularProgressIndicator()
                    Text("Ładowanie listy turniejów...")
                } else if (uiState.tournamentsErrorMessage != null && uiState.tournaments.isEmpty()) {
                    // Error loading the list and list is empty
                    Text("Błąd: ${uiState.tournamentsErrorMessage}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { spiderViewModel.fetchTournaments() }) {
                        Text("Spróbuj ponownie")
                    }
                } else if (uiState.tournaments.isEmpty() && !uiState.isLoadingTournaments) {
                    // No tournaments and not loading
                    Text("Brak turniejów do wyświetlenia.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { spiderViewModel.fetchTournaments() }) {
                        Text("Odśwież listę turniejów")
                    }
                } else {
                    // Displaying the list of tournaments (possibly with refresh indicator)
                    if (uiState.isLoadingTournaments) { // Loading indicator when refreshing non-empty list
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.tournaments) { tournament ->
                            TournamentItem(
                                tournament = tournament,
                                onTournamentClick = {
                                    spiderViewModel.fetchAndParseTournamentDetails(tournament.url)
                                }
                            )
                            Divider()
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { spiderViewModel.fetchTournaments() },
                        enabled = !uiState.isLoadingTournaments // Disable while loading
                    ) {
                        Text(if (uiState.isLoadingTournaments) "Odświeżanie..." else "Odśwież listę turniejów")
                    }
                }
            }
        }
    }
}

@Composable
fun TournamentItem(tournament: TournamentData, onTournamentClick: () -> Unit) {
    val displayDate = "${tournament.day} ${tournament.month}"
    // Clean name from HTML tags for display as tournament type
    val cleanedName = HtmlCompat.fromHtml(tournament.name, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    val separator = " - "
    val tournamentType = if (cleanedName.contains(separator)) {
        cleanedName.substringAfter(separator).trim()
    } else {
        cleanedName.trim() // Fallback if no separator
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTournamentClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(displayDate, style = MaterialTheme.typography.titleMedium)
        Text(tournamentType, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tournament.url, // Display the URL
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun DisplayParsedTournamentResults(
    details: TournamentDetails,
    onBack: () -> Unit,
    onPlayerClick: (playerId: Int) -> Unit
) {
    // This function now receives already parsed 'details'
    // Internal parsing logic, isLoadingParsing, parseError, and LaunchedEffect for parsing are removed.

    Column(
        modifier = Modifier
            .fillMaxSize() // Fill size to take over screen
            .padding(16.dp)
    ) {
        Button(onClick = onBack) { // Use the passed onBack lambda
            Text("Wróć do listy turniejów")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Directly use the passed 'details' object
        Text(details.tournamentTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(details.tournamentDate, fontSize = 16.sp)
        details.tournamentCenterCount?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (details.pairResults.isEmpty()) {
            Text("Brak wyników do wyświetlenia dla tego turnieju.")
        } else {
            // Using LazyColumn for potentially long list of results
            LazyColumn(modifier = Modifier.fillMaxWidth()) { // Removed heightIn(max = 400.dp) for now, can be added back if needed
                items(details.pairResults) { pairResult ->
                    ParsedPairResultItem(pairResult = pairResult, onPlayerClick = onPlayerClick)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ParsedPairResultItem(pairResult: TournamentPairResult, onPlayerClick: (playerId: Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pairResult.place}.",
                modifier = Modifier.width(40.dp),
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f)) {
                ParsedPlayerInfoView(player = pairResult.player1, onPlayerClick = onPlayerClick)
                pairResult.player2.name.takeIf { it != "N/A" }?.let { // Check if player2 exists and name is not N/A
                    ParsedPlayerInfoView(player = pairResult.player2, onPlayerClick = onPlayerClick)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(pairResult.imps, fontWeight = FontWeight.Bold)
                pairResult.pkl?.let { Text(it) }
            }
        }
    }
}

@Composable
fun ParsedPlayerInfoView(player: TournamentPlayerInfo, onPlayerClick: (playerId: Int) -> Unit) {
    val textModifier = if (player.cezarId != null) {
        Modifier.clickable { onPlayerClick(player.cezarId) } // Ensure cezarId is not null
    } else {
        Modifier
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = player.name,
            modifier = textModifier,
            color = if (player.cezarId != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
        player.wk?.takeIf { it.isNotBlank() }?.let { Text(" ($it WK)", fontSize = 12.sp, style = MaterialTheme.typography.bodySmall) }
        player.district?.takeIf { it.isNotBlank() }?.let { Text(" - ${it.trim()}", fontSize = 12.sp, style = MaterialTheme.typography.bodySmall) }
    }
}
