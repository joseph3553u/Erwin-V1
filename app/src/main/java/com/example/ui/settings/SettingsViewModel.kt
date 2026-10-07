package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CivoraDatabase
import com.example.data.repository.TaskRepository
import com.example.data.repository.TimetableRepository
import com.example.widgets.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val timetableCount: Int = 0,
    val taskCount: Int = 0,
    val isRefreshingWidgets: Boolean = false,
    val statusMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CivoraDatabase.getDatabase(application)
    private val timetableRepo = TimetableRepository(db.classPeriodDao())
    private val taskRepo = TaskRepository(db.taskItemDao())

    private val _isRefreshing = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        timetableRepo.allClasses,
        taskRepo.allTasks,
        _isRefreshing,
        _statusMessage
    ) { classes, tasks, refreshing, msg ->
        SettingsUiState(
            timetableCount = classes.size,
            taskCount = tasks.size,
            isRefreshingWidgets = refreshing,
            statusMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun clearTimetable() {
        viewModelScope.launch(Dispatchers.IO) {
            timetableRepo.clearAll()
            WidgetUpdater.updateAllWidgets(getApplication())
            _statusMessage.value = "Timetable cleared successfully"
        }
    }

    fun clearTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.clearAll()
            WidgetUpdater.updateAllWidgets(getApplication())
            _statusMessage.value = "Tasks cleared successfully"
        }
    }

    fun forceRefreshWidgets() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            WidgetUpdater.updateAllWidgets(getApplication())
            _isRefreshing.value = false
            _statusMessage.value = "Home-screen widgets refreshed"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
