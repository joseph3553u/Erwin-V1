package com.example.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entities.ClassPeriod
import com.example.data.entities.TaskItem
import com.example.domain.models.ClassState
import com.example.ui.components.TaskCard
import com.example.ui.components.TaskPdfVerificationDialog
import com.example.ui.components.TimetablePdfVerificationDialog
import com.example.ui.theme.ProgressSuccessColor

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTimetable: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // SAF File pickers
    val timetablePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importTimetablePdfUri(it, "Selected_Timetable.pdf") }
    }

    val taskPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importTaskPdfUri(it, "Selected_Tasks.pdf") }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Student Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Civora Native Home-Screen Widgets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 1. Next Class / Current Class Hero Section
            item {
                Text(
                    text = "CLASS SCHEDULE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                NextClassHeroCard(
                    classState = uiState.classState,
                    onImportClick = { timetablePicker.launch(arrayOf("application/pdf")) },
                    onViewFullTimetable = onNavigateToTimetable
                )
            }

            // 2. Upcoming Tasks Section (Next 3)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPCOMING TASKS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.topTasks.isNotEmpty()) {
                        Text(
                            text = "View all",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onNavigateToTasks() }
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (uiState.topTasks.isEmpty()) {
                item {
                    EmptyTasksDashboardCard(
                        onImportClick = { taskPicker.launch(arrayOf("application/pdf")) }
                    )
                }
            } else {
                items(uiState.topTasks, key = { it.task.id }) { item ->
                    TaskCard(
                        urgency = item,
                        onToggleCompletion = { isCompleted ->
                            viewModel.toggleTaskCompletion(item.task, isCompleted)
                        },
                        onEdit = { onNavigateToTasks() },
                        onDelete = { onNavigateToTasks() }
                    )
                }
            }

            // 3. PDF Import & Widget Management Section
            item {
                Text(
                    text = "PDF DOCUMENT IMPORT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                PdfImportSection(
                    totalClasses = uiState.totalClassesCount,
                    totalTasks = uiState.totalPendingTasksCount,
                    onImportTimetable = { timetablePicker.launch(arrayOf("application/pdf")) },
                    onImportTasks = { taskPicker.launch(arrayOf("application/pdf")) },
                    onLoadSampleTimetable = { viewModel.loadSampleTimetable() },
                    onLoadSampleTasks = { viewModel.loadSampleTasks() }
                )
            }
        }
    }

    // PDF Import Dialogs
    when (val importState = uiState.pdfImportState) {
        is PdfImportUiState.Parsing -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Parsing PDF document locally...")
                }
            }
        }

        is PdfImportUiState.TimetablePreview -> {
            TimetablePdfVerificationDialog(
                detectedClasses = importState.classes,
                pdfFileName = importState.fileName,
                onDismiss = { viewModel.dismissImportPreview() },
                onConfirmSave = { viewModel.confirmSaveTimetable(it) },
                onManualEntry = { onNavigateToTimetable() }
            )
        }

        is PdfImportUiState.TasksPreview -> {
            TaskPdfVerificationDialog(
                detectedTasks = importState.tasks,
                pdfFileName = importState.fileName,
                onDismiss = { viewModel.dismissImportPreview() },
                onConfirmSave = { viewModel.confirmSaveTasks(it) },
                onManualEntry = { onNavigateToTasks() }
            )
        }

        is PdfImportUiState.Error -> {
            LaunchedEffect(importState.message) {
                snackbarHostState.showSnackbar("PDF Parsing Error: ${importState.message}")
                viewModel.dismissImportPreview()
            }
        }

        is PdfImportUiState.Idle -> {
            // No dialog
        }
    }
}

@Composable
private fun NextClassHeroCard(
    classState: ClassState,
    onImportClick: () -> Unit,
    onViewFullTimetable: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("next_class_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            1.dp,
            if (classState is ClassState.InClass) ProgressSuccessColor.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            when (classState) {
                is ClassState.InClass -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ProgressSuccessColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "CURRENT CLASS",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "${classState.remainingMinutes} min remaining",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = ProgressSuccessColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = classState.classPeriod.subject,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${classState.classPeriod.formattedTimeRange()}  •  ${classState.classPeriod.room ?: "Room TBD"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { classState.progressPercent / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ProgressSuccessColor,
                            trackColor = ProgressSuccessColor.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${classState.progressPercent}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ProgressSuccessColor
                        )
                    }
                }

                is ClassState.NextClassToday -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "NEXT CLASS",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Starts in ${classState.startsInMinutes} min",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = classState.classPeriod.subject,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${classState.classPeriod.formattedTimeRange()}  •  ${classState.classPeriod.room ?: "Classroom"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ClassState.NoMoreClassesToday -> {
                    Text(
                        text = "NO MORE CLASSES TODAY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (classState.nextClass != null) {
                        Text(
                            text = "Next: ${classState.nextClass.subject}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = classState.nextClassDescription ?: classState.nextClass.formattedTimeRange(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Enjoy your free time! No more classes scheduled.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ClassState.NoTimetable -> {
                    Column {
                        Text(
                            text = "No timetable imported yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Import your timetable PDF to get started with the Next Class home-screen widget.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onImportClick,
                            modifier = Modifier.testTag("home_import_timetable_btn")
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Timetable PDF")
                        }
                    }
                }

                is ClassState.NextClassUpcoming -> {
                    Text(
                        text = "UPCOMING CLASS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = classState.classPeriod.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${classState.dayDescription} • ${classState.startsAtFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (classState !is ClassState.NoTimetable) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewFullTimetable() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Weekly Schedule",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksDashboardCard(onImportClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_tasks_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AssignmentTurnedIn,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No tasks imported yet.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Import your task PDF to get started with deadline urgency tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onImportClick,
                modifier = Modifier.testTag("home_import_tasks_btn")
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Tasks PDF")
            }
        }
    }
}

@Composable
private fun PdfImportSection(
    totalClasses: Int,
    totalTasks: Int,
    onImportTimetable: () -> Unit,
    onImportTasks: () -> Unit,
    onLoadSampleTimetable: () -> Unit,
    onLoadSampleTasks: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onImportTimetable,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_timetable_pdf_btn")
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (totalClasses > 0) "Replace Timetable" else "Import Timetable PDF", fontSize = 12.sp)
                }

                Button(
                    onClick = onImportTasks,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_tasks_pdf_btn")
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (totalTasks > 0) "Replace Tasks" else "Import Tasks PDF", fontSize = 12.sp)
                }
            }

            // Quick offline sample test helpers for users who don't have immediate PDF files ready on device
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLoadSampleTimetable,
                    modifier = Modifier.weight(1f).testTag("sample_timetable_btn")
                ) {
                    Text("Load Sample Schedule", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onLoadSampleTasks,
                    modifier = Modifier.weight(1f).testTag("sample_tasks_btn")
                ) {
                    Text("Load Sample Tasks", fontSize = 11.sp)
                }
            }
        }
    }
}
