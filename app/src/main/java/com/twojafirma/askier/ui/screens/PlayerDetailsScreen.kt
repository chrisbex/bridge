package com.twojafirma.askier.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import com.twojafirma.askier.R
import com.twojafirma.askier.ui.data.PklHistoryEntry // Upewnij się, że PlayerProfile jest importowane, jeśli ViewModel go używa
import com.twojafirma.askier.ui.data.PlayerProfile // Potrzebne dla uiState.player
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailsScreen(
    pid: Int,
    navController: NavController,
    viewModel: PlayerDetailsViewModel = viewModel()
) {
    Log.e("PlayerDetailsScreen_ENTRY", "PlayerDetailsScreen Composable entered for pid: $pid")
    LaunchedEffect(key1 = pid) {
        viewModel.loadPlayerDetails(pid)
    }

    val uiState = viewModel.uiState

    val context = LocalContext.current
    val imageLoader = remember {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
        ImageLoader.Builder(context).okHttpClient(okHttpClient).build()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.player?.name ?: "Profil Zawodnika") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.player == null) {
                Text("Nie udało się załadować danych zawodnika.")
            } else {
                val player = uiState.player!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        AsyncImage(
                            model = player.photoUrl,
                            imageLoader = imageLoader,
                            contentDescription = "Zdjęcie ${player.name}",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.ic_players)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(player.name, style = MaterialTheme.typography.headlineLarge)
                        if (player.title != null) {
                            Text(player.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("PID: ${player.pid}", style = MaterialTheme.typography.bodyMedium)
                        Text(player.club, style = MaterialTheme.typography.bodyMedium)

                        // Zmienione wywołanie WkProgressIndicator
                        if (player.totalPkl != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            WkProgressIndicator(
                                totalPkl = player.totalPkl
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            Text("Brak danych o punktach PKL do wyświetlenia postępu WK.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Divider()
                    }

                    if (player.pklHistory.isNotEmpty()) {
                        item {
                            Text(
                                "Historia Punktów",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                        items(player.pklHistory) { historyEntry ->
                            HistoryRow(entry = historyEntry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRow(entry: PklHistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(entry.year, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("PKL: ${entry.pkl}", style = MaterialTheme.typography.bodyLarge)
            Text("WK: ${entry.wk}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

// Zmieniona sygnatura WkProgressIndicator
@Composable
fun WkProgressIndicator(totalPkl: Float) {
    // Zmieniony klucz remember
    val (progress, targetLabel) = remember(totalPkl) {
        calculateWkProgress(totalPkl)
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "WkProgressAnimation")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Postęp do następnego WK", style = MaterialTheme.typography.labelMedium)
            Text(targetLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// NOWA MAPA OFICJALNYCH PROGÓW WK (z sumarycznymi PKL)
// Uporządkowana rosnąco według WK
private val officialWkPklThresholds = linkedMapOf(
    0.5f to 50,    // Kandydat
    1.0f to 150,   // Adept (50+100)
    1.5f to 350,   // M. Klubowy (150+200)
    2.0f to 850,   // M. Okręgowy (350+500)
    2.5f to 1650,  // M. Wojewódzki (850+800)
    3.0f to 2850,  // M. Regionalny (1650+1200)
    4.0f to 4850,  // M. Krajowy (2850+2000)
    5.0f to 7850,  // M. Krajowy (4850+3000)
    7.0f to 12850, // M. Międzynarodowy (7850+5000)
    9.0f to 20850, // M. Międzynarodowy (12850+8000)
    11.0f to 28850,// M. Międzynarodowy (20850+8000)
    12.0f to 38850,// Arcymistrz (28850+10000)
    13.0f to 38850,// Arcymistrz (+0 PKL, +1 PM) - taki sam próg PKL jak 12.0
    15.0f to 48850,// Arcymistrz (38850+10000)
    17.0f to 63850,// Arcymistrz (48850+15000)
    18.0f to 83850,// A. Międzynarodowy (63850+20000)
    19.0f to 83850,// A. Międzynarodowy (+0 PKL, +2 MPM) - taki sam próg PKL jak 18.0
    21.0f to 103850 // A. Międzynarodowy (83850+20000)
    // Poziom 24.0f (Arcymistrz Światowy) jest specjalny i pominięty dla uproszczenia
)

// PRZEPISANA funkcja calculateWkProgress
fun calculateWkProgress(playerGrandTotalPkl: Float): Pair<Float, String> {
    if (playerGrandTotalPkl < 0) return 0f to "Błędne PKL"
    if (officialWkPklThresholds.isEmpty()) return 0f to "Brak definicji progów WK"

    var currentOfficialWk = 0.0f
    var pklForCurrentOfficialWk = 0

    // Ustal aktualny oficjalny poziom WK gracza na podstawie jego sumy PKL
    for ((wk, pklNeeded) in officialWkPklThresholds) {
        if (playerGrandTotalPkl >= pklNeeded) {
            currentOfficialWk = wk
            pklForCurrentOfficialWk = pklNeeded
        } else {
            // Gracz nie osiągnął tego progu, więc poprzedni był jego aktualnym
            break
        }
    }

    // Znajdź następny oficjalny poziom WK
    var nextOfficialWk: Float? = null
    var pklForNextOfficialWk: Int? = null
    for ((wk, pklNeeded) in officialWkPklThresholds) {
        if (wk > currentOfficialWk) {
            nextOfficialWk = wk
            pklForNextOfficialWk = pklNeeded
            break
        }
    }

    // Przypadek 1: Gracz osiągnął najwyższy zdefiniowany poziom WK lub go przekroczył
    if (nextOfficialWk == null || pklForNextOfficialWk == null) {
        return 1.0f to "Najwyższy poziom WK (${"%.1f".format(currentOfficialWk)})"
    }

    // Przypadek 2: Istnieje następny poziom WK
    val pointsEarnedInThisTier = playerGrandTotalPkl - pklForCurrentOfficialWk
    val pointsNeededForNextTierStep = pklForNextOfficialWk - pklForCurrentOfficialWk

    if (pointsNeededForNextTierStep <= 0) {
        // Ten przypadek oznacza, że następny poziom WK ma taki sam lub niższy próg PKL
        // (np. WK 12.0 i 13.0 mają ten sam próg PKL).
        // Traktujemy to jako spełnione wymaganie PKL dla następnego poziomu.
        return 1.0f to "Wymagania PKL dla ${"%.1f".format(nextOfficialWk)} WK spełnione"
    }

    val progress = (pointsEarnedInThisTier.toFloat() / pointsNeededForNextTierStep.toFloat()).coerceIn(0f, 1f)

    val label: String
    if (playerGrandTotalPkl >= pklForNextOfficialWk) {
        // To się nie powinno zdarzyć, jeśli pointsNeededForNextTierStep > 0 i progress jest < 1.0,
        // ale dla pewności.
        label = "Osiągnięto ${"%.1f".format(nextOfficialWk)} WK!"
    } else {
        // Ile PKL brakuje do następnego progu
        // val remainingPkl = pointsNeededForNextTierStep - pointsEarnedInThisTier
        // label = "${remainingPkl.toInt()} PKL do ${"%.1f".format(nextOfficialWk)} WK"
        // Lepsza etykieta pokazująca postęp: X/Y
        label = "${pointsEarnedInThisTier.toInt()}/${pointsNeededForNextTierStep.toInt()} PKL do ${"%.1f".format(nextOfficialWk)} WK"
    }

    return progress to label
}

