package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CivoraBlueDark,
    onPrimary = CivoraOnBlueDark,
    primaryContainer = CivoraBlueContainerDark,
    onPrimaryContainer = CivoraOnBlueContainerDark,
    secondary = CivoraSecondaryDark,
    surface = CivoraSurfaceDark,
    onSurface = CivoraOnSurfaceDark,
    surfaceVariant = CivoraCardDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = CivoraBlueLight,
    onPrimary = CivoraOnBlueLight,
    primaryContainer = CivoraBlueContainerLight,
    onPrimaryContainer = CivoraOnBlueContainerLight,
    secondary = CivoraSecondaryLight,
    surface = CivoraSurfaceLight,
    onSurface = CivoraOnSurfaceLight,
    surfaceVariant = CivoraCardLight,
    onSurfaceVariant = Color(0xFF475569),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A)
)

@Composable
fun CivoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
