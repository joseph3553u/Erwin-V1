package com.example.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CivoraDatabase
import com.example.data.entities.TaskItem
import com.example.data.repository.TaskRepository
import com.example.domain.DeadlineCalculator
import com.example.domain.models.TaskUrgency
import com.example.domain.models.UrgencyLevel
import com.example.widgets.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val title: String) {
    PENDING("Active"),
    ALL("All"),
    OVERDUE("Overdue"),
    COMPLETED("Completed")
}

data class TasksUiState(
    val selectedFilter: TaskFilter = TaskFilter.PENDING,
    val tasks: List<TaskUrgency> = emptyList(),
    val activeCount: Int = 0,
    val overdueCount: Int = 0,
    val completedCount: Int = 0
)

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CivoraDatabase.getDatabase(application)
    private val repository = TaskRepository(db.taskItemDao())

    private val _selectedFilter = MutableStateFlow(TaskFilter.PENDING)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter

    val uiState: StateFlow<TasksUiState> = combine(
        repository.allTasks,
        _selectedFilter
    ) { allTasks, filter ->
        val urgencies = DeadlineCalculator.sortTasksByUrgency(allTasks)

        val active = urgencies.filter { !it.task.isCompleted }
        val overdue = urgencies.filter { !it.task.isCompleted && it.urgencyLevel == UrgencyLevel.OVERDUE }
        val completed = urgencies.filter { it.task.isCompleted }

        val displayed = when (filter) {
            TaskFilter.PENDING -> active
            TaskFilter.ALL -> urgencies
            TaskFilter.OVERDUE -> overdue
            TaskFilter.COMPLETED -> completed
        }

        TasksUiState(
            selectedFilter = filter,
            tasks = displayed,
            activeCount = active.size,
            overdueCount = overdue.size,
            completedCount = completed.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState()
    )

    fun selectFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun toggleTaskCompletion(task: TaskItem, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCompletionStatus(task.id, isCompleted)
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun addOrUpdateTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            if (task.id == 0L) {
                repository.insert(task)
            } else {
                repository.update(task)
            }
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(task)
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }
}
