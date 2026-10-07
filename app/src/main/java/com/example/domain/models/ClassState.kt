package com.example.domain.models

import com.example.data.entities.ClassPeriod

sealed class ClassState {
    data class InClass(
        val classPeriod: ClassPeriod,
        val progressPercent: Int,
        val remainingMinutes: Int
    ) : ClassState()

    data class NextClassToday(
        val classPeriod: ClassPeriod,
        val startsInMinutes: Int
    ) : ClassState()

    data class NextClassUpcoming(
        val classPeriod: ClassPeriod,
        val dayDescription: String,
        val startsAtFormatted: String
    ) : ClassState()

    data class NoMoreClassesToday(
        val nextClass: ClassPeriod? = null,
        val nextClassDescription: String? = null
    ) : ClassState()

    data object NoTimetable : ClassState()
}
