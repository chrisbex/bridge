package com.twojafirma.askier

import android.os.Bundle
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
import com.twojafirma.askier.ui.theme.AsKierTheme

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val bottomBarItems = listOf(Screen.Tournaments, Screen.Players, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomBarItems.forEach { screen ->
                    val label = screen.label ?: ""
                    val iconRes = screen.icon ?: 0

                    if (iconRes != 0) {
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
    settingsViewModel: SettingsViewModel
) {
    NavHost(navController, startDestination = Screen.Tournaments.route, modifier) {
        composable(Screen.Tournaments.route) { TournamentsScreen(navController = navController) }
        // Przekazujemy isDialogMode = false, bo to jest główna zakładka
        composable(Screen.Players.route) { PlayersScreen(navController = navController, isDialogMode = false) }
        composable(Screen.Settings.route) { SettingsScreen(viewModel = settingsViewModel) }

        composable(
            route = Screen.PlayerDetails.route,
            arguments = listOf(navArgument("pid") { type = NavType.IntType })
        ) { backStackEntry ->
            val pid = backStackEntry.arguments?.getInt("pid")
            if (pid != null) {
                PlayerDetailsScreen(pid = pid, navController = navController)
            }
        }
    }
}