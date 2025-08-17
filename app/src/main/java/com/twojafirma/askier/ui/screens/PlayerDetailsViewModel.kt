package com.twojafirma.askier.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twojafirma.askier.ui.data.PlayerProfile
import com.twojafirma.askier.ui.data.PzbsRepository
import kotlinx.coroutines.launch

data class PlayerDetailsUiState(
    val player: PlayerProfile? = null,
    val isLoading: Boolean = true
)

class PlayerDetailsViewModel : ViewModel() {

    private val repository = PzbsRepository()
    var uiState by mutableStateOf(PlayerDetailsUiState())
        private set

    fun loadPlayerDetails(pid: Int) {
        Log.e("PlayerDetailsVM_LOAD", "loadPlayerDetails called for pid: $pid") // Ten log już masz
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val playerDetails =
                repository.getPlayerDetails(pid) // Zakładam, że to jest metoda zwracająca PlayerProfile?


            Log.d(
                "PlayerDetailsVM_DATA", "Fetched playerDetails from repository: " +
                        "Name=${playerDetails?.name}, " +
                        "PKL=${playerDetails?.totalPkl}, " + // Upewnij się, że PlayerProfile ma pole 'totalPkl'
                        "WK=${playerDetails?.currentWk}, " +   // Upewnij się, że PlayerProfile ma pole 'currentWk'
                        "Club=${playerDetails?.club}, " +
                        "Title=${playerDetails?.title}, " +
                        "PhotoURL=${playerDetails?.photoUrl}"
            )

            uiState = uiState.copy(player = playerDetails, isLoading = false)


            Log.d(
                "PlayerDetailsVM_STATE", "uiState updated: " +
                        "Player Name=${uiState.player?.name}, " +
                        "isLoading=${uiState.isLoading}, " +
                        "PKL from uiState=${uiState.player?.totalPkl}, " + // j.w.
                        "WK from uiState=${uiState.player?.currentWk}"
            )     // j.w.
        }
    }
}