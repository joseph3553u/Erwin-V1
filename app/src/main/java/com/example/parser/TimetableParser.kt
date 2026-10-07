package com.example.parser

import com.example.data.entities.ClassPeriod
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimetableParser {

    private val DAY_MAP = mapOf(
        "MONDAY" to DayOfWeek.MONDAY,
        "MON" to DayOfWeek.MONDAY,
        "TUESDAY" to DayOfWeek.TUESDAY,
        "TUE" to DayOfWeek.TUESDAY,
        "WEDNESDAY" to DayOfWeek.WEDNESDAY,
        "WED" to DayOfWeek.WEDNESDAY,
        "THURSDAY" to DayOfWeek.THURSDAY,
        "THU" to DayOfWeek.THURSDAY,
        "THURS" to DayOfWeek.THURSDAY,
        "FRIDAY" to DayOfWeek.FRIDAY,
        "FRI" to DayOfWeek.FRIDAY,
        "SATURDAY" to DayOfWeek.SATURDAY,
        "SAT" to DayOfWeek.SATURDAY,
        "SUNDAY" to DayOfWeek.SUNDAY,
        "SUN" to DayOfWeek.SUNDAY
    )

    // Regex for time ranges: e.g. 09:00 - 10:00, 9:00 AM - 10:00 AM, 9:00 to 10:00, 9.30 - 10.30
    private val TIME_RANGE_REGEX = Regex(
        """(\d{1,2}[:.]\d{2}(?:\s*(?:AM|PM|am|pm))?)\s*(?:[-–—toTO]|until)\s*(\d{1,2}[:.]\d{2}(?:\s*(?:AM|PM|am|pm))?)""",
        RegexOption.IGNORE_CASE
    )

    // Single time pattern if only start time is found
    private val SINGLE_TIME_REGEX = Regex(
        """\b(\d{1,2}[:.]\d{2}(?:\s*(?:AM|PM|am|pm))?)\b""",
        RegexOption.IGNORE_CASE
    )

    // Room detection pattern
    private val ROOM_REGEX = Regex(
        """(?:Room|Hall|Lab|LH|Room No|Rm\.?)[.:\s]+([A-Za-z0-9-]+)|\b([A-Z]{2,4}[-\s]?[0-9]{2,4})\b|\b(Lab\s*[0-9A-Z]+)\b""",
        RegexOption.IGNORE_CASE
    )

    // Faculty detection pattern
    private val TEACHER_REGEX = Regex(
        """(?:Prof\.?|Dr\.?|Faculty[:\s]+|Instructor[:\s]+|Teacher[:\s]+|Mr\.?|Ms\.?)\s+([A-Za-z\s.]{3,30})""",
        RegexOption.IGNORE_CASE
    )

    fun parse(rawText: String): List<ClassPeriod> {
        val results = mutableListOf<ClassPeriod>()
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        var currentDay: DayOfWeek = DayOfWeek.MONDAY

        for (line in lines) {
            // Check if line represents a Day header or begins with a day
            val detectedDay = detectDayInLine(line)
            if (detectedDay != null) {
                currentDay = detectedDay
            }

            // Look for time range in the line
            val timeMatch = TIME_RANGE_REGEX.find(line)
            if (timeMatch != null) {
                val startRaw = timeMatch.groupValues[1]
                val endRaw = timeMatch.groupValues[2]
                val startTime = normalizeTime(startRaw, defaultHour = 9)
                val endTime = normalizeTime(endRaw, defaultHour = 10)

                // Clean the line without the time range and day name
                var cleaned = line.replace(timeMatch.value, " ")
                for (dayKey in DAY_MAP.keys) {
                    cleaned = cleaned.replace(Regex("""\b$dayKey\b""", RegexOption.IGNORE_CASE), " ")
                }
                cleaned = cleaned.replace("|", " ").replace(";", " ")

                // Extract Room
                var room: String? = null
                val roomMatch = ROOM_REGEX.find(cleaned)
                if (roomMatch != null) {
                    val rawRoom = roomMatch.value.trim().trim('(', ')', '[', ']', ',')
                    room = rawRoom.replace(Regex("""^(?:Room|Rm\.?|Hall|LH)[:\s]+""", RegexOption.IGNORE_CASE), "").trim().ifBlank { rawRoom }
                    cleaned = cleaned.replace(roomMatch.value, " ")
                }

                // Extract Teacher
                var teacher: String? = null
                val teacherMatch = TEACHER_REGEX.find(cleaned)
                if (teacherMatch != null) {
                    teacher = teacherMatch.value.trim().trim('(', ')', '[', ']', ',')
                    cleaned = cleaned.replace(teacherMatch.value, " ")
                }

                // Clean Subject name
                val subject = cleaned
                    .replace(Regex("""\s+"""), " ")
                    .trim(' ', '-', ':', '–', '—', ',', '(', ')')
                    .ifBlank { "Lecture" }

                results.add(
                    ClassPeriod(
                        dayOfWeek = currentDay.value,
                        startTime = startTime,
                        endTime = endTime,
                        subject = subject,
                        room = room,
                        teacher = teacher
                    )
                )
            } else {
                // If line has single time followed by subject: "09:00 Mathematics"
                val singleMatch = SINGLE_TIME_REGEX.find(line)
                if (singleMatch != null && !line.startsWith("Page", ignoreCase = true)) {
                    val startRaw = singleMatch.groupValues[1]
                    val startTime = normalizeTime(startRaw, defaultHour = 9)
                    val endTime = addHourToTime(startTime)

                    var cleaned = line.replace(singleMatch.value, " ")
                    for (dayKey in DAY_MAP.keys) {
                        cleaned = cleaned.replace(Regex("""\b$dayKey\b""", RegexOption.IGNORE_CASE), " ")
                    }
                    val subject = cleaned.replace(Regex("""\s+"""), " ").trim(' ', '-', ':', '|')
                    if (subject.length > 2 && !subject.matches(Regex("""[0-9/.-]+"""))) {
                        results.add(
                            ClassPeriod(
                                dayOfWeek = currentDay.value,
                                startTime = startTime,
                                endTime = endTime,
                                subject = subject.ifBlank { "Class Period" },
                                room = null,
                                teacher = null
                            )
                        )
                    }
                }
            }
        }

        return results
    }

    private fun detectDayInLine(line: String): DayOfWeek? {
        val uppercase = line.uppercase(Locale.getDefault())
        for ((key, day) in DAY_MAP) {
            // Check for exact word boundary
            if (Regex("""\b$key\b""").containsMatchIn(uppercase)) {
                return day
            }
        }
        return null
    }

    fun normalizeTime(raw: String, defaultHour: Int): String {
        val cleaned = raw.trim().replace(".", ":").uppercase(Locale.getDefault())
        return try {
            val isPm = cleaned.contains("PM")
            val isAm = cleaned.contains("AM")
            val digitsOnly = cleaned.replace("AM", "").replace("PM", "").trim()
            val parts = digitsOnly.split(":")
            var hour = parts[0].toIntOrNull() ?: defaultHour
            val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0

            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0

            String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        } catch (e: Exception) {
            String.format(Locale.US, "%02d:00", defaultHour)
        }
    }

    private fun addHourToTime(time: String): String {
        return try {
            val parts = time.split(":")
            val hour = (parts[0].toInt() + 1) % 24
            val minute = parts[1].toInt()
            String.format(Locale.US, "%02d:%02d", hour, minute)
        } catch (e: Exception) {
            "10:00"
        }
    }
}
