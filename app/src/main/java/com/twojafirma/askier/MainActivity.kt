package com.twojafirma.askier

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.twojafirma.askier.ui.data.ThemeSetting
import com.twojafirma.askier.ui.screens.*
// Upewniamy się, że import dla SpiderScreen jest obecny i odkomentowany
import com.twojafirma.askier.ui.screens.SpiderScreen 
import com.twojafirma.askier.ui.theme.AsKierTheme

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("MY_APP_TAG", "MainActivity onCreate - TEST LOG")
        setContent {
            val themeSetting by settingsViewModel.themeSetting.collectAsState()
            val useDarkTheme = when (themeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }
            AsKierTheme(darkTheme = useDarkTheme) {
                MainScreen(settingsViewModel = settingsViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val bottomBarItems = listOf(Screen.Tournaments, Screen.Players, Screen.Spider, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomBarItems.forEach { screen ->
                    val label = screen.label ?: ""
                    screen.icon?.let { iconRes ->
                        NavigationBarItem(
                            icon = { Icon(painterResource(id = iconRes), contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            settingsViewModel = settingsViewModel
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel // Dodajemy settingsViewModel jako parametr
) {
    NavHost(navController, startDestination = Screen.Tournaments.route, modifier) {
        composable(Screen.Tournaments.route) { TournamentsScreen(navController = navController) }
        composable(Screen.Players.route) { PlayersScreen(navController = navController, isDialogMode = false) }

        // Zaktualizowane wywołanie SettingsScreen
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = settingsViewModel) // Już nie przekazujemy onNavigateToPlayerProfile
        }

        // Zaktualizowane wywołanie SpiderScreen
        composable(Screen.Spider.route) {
            SpiderScreen(
                navController = navController, // Przekazujemy navController
                onNavigateToPlayerProfile = { playerId ->
                    navController.navigate(Screen.PlayerDetails.route.replace("{pid}", playerId.toString()))
                }
            )
        }

        composable(
            route = Screen.PlayerDetails.route,
            arguments = listOf(navArgument("pid") { type = NavType.IntType })
        ) { backStackEntry ->
            val pid = backStackEntry.arguments?.getInt("pid")
            if (pid != null) {
                PlayerDetailsScreen(pid = pid, navController = navController)
            } else {
                Log.e("AppNavHost", "PID is null for PlayerDetailsScreen route.")
                // Można dodać Text("Błąd: Brak ID gracza") lub podobny komunikat
            }
        }
    }
}
