package com.example.domain.models

enum class UrgencyLevel(val label: String) {
    LOW("Low Urgency"),
    MEDIUM("Medium Urgency"),
    HIGH("High Urgency"),
    CRITICAL("Critical"),
    OVERDUE("Overdue")
}
