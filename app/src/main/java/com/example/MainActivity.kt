package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.navigation.CivoraNavDestination
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.tasks.TasksScreen
import com.example.ui.tasks.TasksViewModel
import com.example.ui.theme.CivoraTheme
import com.example.ui.timetable.TimetableScreen
import com.example.ui.timetable.TimetableViewModel
import com.example.widgets.WidgetUpdater

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val timetableViewModel: TimetableViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CivoraTheme {
                CivoraMainApp(
                    homeViewModel = homeViewModel,
                    timetableViewModel = timetableViewModel,
                    tasksViewModel = tasksViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WidgetUpdater.triggerImmediateUpdate(this)
    }
}

@Composable
fun CivoraMainApp(
    homeViewModel: HomeViewModel,
    timetableViewModel: TimetableViewModel,
    tasksViewModel: TasksViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CivoraNavDestination.items.forEach { destination ->
                    val isSelected = currentRoute == destination.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        modifier = Modifier.testTag("nav_${destination.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CivoraNavDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CivoraNavDestination.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToTimetable = {
                        navController.navigate(CivoraNavDestination.Timetable.route)
                    },
                    onNavigateToTasks = {
                        navController.navigate(CivoraNavDestination.Tasks.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(CivoraNavDestination.Settings.route)
                    }
                )
            }

            composable(CivoraNavDestination.Timetable.route) {
                TimetableScreen(viewModel = timetableViewModel)
            }

            composable(CivoraNavDestination.Tasks.route) {
                TasksScreen(viewModel = tasksViewModel)
            }

            composable(CivoraNavDestination.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onImportTimetableUri = { uri ->
                        homeViewModel.importTimetablePdfUri(uri, "Timetable.pdf")
                        navController.navigate(CivoraNavDestination.Home.route)
                    },
                    onImportTasksUri = { uri ->
                        homeViewModel.importTaskPdfUri(uri, "Tasks.pdf")
                        navController.navigate(CivoraNavDestination.Home.route)
                    }
                )
            }
        }
    }
}
