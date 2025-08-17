package com.twojafirma.askier.ui.screens

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
import com.twojafirma.askier.ui.data.PklHistoryEntry
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
    LaunchedEffect(key1 = pid) {
        viewModel.loadPlayerDetails(pid)
    }

    val uiState = viewModel.uiState

    // Tworzymy ImageLoader, który omija problem SSL
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
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
                            modifier = Modifier.size(120.dp).clip(CircleShape),
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

                        if (player.totalPkl != null && player.currentWk != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            WkProgressIndicator(
                                totalPkl = player.totalPkl,
                                currentWk = player.currentWk
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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

@Composable
fun WkProgressIndicator(totalPkl: Float, currentWk: Float) {
    val (progress, targetLabel) = remember(totalPkl, currentWk) {
        calculateWkProgress(totalPkl, currentWk)
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

private val wkThresholds = mapOf(
    0.5f to 5, 1.0f to 20, 1.5f to 50, 2.0f to 100, 2.5f to 200,
    3.0f to 300, 3.5f to 500, 4.0f to 750, 4.5f to 1000, 5.0f to 1500,
    6.0f to 2500, 7.0f to 4000, 8.0f to 6000, 9.0f to 8000, 10.0f to 10000,
    12.0f to 15000, 15.0f to 25000, 20.0f to 50000, 25.0f to 75000, 30.0f to 100000
)

fun calculateWkProgress(totalPkl: Float, currentWk: Float): Pair<Float, String> {
    val nextWkThresholdEntry = wkThresholds.entries.find { it.key > currentWk }

    if (nextWkThresholdEntry == null) {
        return 1f to "Arcymistrz Międzynarodowy"
    }

    val nextWk = nextWkThresholdEntry.key
    val pointsForNextWk = nextWkThresholdEntry.value
    val pointsForCurrentWk = wkThresholds.entries.lastOrNull { it.key <= currentWk }?.value ?: 0

    val pointsNeededInTier = (pointsForNextWk - pointsForCurrentWk).toFloat()
    val pointsEarnedInTier = (totalPkl - pointsForCurrentWk).toFloat()

    if (pointsNeededInTier <= 0) return 1f to "MAX"

    val progress = (pointsEarnedInTier / pointsNeededInTier).coerceIn(0f, 1f)

    val targetLabel = "${totalPkl.toInt()}/$pointsForNextWk PKL (do ${nextWk} WK)"

    return progress to targetLabel
}