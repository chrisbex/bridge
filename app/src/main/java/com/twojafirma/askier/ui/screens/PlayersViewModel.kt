package com.twojafirma.askier.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twojafirma.askier.ui.data.PlayerProfile
import com.twojafirma.askier.ui.data.PzbsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PlayersUiState(
    val searchQuery: String = "",
    val players: List<PlayerProfile> = emptyList(),
    val isLoading: Boolean = false
)

class PlayersViewModel : ViewModel() {

    private val repository = PzbsRepository()
    var uiState by mutableStateOf(PlayersUiState())
        private set

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
        searchJob?.cancel()

        if (query.length < 1) {
            uiState = uiState.copy(players = emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            uiState = uiState.copy(isLoading = true)
            val results = repository.searchPlayers(query)
            uiState = uiState.copy(
                players = results,
                isLoading = false
            )
        }
    }
}