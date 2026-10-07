package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "class_periods")
data class ClassPeriod(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1 = Monday, ..., 7 = Sunday (corresponds to DayOfWeek.value)
    val startTime: String, // "09:00" (HH:mm)
    val endTime: String, // "10:00" (HH:mm)
    val subject: String,
    val room: String? = null,
    val teacher: String? = null,
    val notes: String? = null
) {
    val dayOfWeekEnum: DayOfWeek
        get() = DayOfWeek.of(dayOfWeek.coerceIn(1, 7))

    val parsedStartTime: LocalTime
        get() = try {
            LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.of(9, 0)
        }

    val parsedEndTime: LocalTime
        get() = try {
            LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            parsedStartTime.plusHours(1)
        }

    fun formattedTimeRange(): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return "${parsedStartTime.format(formatter)} – ${parsedEndTime.format(formatter)}"
    }
}
