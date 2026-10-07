package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.entities.ClassPeriod
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun EditClassDialog(
    initialClass: ClassPeriod? = null,
    defaultDayOfWeek: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (ClassPeriod) -> Unit
) {
    var dayOfWeek by remember { mutableIntStateOf(initialClass?.dayOfWeek ?: defaultDayOfWeek) }
    var subject by remember { mutableStateOf(initialClass?.subject ?: "") }
    var startTime by remember { mutableStateOf(initialClass?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialClass?.endTime ?: "10:00") }
    var room by remember { mutableStateOf(initialClass?.room ?: "") }
    var teacher by remember { mutableStateOf(initialClass?.teacher ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val days = (1..7).map { DayOfWeek.of(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialClass == null) "Add Class Period" else "Edit Class Period") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Day")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.take(4).forEach { day ->
                        FilterChip(
                            selected = dayOfWeek == day.value,
                            onClick = { dayOfWeek = day.value },
                            label = { Text(day.getDisplayName(TextStyle.NARROW, Locale.getDefault())) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.drop(4).forEach { day ->
                        FilterChip(
                            selected = dayOfWeek == day.value,
                            onClick = { dayOfWeek = day.value },
                            label = { Text(day.getDisplayName(TextStyle.NARROW, Locale.getDefault())) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("class_subject_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (HH:mm)") },
                        modifier = Modifier.weight(1f).testTag("class_start_time_input")
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (HH:mm)") },
                        modifier = Modifier.weight(1f).testTag("class_end_time_input")
                    )
                }

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Hall (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Faculty / Teacher (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(text = it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isBlank()) {
                        errorMessage = "Subject cannot be empty"
                        return@Button
                    }
                    val created = ClassPeriod(
                        id = initialClass?.id ?: 0,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        subject = subject.trim(),
                        room = room.trim().ifBlank { null },
                        teacher = teacher.trim().ifBlank { null }
                    )
                    onConfirm(created)
                },
                modifier = Modifier.testTag("save_class_button")
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
