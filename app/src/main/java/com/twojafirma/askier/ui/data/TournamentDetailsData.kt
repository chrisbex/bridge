package com.twojafirma.askier.ui.data

data class TournamentPlayerInfo(
    val name: String,
    val cezarId: Int?, // ID gracza w systemie Cezar, jeśli dostępne
    val wk: String?,
    val district: String? // Okręg
)

data class TournamentPairResult(
    val place: Int,
    val pairNumber: Int,
    val player1: TournamentPlayerInfo,
    val player2: TournamentPlayerInfo,
    val clubCode: String?, // Kod ośrodka/klubu
    val resultPlusMinus: String?,
    val imps: String,
    val pkl: String?
)

data class TournamentDetails(
    val tournamentTitle: String,
    val tournamentDate: String,
    val tournamentCenterCount: String?, // Np. "Wyniki z X ośrodków"
    val pairResults: List<TournamentPairResult>
)

