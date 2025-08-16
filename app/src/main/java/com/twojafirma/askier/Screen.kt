package com.twojafirma.askier

sealed class Screen(val route: String, val label: String, val icon: Int) {
    object Tournaments : Screen("tournaments", "Turnieje", R.drawable.ic_tournaments)
    object Players : Screen("players", "Zawodnicy", R.drawable.ic_players)
    object Settings : Screen("settings", "Ustawienia", R.drawable.ic_settings)
}