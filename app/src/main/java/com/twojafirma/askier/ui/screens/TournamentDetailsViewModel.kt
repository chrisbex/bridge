package com.twojafirma.askier.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twojafirma.askier.ui.data.TournamentDetails
import com.twojafirma.askier.util.TournamentHtmlParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sealed interface to represent the different states of the screen
sealed interface TournamentDetailsUiState {
    data class Success(val tournamentDetails: TournamentDetails) : TournamentDetailsUiState
    data class Error(val message: String) : TournamentDetailsUiState
    object Loading : TournamentDetailsUiState
    object Idle : TournamentDetailsUiState // Initial state
}

class TournamentDetailsViewModel : ViewModel() {

    private val tournamentHtmlParser = TournamentHtmlParser()

    private val _uiState = MutableStateFlow<TournamentDetailsUiState>(TournamentDetailsUiState.Idle)
    val uiState: StateFlow<TournamentDetailsUiState> = _uiState.asStateFlow()

    fun loadTournamentDetails(htmlContent: String) {
        viewModelScope.launch {
            _uiState.value = TournamentDetailsUiState.Loading
            try {
                val details = tournamentHtmlParser.parseTournamentDetails(htmlContent)
                _uiState.value = TournamentDetailsUiState.Success(details)
            } catch (e: Exception) {
                // In a real app, provide a more user-friendly error message
                _uiState.value = TournamentDetailsUiState.Error("Failed to parse tournament details: ${e.message}")
                // Log the exception e.g. using Timber.e(e, "Error parsing tournament HTML")
            }
        }
    }
}
