package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class CivoraNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : CivoraNavDestination(
        route = "home",
        label = "Dashboard",
        icon = Icons.Default.Dashboard
    )

    data object Timetable : CivoraNavDestination(
        route = "timetable",
        label = "Timetable",
        icon = Icons.Default.CalendarMonth
    )

    data object Tasks : CivoraNavDestination(
        route = "tasks",
        label = "Tasks",
        icon = Icons.Default.AssignmentLate
    )

    data object Settings : CivoraNavDestination(
        route = "settings",
        label = "Settings",
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Home, Timetable, Tasks, Settings)
    }
}
