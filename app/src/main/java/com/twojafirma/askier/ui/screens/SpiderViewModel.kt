package com.twojafirma.askier.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twojafirma.askier.ui.data.SpiderRepository
import com.twojafirma.askier.ui.data.TournamentData
import com.twojafirma.askier.ui.data.TournamentDetails // Dodany import
import com.twojafirma.askier.util.TournamentHtmlParser // Dodany import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Zaktualizowany SpiderUiState
data class SpiderUiState(
    // Pola dla listy turniejów
    val tournaments: List<TournamentData> = emptyList(),
    val isLoadingTournaments: Boolean = false, // Zmieniona nazwa
    val tournamentsErrorMessage: String? = null, // Zmieniona nazwa

    // Pola dla szczegółów wybranego turnieju
    val selectedTournamentDetails: TournamentDetails? = null,
    val isLoadingDetails: Boolean = false,
    val detailsErrorMessage: String? = null
)

class SpiderViewModel(
    private val spiderRepository: SpiderRepository = SpiderRepository() // Domyślna instancja
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpiderUiState())
    val uiState: StateFlow<SpiderUiState> = _uiState.asStateFlow()

    fun fetchTournaments() {
        viewModelScope.launch {
            // Używamy .copy() do aktualizacji stanu
            _uiState.value = _uiState.value.copy(isLoadingTournaments = true, tournamentsErrorMessage = null)
            try {
                val result = spiderRepository.fetchTournamentLinks()
                result.fold(
                    onSuccess = { tournamentList ->
                        _uiState.value = _uiState.value.copy(
                            tournaments = tournamentList,
                            isLoadingTournaments = false
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            tournaments = emptyList(), // Można rozważyć zachowanie starych danych: _uiState.value.tournaments
                            tournamentsErrorMessage = exception.message ?: "Unknown error while fetching tournaments",
                            isLoadingTournaments = false
                        )
                    }
                )
            } catch (e: Exception) { // Dodatkowy catch na wszelki wypadek
                _uiState.value = _uiState.value.copy(
                    tournaments = emptyList(), // Można rozważyć zachowanie starych danych
                    tournamentsErrorMessage = e.message ?: "Unexpected error while fetching tournaments",
                    isLoadingTournaments = false
                )
            }
        }
    }

    fun fetchAndParseTournamentDetails(url: String) {
        viewModelScope.launch {
            // Ustaw stan ładowania i wyczyść poprzednie dane/błędy szczegółów
            _uiState.value = _uiState.value.copy(
                isLoadingDetails = true,
                selectedTournamentDetails = null,
                detailsErrorMessage = null
            )

            try {
                // 1. Pobierz HTML
                val htmlResult = spiderRepository.fetchHtmlContent(url)

                htmlResult.fold(
                    onSuccess = { htmlString ->
                        // 2. Sparsuj HTML
                        try {
                            val parser = TournamentHtmlParser() // Utwórz instancję parsera
                            val details = parser.parseTournamentDetails(htmlString)
                            _uiState.value = _uiState.value.copy(
                                selectedTournamentDetails = details,
                                isLoadingDetails = false
                            )
                        } catch (e: Exception) {
                            // Błąd podczas parsowania
                            _uiState.value = _uiState.value.copy(
                                detailsErrorMessage = "Błąd parsowania danych turnieju: ${e.message}",
                                isLoadingDetails = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        // Błąd podczas pobierania HTML
                        _uiState.value = _uiState.value.copy(
                            detailsErrorMessage = "Błąd pobierania szczegółów turnieju: ${exception.message}",
                            isLoadingDetails = false
                        )
                    }
                )
            } catch (e: Exception) {
                // Ogólny błąd (np. problem z coroutine)
                _uiState.value = _uiState.value.copy(
                    detailsErrorMessage = "Niespodziewany błąd: ${e.message}",
                    isLoadingDetails = false
                )
            }
        }
    }

    fun clearSelectedTournamentDetails() {
        _uiState.value = _uiState.value.copy(
            selectedTournamentDetails = null,
            isLoadingDetails = false,
            detailsErrorMessage = null
        )
    }

    init {
       // fetchTournaments() // Pozostaje zakomentowane, chyba że chcesz ładować listę przy starcie
    }
}
