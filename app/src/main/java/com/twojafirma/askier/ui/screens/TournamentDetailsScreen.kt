package com.twojafirma.askier.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.twojafirma.askier.ui.data.TournamentDetails
import com.twojafirma.askier.ui.data.TournamentPairResult
import com.twojafirma.askier.ui.data.TournamentPlayerInfo

@Composable
fun TournamentDetailsScreen(
    htmlContent: String, // For now, we pass HTML directly. Later, ViewModel might fetch it.
    tournamentDetailsViewModel: TournamentDetailsViewModel = viewModel(),
    onPlayerClickNavigation: (playerId: Int) -> Unit // Callback to navigate
) {
    LaunchedEffect(key1 = htmlContent) {
        tournamentDetailsViewModel.loadTournamentDetails(htmlContent)
    }

    val uiState by tournamentDetailsViewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is TournamentDetailsUiState.Loading -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }
            is TournamentDetailsUiState.Error -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is TournamentDetailsUiState.Success -> {
                TournamentDetailsContent(
                    tournamentDetails = state.tournamentDetails,
                    onPlayerClick = onPlayerClickNavigation
                )
            }
            is TournamentDetailsUiState.Idle -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Loading tournament data...") // Or some placeholder
                }
            }
        }
    }
}

@Composable
fun TournamentDetailsContent(
    tournamentDetails: TournamentDetails,
    onPlayerClick: (playerId: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = tournamentDetails.tournamentTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tournamentDetails.tournamentDate,
            style = MaterialTheme.typography.bodyMedium
        )
        tournamentDetails.tournamentCenterCount?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Wyniki z $it ośrodków",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(tournamentDetails.pairResults) { pairResult ->
                PairResultItem(pairResult = pairResult, onPlayerClick = onPlayerClick)
                Divider()
            }
        }
    }
}

@Composable
fun PairResultItem(
    pairResult: TournamentPairResult,
    onPlayerClick: (playerId: Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pairResult.place}.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                PlayerInfoView(player = pairResult.player1, onPlayerClick = onPlayerClick)
                PlayerInfoView(player = pairResult.player2, onPlayerClick = onPlayerClick)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("Klub: ${pairResult.clubCode ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    pairResult.resultPlusMinus?.let {
                         Text("+/-: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = pairResult.imps,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PKL: ${pairResult.pkl ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun PlayerInfoView(
    player: TournamentPlayerInfo,
    onPlayerClick: (playerId: Int) -> Unit
) {
    val playerNameStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
    val playerDetailsStyle = MaterialTheme.typography.bodySmall

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = player.name,
            style = playerNameStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = if (player.cezarId != null) {
                Modifier.clickable { onPlayerClick(player.cezarId) }
            } else {
                Modifier
            }
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "(WK: ${player.wk ?: "-"}, Okr: ${player.district ?: "-"})",
            style = playerDetailsStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
