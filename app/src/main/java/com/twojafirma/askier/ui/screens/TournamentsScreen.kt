package com.twojafirma.askier.ui.screens

import androidx.compose.animation.AnimatedVisibility // NOWY IMPORT
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown // NOWY IMPORT
import androidx.compose.material.icons.filled.KeyboardArrowUp // NOWY IMPORT
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.twojafirma.askier.Screen
import com.twojafirma.askier.ui.data.*
import com.twojafirma.askier.ui.data.Double // Upewnij się, że to jest poprawny import dla Twojego enum 'Double'
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsScreen(
    navController: NavController,
    tournamentsViewModel: TournamentsViewModel = viewModel()
) {
    val uiState by remember { derivedStateOf { tournamentsViewModel.uiState } }
    val sessions by tournamentsViewModel.sessionsFlow.collectAsState()
    val activeSession = sessions.find { it.id == uiState.activeSessionId }

    Scaffold(
        topBar = {
            if (activeSession != null) {
                TopAppBar(
                    title = { Text(activeSession.title, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = { tournamentsViewModel.closeSession() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Wróć do listy")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            val allPlayersSelected = activeSession?.players?.values?.all { it != null } == true
            FloatingActionButton(
                onClick = {
                    if (activeSession == null) {
                        tournamentsViewModel.createNewSession()
                    } else if (allPlayersSelected) {
                        tournamentsViewModel.onAddDealClick()
                    }
                },
                containerColor = if (activeSession == null || allPlayersSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Dodaj",
                    tint = if (activeSession == null || allPlayersSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isPlayerSelectionDialogVisible) {
            SelectPlayerDialog(
                navController = navController,
                onDismiss = { tournamentsViewModel.onDismissPlayerSelection() },
                onPlayerSelected = { tournamentsViewModel.onPlayerSelected(it) }
            )
        }

        if (uiState.isAddDealDialogVisible) {
            AddSessionDealDialog(
                onDismissRequest = { tournamentsViewModel.onDismissAddDeal() },
                onConfirmation = { deal, nsHcp, ewHcp ->
                    tournamentsViewModel.onDealSubmitted(deal, nsHcp, ewHcp)
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (activeSession == null) {
                SessionListView(
                    sessions = sessions,
                    onSessionClick = { tournamentsViewModel.openSession(it) },
                    onSessionDelete = { tournamentsViewModel.deleteSession(it) }
                )
            } else {
                GameSessionView(
                    session = activeSession,
                    navController = navController,
                    onPlayerSeatClick = { tournamentsViewModel.onPlayerSeatClick(it) },
                    onPlayerSeatLongClick = { tournamentsViewModel.onPlayerSeatLongClick(it) },
                    onDeleteLastDeal = { tournamentsViewModel.deleteLastDeal() }
                )
            }
        }
    }
}

@Composable
fun SessionListView(
    sessions: List<GameSession>,
    onSessionClick: (Long) -> Unit,
    onSessionDelete: (Long) -> Unit
) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak zapisanych sesji.\nKliknij '+' aby dodać pierwszą.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    onClick = { onSessionClick(session.id) },
                    onDelete = { onSessionDelete(session.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionRow(
    session: GameSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val date = Date(session.id)
    val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(date)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Potwierdź usunięcie") },
            text = { Text("Czy na pewno chcesz usunąć sesję '${session.title}'? Tej operacji nie można cofnąć.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = onClick,
            onLongClick = { showDeleteDialog = true }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Utworzono: $formattedDate", style = MaterialTheme.typography.bodySmall)
                Text("Rozdań: ${session.deals.size}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń sesję", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun GameSessionView(
    session: GameSession,
    navController: NavController,
    onPlayerSeatClick: (Player) -> Unit,
    onPlayerSeatLongClick: (Player) -> Unit,
    onDeleteLastDeal: () -> Unit
) {
    var isPlayerTableExpanded by remember { mutableStateOf(true) } // NOWY STAN

    ScoreboardView(nsScore = session.totalNsImps)
    Spacer(modifier = Modifier.height(16.dp))

    // NOWY BLOK - Przycisk do rozwijania/zwijania stołu graczy
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isPlayerTableExpanded = !isPlayerTableExpanded }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isPlayerTableExpanded) "Ukryj stół graczy" else "Pokaż stół graczy",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isPlayerTableExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (isPlayerTableExpanded) "Ukryj stół graczy" else "Pokaż stół graczy"
        )
    }

    // PlayerTableView opakowane w AnimatedVisibility
    AnimatedVisibility(visible = isPlayerTableExpanded) {
        PlayerTableView(
            players = session.players,
            navController = navController,
            onPlayerClick = onPlayerSeatClick,
            onPlayerLongClick = onPlayerSeatLongClick
        )
    }
    // Koniec modyfikacji związanych z PlayerTableView

    Divider(modifier = Modifier.padding(vertical = 16.dp))
    if (session.deals.isEmpty()) {
        if (session.players.values.all { it != null }) { // Upewnij się, że ta logika jest poprawna dla Twojej definicji "wszyscy gracze wybrani"
            Text("Wszyscy gracze wybrani. Kliknij '+' aby dodać rozdanie.", textAlign = TextAlign.Center)
        } else {
            Text("Wybierz wszystkich graczy, aby rozpocząć.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(session.deals, key = { it.baseDeal.id }) { deal ->
                SessionDealRow(
                    sessionDeal = deal,
                    onLongClick = { onDeleteLastDeal() }
                )
            }
        }
    }
}

@Composable
fun ScoreboardView(nsScore: Int) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NS", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$nsScore", style = MaterialTheme.typography.headlineMedium,
                    color = if (nsScore >= 0) Color(0xFF008000) else MaterialTheme.colorScheme.error
                )
            }
            Text("vs", style = MaterialTheme.typography.titleLarge)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("EW", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${-nsScore}", style = MaterialTheme.typography.headlineMedium,
                    color = if (-nsScore >= 0) Color(0xFF008000) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PlayerTableView(
    players: Map<Player, PlayerProfile?>,
    navController: NavController,
    onPlayerClick: (Player) -> Unit,
    onPlayerLongClick: (Player) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, content = {})
        PlayerSeat(modifier = Modifier.align(Alignment.TopCenter), player = players[Player.N], position = Player.N, navController = navController, onClick = { onPlayerClick(Player.N) }, onLongClick = { onPlayerLongClick(Player.N) })
        PlayerSeat(modifier = Modifier.align(Alignment.BottomCenter), player = players[Player.S], position = Player.S, navController = navController, onClick = { onPlayerClick(Player.S) }, onLongClick = { onPlayerLongClick(Player.S) })
        PlayerSeat(modifier = Modifier.align(Alignment.CenterStart), player = players[Player.W], position = Player.W, navController = navController, onClick = { onPlayerClick(Player.W) }, onLongClick = { onPlayerLongClick(Player.W) })
        PlayerSeat(modifier = Modifier.align(Alignment.CenterEnd), player = players[Player.E], position = Player.E, navController = navController, onClick = { onPlayerClick(Player.E) }, onLongClick = { onPlayerLongClick(Player.E) })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerSeat(
    modifier: Modifier = Modifier,
    player: PlayerProfile?,
    position: Player,
    navController: NavController,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = modifier.combinedClickable(
            onClick = {
                if (player != null) {
                    navController.navigate(Screen.PlayerDetails.createRoute(player.pid))
                } else {
                    onClick()
                }
            },
            onLongClick = onLongClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (player?.photoData != null) {
                AsyncImage(model = player.photoData, contentDescription = "Zdjęcie ${player.name}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Wybierz gracza", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = player?.name ?: position.name, fontWeight = if (player != null) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun SelectPlayerDialog(
    navController: NavController,
    onDismiss: () -> Unit,
    onPlayerSelected: (PlayerProfile) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxSize().padding(vertical = 32.dp)) {
            Box(contentAlignment = Alignment.TopEnd) {
                // Przekazujemy isDialogMode = true, aby ekran wiedział, jak się zachować
                PlayersScreen(
                    navController = navController,
                    isDialogMode = true,
                    onPlayerSelected = onPlayerSelected
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Zamknij") }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDealDialog(onDismissRequest: () -> Unit, onConfirmation: (Deal, Int, Int) -> Unit) {
    var level by remember { mutableStateOf(1) }
    var suit by remember { mutableStateOf(Suit.NO_TRUMP) }
    var double by remember { mutableStateOf(Double.NONE) }
    var declarer by remember { mutableStateOf(Player.N) }
    var tricks by remember { mutableStateOf(0) }
    var vulnerability by remember { mutableStateOf(Vulnerability.NONE) }
    var nsHcp by remember { mutableFloatStateOf(20f) }
    val ewHcp = 40 - nsHcp.toInt()
    val isVulnerable = when (vulnerability) {
        Vulnerability.NS -> declarer == Player.N || declarer == Player.S
        Vulnerability.EW -> declarer == Player.E || declarer == Player.W
        Vulnerability.BOTH -> true
        Vulnerability.NONE -> false
    }
    val calculatedScore by remember(level, suit, double, tricks, isVulnerable) {
        derivedStateOf { calculateScore(level, suit, double, tricks, isVulnerable) }
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(16.dp)) {
            LazyColumn(modifier = Modifier.padding(24.dp)) {
                item {
                    Text("Nowe rozdanie", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 20.dp))
                    SectionTitle("Założenia")
                    SelectableSegmentedButtonRow(
                        items = Vulnerability.entries.map { it.description.replace(" po partii", "") },
                        selectedItem = vulnerability.description.replace(" po partii", ""),
                        onItemSelected = { selectedText -> vulnerability = Vulnerability.entries.first { it.description.replace(" po partii", "") == selectedText } }
                    )
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Kontrakt")
                    SelectableSegmentedButtonRow(
                        items = (1..7).map { it.toString() },
                        selectedItem = level.toString(),
                        onItemSelected = { level = it.toInt() }
                    )
                    Spacer(Modifier.height(8.dp))
                    SelectableSegmentedButtonRow(
                        items = Suit.entries.map { it.symbol },
                        selectedItem = suit.symbol,
                        onItemSelected = { selectedSymbol -> suit = Suit.entries.first { it.symbol == selectedSymbol } }
                    )
                    Spacer(Modifier.height(8.dp))
                    SelectableSegmentedButtonRow(
                        items = Double.entries.map { if (it == Double.NONE) "PASS" else it.symbol },
                        selectedItem = if (double == Double.NONE) "PASS" else double.symbol,
                        onItemSelected = { selectedText -> double = Double.entries.first { (if (it == Double.NONE) "PASS" else it.symbol) == selectedText } }
                    )
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Rozgrywający")
                    SelectableSegmentedButtonRow(
                        items = Player.entries.map { it.name },
                        selectedItem = declarer.name,
                        onItemSelected = { selectedName -> declarer = Player.valueOf(selectedName) }
                    )
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Wynik (w lewach)")
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { tricks-- }) { Text("-") }
                        Text(text = when {
                            tricks == 0 -> "=="
                            tricks > 0 -> "+$tricks"
                            else -> "$tricks"
                        }, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp))
                        OutlinedButton(onClick = { tricks++ }) { Text("+") }
                    }
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Punkty za Karty (PC)")
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NS", style = MaterialTheme.typography.labelLarge)
                                Text("${nsHcp.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EW", style = MaterialTheme.typography.labelLarge)
                                Text("$ewHcp", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        Slider(
                            value = nsHcp,
                            onValueChange = { nsHcp = it },
                            valueRange = 0f..40f,
                            steps = 39
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Zapis: ", style = MaterialTheme.typography.titleMedium)
                        Text(text = "$calculatedScore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (calculatedScore >= 0) Color(0xFF008000) else MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismissRequest) { Text("Anuluj") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val newDeal = Deal(
                                id = (System.currentTimeMillis() % 10000).toInt(),
                                level = level, suit = suit, declarer = declarer,
                                double = double, tricks = tricks, score = calculatedScore,
                                vulnerability = vulnerability
                            )
                            onConfirmation(newDeal, nsHcp.toInt(), ewHcp)
                        }) { Text("Dodaj") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionDealRow(
    sessionDeal: SessionDeal,
    onLongClick: () -> Unit
) {
    val deal = sessionDeal.baseDeal
    val contractString = "${deal.level}${deal.suit.symbol}${deal.double.symbol} przez ${deal.declarer.name}"
    val resultString = when {
        deal.tricks == 0 -> "zrealizowany"
        deal.tricks > 0 -> "+${deal.tricks}"
        else -> "${deal.tricks}"
    }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = {},
            onLongClick = onLongClick
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = "Kontrakt: $contractString", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "Wynik: $resultString (${deal.score} pkt)", style = MaterialTheme.typography.bodyMedium)
            }
            val impColor = if (sessionDeal.imps > 0) Color(0xFF008000) else if (sessionDeal.imps < 0) MaterialTheme.colorScheme.error else Color.Gray
            Text(text = "${sessionDeal.imps} IMP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = impColor)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableSegmentedButtonRow(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                onClick = { onItemSelected(item) },
                selected = item == selectedItem,
                icon = {},
                modifier = Modifier.weight(1f)
            ) { Text(item) }
        }
    }
}

fun calculateScore(level: Int, suit: Suit, double: Double, tricks: Int, isVulnerable: Boolean): Int {
    val tricksNeeded = level + 6
    val tricksTaken = tricksNeeded + tricks
    val downTricks = tricksNeeded - tricksTaken
    if (downTricks > 0) {
        if (double == Double.NONE) return -downTricks * (if (isVulnerable) 100 else 50)
        var penalty = if (isVulnerable) -200 else -100
        if (downTricks >= 2) penalty += (if (isVulnerable) -300 else -200)
        if (downTricks >= 3) penalty += (if (isVulnerable) -300 else -200)
        if (downTricks >= 4) penalty += (downTricks - 3) * (if (isVulnerable) -300 else -200)
        return if (double == Double.DBL) penalty else penalty * 2
    }
    var score = 0
    val baseTrickValue = if (suit.ordinal <= 1) 20 else 30
    var contractPoints = level * baseTrickValue
    if (suit == Suit.NO_TRUMP) contractPoints = (level * 30) + 10
    if (double != Double.NONE) contractPoints *= if (double == Double.DBL) 2 else 4
    score += contractPoints
    val gameBonus = if (isVulnerable) 500 else 300
    score += if (contractPoints >= 100) gameBonus else 50
    if (level == 6) score += if (isVulnerable) 750 else 500
    if (level == 7) score += if (isVulnerable) 1500 else 1000
    val overtrickValue = when (double) {
        Double.NONE -> baseTrickValue
        Double.DBL -> if (isVulnerable) 200 else 100
        Double.RDBL -> if (isVulnerable) 400 else 200
    }
    score += tricks * overtrickValue
    if (double == Double.DBL) score += 50
    if (double == Double.RDBL) score += 100
    return score
}