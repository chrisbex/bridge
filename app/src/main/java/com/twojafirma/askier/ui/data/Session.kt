package com.twojafirma.askier.ui.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GameSession(
    val id: Long = System.currentTimeMillis(),
    val title: String = "Sesja z dnia ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}",
    val players: Map<Player, PlayerProfile?> = mapOf(
        Player.N to null,
        Player.S to null,
        Player.E to null,
        Player.W to null
    ),
    val deals: List<SessionDeal> = emptyList()
) {
    val totalNsImps: Int
        get() = deals.sumOf { deal ->
            val declarer = deal.baseDeal.declarer
            if (declarer == Player.N || declarer == Player.S) {
                deal.imps
            } else {
                -deal.imps
            }
        }
}

data class SessionDeal(
    val baseDeal: Deal,
    val nsPoints: Int,
    val ewPoints: Int,
    val imps: Int
)