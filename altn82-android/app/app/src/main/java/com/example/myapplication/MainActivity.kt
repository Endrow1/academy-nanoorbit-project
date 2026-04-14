package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.screen.DashboardScreen
import com.example.myapplication.screen.DetailScreen
import com.example.myapplication.screen.PlanningScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.NanoOrbitViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val PLANNING = "planning"
    const val MAP = "map"
    const val DETAIL = "detail/{satelliteId}"

    fun detailRoute(id: Int) = "detail/$id"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val orbitViewModel: NanoOrbitViewModel = viewModel()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Routes.DETAIL

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.DASHBOARD || currentRoute?.startsWith("detail/") == true,
                        onClick = {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, null) }
                    )

                    NavigationBarItem(
                        selected = currentRoute == Routes.PLANNING,
                        onClick = {
                            navController.navigate(Routes.PLANNING) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.DateRange, null) }
                    )

                    NavigationBarItem(
                        selected = currentRoute == Routes.MAP,
                        onClick = {
                            navController.navigate(Routes.MAP) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.LocationOn, null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.EnterTransition.None },
            exitTransition = { androidx.compose.animation.ExitTransition.None },
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    viewModel = orbitViewModel,
                    onSatelliteClick = { id ->
                        navController.navigate(Routes.detailRoute(id))
                    }
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("satelliteId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("satelliteId") ?: 0
                DetailScreen(
                    satelliteId = id,
                    viewModel = orbitViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PLANNING) { PlanningScreen(viewModel = orbitViewModel) }
            composable(Routes.MAP) { Text("Écran Carte", Modifier.padding(innerPadding)) }
        }
    }
}