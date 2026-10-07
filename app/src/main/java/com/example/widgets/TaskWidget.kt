package com.example.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.database.CivoraDatabase
import com.example.domain.DeadlineCalculator
import com.example.domain.models.TaskUrgency
import com.example.domain.models.UrgencyLevel

class TaskWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = CivoraDatabase.getDatabase(context)
        val allTasks = db.taskItemDao().getAllTasksSync()
        val pendingTasks = allTasks.filter { !it.isCompleted }
        val urgencies = DeadlineCalculator.sortTasksByUrgency(pendingTasks)
        val topThree = urgencies.take(3)

        provideContent {
            GlanceTheme {
                TaskWidgetContent(tasks = topThree, totalPending = pendingTasks.size)
            }
        }
    }

    @Composable
    private fun TaskWidgetContent(
        tasks: List<TaskUrgency>,
        totalPending: Int
    ) {
        val darkBg = Color(0xFF0F172A)
        val sky = Color(0xFF38BDF8)
        val slate = Color(0xFF94A3B8)
        val lightText = Color(0xFFE2E8F0)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(darkBg))
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPCOMING TASKS",
                        style = TextStyle(
                            color = ColorProvider(sky),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (totalPending > 0) {
                        Text(
                            text = "$totalPending active",
                            style = TextStyle(
                                color = ColorProvider(slate),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                if (tasks.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No upcoming tasks",
                                style = TextStyle(
                                    color = ColorProvider(lightText),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = "Tap to import or add assignments",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF64748B)),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else {
                    for ((index, item) in tasks.withIndex()) {
                        TaskRow(item = item)
                        if (index < tasks.size - 1) {
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TaskRow(item: TaskUrgency) {
        val (badgeBg, badgeText) = when (item.urgencyLevel) {
            UrgencyLevel.CRITICAL -> Pair(Color(0xFFEF4444), "CRITICAL")
            UrgencyLevel.HIGH -> Pair(Color(0xFFF97316), "HIGH")
            UrgencyLevel.MEDIUM -> Pair(Color(0xFFEAB308), "MEDIUM")
            UrgencyLevel.LOW -> Pair(Color(0xFF0EA5E9), "LOW")
            UrgencyLevel.OVERDUE -> Pair(Color(0xFFDC2626), "OVERDUE")
        }

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(Color(0xFF1E293B)))
                .cornerRadius(8.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.task.title,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFF8FAFC)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(badgeBg))
                            .cornerRadius(3.dp)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due: ${item.task.formattedDeadline()}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF94A3B8)),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = item.remainingTimeFormatted,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF38BDF8)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Urgency intensity progress bar
                LinearProgressIndicator(
                    progress = item.urgencyProgress,
                    modifier = GlanceModifier.fillMaxWidth().height(4.dp).cornerRadius(2.dp),
                    color = ColorProvider(badgeBg),
                    backgroundColor = ColorProvider(Color(0xFF334155))
                )
            }
        }
    }
}
