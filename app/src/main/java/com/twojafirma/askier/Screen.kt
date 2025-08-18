package com.twojafirma.askier

// Upewnij się, że R jest poprawnie zaimportowane, jeśli IDE tego nie zrobi automatycznie.
// Jeśli Twój pakiet to com.twojafirma.askier, import R powinien być automatyczny.

sealed class Screen(val route: String, val label: String? = null, val icon: Int? = null) {
    object Tournaments : Screen("tournaments", "Turnieje", R.drawable.ic_tournaments)
    object Players : Screen("players", "Zawodnicy", R.drawable.ic_players)
    object Settings : Screen("settings", "Ustawienia", R.drawable.ic_settings)

    // NOWY EKRAN SPIDER
    object Spider : Screen("spider", "Spider", R.drawable.ic_spider) // Użycie nowej ikony

    object PlayerDetails : Screen("player_details/{pid}") {
        fun createRoute(pid: Int) = "player_details/$pid"
    }
}