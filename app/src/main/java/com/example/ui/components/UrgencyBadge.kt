package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.UrgencyLevel
import com.example.ui.theme.UrgencyCriticalColor
import com.example.ui.theme.UrgencyHighColor
import com.example.ui.theme.UrgencyLowColor
import com.example.ui.theme.UrgencyMediumColor
import com.example.ui.theme.UrgencyOverdueColor

@Composable
fun UrgencyBadge(
    urgencyLevel: UrgencyLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (urgencyLevel) {
        UrgencyLevel.LOW -> Pair(UrgencyLowColor.copy(alpha = 0.15f), UrgencyLowColor)
        UrgencyLevel.MEDIUM -> Pair(UrgencyMediumColor.copy(alpha = 0.20f), UrgencyMediumColor)
        UrgencyLevel.HIGH -> Pair(UrgencyHighColor.copy(alpha = 0.20f), UrgencyHighColor)
        UrgencyLevel.CRITICAL -> Pair(UrgencyCriticalColor.copy(alpha = 0.22f), UrgencyCriticalColor)
        UrgencyLevel.OVERDUE -> Pair(UrgencyOverdueColor.copy(alpha = 0.25f), UrgencyOverdueColor)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = urgencyLevel.label.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
