package com.example.ui.timetable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CivoraDatabase
import com.example.data.entities.ClassPeriod
import com.example.data.repository.TimetableRepository
import com.example.domain.ClassProgressCalculator
import com.example.widgets.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class TimetableUiState(
    val selectedDayOfWeek: Int = LocalDate.now().dayOfWeek.value,
    val classesForSelectedDay: List<ClassPeriod> = emptyList(),
    val currentRunningClassId: Long? = null,
    val currentProgressFraction: Float = 0f,
    val currentRemainingMinutes: Int = 0
)

class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CivoraDatabase.getDatabase(application)
    private val repository = TimetableRepository(db.classPeriodDao())

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)
    val selectedDay: StateFlow<Int> = _selectedDay

    val uiState: StateFlow<TimetableUiState> = combine(
        repository.allClasses,
        _selectedDay
    ) { allClasses, day ->
        val forDay = allClasses
            .filter { it.dayOfWeek == day }
            .sortedBy { it.parsedStartTime }

        val nowDay = LocalDate.now().dayOfWeek.value
        val nowTime = LocalTime.now()
        var runningId: Long? = null
        var fraction = 0f
        var remMin = 0

        if (day == nowDay) {
            val running = forDay.find {
                !nowTime.isBefore(it.parsedStartTime) && nowTime.isBefore(it.parsedEndTime)
            }
            if (running != null) {
                runningId = running.id
                fraction = ClassProgressCalculator.calculateProgressFraction(running, nowTime)
                val endSec = running.parsedEndTime.toSecondOfDay()
                val curSec = nowTime.toSecondOfDay()
                remMin = ((endSec - curSec) / 60).coerceAtLeast(0)
            }
        }

        TimetableUiState(
            selectedDayOfWeek = day,
            classesForSelectedDay = forDay,
            currentRunningClassId = runningId,
            currentProgressFraction = fraction,
            currentRemainingMinutes = remMin
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimetableUiState()
    )

    fun selectDay(dayOfWeek: Int) {
        _selectedDay.value = dayOfWeek.coerceIn(1, 7)
    }

    fun addOrUpdateClass(classPeriod: ClassPeriod) {
        viewModelScope.launch(Dispatchers.IO) {
            if (classPeriod.id == 0L) {
                repository.insert(classPeriod)
            } else {
                repository.update(classPeriod)
            }
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun deleteClass(classPeriod: ClassPeriod) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(classPeriod)
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }
}
