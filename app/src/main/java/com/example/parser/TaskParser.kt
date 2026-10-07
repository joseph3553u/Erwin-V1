package com.example.parser

import com.example.data.entities.TaskItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

object TaskParser {

    private val KEYWORDS = listOf(
        "assignment",
        "submission",
        "due date",
        "deadline",
        "last date",
        "submit by",
        "project",
        "lab record",
        "record submission",
        "homework",
        "exam",
        "quiz",
        "report",
        "presentation",
        "viva",
        "assessment"
    )

    // Match full date representations:
    // e.g. "12 October 2026", "12 Oct 2026", "October 12, 2026", "10/10/2026", "12-10-2026", "12/10/26"
    private val DATE_REGEX = Regex(
        """\b(\d{1,2}(?:st|nd|rd|th)?[\s/-]+(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)[\s/,-]+(?:\d{4}|\d{2}))\b|\b((?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)[\s/-]+\d{1,2}(?:st|nd|rd|th)?[\s/,-]+(?:\d{4}|\d{2}))\b|\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""",
        RegexOption.IGNORE_CASE
    )

    private val TIME_REGEX = Regex(
        """\b(\d{1,2}[:.]\d{2}\s*(?:AM|PM|am|pm)?)\b"""
    )

    fun parse(rawText: String, sourcePdfName: String? = null): List<TaskItem> {
        val results = mutableListOf<TaskItem>()
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (line in lines) {
            val lower = line.lowercase(Locale.getDefault())
            val hasKeyword = KEYWORDS.any { lower.contains(it) }
            val dateMatch = DATE_REGEX.find(line)

            if (hasKeyword || dateMatch != null) {
                val parsedDateResult = if (dateMatch != null) {
                    parseDateString(dateMatch.value)
                } else {
                    // If no explicit date was in line, default to 3 days in the future
                    LocalDate.now().plusDays(3)
                }

                // Check for explicit time in line
                var deadlineTimeStr = "23:59"
                val timeMatch = TIME_REGEX.find(line)
                if (timeMatch != null && timeMatch.value != dateMatch?.value) {
                    val candidate = TimetableParser.normalizeTime(timeMatch.value, 23)
                    if (candidate != "00:00" || line.contains("12:00 AM", ignoreCase = true)) {
                        deadlineTimeStr = candidate
                    }
                }

                val timeParts = deadlineTimeStr.split(":")
                val deadlineTime = LocalTime.of(
                    timeParts[0].toIntOrNull() ?: 23,
                    timeParts.getOrNull(1)?.toIntOrNull() ?: 59
                )

                val deadlineDateTime = LocalDateTime.of(parsedDateResult, deadlineTime)
                val deadlineEpochMillis = deadlineDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // Clean the title
                var title = line
                if (dateMatch != null) {
                    title = title.replace(dateMatch.value, " ")
                }
                if (timeMatch != null) {
                    title = title.replace(timeMatch.value, " ")
                }
                // Strip common filler prefixes
                title = title
                    .replace(Regex("""(?i)\b(submit by|due date|last date|deadline)[:\s-]*"""), "")
                    .replace(Regex("""\s+"""), " ")
                    .trim(' ', '-', ':', '|', ';', ',', '(', ')')

                if (title.isBlank()) {
                    title = "Academic Assignment"
                }

                // Determine priority
                val priority = when {
                    lower.contains("urgent") || lower.contains("exam") || lower.contains("final") -> "CRITICAL"
                    lower.contains("important") || lower.contains("project") -> "HIGH"
                    lower.contains("optional") || lower.contains("reading") -> "LOW"
                    else -> "MEDIUM"
                }

                // Extract possible subject prefix (e.g. "DBMS Assignment 2" -> subject = "DBMS")
                val subject = extractSubject(title)

                results.add(
                    TaskItem(
                        title = title,
                        subject = subject,
                        description = if (line != title) line else null,
                        deadlineEpochMillis = deadlineEpochMillis,
                        deadlineDate = parsedDateResult.toString(),
                        deadlineTime = deadlineTimeStr,
                        priority = priority,
                        isCompleted = false,
                        sourcePdf = sourcePdfName
                    )
                )
            }
        }

        return results
    }

    private fun extractSubject(title: String): String? {
        val parts = title.split(" ", "-", ":")
        if (parts.isNotEmpty()) {
            val first = parts[0].trim()
            if (first.length in 2..8 && first.all { it.isLetterOrDigit() }) {
                return first
            }
        }
        return null
    }

    fun parseDateString(raw: String): LocalDate {
        val cleaned = raw.trim()
            .replace(Regex("""(\d+)(?:st|nd|rd|th)"""), "$1") // 12th -> 12
            .replace(",", " ")
            .replace(Regex("""\s+"""), " ")

        val formats = listOf(
            "d MMMM yyyy",
            "d MMM yyyy",
            "MMMM d yyyy",
            "MMM d yyyy",
            "d/M/yyyy",
            "d-M-yyyy",
            "d/M/yy",
            "d-M-yy",
            "yyyy-MM-dd"
        )

        for (pattern in formats) {
            try {
                val formatter = DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern(pattern)
                    .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
                    .toFormatter(Locale.ENGLISH)
                return LocalDate.parse(cleaned, formatter)
            } catch (ignored: Exception) {
            }
        }

        // Indian / Commonwealth standard fallback for day/month/year numeric:
        val numMatch = Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})""").find(cleaned)
        if (numMatch != null) {
            val d = numMatch.groupValues[1].toInt()
            val m = numMatch.groupValues[2].toInt()
            var y = numMatch.groupValues[3].toInt()
            if (y < 100) y += 2000
            if (m in 1..12 && d in 1..31) {
                return LocalDate.of(y, m, d.coerceIn(1, 28))
            }
        }

        return LocalDate.now().plusDays(2)
    }
}
