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
import com.example.domain.ClassProgressCalculator
import com.example.domain.models.ClassState
import java.time.LocalDateTime

class NextClassWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = CivoraDatabase.getDatabase(context)
        val allClasses = db.classPeriodDao().getAllClassPeriodsSync()
        val currentState = ClassProgressCalculator.calculateCurrentState(allClasses, LocalDateTime.now())

        provideContent {
            GlanceTheme {
                NextClassWidgetContent(state = currentState)
            }
        }
    }

    @Composable
    private fun NextClassWidgetContent(state: ClassState) {
        val darkBg = Color(0xFF0F172A)
        val emerald = Color(0xFF10B981)
        val sky = Color(0xFF38BDF8)
        val slateText = Color(0xFF94A3B8)
        val whiteText = Color(0xFFF8FAFC)
        val trackBg = Color(0xFF334155)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(darkBg))
                .cornerRadius(16.dp)
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            when (state) {
                is ClassState.InClass -> {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        // Header row
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .background(ColorProvider(emerald))
                                    .cornerRadius(4.dp)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CURRENT CLASS",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = "${state.remainingMinutes} min left",
                                style = TextStyle(
                                    color = ColorProvider(sky),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Subject & time
                        Text(
                            text = state.classPeriod.subject,
                            style = TextStyle(
                                color = ColorProvider(whiteText),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = "${state.classPeriod.formattedTimeRange()}  •  ${state.classPeriod.room ?: "Room N/A"}",
                            style = TextStyle(
                                color = ColorProvider(slateText),
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Progress Bar & Percentage
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = (state.progressPercent / 100f).coerceIn(0f, 1f),
                                modifier = GlanceModifier.defaultWeight().height(8.dp).cornerRadius(4.dp),
                                color = ColorProvider(emerald),
                                backgroundColor = ColorProvider(trackBg)
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "${state.progressPercent}%",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFE2E8F0)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                is ClassState.NextClassToday -> {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .background(ColorProvider(Color(0xFF0284C7)))
                                    .cornerRadius(4.dp)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NEXT CLASS TODAY",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = "In ${state.startsInMinutes} min",
                                style = TextStyle(
                                    color = ColorProvider(sky),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        Text(
                            text = state.classPeriod.subject,
                            style = TextStyle(
                                color = ColorProvider(whiteText),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = "${state.classPeriod.formattedTimeRange()}  •  ${state.classPeriod.room ?: "Classroom"}",
                            style = TextStyle(
                                color = ColorProvider(slateText),
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )

                        Spacer(modifier = GlanceModifier.height(6.dp))
                        Text(
                            text = state.classPeriod.teacher?.let { "Faculty: $it" } ?: "Civora Schedule Active",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF64748B)),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                is ClassState.NoMoreClassesToday -> {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NO MORE CLASSES TODAY",
                            style = TextStyle(
                                color = ColorProvider(sky),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        if (state.nextClass != null) {
                            Text(
                                text = "Next: ${state.nextClass.subject}",
                                style = TextStyle(
                                    color = ColorProvider(whiteText),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = state.nextClassDescription ?: state.nextClass.formattedTimeRange(),
                                style = TextStyle(
                                    color = ColorProvider(slateText),
                                    fontSize = 12.sp
                                )
                            )
                        } else {
                            Text(
                                text = state.nextClassDescription ?: "All classes finished for this week",
                                style = TextStyle(
                                    color = ColorProvider(slateText),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                is ClassState.NoTimetable -> {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Civora Timetable Widget",
                            style = TextStyle(
                                color = ColorProvider(sky),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "No timetable imported yet.",
                            style = TextStyle(
                                color = ColorProvider(whiteText),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = "Tap to import your PDF schedule.",
                            style = TextStyle(
                                color = ColorProvider(slateText),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                is ClassState.NextClassUpcoming -> {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Text(
                            text = "UPCOMING CLASS",
                            style = TextStyle(
                                color = ColorProvider(sky),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(6.dp))
                        Text(
                            text = state.classPeriod.subject,
                            style = TextStyle(
                                color = ColorProvider(whiteText),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = "${state.dayDescription}  •  ${state.startsAtFormatted}",
                            style = TextStyle(
                                color = ColorProvider(slateText),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
