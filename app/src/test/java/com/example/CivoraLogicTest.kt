package com.example

import com.example.data.entities.ClassPeriod
import com.example.data.entities.TaskItem
import com.example.domain.ClassProgressCalculator
import com.example.domain.DeadlineCalculator
import com.example.domain.models.ClassState
import com.example.domain.models.UrgencyLevel
import com.example.parser.TaskParser
import com.example.parser.TimetableParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class CivoraLogicTest {

    @Test
    fun testClassProgressCalculations() {
        val classPeriod = ClassPeriod(
            id = 1,
            dayOfWeek = DayOfWeek.MONDAY.value,
            startTime = "10:00",
            endTime = "11:00",
            subject = "Data Structures",
            room = "CSE-204"
        )
        val allClasses = listOf(classPeriod)

        // Case 1: Time is 10:30 (50% progress, 30 min remaining)
        val time1030 = LocalDateTime.of(LocalDate.of(2026, 10, 12), LocalTime.of(10, 30)) // Monday
        val state1 = ClassProgressCalculator.calculateCurrentState(allClasses, time1030)
        assertTrue(state1 is ClassState.InClass)
        val inClass1 = state1 as ClassState.InClass
        assertEquals(50, inClass1.progressPercent)
        assertEquals(30, inClass1.remainingMinutes)

        // Case 2: Time is 10:59 (~98% progress, 1 min remaining)
        val time1059 = LocalDateTime.of(LocalDate.of(2026, 10, 12), LocalTime.of(10, 59))
        val state2 = ClassProgressCalculator.calculateCurrentState(allClasses, time1059)
        assertTrue(state2 is ClassState.InClass)
        val inClass2 = state2 as ClassState.InClass
        assertEquals(98, inClass2.progressPercent)
        assertEquals(1, inClass2.remainingMinutes)

        // Case 3: Time is 11:01 (class is finished)
        val time1101 = LocalDateTime.of(LocalDate.of(2026, 10, 12), LocalTime.of(11, 1))
        val state3 = ClassProgressCalculator.calculateCurrentState(allClasses, time1101)
        assertTrue(state3 is ClassState.NoMoreClassesToday)
    }

    @Test
    fun testNextClassDetection() {
        val morningClass = ClassPeriod(
            id = 1,
            dayOfWeek = DayOfWeek.MONDAY.value,
            startTime = "09:00",
            endTime = "10:00",
            subject = "Algorithms"
        )
        val afternoonClass = ClassPeriod(
            id = 2,
            dayOfWeek = DayOfWeek.MONDAY.value,
            startTime = "11:30",
            endTime = "12:30",
            subject = "Operating Systems"
        )
        val allClasses = listOf(morningClass, afternoonClass)

        // Current time is 10:30 (between classes)
        val time1030 = LocalDateTime.of(LocalDate.of(2026, 10, 12), LocalTime.of(10, 30))
        val state = ClassProgressCalculator.calculateCurrentState(allClasses, time1030)
        assertTrue(state is ClassState.NextClassToday)
        val nextClass = state as ClassState.NextClassToday
        assertEquals("Operating Systems", nextClass.classPeriod.subject)
        assertEquals(60, nextClass.startsInMinutes)
    }

    @Test
    fun testTaskUrgencyAndOverdueCalculations() {
        val nowMillis = 1000000000L

        // Overdue task (deadline 1 hour ago)
        val overdueTask = TaskItem(
            id = 1,
            title = "History Essay",
            deadlineEpochMillis = nowMillis - TimeUnit.HOURS.toMillis(1),
            deadlineDate = "2026-10-06"
        )
        val overdueResult = DeadlineCalculator.calculateUrgency(overdueTask, nowMillis)
        assertEquals(UrgencyLevel.OVERDUE, overdueResult.urgencyLevel)
        assertTrue(overdueResult.remainingTimeFormatted.contains("Overdue"))

        // Critical task (due in 5 hours)
        val criticalTask = TaskItem(
            id = 2,
            title = "Physics Lab Record",
            deadlineEpochMillis = nowMillis + TimeUnit.HOURS.toMillis(5),
            deadlineDate = "2026-10-06"
        )
        val criticalResult = DeadlineCalculator.calculateUrgency(criticalTask, nowMillis)
        assertEquals(UrgencyLevel.CRITICAL, criticalResult.urgencyLevel)

        // High urgency task (due in 48 hours)
        val highTask = TaskItem(
            id = 3,
            title = "DBMS Assignment 2",
            deadlineEpochMillis = nowMillis + TimeUnit.HOURS.toMillis(48),
            deadlineDate = "2026-10-08"
        )
        val highResult = DeadlineCalculator.calculateUrgency(highTask, nowMillis)
        assertEquals(UrgencyLevel.HIGH, highResult.urgencyLevel)

        // Low urgency task (due in 10 days)
        val lowTask = TaskItem(
            id = 4,
            title = "Math Final Project",
            deadlineEpochMillis = nowMillis + TimeUnit.DAYS.toMillis(10),
            deadlineDate = "2026-10-16"
        )
        val lowResult = DeadlineCalculator.calculateUrgency(lowTask, nowMillis)
        assertEquals(UrgencyLevel.LOW, lowResult.urgencyLevel)
    }

    @Test
    fun testTimetableParserText() {
        val text = """
            MONDAY
            09:00 - 10:00 Mathematics | Room CSE-204 | Prof. Smith
            10:00 - 11:00 Operating Systems | Room CSE-201 | Dr. Rao
        """.trimIndent()

        val parsed = TimetableParser.parse(text)
        assertEquals(2, parsed.size)
        assertEquals("Mathematics", parsed[0].subject)
        assertEquals("09:00", parsed[0].startTime)
        assertEquals("10:00", parsed[0].endTime)
        assertEquals("CSE-204", parsed[0].room)
        assertEquals("Prof. Smith", parsed[0].teacher)
    }

    @Test
    fun testTaskParserText() {
        val text = """
            DBMS Assignment 2 - Submit by 12 October 2026 5:00 PM
            Physics Lab Record - Due Date: 10/10/2026
        """.trimIndent()

        val parsed = TaskParser.parse(text)
        assertEquals(2, parsed.size)
        assertTrue(parsed[0].title.contains("DBMS Assignment 2"))
        assertEquals("17:00", parsed[0].deadlineTime)
        assertTrue(parsed[1].title.contains("Physics Lab Record"))
    }
}
