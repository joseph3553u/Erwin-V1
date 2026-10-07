package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String? = null,
    val description: String? = null,
    val deadlineEpochMillis: Long,
    val deadlineDate: String, // "YYYY-MM-DD"
    val deadlineTime: String = "23:59", // "HH:mm"
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    val isCompleted: Boolean = false,
    val sourcePdf: String? = null
) {
    val deadlineLocalDateTime: LocalDateTime
        get() = try {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(deadlineEpochMillis),
                ZoneId.systemDefault()
            )
        } catch (e: Exception) {
            LocalDateTime.now()
        }

    fun formattedDeadline(): String {
        return try {
            val d = deadlineLocalDateTime
            val now = LocalDateTime.now()
            val dateFmt = DateTimeFormatter.ofPattern("MMM d")
            val timeFmt = DateTimeFormatter.ofPattern("h:mm a")

            if (d.toLocalDate() == now.toLocalDate()) {
                "Today, ${d.format(timeFmt)}"
            } else if (d.toLocalDate() == now.toLocalDate().plusDays(1)) {
                "Tomorrow, ${d.format(timeFmt)}"
            } else {
                "${d.format(dateFmt)}, ${d.format(timeFmt)}"
            }
        } catch (e: Exception) {
            deadlineDate
        }
    }
}
