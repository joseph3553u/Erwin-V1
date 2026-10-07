package com.example.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CivoraDatabase
import com.example.data.entities.ClassPeriod
import com.example.data.entities.TaskItem
import com.example.data.repository.TaskRepository
import com.example.data.repository.TimetableRepository
import com.example.domain.ClassProgressCalculator
import com.example.domain.DeadlineCalculator
import com.example.domain.models.ClassState
import com.example.domain.models.TaskUrgency
import com.example.parser.PdfTextExtractor
import com.example.parser.SamplePdfGenerator
import com.example.parser.TaskParser
import com.example.parser.TimetableParser
import com.example.widgets.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed class PdfImportUiState {
    data object Idle : PdfImportUiState()
    data object Parsing : PdfImportUiState()
    data class TimetablePreview(val classes: List<ClassPeriod>, val fileName: String) : PdfImportUiState()
    data class TasksPreview(val tasks: List<TaskItem>, val fileName: String) : PdfImportUiState()
    data class Error(val message: String) : PdfImportUiState()
}

data class HomeUiState(
    val classState: ClassState = ClassState.NoTimetable,
    val topTasks: List<TaskUrgency> = emptyList(),
    val totalClassesCount: Int = 0,
    val totalPendingTasksCount: Int = 0,
    val pdfImportState: PdfImportUiState = PdfImportUiState.Idle
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CivoraDatabase.getDatabase(application)
    private val timetableRepo = TimetableRepository(db.classPeriodDao())
    private val taskRepo = TaskRepository(db.taskItemDao())

    private val _pdfImportState = MutableStateFlow<PdfImportUiState>(PdfImportUiState.Idle)
    val pdfImportState: StateFlow<PdfImportUiState> = _pdfImportState

    val uiState: StateFlow<HomeUiState> = combine(
        timetableRepo.allClasses,
        taskRepo.allTasks,
        _pdfImportState
    ) { classes, tasks, importState ->
        val classState = ClassProgressCalculator.calculateCurrentState(classes, LocalDateTime.now())
        val pendingTasks = tasks.filter { !it.isCompleted }
        val sortedUrgencies = DeadlineCalculator.sortTasksByUrgency(pendingTasks)
        val topThree = sortedUrgencies.take(3)

        HomeUiState(
            classState = classState,
            topTasks = topThree,
            totalClassesCount = classes.size,
            totalPendingTasksCount = pendingTasks.size,
            pdfImportState = importState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun importTimetablePdfUri(uri: Uri, fileName: String = "Timetable.pdf") {
        viewModelScope.launch(Dispatchers.IO) {
            _pdfImportState.value = PdfImportUiState.Parsing
            val extractResult = PdfTextExtractor.extractText(getApplication(), uri)
            extractResult.fold(
                onSuccess = { rawText ->
                    val detected = TimetableParser.parse(rawText)
                    _pdfImportState.value = PdfImportUiState.TimetablePreview(detected, fileName)
                },
                onFailure = { error ->
                    _pdfImportState.value = PdfImportUiState.Error(
                        error.message ?: "Failed to extract text from PDF."
                    )
                }
            )
        }
    }

    fun importTaskPdfUri(uri: Uri, fileName: String = "Tasks.pdf") {
        viewModelScope.launch(Dispatchers.IO) {
            _pdfImportState.value = PdfImportUiState.Parsing
            val extractResult = PdfTextExtractor.extractText(getApplication(), uri)
            extractResult.fold(
                onSuccess = { rawText ->
                    val detected = TaskParser.parse(rawText, fileName)
                    _pdfImportState.value = PdfImportUiState.TasksPreview(detected, fileName)
                },
                onFailure = { error ->
                    _pdfImportState.value = PdfImportUiState.Error(
                        error.message ?: "Failed to extract text from PDF."
                    )
                }
            )
        }
    }

    fun confirmSaveTimetable(classes: List<ClassPeriod>) {
        viewModelScope.launch(Dispatchers.IO) {
            timetableRepo.clearAll()
            timetableRepo.insertAll(classes)
            _pdfImportState.value = PdfImportUiState.Idle
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun confirmSaveTasks(tasks: List<TaskItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.insertAll(tasks)
            _pdfImportState.value = PdfImportUiState.Idle
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun dismissImportPreview() {
        _pdfImportState.value = PdfImportUiState.Idle
    }

    fun toggleTaskCompletion(task: TaskItem, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.updateCompletionStatus(task.id, isCompleted)
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun loadSampleTimetable() {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = SamplePdfGenerator.createSampleTimetablePdf(getApplication())
            importTimetablePdfUri(uri, "Sample_College_Timetable.pdf")
        }
    }

    fun loadSampleTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = SamplePdfGenerator.createSampleTaskPdf(getApplication())
            importTaskPdfUri(uri, "Sample_Semester_Tasks.pdf")
        }
    }
}
