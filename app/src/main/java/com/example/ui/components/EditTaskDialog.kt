package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.entities.TaskItem
import com.example.parser.TaskParser
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun EditTaskDialog(
    initialTask: TaskItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var subject by remember { mutableStateOf(initialTask?.subject ?: "") }
    var deadlineDate by remember {
        mutableStateOf(initialTask?.deadlineDate ?: LocalDate.now().plusDays(2).toString())
    }
    var deadlineTime by remember { mutableStateOf(initialTask?.deadlineTime ?: "23:59") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: "MEDIUM") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val priorities = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTask == null) "Add New Task" else "Edit Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (optional)") },
                    modifier = Modifier.fillMaxWidth().testTag("task_subject_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deadlineDate,
                        onValueChange = { deadlineDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.3f).testTag("task_date_input")
                    )
                    OutlinedTextField(
                        value = deadlineTime,
                        onValueChange = { deadlineTime = it },
                        label = { Text("Time (HH:mm)") },
                        modifier = Modifier.weight(1f).testTag("task_time_input")
                    )
                }

                Text("Priority Level", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Task title cannot be empty"
                        return@Button
                    }
                    try {
                        val parsedDate = TaskParser.parseDateString(deadlineDate)
                        val timeParts = deadlineTime.split(":")
                        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 23
                        val min = timeParts.getOrNull(1)?.toIntOrNull() ?: 59
                        val localDateTime = LocalDateTime.of(parsedDate, LocalTime.of(hour, min))
                        val epochMs = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        val task = TaskItem(
                            id = initialTask?.id ?: 0,
                            title = title.trim(),
                            subject = subject.trim().ifBlank { null },
                            description = description.trim().ifBlank { null },
                            deadlineEpochMillis = epochMs,
                            deadlineDate = parsedDate.toString(),
                            deadlineTime = String.format("%02d:%02d", hour, min),
                            priority = priority,
                            isCompleted = initialTask?.isCompleted ?: false,
                            sourcePdf = initialTask?.sourcePdf
                        )
                        onConfirm(task)
                    } catch (e: Exception) {
                        errorMessage = "Invalid date/time format: ${e.message}"
                    }
                },
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
