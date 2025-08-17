package com.twojafirma.askier.ui.screens

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
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val playerDetails = repository.getPlayerDetails(pid)
            uiState = uiState.copy(player = playerDetails, isLoading = false)
        }
    }
}