package com.example.domain.models

import com.example.data.entities.TaskItem

data class TaskUrgency(
    val task: TaskItem,
    val urgencyLevel: UrgencyLevel,
    val remainingTimeFormatted: String,
    val urgencyProgress: Float // 0.0f (lowest urgency / plenty of time) to 1.0f (highest urgency / right at deadline)
)
