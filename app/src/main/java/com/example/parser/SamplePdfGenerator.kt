package com.example.parser

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SamplePdfGenerator {

    fun createSampleTimetablePdf(context: Context): Uri {
        val document = PDDocument()
        val page = PDPage()
        document.addPage(page)

        val contentStream = PDPageContentStream(document, page)
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16f)
        contentStream.newLineAtOffset(50f, 750f)
        contentStream.showText("COLLEGE OF ENGINEERING & TECHNOLOGY")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.showText("DEPARTMENT OF COMPUTER SCIENCE - WEEKLY TIMETABLE")

        contentStream.newLineAtOffset(0f, -30f)
        contentStream.setFont(PDType1Font.HELVETICA, 10f)

        val scheduleLines = listOf(
            "MONDAY",
            "09:00 - 10:00 Data Structures | Room CSE-204 | Prof. Roy",
            "10:00 - 11:00 Operating Systems | Room CSE-201 | Dr. Anita",
            "11:15 - 12:15 Computer Networks | Room CSE-204 | Prof. Smith",
            "14:00 - 16:00 Advanced Algorithms Lab | Lab CSE-3 | Instructor John",
            "",
            "TUESDAY",
            "09:00 - 10:00 Database Management Systems | Room CSE-102 | Dr. Anita",
            "10:00 - 11:00 Software Engineering | Room CSE-204 | Prof. David",
            "11:15 - 12:15 Discrete Mathematics | Room Math-301 | Prof. Roy",
            "14:00 - 15:00 Web Technologies | Lab CSE-2 | Dr. Meera",
            "",
            "WEDNESDAY",
            "09:00 - 10:00 Computer Architecture | Room CSE-101 | Prof. Smith",
            "10:00 - 11:00 Data Structures | Room CSE-204 | Prof. Roy",
            "11:15 - 12:15 Cloud Computing | Room CSE-202 | Dr. Anita",
            "14:00 - 16:00 DBMS Lab Record Session | Lab CSE-1 | Dr. Anita",
            "",
            "THURSDAY",
            "09:00 - 10:00 Operating Systems | Room CSE-201 | Dr. Anita",
            "10:00 - 11:00 Artificial Intelligence | Room CSE-204 | Prof. Roy",
            "11:15 - 12:15 Formal Languages | Room Math-302 | Prof. David",
            "",
            "FRIDAY",
            "09:00 - 10:00 Machine Learning | Room CSE-204 | Dr. Meera",
            "10:00 - 11:00 Database Systems | Room CSE-102 | Dr. Anita",
            "11:15 - 12:15 Cyber Security | Room CSE-201 | Prof. Smith"
        )

        for (line in scheduleLines) {
            contentStream.showText(line)
            contentStream.newLineAtOffset(0f, -16f)
        }

        contentStream.endText()
        contentStream.close()

        val file = File(context.cacheDir, "sample_timetable.pdf")
        document.save(file)
        document.close()

        return Uri.fromFile(file)
    }

    fun createSampleTaskPdf(context: Context): Uri {
        val document = PDDocument()
        val page = PDPage()
        document.addPage(page)

        val contentStream = PDPageContentStream(document, page)
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16f)
        contentStream.newLineAtOffset(50f, 750f)
        contentStream.showText("SEMESTER ACADEMIC ASSIGNMENTS & DEADLINES")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.showText("Academic Year 2026-2027")

        contentStream.newLineAtOffset(0f, -30f)
        contentStream.setFont(PDType1Font.HELVETICA, 11f)

        val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        val in3Days = LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        val in6Days = LocalDate.now().plusDays(6).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        val in10Days = LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

        val taskLines = listOf(
            "1. DBMS Assignment 2 - Submit by $tomorrow 5:00 PM",
            "   Implement B+ Tree indexing module and SQL benchmarks.",
            "",
            "2. Physics Lab Record Submission - Due Date: $in3Days 11:59 PM",
            "   Submit complete verified optics observation sheets to faculty.",
            "",
            "3. Operating Systems Kernel Project - Deadline: $in6Days 6:00 PM",
            "   Process scheduling simulation with Round Robin and Priority queues.",
            "",
            "4. Computer Networks Socket Programming - Submit by $in10Days 11:59 PM",
            "   Multi-threaded client-server chat application report.",
            "",
            "5. Discrete Mathematics Homework 4 - Due Date: $in10Days 5:00 PM",
            "   Graph theory theorems and proof exercises."
        )

        for (line in taskLines) {
            contentStream.showText(line)
            contentStream.newLineAtOffset(0f, -18f)
        }

        contentStream.endText()
        contentStream.close()

        val file = File(context.cacheDir, "sample_tasks.pdf")
        document.save(file)
        document.close()

        return Uri.fromFile(file)
    }
}
