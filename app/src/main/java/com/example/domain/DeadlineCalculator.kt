package com.example.domain

import com.example.data.entities.TaskItem
import com.example.domain.models.TaskUrgency
import com.example.domain.models.UrgencyLevel
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object DeadlineCalculator {

    fun calculateUrgency(
        task: TaskItem,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): TaskUrgency {
        val diffMillis = task.deadlineEpochMillis - nowEpochMillis

        val urgencyLevel = when {
            diffMillis < 0 -> UrgencyLevel.OVERDUE
            diffMillis < TimeUnit.HOURS.toMillis(24) -> UrgencyLevel.CRITICAL
            diffMillis <= TimeUnit.HOURS.toMillis(72) -> UrgencyLevel.HIGH
            diffMillis <= TimeUnit.DAYS.toMillis(7) -> UrgencyLevel.MEDIUM
            else -> UrgencyLevel.LOW
        }

        val remainingFormatted = formatRemainingTime(diffMillis)
        val urgencyProgress = calculateUrgencyProgress(diffMillis)

        return TaskUrgency(
            task = task,
            urgencyLevel = urgencyLevel,
            remainingTimeFormatted = remainingFormatted,
            urgencyProgress = urgencyProgress
        )
    }

    private fun formatRemainingTime(diffMillis: Long): String {
        if (diffMillis < 0) {
            val overdueMs = abs(diffMillis)
            val overdueMinutes = TimeUnit.MILLISECONDS.toMinutes(overdueMs)
            val overdueHours = TimeUnit.MILLISECONDS.toHours(overdueMs)
            val overdueDays = TimeUnit.MILLISECONDS.toDays(overdueMs)

            return when {
                overdueDays > 0 -> "Overdue by ${overdueDays}d"
                overdueHours > 0 -> "Overdue by ${overdueHours}h"
                overdueMinutes > 0 -> "Overdue by ${overdueMinutes}m"
                else -> "Just overdue"
            }
        }

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            days >= 2 -> "$days days left"
            days == 1L -> "1d ${hours % 24}h left"
            hours > 0 -> "${hours}h ${minutes % 60}m left"
            minutes > 0 -> "${minutes}m left"
            else -> "Due now"
        }
    }

    private fun calculateUrgencyProgress(diffMillis: Long): Float {
        if (diffMillis <= 0) return 1.0f

        // Map diffMillis (from 14 days down to 0) to an urgency progress (0.1 to 1.0)
        val maxWindowMs = TimeUnit.DAYS.toMillis(14).toFloat()
        val remainingClamped = diffMillis.toFloat().coerceIn(0f, maxWindowMs)
        // 0 days left -> 1.0f urgency, 14 days left -> 0.1f urgency
        val inverseRatio = 1.0f - (remainingClamped / maxWindowMs)
        return (0.1f + (inverseRatio * 0.9f)).coerceIn(0.1f, 1.0f)
    }

    fun sortTasksByUrgency(
        tasks: List<TaskItem>,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): List<TaskUrgency> {
        return tasks.map { calculateUrgency(it, nowEpochMillis) }
            .sortedWith(
                compareBy<TaskUrgency> { it.task.isCompleted }
                    .thenBy {
                        when (it.urgencyLevel) {
                            UrgencyLevel.CRITICAL -> 0
                            UrgencyLevel.HIGH -> 1
                            UrgencyLevel.MEDIUM -> 2
                            UrgencyLevel.LOW -> 3
                            UrgencyLevel.OVERDUE -> 4 // Put overdue after critical or pending, but before completed
                        }
                    }
                    .thenBy { it.task.deadlineEpochMillis }
            )
    }
}
