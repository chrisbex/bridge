package com.twojafirma.askier.ui.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.twojafirma.askier.ui.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TournamentsUiState(
    val activeSessionId: Long? = null,
    val isPlayerSelectionDialogVisible: Boolean = false,
    val isAddDealDialogVisible: Boolean = false,
    val selectedPosition: Player? = null
)

class TournamentsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = SessionRepository(application)
    var uiState by mutableStateOf(TournamentsUiState())
        private set

    val sessionsFlow: StateFlow<List<GameSession>> = sessionRepository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createNewSession() {
        viewModelScope.launch {
            val newSessionId = sessionRepository.createNewSession(GameSession())
            uiState = uiState.copy(activeSessionId = newSessionId)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }

    fun openSession(sessionId: Long) {
        uiState = uiState.copy(activeSessionId = sessionId)
    }

    fun closeSession() {
        uiState = uiState.copy(activeSessionId = null)
    }

    fun onPlayerSeatClick(position: Player) {
        uiState = uiState.copy(isPlayerSelectionDialogVisible = true, selectedPosition = position)
    }

    // NOWA FUNKCJA: Czyszczenie/Edycja gracza
    fun onPlayerSeatLongClick(position: Player) {
        val sessionId = uiState.activeSessionId ?: return
        val currentSession = sessionsFlow.value.find { it.id == sessionId } ?: return

        val updatedPlayers = currentSession.players.toMutableMap()
        updatedPlayers[position] = null // Usuwamy gracza z danej pozycji

        viewModelScope.launch {
            sessionRepository.updatePlayersInSession(sessionId, updatedPlayers)
        }
    }

    fun onPlayerSelected(player: PlayerProfile) {
        val sessionId = uiState.activeSessionId ?: return
        val positionToUpdate = uiState.selectedPosition ?: return
        val currentSession = sessionsFlow.value.find { it.id == sessionId } ?: return
        val updatedPlayers = currentSession.players.toMutableMap()
        updatedPlayers[positionToUpdate] = player
        viewModelScope.launch {
            sessionRepository.updatePlayersInSession(sessionId, updatedPlayers)
            uiState = uiState.copy(isPlayerSelectionDialogVisible = false, selectedPosition = null)
        }
    }

    fun onDismissPlayerSelection() {
        uiState = uiState.copy(isPlayerSelectionDialogVisible = false, selectedPosition = null)
    }

    fun onAddDealClick() {
        uiState = uiState.copy(isAddDealDialogVisible = true)
    }

    fun onDismissAddDeal() {
        uiState = uiState.copy(isAddDealDialogVisible = false)
    }

    fun onDealSubmitted(deal: Deal, nsHcp: Int, ewHcp: Int) {
        val sessionId = uiState.activeSessionId ?: return
        val imps = calculateImps(deal.score)
        val newSessionDeal = SessionDeal(deal, nsHcp, ewHcp, imps)
        viewModelScope.launch {
            sessionRepository.addDealToSession(sessionId, newSessionDeal)
            uiState = uiState.copy(isAddDealDialogVisible = false)
        }
    }

    // NOWA FUNKCJA: Usuwanie ostatniego rozdania
    fun deleteLastDeal() {
        val sessionId = uiState.activeSessionId ?: return
        viewModelScope.launch {
            sessionRepository.deleteLastDeal(sessionId)
        }
    }

    private fun calculateImps(scoreDifference: Int): Int {
        val points = kotlin.math.abs(scoreDifference)
        val impValue = when (points) {
            in 0..10 -> 0; in 20..40 -> 1; in 50..80 -> 2; in 90..120 -> 3; in 130..160 -> 4;
            in 170..210 -> 5; in 220..260 -> 6; in 270..310 -> 7; in 320..360 -> 8; in 370..420 -> 9;
            in 430..490 -> 10; in 500..590 -> 11; in 600..740 -> 12; in 750..890 -> 13;
            in 900..1090 -> 14; in 1100..1290 -> 15; in 1300..1490 -> 16; in 1500..1740 -> 17;
            in 1750..1990 -> 18; in 2000..2240 -> 19; in 2250..2490 -> 20; in 2500..2990 -> 21;
            in 3000..3490 -> 22; in 3500..3990 -> 23; else -> 24
        }
        return if (scoreDifference < 0) -impValue else impValue
    }
}