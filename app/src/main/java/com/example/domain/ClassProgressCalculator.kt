package com.example.domain

import com.example.data.entities.ClassPeriod
import com.example.domain.models.ClassState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

object ClassProgressCalculator {

    fun calculateCurrentState(
        allClasses: List<ClassPeriod>,
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): ClassState {
        if (allClasses.isEmpty()) {
            return ClassState.NoTimetable
        }

        val currentDayValue = currentDateTime.dayOfWeek.value
        val currentTime = currentDateTime.toLocalTime()
        val todayClasses = allClasses.filter { it.dayOfWeek == currentDayValue }

        // 1. Check if user is currently inside a class
        val runningClass = todayClasses.find { classPeriod ->
            val start = classPeriod.parsedStartTime
            val end = classPeriod.parsedEndTime
            !currentTime.isBefore(start) && currentTime.isBefore(end)
        }

        if (runningClass != null) {
            val startSeconds = runningClass.parsedStartTime.toSecondOfDay()
            val endSeconds = runningClass.parsedEndTime.toSecondOfDay()
            val currentSeconds = currentTime.toSecondOfDay()
            val totalSeconds = (endSeconds - startSeconds).coerceAtLeast(1)
            val elapsedSeconds = (currentSeconds - startSeconds).coerceAtLeast(0)

            val fraction = (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
            val percent = (fraction * 100f).roundToInt()
            val remainingMinutes = ((endSeconds - currentSeconds) / 60).coerceAtLeast(0)

            return ClassState.InClass(
                classPeriod = runningClass,
                progressPercent = percent,
                remainingMinutes = remainingMinutes
            )
        }

        // 2. Check if a future class exists today
        val nextClassToday = todayClasses
            .filter { it.parsedStartTime.isAfter(currentTime) }
            .minByOrNull { it.parsedStartTime }

        if (nextClassToday != null) {
            val startSeconds = nextClassToday.parsedStartTime.toSecondOfDay()
            val currentSeconds = currentTime.toSecondOfDay()
            val diffSeconds = (startSeconds - currentSeconds).coerceAtLeast(0)
            val startsInMinutes = diffSeconds / 60

            return ClassState.NextClassToday(
                classPeriod = nextClassToday,
                startsInMinutes = startsInMinutes
            )
        }

        // 3. No more classes today: search future days in the upcoming week
        val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
        for (dayOffset in 1..7) {
            val targetDayValue = ((currentDayValue - 1 + dayOffset) % 7) + 1
            val targetClasses = allClasses
                .filter { it.dayOfWeek == targetDayValue }
                .sortedBy { it.parsedStartTime }

            if (targetClasses.isNotEmpty()) {
                val nextClass = targetClasses.first()
                val targetDayOfWeek = DayOfWeek.of(targetDayValue)
                val dayDesc = if (dayOffset == 1) {
                    "Tomorrow, ${nextClass.parsedStartTime.format(timeFmt)}"
                } else {
                    "${targetDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, ${nextClass.parsedStartTime.format(timeFmt)}"
                }

                return ClassState.NoMoreClassesToday(
                    nextClass = nextClass,
                    nextClassDescription = dayDesc
                )
            }
        }

        return ClassState.NoMoreClassesToday(
            nextClass = null,
            nextClassDescription = "No upcoming classes found"
        )
    }

    fun calculateProgressFraction(
        classPeriod: ClassPeriod,
        currentTime: LocalTime = LocalTime.now()
    ): Float {
        val startSeconds = classPeriod.parsedStartTime.toSecondOfDay()
        val endSeconds = classPeriod.parsedEndTime.toSecondOfDay()
        val currentSeconds = currentTime.toSecondOfDay()
        val totalSeconds = (endSeconds - startSeconds).coerceAtLeast(1)
        val elapsedSeconds = (currentSeconds - startSeconds).coerceAtLeast(0)

        return (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    }
}
