package com.twojafirma.askier

sealed class Screen(val route: String, val label: String? = null, val icon: Int? = null) {
    object Tournaments : Screen("tournaments", "Turnieje", R.drawable.ic_tournaments)
    object Players : Screen("players", "Zawodnicy", R.drawable.ic_players)
    object Settings : Screen("settings", "Ustawienia", R.drawable.ic_settings)

    object PlayerDetails : Screen("player_details/{pid}") {
        fun createRoute(pid: Int) = "player_details/$pid"
    }
}